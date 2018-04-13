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

package com.shenhua.libs.sensocketcore;

import com.shenhua.libs.sensocketcore.message.Message;

import java.util.LinkedList;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public abstract class BaseMessageProcessor {

    public final void send(BaseClient mClient, byte[] src) {
        this.send(mClient, src, 0, src.length);
    }

    public final void send(BaseClient mClient, byte[] src, int offset, int length) {
        mClient.onSendMessage(src, offset, length);
    }

    public final void onReceiveData(BaseClient mClient, byte[] src, int offset, int length) {
        mClient.onReceiveData(src, offset, length);
    }

    public final void onReceiveDataCompleted(BaseClient mClient) {
        if (mClient.mReadMessageQueen.mReadQueen.size() > 0) {
            System.out.println("-- onReceiveDataCompleted");
            onReceiveMessages(mClient, mClient.mReadMessageQueen.mReadQueen);
            mClient.onReceiveMessageClear();
        }
    }

    /**
     * 客户端接收到消息回调
     *
     * @param mClient client
     * @param mQueen  消息队列
     */
    public abstract void onReceiveMessages(BaseClient mClient, LinkedList<Message> mQueen);

}
