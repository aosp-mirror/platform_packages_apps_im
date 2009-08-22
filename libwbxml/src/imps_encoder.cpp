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

static bool isDatetimeElement(const char *name)
{
    return (strcmp("DateTime", name) == 0 || strcmp("DeliveryTime", name) == 0);
}

void ImpsWbxmlEncoder::reset()
{
    clearResult();

    mTagCodePage = 0;
    mCurrElement.clear();
    mDepth = 0;
}

EncoderError ImpsWbxmlEncoder::startElement(const char *name, const char **atts)
{
    if (name == NULL) {
        return ERROR_INVALID_DATA;
    }

    bool isUnknownTag = false;
    int stag = csp13TagNameToKey(name);
    if (stag == -1) {
        stag = TOKEN_LITERAL;
        isUnknownTag = true;
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

    if (isUnknownTag) {
        int index = appendToStringTable(name);
        encodeMbuint(index);
    }
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
    if (mDepth == 0) {
        sendResult();
    }
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
    size_t csp13xmlnsCount = sizeof(csp13xmlns) / sizeof(csp13xmlns[0]);
    size_t i;
    for (i = 0; i < csp13xmlnsCount; i++) {
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
    if (i == csp13xmlnsCount) {
        // not predefined attribute
        appendResult(TOKEN_LITERAL);
        int index = appendToStringTable(name);
        encodeMbuint(index);
    }
    encodeInlinedStr(value, valueLen);
    return NO_ERROR;
}
