/*
 * Copyright 2018 shenhuanet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shenhua.libs.sensockettcp.nio;

import android.support.annotation.NonNull;

import com.shenhua.libs.sensocketcore.BaseClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public final class TcpNioReadWriteProcessor {

    private static final String TAG = "TcpNioReadWriteProcessor";

    private static int G_SOCKET_ID = 0;

    private int mSocketId;
    private String mIp = "192.168.97.114";
    private int mPort = 8080;
    private long connectTimeout = 10000;

    private BaseClient mClient;
    private TcpNioConnectListener mNioConnectListener;

    private SocketChannel mSocketChannel;
    private Selector mSelector;

    private ConnectRunnable mConnectProcessor;

    private boolean closed = false;

    public TcpNioReadWriteProcessor(String mIp, int mPort, long connectTimeout, BaseClient mClient, TcpNioConnectListener mNioConnectListener) {
        G_SOCKET_ID++;

        this.mSocketId = G_SOCKET_ID;
        this.mIp = mIp;
        this.mPort = mPort;
        this.connectTimeout = connectTimeout;
        this.mClient = mClient;
        this.mNioConnectListener = mNioConnectListener;
    }

    public void start() {
        mConnectProcessor = new ConnectRunnable();
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger integer = new AtomicInteger();

            @Override
            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r, "Connect Processor ThreadPool thread:" + integer.getAndIncrement());
            }
        };
        ExecutorService executor = new ThreadPoolExecutor(2, 4, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(1024), factory);
        executor.execute(mConnectProcessor);
    }

    public synchronized void close() {
        closed = true;

        if (null != mSocketChannel) {
            try {
                SelectionKey key = mSocketChannel.keyFor(mSelector);
                if (null != key) {
                    key.cancel();
                }
                mSocketChannel.socket().close();
                mSocketChannel.close();
                mSocketChannel = null;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if (null != mSelector) {
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSelector = null;
        }

        wakeUp();
    }

    public void wakeUp() {
        if (null != mConnectProcessor) {
            mConnectProcessor.wakeUp();
        }
    }

    public void onSocketExit(int exitCode) {
        System.out.println(TAG + "onSocketExit mSocketId " + mSocketId + " exit_code " + exitCode);

        close();
        if (null != mNioConnectListener) {
            mNioConnectListener.onConnectFailed(TcpNioReadWriteProcessor.this);
        }
    }

    private class ConnectRunnable implements Runnable {

        void wakeUp() {
            if (null != mSelector) {
                mSelector.wakeup();
            }
        }

        @Override
        public void run() {
            try {

                mSelector = SelectorProvider.provider().openSelector();
                mSocketChannel = SocketChannel.open();
                mSocketChannel.configureBlocking(false);

                InetSocketAddress address = new InetSocketAddress(mIp, mPort);
                mSocketChannel.connect(address);
                mSocketChannel.register(mSelector, SelectionKey.OP_CONNECT, mClient);

                boolean isConnectSuccess = connect(connectTimeout);

                if (isConnectSuccess) {
                    boolean isExit = false;
                    while (!isExit) {

                        int readKeys = mSelector.select();
                        if (readKeys > 0) {
                            Iterator<SelectionKey> selectedKeys = mSelector.selectedKeys().iterator();
                            while (selectedKeys.hasNext()) {
                                SelectionKey key = selectedKeys.next();
                                selectedKeys.remove();

                                if (!key.isValid()) {
                                    continue;
                                }

                                if (key.isReadable()) {
                                    BaseClient mClient = (BaseClient) key.attachment();
                                    boolean ret = mClient.onRead();
                                    if (!ret) {
                                        isExit = true;
                                        key.cancel();
                                        key.attach(null);
                                        key.channel().close();
                                        break;
                                    }

                                } else if (key.isWritable()) {
                                    BaseClient mClient = (BaseClient) key.attachment();
                                    boolean ret = mClient.onWrite();
                                    if (!ret) {
                                        isExit = true;
                                        key.cancel();
                                        key.attach(null);
                                        key.channel().close();
                                        break;
                                    }
                                    key.interestOps(SelectionKey.OP_READ);
                                }
                            }
                        }

                        if (isExit || closed) {
                            break;
                        }

                        if (!mClient.mWriteMessageQueen.mWriteQueen.isEmpty()) {
                            SelectionKey key = mSocketChannel.keyFor(mSelector);
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            onSocketExit(1);
        }

        private boolean connect(long connectTimeout) throws IOException {
            boolean isConnectSuccess = false;
            int connectReady = 0;
            if (connectTimeout == -1) {
                connectReady = mSelector.select();
            } else {
                connectReady = mSelector.select(connectTimeout);
            }
            if (connectReady > 0) {
                Iterator<SelectionKey> selectedKeys = mSelector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {
                        boolean ret = finishConnection(key);
                        isConnectSuccess = ret;
                        if (!ret) {
                            key.cancel();
                            key.attach(null);
                            key.channel().close();
                            break;
                        }
                    }
                }
            } else {
                isConnectSuccess = false;
                try {
                    for (SelectionKey key : mSelector.keys()) {
                        key.cancel();
                        key.attach(null);
                        key.channel().close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return isConnectSuccess;
        }

        private boolean finishConnection(SelectionKey key) {
            boolean connectRet = true;
            try {
                boolean result;
                SocketChannel socketChannel = (SocketChannel) key.channel();
                result = socketChannel.finishConnect();
                if (result) {
                    key.interestOps(SelectionKey.OP_READ);
                    if (null != mNioConnectListener) {
                        mNioConnectListener.onConnectSuccess(TcpNioReadWriteProcessor.this, mSocketChannel);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                connectRet = false;
            }
            return connectRet;
        }
    }
}