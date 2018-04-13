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
import com.shenhua.libs.sensocketcore.message.MessageReadQueen;
import com.shenhua.libs.sensocketcore.message.MessageWriteQueen;

/**
 * Socket 客户端基类
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public abstract class BaseClient {

    public MessageReadQueen mReadMessageQueen = new MessageReadQueen();

    public MessageWriteQueen mWriteMessageQueen = new MessageWriteQueen();

    protected BaseMessageProcessor mMessageProcessor;

    public BaseClient(BaseMessageProcessor mMessageProcessor) {
        this.mMessageProcessor = mMessageProcessor;
    }

    public void clearUnreachableMessages() {
        Message msg = pollWriteMessage();
        while (null != msg) {
            removeWriteMessage(msg);
            msg = pollWriteMessage();
        }
    }

    public void onReceiveData(byte[] src, int offset, int length) {
        Message msg = mReadMessageQueen.build(src, offset, length);
        mReadMessageQueen.add(msg);
    }

    public void onReceiveMessageClear() {
        Message msg = mReadMessageQueen.mReadQueen.poll();
        while (null != msg) {
            mReadMessageQueen.remove(msg);
            msg = mReadMessageQueen.mReadQueen.poll();
        }
    }

    public void onSendMessage(byte[] src, int offset, int length) {
        Message msg = mWriteMessageQueen.build(src, offset, length);
        mWriteMessageQueen.add(msg);
        onCheckConnect();
    }

    protected Message pollWriteMessage() {
        return mWriteMessageQueen.mWriteQueen.poll();
    }

    protected void removeWriteMessage(Message msg) {
        mWriteMessageQueen.remove(msg);
    }

    /**
     * 检测连接回调
     */
    public abstract void onCheckConnect();

    /**
     * 关闭回调
     */
    public abstract void onClose();

    /**
     * 读取数据回调
     *
     * @return boolean
     */
    public abstract boolean onRead();

    /**
     * 写数据回调
     *
     * @return boolean
     */
    public abstract boolean onWrite();
}
