/*
 * Copyright (C) 2009 Esmertec AG.
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdio.h>
#include <stdlib.h>
#include "imps_encoder.h"

bool WbxmlEncoder::isXmlWhitespace(int ch)
{
    return ch == ' ' || ch == 9 || ch == 0xd || ch == 0xa;
}

bool WbxmlEncoder::parseUint(const char * s, int len, uint32_t *res)
{
    string str(s, len);
    char *end;
    long long val = strtoll(str.c_str(), &end, 10);
    if (*end != 0 || val < 0 || val > 0xFFFFFFFFU) {
        return false;
    }
    *res = (uint32_t)val;
    return true;
}

EncoderError WbxmlEncoder::encodeInteger(const char *chars, int len)
{
    uint32_t val;
    if (!parseUint(chars, len, &val)) {
        return ERROR_INVALID_INTEGER_VALUE;
    }

    appendResult(TOKEN_OPAQUE);
    uint32_t mask = 0xff000000U;
    int numBytes = 4;
    while (!(val & mask) && mask) {
        numBytes--;
        mask >>= 8;
    }
    if (!numBytes) {
        // Zero value. We generate at least 1 byte OPAQUE data.
        // libwbxml2 generates 0 byte long OPAQUE data (0xC3 0x00) in this case.
        numBytes = 1;
    }

    appendResult(numBytes);
    while (numBytes) {
        numBytes--;
        appendResult((val >> (numBytes * 8)) & 0xff);
    }

    return NO_ERROR;
}

EncoderError WbxmlEncoder::encodeDatetime(const char *chars, int len)
{
    // to make life easier we accept only yyyymmddThhmmssZ
    if (len != 16 || chars[8] != 'T' || chars[15] != 'Z') {
        return ERROR_INVALID_DATETIME_VALUE;
    }
    appendResult(TOKEN_OPAQUE);
    appendResult(6);

    uint32_t year, month, day, hour, min, sec;
    if (!parseUint(chars, 4, &year)
            || !parseUint(chars + 4, 2, &month)
            || !parseUint(chars + 6, 2, &day)
            || !parseUint(chars + 9, 2, &hour)
            || !parseUint(chars + 11,2, &min)
            || !parseUint(chars + 13,2, &sec)) {
        return ERROR_INVALID_DATETIME_VALUE;
    }
    if (year > 4095 || month > 12 || day > 31 || hour > 23 || min > 59 || sec > 59) {
        return ERROR_INVALID_DATETIME_VALUE;
    }

    appendResult(year >> 6);
    appendResult(((year & 0x3f) << 2) | (month >> 2));
    appendResult(((month & 0x3) << 6) | (day << 1) | (hour >> 4));
    appendResult(((hour & 0xf) << 4) | (min >> 2));
    appendResult(((min & 0x2) << 6) | sec);
    appendResult('Z');
    return NO_ERROR;
}

void WbxmlEncoder::encodeInlinedStr(const char *s, int len)
{
    // TODO: handle ENTITY
    appendResult(TOKEN_STR_I);
    appendResult(s, len);
    appendResult('\0');
}

void WbxmlEncoder::encodeMbuint(uint32_t val)
{
    char buf[32 / 7 + 1];   // each byte holds up to 7 bits
    int i = sizeof(buf);

    buf[--i] = val & 0x7f;
    val >>= 7;
    while ((i > 0) && (val & 0x7f)) {
        buf[--i] = 0x80 | (val & 0x7f);
        val >>= 7;
    }

    appendResult(buf + i, sizeof(buf) - i);
}

int WbxmlEncoder::appendToStringTable(const char *s)
{
    int stringTableSize = mStringTable.size();
    int offset = 0;
    
    // search the string table to find if the string already exist
    int index = 0;
    for (; index < stringTableSize; index++) {
        if (mStringTable[index] == s) {
            break;
        }
        offset += mStringTable[index].length();
        ++offset; // '\0' for each string in the table
    }
    if (index == stringTableSize) {
        // not found, insert a new one
        mStringTable.push_back(s);
    }
    return offset;
}

void WbxmlEncoder::sendResult()
{
    if (mHandler) {
        string data;
        string tmp = mResult;
        mResult = data;

        // WBXML 1.3, UTF-8
        char header[3] = { 0x03, (char) mPublicId, 0x6A };
        appendResult(header, 3);

        // calculate the length of string table
        int len = 0;
        for (int i = 0; i < mStringTable.size(); i++) {
            len += mStringTable[i].length();
            ++len;
        }

        encodeMbuint(len);

        // encode each string in the table
        for (int i = 0; i < mStringTable.size(); i++) {
            mResult += mStringTable[i];
            mResult += '\0';
        }

        mResult += tmp;

        mHandler->wbxmlData(mResult.c_str(), mResult.size());
    }
}
