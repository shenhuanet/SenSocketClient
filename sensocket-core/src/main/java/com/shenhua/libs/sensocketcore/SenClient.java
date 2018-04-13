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

import com.shenhua.libs.sensocketcore.message.MessageBuffer;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public final class SenClient {

    private static boolean isInitialized = false;

    public static void init() {
        if (!isInitialized) {
            MessagePool.init(6);
            MessageBuffer.init(8 * MessageBuffer.KB, 64 * MessageBuffer.KB,
                    MessageBuffer.MB, 5, 2, 0, 2);
            isInitialized = true;
        }
    }
}
