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

import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public interface UdpBioConnectListener {

    /**
     * 连接成功时回调
     *
     * @param mSocketProcessor     读写处理器
     * @param mSocket              数据报
     * @param mWriteDatagramPacket 写数据报包
     * @param mReadDatagramPacket  读数据报包
     */
    void onConnectSuccess(UdpBioReadWriteProcessor mSocketProcessor, DatagramSocket mSocket, DatagramPacket mWriteDatagramPacket, DatagramPacket mReadDatagramPacket);

    /**
     * 连接失败时回调
     *
     * @param mSocketProcessor 读写处理器
     */
    void onConnectFailed(UdpBioReadWriteProcessor mSocketProcessor);

}
