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

package com.shenhua.libs.sensockettcp.bio;

import com.shenhua.libs.sensocketcore.BaseClient;
import com.shenhua.libs.sensocketcore.BaseMessageProcessor;
import com.shenhua.libs.sensocketcore.SenClient;
import com.shenhua.libs.sensocketcore.IConnectListener;
import com.shenhua.libs.sensocketcore.message.Message;
import com.shenhua.libs.sensockettcp.TcpAddress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class TcpBioClient extends BaseClient {

    static {
        SenClient.init();
    }

    private TcpBioConnector mConnector;

    public TcpBioClient(BaseMessageProcessor mMessageProcessor, IConnectListener mConnectListener) {
        super(mMessageProcessor);
        mConnector = new TcpBioConnector(this, mConnectListener);
    }

    public void setConnectAddress(TcpAddress[] tcpArray) {
        mConnector.setConnectAddress(tcpArray);
    }

    public void setConnectTimeout(long connectTimeout) {
        mConnector.setConnectTimeout(connectTimeout);
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

    public boolean isConnected() {
        return mConnector.isConnected();
    }

    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;

    public void init(OutputStream mOutputStream, InputStream mInputStream) throws IOException {
        this.mOutputStream = mOutputStream;
        this.mInputStream = mInputStream;
    }

    @Override
    public void onCheckConnect() {
        mConnector.checkConnect();
    }

    @Override
    public void onClose() {
        mOutputStream = null;
        mInputStream = null;
    }

    @Override
    public boolean onRead() {
        boolean readRet = false;
        try {
            int maximumLength = 64 * 1024;
            byte[] bodyBytes = new byte[maximumLength];
            int numRead;

            while ((numRead = mInputStream.read(bodyBytes, 0, maximumLength)) > 0) {
                if (numRead > 0) {
                    if (null != mMessageProcessor) {
                        mMessageProcessor.onReceiveData(this, bodyBytes, 0, numRead);
                        mMessageProcessor.onReceiveDataCompleted(this);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            readRet = false;
        } catch (IOException e1) {
            e1.printStackTrace();
            readRet = false;
        } catch (Exception e2) {
            e2.printStackTrace();
            readRet = false;
        }

        if (null != mMessageProcessor) {
            mMessageProcessor.onReceiveDataCompleted(this);
        }

        if (!readRet) {
            Message msg = pollWriteMessage();
            while (null != msg) {
                removeWriteMessage(msg);
                msg = pollWriteMessage();
            }
        }
        return false;
    }

    @Override
    public boolean onWrite() {
        boolean writeRet = true;
        Message msg = pollWriteMessage();
        try {
            while (null != msg) {
                mOutputStream.write(msg.data, msg.offset, msg.length);
                mOutputStream.flush();
                removeWriteMessage(msg);
                msg = pollWriteMessage();
            }
        } catch (SocketException e) {
            e.printStackTrace();
            writeRet = false;
        } catch (IOException e1) {
            e1.printStackTrace();
            writeRet = false;
        } catch (Exception e2) {
            e2.printStackTrace();
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
