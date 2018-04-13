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

package com.shenhua.libs.sensocketudp.nio;

import com.shenhua.libs.sensocketcore.BaseClient;
import com.shenhua.libs.sensocketcore.BaseMessageProcessor;
import com.shenhua.libs.sensocketcore.SenClient;
import com.shenhua.libs.sensocketcore.IConnectListener;
import com.shenhua.libs.sensocketcore.message.Message;
import com.shenhua.libs.sensocketudp.UdpAddress;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public final class UdpNioClient extends BaseClient {

    static {
        SenClient.init();
    }

    private UdpNioConnector mConnector;

    public UdpNioClient(BaseMessageProcessor mMessageProcessor, IConnectListener mConnectListener) {
        super(mMessageProcessor);
        mConnector = new UdpNioConnector(this, mConnectListener);
    }

    public void setConnectAddress(UdpAddress[] tcpArray) {
        mConnector.setConnectAddress(tcpArray);
    }

    public void connect() {
        mConnector.connect();
    }

    public void disconnect() {
        mConnector.disconnect();
    }

    public void reconnect() {
        mConnector.reconnect();
    }

    private DatagramChannel mSocketChannel;
    private ByteBuffer mReadByteBuffer = ByteBuffer.allocate(64 * 1024);
    private ByteBuffer mWriteByteBuffer = ByteBuffer.allocate(64 * 1024);

    void init(DatagramChannel socketChannel) {
        this.mSocketChannel = socketChannel;
    }

    @Override
    public void onCheckConnect() {
        mConnector.checkConnect();
    }

    @Override
    public void onClose() {
        mSocketChannel = null;
    }

    @Override
    public boolean onRead() {
        boolean readRet = true;
        try {
            mReadByteBuffer.clear();
            while (true) {
                int readLength = mSocketChannel.read(mReadByteBuffer);
                if (readLength == -1) {
                    readRet = false;
                    break;
                }

                mReadByteBuffer.flip();
                if (mReadByteBuffer.remaining() > 0) {
                    this.mMessageProcessor.onReceiveData(this, mReadByteBuffer.array(), 0, mReadByteBuffer.remaining());
                }
                mReadByteBuffer.clear();

                if (readLength == 0) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            readRet = false;
        }

        mMessageProcessor.onReceiveDataCompleted(this);
        if (!readRet) {
            Message msg = pollWriteMessage();
            while (null != msg) {
                removeWriteMessage(msg);
                msg = pollWriteMessage();
            }
        }
        return readRet;
    }

    @Override
    public boolean onWrite() {
        boolean writeRet = true;
        Message msg = pollWriteMessage();
        try {
            while (null != msg) {

                if (mWriteByteBuffer.capacity() < msg.length) {

                    int offset = 0;
                    int leftLength = msg.length;

                    while (true) {
                        int putLength = leftLength > mWriteByteBuffer.capacity() ? mWriteByteBuffer.capacity() : leftLength;
                        mWriteByteBuffer.put(msg.data, offset, putLength);
                        mWriteByteBuffer.flip();
                        offset += putLength;
                        leftLength -= putLength;

                        int writtenLength = mSocketChannel.write(mWriteByteBuffer);

                        while (writtenLength > 0 && mWriteByteBuffer.hasRemaining()) {
                            writtenLength = mSocketChannel.write(mWriteByteBuffer);
                        }
                        mWriteByteBuffer.clear();

                        if (leftLength <= 0) {
                            break;
                        }
                    }
                } else {
                    mWriteByteBuffer.put(msg.data, msg.offset, msg.length);
                    mWriteByteBuffer.flip();

                    int writtenLength = mSocketChannel.write(mWriteByteBuffer);

                    while (writtenLength > 0 && mWriteByteBuffer.hasRemaining()) {
                        writtenLength = mSocketChannel.write(mWriteByteBuffer);
                    }
                    mWriteByteBuffer.clear();
                }

                removeWriteMessage(msg);
                msg = pollWriteMessage();
            }

        } catch (IOException e) {
            e.printStackTrace();
            writeRet = false;
        }

        if (!writeRet) {
            removeWriteMessage(msg);
            msg = pollWriteMessage();
            while (null != msg) {
                removeWriteMessage(msg);
                msg = pollWriteMessage();
            }
        }

        return writeRet;
    }
}
