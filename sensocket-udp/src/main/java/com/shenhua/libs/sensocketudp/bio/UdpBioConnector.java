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

import com.shenhua.libs.sensocketcore.IConnectListener;
import com.shenhua.libs.sensocketudp.UdpAddress;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class UdpBioConnector {

    private final int STATE_CLOSE = 0x1;
    private final int STATE_CONNECT_START = 0x2;
    private final int STATE_CONNECT_SUCCESS = 0x3;

    private UdpAddress[] mAddress = null;
    private int mConnectIndex = -1;
    private int state = STATE_CLOSE;

    private UdpBioClient mClient;
    private IConnectListener mIConnectListener;
    private UdpBioReadWriteProcessor mSocketProcessor;

    private UdpBioConnectListener mProxyConnectStatusListener = new UdpBioConnectListener() {

        @Override
        public void onConnectSuccess(UdpBioReadWriteProcessor mSocketProcessor, DatagramSocket mSocket, DatagramPacket mWriteDatagramPacket, DatagramPacket mReadDatagramPacket) {
            if (mSocketProcessor != UdpBioConnector.this.mSocketProcessor) {
                if (null != mSocketProcessor) {
                    mSocketProcessor.close();
                }
                return;
            }

            state = STATE_CONNECT_SUCCESS;
            mClient.init(mSocket, mWriteDatagramPacket, mReadDatagramPacket);

            if (null != mIConnectListener) {
                mIConnectListener.onConnectionSuccess();
            }
        }

        @Override
        public synchronized void onConnectFailed(UdpBioReadWriteProcessor mSocketProcessor) {
            if (mSocketProcessor != UdpBioConnector.this.mSocketProcessor) {
                if (null != mSocketProcessor) {
                    mSocketProcessor.close();
                }
                return;
            }

            state = STATE_CLOSE;
            connect();
        }
    };

    public UdpBioConnector(UdpBioClient mClient, IConnectListener mIConnectListener) {
        this.mClient = mClient;
        this.mIConnectListener = mIConnectListener;
    }

    private boolean isConnected() {
        return state == STATE_CONNECT_SUCCESS;
    }

    private boolean isConnecting() {
        return state == STATE_CONNECT_START;
    }

    private boolean isClosed() {
        return state == STATE_CLOSE;
    }

    public void setConnectAddress(UdpAddress[] tcpArray) {
        this.mConnectIndex = -1;
        this.mAddress = tcpArray;
    }

    public synchronized void connect() {
        startConnect();
    }

    public synchronized void reconnect() {
        stopConnect();
        if (mConnectIndex + 1 >= mAddress.length || mConnectIndex + 1 < 0) {
            mConnectIndex = -1;
        }
        startConnect();
    }

    public synchronized void disconnect() {
        stopConnect();
    }

    public void checkConnect() {
        if (null == mSocketProcessor) {
            startConnect();
        } else if (!isConnected() && !isConnecting()) {
            startConnect();
        } else {
            if (isConnected()) {
                mSocketProcessor.wakeUp();
            }
        }
    }

    private void startConnect() {
        if (!isClosed()) {
            return;
        }

        mConnectIndex++;
        if (mConnectIndex < mAddress.length && mConnectIndex >= 0) {
            state = STATE_CONNECT_START;
            mSocketProcessor = new UdpBioReadWriteProcessor(mAddress[mConnectIndex].ip, mAddress[mConnectIndex].port, mClient, mProxyConnectStatusListener);
            mSocketProcessor.start();
        } else {
            mConnectIndex = -1;

            if (null != mIConnectListener) {
                mIConnectListener.onConnectionFailed();
            }
        }
    }

    private void stopConnect() {
        state = STATE_CLOSE;
        mClient.onClose();

        if (null != mSocketProcessor) {
            mSocketProcessor.close();
            mSocketProcessor = null;
        }
    }

}
