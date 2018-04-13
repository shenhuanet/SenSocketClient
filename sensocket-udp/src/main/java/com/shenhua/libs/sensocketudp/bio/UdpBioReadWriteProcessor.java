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

package com.shenhua.libs.sensocketudp.bio;

import android.support.annotation.NonNull;

import com.shenhua.libs.sensocketcore.BaseClient;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
public class UdpBioReadWriteProcessor {

    private static final String TAG = "UdpBioReadWriteProcessor";

    private static int G_SOCKET_ID = 0;

    private int mSocketId;
    private String mIp = "192.168.1.1";
    private int mPort = 9999;

    private BaseClient mClient;
    private UdpBioConnectListener mConnectStatusListener;

    private DatagramSocket mSocket;

    private WriteRunnable mWriteProcessor;
    private ExecutorService mExecutorService;

    private int rWCount = 2;

    public UdpBioReadWriteProcessor(String mIp, int mPort, BaseClient mClient, UdpBioConnectListener mConnectionStatusListener) {
        G_SOCKET_ID++;

        this.mSocketId = G_SOCKET_ID;
        this.mIp = mIp;
        this.mPort = mPort;
        this.mClient = mClient;
        this.mConnectStatusListener = mConnectionStatusListener;
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger integer = new AtomicInteger();

            @Override
            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r, "Connect Processor ThreadPool thread:" + integer.getAndIncrement());
            }
        };
        mExecutorService = new ThreadPoolExecutor(3, 4, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(1024), factory);
    }

    public void start() {
        ConnectRunnable mConnectProcessor = new ConnectRunnable();
        mExecutorService.execute(mConnectProcessor);
    }

    public synchronized void close() {

        wakeUp();

        if (null != mSocket) {
            mSocket.close();
        }

        try {
            if (null != mExecutorService && !mExecutorService.isTerminated()) {
                mExecutorService.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void wakeUp() {
        if (null != mWriteProcessor) {
            mWriteProcessor.wakeup();
        }
    }

    public synchronized void onSocketExit(int exitCode) {

        --rWCount;
        boolean isWriterReaderExit = (rWCount <= 0);
        System.out.println(TAG + "onSocketExit mSocketId " + mSocketId + " exit_code " + exitCode + (exitCode == 1 ? " onWrite" : " onRead") + " isWriterReaderExit " + isWriterReaderExit);
        close();
        if (isWriterReaderExit) {
            if (null != mConnectStatusListener) {
                mConnectStatusListener.onConnectFailed(UdpBioReadWriteProcessor.this);
            }
        }
    }

    private class ConnectRunnable implements Runnable {

        @Override
        public void run() {
            byte[] mWriteBuff = ((UdpBioClient) mClient).mWriteBuff;
            byte[] mReadBuff = ((UdpBioClient) mClient).mReadBuff;

            boolean connectRet;
            try {
                mSocket = new DatagramSocket();
                InetAddress address = InetAddress.getByName(mIp);
                DatagramPacket mWriteDatagramPacket = new DatagramPacket(mWriteBuff, mWriteBuff.length, address, mPort);
                DatagramPacket mReadDatagramPacket = new DatagramPacket(mReadBuff, mReadBuff.length);

                if (null != mConnectStatusListener) {
                    mConnectStatusListener.onConnectSuccess(UdpBioReadWriteProcessor.this, mSocket, mWriteDatagramPacket, mReadDatagramPacket);
                }
                connectRet = true;

                mWriteProcessor = new WriteRunnable();
                ReadRunnable mReadProcessor = new ReadRunnable();
                mExecutorService.execute(mWriteProcessor);
                mExecutorService.execute(mReadProcessor);

            } catch (UnknownHostException e) {
                e.printStackTrace();
                connectRet = false;
            } catch (SocketException e) {
                e.printStackTrace();
                connectRet = false;
            }

            if (!connectRet) {
                if (null != mConnectStatusListener) {
                    mConnectStatusListener.onConnectFailed(UdpBioReadWriteProcessor.this);
                }
            }
        }
    }

    private class WriteRunnable implements Runnable {

        private final Object lock = new Object();

        public void wakeup() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (!mClient.onWrite()) {
                        break;
                    }
                    synchronized (lock) {
                        lock.wait();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            onSocketExit(1);
        }
    }

    private class ReadRunnable implements Runnable {

        @Override
        public void run() {
            mClient.onRead();
            onSocketExit(2);
        }
    }
}
