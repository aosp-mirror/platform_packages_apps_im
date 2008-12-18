/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
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

#ifndef WBXML_PARSER_H
#define WBXML_PARSER_H

#include <setjmp.h>
#include <stdint.h>
#include "wbxml_const.h"
#include "wbxml_stl.h"
#include "wbxml_tabledef.h"

struct Attribute
{
    string name;
    string value;
};

class WbxmlContentHandler
{
public:
    virtual ~WbxmlContentHandler() {}
    virtual void handlePublicId(uint32_t id) = 0;
    virtual void startElement(const char * name, const vector<Attribute> & attribs) = 0;
    virtual void endElement(const char * name) = 0;
    virtual void characters(const char * data, int len) = 0;
    virtual void opaque(const char * data, int len) = 0;
};

class DefaultWbxmlContentHandler: public WbxmlContentHandler
{
public:
    DefaultWbxmlContentHandler()
    {
        mPublicId = -1;
    }

    void handlePublicId(uint32_t id)
    {
        mPublicId = id;
    }

    // @return public ID or -1 if no public ID seen
    int getPublicId(void) const
    {
        return mPublicId;
    }

    void startElement(const char * name, const vector<Attribute> & attribs)
    {
    }

    void endElement(const char * name)
    {
    }

    void characters(const char * data, int len)
    {
    }

    void opaque(const char * data, int len)
    {
    }

private:
    int mPublicId;
};

class WbxmlParser
{
public:
    WbxmlParser(uint32_t transportEncoding);
    ~WbxmlParser();

    void setContentHandler(WbxmlContentHandler * handler);

    //void setTokenMappings(uint32_t publicId, TagTable tagTable, AttrTable attrTable);
    int parse(const char * data, uint32_t len, bool end);

    void reset(void);

    int getError(void) const
    {
        return mLastError;
    }

private:
    enum ParserState {
        EXPECT_HEADER,
        EXPECT_STRING_TABLE,
        EXPECT_BODY_START,
        EXPECT_ELEMENT_START,
        EXPECT_ELEMENT_END,
        ELEMENT_END,
        EXPECT_CONTENT,
        EXPECT_BODY_END,
    };
    enum ParserError {
        ERROR_NO_ERROR = 0,
        ERROR_INVALID_DATA = 1,
        ERROR_NEED_MORE_DATA,
        ERROR_UNSUPPORTED_PUBID,
        ERROR_UNSUPPORTED_CHARSET,
        ERROR_INVALID_STRING_TABLE,
        ERROR_INVALID_STRING_TABLE_REFERENCE,
        ERROR_INVALID_EXT_TOKEN,
        ERROR_INVALID_MBUINT,
        ERROR_INVALID_ENTITY,
        ERROR_UNRECOGNIZED_TAG,
        ERROR_UNRECOGNIZED_ATTR,
        ERROR_MISSING_ATTR,
        ERROR_MISSING_TOKEN_END,
        ERROR_NOT_SUPPORTED_YET   = 999,
    };

    int readByte();
    int peekByte();
    uint32_t readMbuint32();
    void readString(string & str);
    const char * resolveStrTableRef(void);

    const char * lookupTagName(int tag) const;
    const char * lookupAttrName(int tag, const char **valuePrefix) const;
    void readAttribute(Attribute * attrib);

    jmp_buf mJmpbuf;

    string mLastChunk;
    const char * mExternalChunk;
    uint32_t mExternalChunkLen;
    uint32_t mDataOffset;
    bool mIsDataEnd;

    int getReadPos(void) const
    {
        return mDataOffset;
    }
    void setReadPos(int pos)
    {
        mDataOffset = pos;
    }
    void appendData(const char * data, uint32_t len, bool end);
    void saveRemainingData();
    uint32_t availDataSize(void) const
    {
        return mLastChunk.size() + mExternalChunkLen - mDataOffset;
    }

    bool selectTokenMapping(int publicId);

    const TagCodePage * mTagPages;
    uint32_t mNumTagPages;
    const AttrCodePage * mAttrPages;
    uint32_t mNumAttrPages;

    uint32_t mTransportEncoding;
    WbxmlContentHandler * mContentHandler;

    vector<string> mStartElemStack;
    string mStringTable;
    uint32_t mCurrTagPage;
    uint32_t mCurrAttrPage;

    ParserState mState;
    ParserError mLastError;

    int mDocVersion;
    uint32_t mPublicId;
    uint32_t mCharset;
};

#endif

