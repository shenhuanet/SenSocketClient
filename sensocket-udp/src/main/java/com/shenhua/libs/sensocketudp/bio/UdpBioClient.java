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

import com.shenhua.libs.sensocketcore.BaseClient;
import com.shenhua.libs.sensocketcore.BaseMessageProcessor;
import com.shenhua.libs.sensocketcore.SenClient;
import com.shenhua.libs.sensocketcore.IConnectListener;
import com.shenhua.libs.sensocketcore.message.Message;
import com.shenhua.libs.sensocketudp.UdpAddress;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class UdpBioClient extends BaseClient {

    static {
        SenClient.init();
    }

    private UdpBioConnector mConnector;

    public UdpBioClient(BaseMessageProcessor mMessageProcessor, IConnectListener mConnectListener) {
        super(mMessageProcessor);
        mConnector = new UdpBioConnector(this, mConnectListener);
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

    private DatagramSocket mSocket;
    private DatagramPacket mWriteDatagramPacket;
    private DatagramPacket mReadDatagramPacket;
    public byte[] mWriteBuff = new byte[65500];
    public byte[] mReadBuff = new byte[65500];

    public void init(DatagramSocket mSocket, DatagramPacket mWriteDatagramPacket, DatagramPacket mReadDatagramPacket) {
        this.mSocket = mSocket;
        this.mWriteDatagramPacket = mWriteDatagramPacket;
        this.mReadDatagramPacket = mReadDatagramPacket;
    }

    @Override
    public void onCheckConnect() {
        mConnector.checkConnect();
    }

    @Override
    public void onClose() {
        mSocket = null;
    }

    @Override
    public boolean onRead() {
        try {
            while (true) {
                mSocket.receive(mReadDatagramPacket);
                if (null != mMessageProcessor) {
                    mMessageProcessor.onReceiveData(this, mReadDatagramPacket.getData(), mReadDatagramPacket.getOffset(), mReadDatagramPacket.getLength());
                    mMessageProcessor.onReceiveDataCompleted(this);
                }
                mReadDatagramPacket.setLength(mReadDatagramPacket.getData().length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != mMessageProcessor) {
            mMessageProcessor.onReceiveDataCompleted(this);
        }

        return true;
    }

    @Override
    public boolean onWrite() {
        boolean writeRet = true;
        Message msg = pollWriteMessage();
        try {
            while (null != msg) {
                mWriteDatagramPacket.setData(msg.data, msg.offset, msg.length);
                mSocket.send(mWriteDatagramPacket);
                removeWriteMessage(msg);
                msg = pollWriteMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!writeRet) {
            if (null != msg) {
                removeWriteMessage(msg);
            }
            msg = pollWriteMessage();
            while (null != msg) {
                removeWriteMessage(msg);
                msg = pollWriteMessage();
            }
        }

        return writeRet;
    }
}
