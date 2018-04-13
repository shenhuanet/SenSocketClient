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

import com.shenhua.libs.sensocketcore.MessagePool;

import java.util.LinkedList;

/**
 * Created by shenhua on 2018/4/11.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public final class MessageBuffer {

    public static final int KB = 1024;
    public static final int MB = 1024 * KB;

    private static int capacity_small = 8 * KB;
    private static int capacity_middle = 128 * KB;
    private static int capacity_large = MB;

    private static int size_small = 5;
    private static int size_middle = 2;
    private static int size_large = 0;

    private byte[] bufferSmall;
    private byte[] bufferMiddle;
    private byte[] bufferLarge;

    private MessageBufferTracker trackerBufferSmall;
    private MessageBufferTracker trackerBufferMiddle;
    private MessageBufferTracker trackerBufferLarge;

    private static int max_size_temporary_cache = 2;
    private LinkedList<byte[]> mTemporaryCacheList = new LinkedList<>();

    private final int REUSE_SMALL = 1;
    private final int REUSE_MIDDLE = 2;
    private final int REUSE_LARGE = 3;
    private final int REUSE_TEMP = 4;

    public static void init(int capacitySmall, int capacityMiddle, int capacityLarge,
                            int sizeSmall, int sizeMiddle, int sizeLarge, int maxTemporaryCacheSize) {

        MessageBuffer.capacity_small = capacitySmall;
        MessageBuffer.capacity_middle = capacityMiddle;
        MessageBuffer.capacity_large = capacityLarge;

        MessageBuffer.size_small = sizeSmall;
        MessageBuffer.size_middle = sizeMiddle;
        MessageBuffer.size_large = sizeLarge;
        MessageBuffer.max_size_temporary_cache = maxTemporaryCacheSize;
    }

    public MessageBuffer() {
        bufferSmall = new byte[size_small * capacity_small];
        bufferMiddle = new byte[size_middle * capacity_middle];
        bufferLarge = new byte[size_large * capacity_large];

        trackerBufferSmall = new MessageBufferTracker(size_small);
        trackerBufferMiddle = new MessageBufferTracker(size_middle);
        trackerBufferLarge = new MessageBufferTracker(size_large);
    }

    public Message build(byte[] src, int offset, int length) {
        Message msg = build(length);
        System.arraycopy(src, offset, msg.data, msg.offset, length);
        msg.length = length;
        return msg;
    }

    private Message build(int length) {
        Message ret = MessagePool.get();

        if (length <= capacity_small) {
            ret.srcReuseType = REUSE_SMALL;
        } else if (length <= capacity_middle) {
            ret.srcReuseType = REUSE_MIDDLE;
        } else if (length <= capacity_large) {
            ret.srcReuseType = REUSE_LARGE;
        } else {
            ret.srcReuseType = REUSE_TEMP;
        }

        if (length <= capacity_small) {
            int blockIndex = trackerBufferSmall.get();
            if (blockIndex != -1) {
                ret.blockIndex = blockIndex;
                ret.data = bufferSmall;
                ret.capacity = capacity_small;
                ret.offset = capacity_small * ret.blockIndex;
                ret.length = 0;
                ret.dstReuseType = REUSE_SMALL;
                return ret;
            }
        }

        if (length <= capacity_middle) {
            int blockIndex = trackerBufferMiddle.get();
            if (blockIndex != -1) {
                ret.blockIndex = blockIndex;
                ret.data = bufferMiddle;
                ret.capacity = capacity_middle;
                ret.offset = capacity_middle * ret.blockIndex;
                ret.length = 0;
                ret.dstReuseType = REUSE_MIDDLE;
                return ret;
            }
        }

        if (length <= capacity_large) {
            int blockIndex = trackerBufferLarge.get();
            if (blockIndex != -1) {
                ret.blockIndex = blockIndex;
                ret.data = bufferLarge;
                ret.capacity = capacity_large;
                ret.offset = capacity_large * ret.blockIndex;
                ret.length = 0;
                ret.dstReuseType = REUSE_LARGE;
                return ret;
            }
        }

        int mTemporaryCacheListSize = mTemporaryCacheList.size();
        for (int i = 0; i < mTemporaryCacheListSize; i++) {
            if (length <= mTemporaryCacheList.get(i).length) {
                ret.blockIndex = 0;
                ret.data = mTemporaryCacheList.remove(i);
                ret.capacity = ret.data.length;
                ret.offset = 0;
                ret.length = 0;
                ret.dstReuseType = REUSE_TEMP;
                return ret;
            }
        }

        // 要么数据体太大，要么数据体全部用完了，自由创建数据
        ret.blockIndex = 0;
        ret.data = new byte[length];
        ret.capacity = ret.data.length;
        ret.offset = 0;
        ret.length = 0;
        ret.dstReuseType = REUSE_TEMP;

        return ret;
    }

    public void release(Message msg) {
        if (msg.dstReuseType == REUSE_SMALL) {
            trackerBufferSmall.release(msg.blockIndex);
            MessagePool.put(msg);
        } else if (msg.dstReuseType == REUSE_MIDDLE) {
            trackerBufferMiddle.release(msg.blockIndex);
            MessagePool.put(msg);
        } else if (msg.dstReuseType == REUSE_LARGE) {
            trackerBufferLarge.release(msg.blockIndex);
            MessagePool.put(msg);
        } else if (msg.dstReuseType == REUSE_TEMP) {
            if (max_size_temporary_cache > 0) {
                while (mTemporaryCacheList.size() >= max_size_temporary_cache) {
                    mTemporaryCacheList.poll();
                }
                mTemporaryCacheList.add(msg.data);
                msg.reset();
            } else {
                mTemporaryCacheList.clear();
                msg.reset();
            }
        }
    }


    public class MessageBufferTracker {

        private int size;

        private byte[] arrayAvailableIndex;
        private int usedCount;
        private int nextAvailableIndex;

        public MessageBufferTracker(int size) {
            this.size = size;
            arrayAvailableIndex = new byte[size];
            for (int i = 0; i < size; i++) {
                arrayAvailableIndex[i] = 1;
            }
            usedCount = 0;
            nextAvailableIndex = 0;
        }

        private int findAvailableIndex() {
            if (usedCount < size) {
                for (int i = nextAvailableIndex; i >= 0 && i < size; i++) {
                    if (arrayAvailableIndex[i] == 1) {
                        return i;
                    }
                }

                for (int i = 0; i >= 0 && i < nextAvailableIndex; i++) {
                    if (arrayAvailableIndex[i] == 1) {
                        return i;
                    }
                }
            }
            return -1;
        }

        public int get() {
            int index = findAvailableIndex();
            if (index != -1) {
                arrayAvailableIndex[index] = 0;
                usedCount++;

                nextAvailableIndex++;
                if (nextAvailableIndex >= size) {
                    nextAvailableIndex = 0;
                }
            }
            return index;
        }

        public void release(int index) {
            if (index < 0 || index >= size) {
                return;
            }
            if (arrayAvailableIndex[index] == 0) {
                arrayAvailableIndex[index] = 1;
                usedCount--;
            }
        }
    }
}
