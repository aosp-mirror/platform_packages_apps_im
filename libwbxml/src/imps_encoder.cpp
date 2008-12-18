/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
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
#include "csp13_hash.h"

/* TODOs:
 * - use string table?
 * - move common WBXML routines to WbxmlEncoder
 * - so called "token" based IMPS value encoding
 */

struct XmlnsPrefix {
    const char * prefix;
    int attrToken;
};
static const XmlnsPrefix csp13xmlns[] = {
    { "http://www.wireless-village.org/CSP",        0x05 },
    { "http://www.wireless-village.org/PA",         0x06 },
    { "http://www.wireless-village.org/TRC",        0x07 },
    { "http://www.openmobilealliance.org/DTD/WV-CSP",   0x08 },
    { "http://www.openmobilealliance.org/DTD/WV-PA",    0x09 },
    { "http://www.openmobilealliance.org/DTD/WV-TRC",   0x0a },
    { "http://www.openmobilealliance.org/DTD/IMPS-CSP", 0x0b },
    { "http://www.openmobilealliance.org/DTD/IMPS-PA",  0x0c },
    { "http://www.openmobilealliance.org/DTD/IMPS-TRC", 0x0d },
};

static bool isXmlWhitespace(int ch)
{
    return ch == ' ' || ch == 9 || ch == 0xd || ch == 0xa;
}

static bool isDatetimeElement(const char *name)
{
    return (strcmp("DateTime", name) == 0 || strcmp("DeliveryTime", name) == 0);
}

static bool parseUint(const char * s, int len, uint32_t *res)
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

void ImpsWbxmlEncoder::reset()
{
    // WBXML 1.3, UTF-8, no string table
    char header[4] = {0x03, (char)mPublicId, 0x6A, 0x00};
    mResult.clear();
    mResult.append(header, sizeof(header));

    mTagCodePage = 0;
    mCurrElement.clear();
    mDepth = 0;
}

EncoderError ImpsWbxmlEncoder::startElement(const char *name, const char **atts)
{
    if (name == NULL) {
        return ERROR_INVALID_DATA;
    }

    int stag = csp13TagNameToKey(name);
    if (stag == -1) {
        return ERROR_UNSUPPORTED_TAG;
    }
    mDepth++;
    mCurrElement = name;

    if (((stag >> 8) & 0xff) != mTagCodePage) {
        // SWITCH_PAGE
        mTagCodePage = (stag >> 8) & 0xff;
        appendResult(TOKEN_SWITCH_PAGE);
        appendResult(mTagCodePage);
    }
    stag &= 0xff;
    stag |= 0x40;       // TODO: assuming we always have content

    if (atts && atts[0]) {
        stag |= 0x80;   // has attribute
    }
    appendResult(stag);
    if (stag & 0x80) {
        for (size_t i = 0; atts[i]; i += 2) {
            EncoderError err = encodeAttrib(atts[i], atts[i + 1]);
            if (err != NO_ERROR) {
                return err;
            }
        }
        appendResult(TOKEN_END);
    }
    return NO_ERROR;
}

EncoderError ImpsWbxmlEncoder::characters(const char *chars, int len)
{
    if (chars == NULL || len < 0) {
        return ERROR_INVALID_DATA;
    }
    if (!len) {
        return NO_ERROR;
    }
    while (len && isXmlWhitespace(*chars)) {
        chars++;
        len--;
    }
    while (len && isXmlWhitespace(chars[len - 1])) {
        len--;
    }
    if (!len) {
        return NO_ERROR;
    }

    if (csp13IsIntegerTag(mCurrElement.c_str())) {
        return encodeInteger(chars, len);
    } else if (isDatetimeElement(mCurrElement.c_str())) {
        return encodeDatetime(chars, len);
    } else {
        return encodeString(chars, len);
    }
}

EncoderError ImpsWbxmlEncoder::opaque(const char *chars, int len)
{
    if (chars == NULL || len < 0) {
        return ERROR_INVALID_DATA;
    }
    if (!len) {
        return NO_ERROR;
    }
    appendResult(TOKEN_OPAQUE);
    encodeMbuint((uint32_t)len);
    appendResult(chars, len);
    return NO_ERROR;
}

EncoderError ImpsWbxmlEncoder::endElement()
{
    mDepth--;
    if (mDepth < 0) {
        return ERROR_INVALID_END_ELEMENT;
    }
    appendResult(TOKEN_END);
    mCurrElement.clear();
    if (mDepth == 0 && mHandler) {
        mHandler->wbxmlData(mResult.c_str(), mResult.size());
    }
    return NO_ERROR;
}

EncoderError ImpsWbxmlEncoder::encodeInteger(const char *chars, int len)
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

EncoderError ImpsWbxmlEncoder::encodeDatetime(const char *chars, int len)
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

EncoderError ImpsWbxmlEncoder::encodeString(const char *chars, int len)
{
    // FIXME: should match and replace based on tokens (words)
    int token = csp13ValueTokenToKey(chars, len);
    if (token == -1) {
        encodeInlinedStr(chars, len);
    } else {
        appendResult(TOKEN_EXT_T_0);
        encodeMbuint(token);
    }
    return NO_ERROR;
}

EncoderError ImpsWbxmlEncoder::encodeAttrib(const char *name, const char *value)
{
    // IMPS so far has only "xmlns" attribute.
    // TODO: rewrite in a more generic way and move this to WbxmlEncoder
    if (strcmp(name, "xmlns")) {
        return ERROR_UNSUPPORTED_ATTR;
    }
    int valueLen = strlen(value);
    for (size_t i = 0; i < sizeof(csp13xmlns) / sizeof(csp13xmlns[0]); i++) {
        const char * prefix = csp13xmlns[i].prefix;
        int prefixLen = strlen(csp13xmlns[i].prefix);
        if (strncmp(prefix, value, prefixLen) == 0) {
            appendResult(csp13xmlns[i].attrToken);
            if (valueLen > prefixLen) {
                encodeInlinedStr(value + prefixLen, valueLen - prefixLen);
            }
            return NO_ERROR;
        }
    }
    encodeInlinedStr(value, valueLen);
    return NO_ERROR;
}

void ImpsWbxmlEncoder::encodeInlinedStr(const char *s, int len)
{
    // TODO: move this to WbxmlEncoder
    // TODO: handle ENTITY
    appendResult(TOKEN_STR_I);
    appendResult(s, len);
    appendResult('\0');
}

void ImpsWbxmlEncoder::encodeMbuint(uint32_t val)
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

