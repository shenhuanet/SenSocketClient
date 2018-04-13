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

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public final class Message {

    private static long G_MESSAGE_ID = 0;
    public long msgId;

    public int srcReuseType;
    public int dstReuseType;

    public byte[] data;
    public int capacity;
    public int blockIndex;
    public int offset;
    public int length;

    public Message() {
        reset();
    }

    public void reset() {
        ++G_MESSAGE_ID;

        msgId = G_MESSAGE_ID;
        srcReuseType = 0;
        dstReuseType = 0;

        data = null;
        capacity = 0;
        blockIndex = 0;
        offset = 0;
        length = 0;
    }
}
