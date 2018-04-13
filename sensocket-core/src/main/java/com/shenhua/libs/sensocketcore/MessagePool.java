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

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public final class MessagePool {

    public static ConcurrentLinkedQueue<Message> mQueen = new ConcurrentLinkedQueue<>();

    public static void init(int msgMaxSize) {
        for (int i = 0; i < msgMaxSize; i++) {
            mQueen.add(new Message());
        }
    }

    public static Message get() {
        Message ret = mQueen.poll();
        if (null == ret) {
            ret = new Message();
        }
        return ret;
    }

    public static void put(Message obj) {
        if (null != obj) {
            obj.reset();
            mQueen.add(obj);
        }
    }
}
