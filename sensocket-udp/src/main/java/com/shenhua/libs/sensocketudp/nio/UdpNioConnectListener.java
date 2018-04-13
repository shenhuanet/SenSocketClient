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

package com.shenhua.libs.sensocketudp.nio;

import java.io.IOException;
import java.nio.channels.DatagramChannel;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public interface UdpNioConnectListener {

    /**
     * 连接成功时回调
     *
     * @param mSocketProcessor 读写处理器
     * @param socketChannel    数据报通道
     * @throws IOException ioException
     */
    void onConnectSuccess(UdpNioReadWriteProcessor mSocketProcessor, DatagramChannel socketChannel) throws IOException;

    /**
     * 连接失败时回调
     *
     * @param mSocketProcessor 读写处理器
     */
    void onConnectFailed(UdpNioReadWriteProcessor mSocketProcessor);

}