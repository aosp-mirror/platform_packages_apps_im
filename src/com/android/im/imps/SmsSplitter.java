/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.im.imps;

import java.nio.ByteBuffer;

/**
 * A helper class to split the payload into several segments to meet the size
 * constraint of the sms.
 *
 */
public class SmsSplitter {
    private static final int MAX_SEGMENT_COUNT = 26;

    private ByteBuffer mOutBuffer;
    private int mMaxSegmentLen;

    private byte[] mData;
    private int mPreambleEnd;

    private int mCurrentSegment;
    private int mSegmentCount;

    public SmsSplitter(int maxLen) {
        mMaxSegmentLen = maxLen;
        mOutBuffer = ByteBuffer.allocate(maxLen);
    }

    /**
     * Split the data into several segments to meet the size constraint.
     *
     * @param data
     *            The data to split. MUST be a valid PTS primitive.
     * @return The count of segments of the result or -1 if the data is too long.
     */
    public int split(byte[] data) {
        mData = data;
        mCurrentSegment = 0;
        calculateSegments();
        if (mSegmentCount > MAX_SEGMENT_COUNT) {
            mSegmentCount = -1;
        }
        return mSegmentCount;
    }

    public boolean hasNext() {
        return mCurrentSegment < mSegmentCount;
    }

    /**
     * Gets the next segment.
     *
     * @return The next segment.
     * @throws IndexOutOfBoundsException
     */
    public byte[] getNext() {
        if (mCurrentSegment >= mSegmentCount) {
            throw new IndexOutOfBoundsException();
        }
        byte[] segment;
        if (mSegmentCount == 1) {
            segment = mData;
        } else {
            mOutBuffer.clear();
            // The original preamble
            mOutBuffer.put(mData, 0, mPreambleEnd);
            // Two character of DD
            mOutBuffer.put((byte) ('a' + mCurrentSegment));
            mOutBuffer.put((byte) ('a' + mSegmentCount - 1));
            // The space after preamble
            mOutBuffer.put((byte) ' ');

            // The payload
            int segmentPayload = mMaxSegmentLen - mPreambleEnd - 3;
            int offset = mPreambleEnd + 1 + segmentPayload * mCurrentSegment;
            int len = (offset + segmentPayload > mData.length) ?
                    mData.length - offset : segmentPayload;
            mOutBuffer.put(mData, offset, len);

            mOutBuffer.flip();
            segment = new byte[mOutBuffer.limit()];
            mOutBuffer.get(segment);
        }
        mCurrentSegment++;
        return segment;
    }

    private void calculateSegments() {
        int totalLen = mData.length;
        if (totalLen < mMaxSegmentLen) {
            mSegmentCount = 1;
        } else {
            searchPreambleEnd();
            int newPreambleLen = mPreambleEnd + 2;
            int segmentPayload = mMaxSegmentLen - newPreambleLen - 1;
            int totalPayload = totalLen - mPreambleEnd - 1;
            mSegmentCount = (totalPayload + segmentPayload -1) / segmentPayload;
        }
    }

    private void searchPreambleEnd() {
        byte[] data = mData;
        int index = 0;
        while(index < data.length && data[index] != ' ') {
            index++;
        }
        mPreambleEnd = index;
    }
}