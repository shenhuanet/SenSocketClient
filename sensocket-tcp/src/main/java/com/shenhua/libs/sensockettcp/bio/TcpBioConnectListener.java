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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public interface TcpBioConnectListener {

    /**
     * Tcp Bio 连接成功时回调
     *
     * @param mSocketProcessor 读写处理器
     * @param mOutputStream    输出流
     * @param mInputStream     输入流
     * @throws IOException ioException
     */
    void onConnectSuccess(TcpBioReadWriteProcessor mSocketProcessor, OutputStream mOutputStream, InputStream mInputStream) throws IOException;

    /**
     * Tcp Bio 连接失败时回调
     *
     * @param mSocketProcessor 读写处理器
     */
    void onConnectFailed(TcpBioReadWriteProcessor mSocketProcessor);

}