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

package com.shenhua.libs.sensocketcore.message;

import java.util.LinkedList;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public final class MessageWriteQueen {

    private MessageBuffer mWriteMessageBuffer = new MessageBuffer();
    public LinkedList<Message> mWriteQueen = new LinkedList<>();

    public Message build(byte[] src, int offset, int length) {
        Message msg = mWriteMessageBuffer.build(src, offset, length);
        return msg;
    }

    public void add(Message msg) {
        if (null != msg) {
            mWriteQueen.add(msg);
        }
    }

    public void remove(Message msg) {
        if (null != msg) {
            mWriteQueen.remove(msg);
            mWriteMessageBuffer.release(msg);
        }
    }
}
