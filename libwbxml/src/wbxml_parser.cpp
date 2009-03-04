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

#include <stdio.h>
#include <stdlib.h>
#include <setjmp.h>
#include <assert.h>
#include "wbxml_parser.h"
#include "csp13_data.h"
#ifdef SUPPORT_SYNCML
#include "syncml_data.h"
#endif

#ifdef PLATFORM_ANDROID
extern "C" void *bsearch(const void *key, const void *base0, size_t nmemb,
        size_t size, int (*compar)(const void *, const void *));
#endif

#define ARRAY_SIZE(a)   (sizeof(a) / sizeof(a[0]))

//#define WBXML_DEBUG 1

/* Major TODO items:
   - Attribute value tokens (not used by IMPS CSP)
   - EXT_* except EXT_T_0 (not used by IMPS CSP)
   - PI (not used by IMPS CSP)
   - cleanups

   Other TODO:
   - Support more public ID? Only IMPS is supported now.
   - Support other charsets than UTF-8
 */

static int compareTokenData(const void * t1, const void * t2)
{
    return ((TokenData *)t1)->token - ((TokenData *)t2)->token;
}

static int compareAttrData(const void * t1, const void * t2)
{
    return ((AttrData *)t1)->token - ((AttrData *)t2)->token;
}

static bool isTagStart(int token)
{
    if (token == TOKEN_SWITCH_PAGE)
        return true;

    token &= 0x3f;
    return (token >= TOKEN_LITERAL && token < TOKEN_EXT_I_0);
}

static bool isAttrStart(int token)
{
    return (token >= TOKEN_LITERAL && token < TOKEN_EXT_I_0) ||
        (token > TOKEN_LITERAL_C && token < 0x80);
}

WbxmlParser::WbxmlParser(uint32_t transportEncoding) :
    mTransportEncoding(transportEncoding)
{
    reset();
}

WbxmlParser::~WbxmlParser()
{
}

void WbxmlParser::reset(void)
{
    mContentHandler = NULL;

    mExternalChunk = NULL;
    mExternalChunkLen = 0;
    mLastChunk.clear();
    mDataOffset = 0;
    mIsDataEnd = false;

    mStartElemStack.clear();
    mStringTable.clear();

    mCurrTagPage = mCurrAttrPage = 0;
    mPublicId = 0;

    mState = EXPECT_HEADER;
    mLastError = ERROR_NO_ERROR;
}

void WbxmlParser::setContentHandler(WbxmlContentHandler * handler)
{
    mContentHandler = handler;
}

int WbxmlParser::parse(const char * data, uint32_t dataLen, bool end)
{
    if (data == NULL) {
        mLastError = ERROR_INVALID_DATA;
        return WBXML_STATUS_ERROR;
    }

    // All temporary C++ varaibles must be declared before setjmp to make
    // sure they get properly destructed after longjmp.
    vector<Attribute> attribs;
    Attribute attrib;
    string tagName;
    string characters;
    string opaque;

#ifdef WBXML_DEBUG
    printf("\nparse dataLen %d; end %d; readPos %d; availData %d\n",
        dataLen, end, getReadPos(), availDataSize());
#endif
    appendData(data, dataLen, end);
    volatile int readPos = getReadPos();
    int setjmpRet;
    switch (setjmpRet = setjmp(mJmpbuf)) {
        case 0:
            break;

        case ERROR_NEED_MORE_DATA:
            if (!mIsDataEnd) {
#ifdef WBXML_DEBUG
                printf("\nneed more data: readPos %d\n", readPos);
#endif
                setReadPos(readPos);
                saveRemainingData();
                return WBXML_STATUS_OK;
            } else {
#ifdef WBXML_DEBUG
                printf("wbxml parser error: unexpected data end\n");
#endif
                mLastError = ERROR_NEED_MORE_DATA;
                return WBXML_STATUS_ERROR;
            }
            break;

        case ERROR_UNSUPPORTED_PUBID:
        case ERROR_UNSUPPORTED_CHARSET:
        case ERROR_INVALID_STRING_TABLE:
        case ERROR_INVALID_STRING_TABLE_REFERENCE:
        case ERROR_INVALID_EXT_TOKEN:
        case ERROR_INVALID_MBUINT:
        case ERROR_INVALID_ENTITY:
        case ERROR_UNRECOGNIZED_TAG:
        case ERROR_UNRECOGNIZED_ATTR:
        case ERROR_MISSING_ATTR:
        case ERROR_MISSING_TOKEN_END:
#ifdef WBXML_DEBUG
            printf("wbxml parser error %d\n", setjmpRet);
#endif
            mLastError = ParserError(setjmpRet);
            return WBXML_STATUS_ERROR;
            break;

        case ERROR_NOT_SUPPORTED_YET:
            printf("wbxml parser error: Not implemented feature.\n");
            mLastError = ParserError(setjmpRet);
            return WBXML_STATUS_ERROR;
            break;

        default:
            printf("wbxml parser error: Impossible execution path.\n");
            mLastError = ParserError(setjmpRet);
            return WBXML_STATUS_ERROR;
            break;
    }

    for (;;) {
        // save readPos for error recovery
        readPos = getReadPos();

        switch (mState) {
            case EXPECT_HEADER:
                mDocVersion = readByte();

                mPublicId = readMbuint32();
                if (mPublicId != 0) {
                    if (!selectTokenMapping(mPublicId)) {
#ifdef WBXML_DEBUG
                        printf("wbxml parser error: unsupported public id \n");
#endif
                        longjmp(mJmpbuf, ERROR_UNSUPPORTED_PUBID);
                    }
                } else {
                    mPublicId = -readMbuint32();
                }
                mCharset = readMbuint32();
                if (!mCharset) {
                    mCharset = mTransportEncoding;
                    if (!mCharset) {
                        mCharset = CHARSET_UTF8;
                    }
                }
                // TODO: support more charsets other than UTF-8
                if (mCharset != CHARSET_UTF8) {
#ifdef WBXML_DEBUG
                    printf("wbxml parser error: unsupported charset\n");
#endif
                    longjmp(mJmpbuf, ERROR_UNSUPPORTED_CHARSET);
                }

                // now advance to next state
                if (mContentHandler) {
                    mContentHandler->handlePublicId(mPublicId);
                }
                mState = EXPECT_STRING_TABLE;
                break;

            case EXPECT_STRING_TABLE:
            {
                uint32_t len = readMbuint32();
                if (availDataSize() < len) {
                    longjmp(mJmpbuf, ERROR_NEED_MORE_DATA);
                }
                mStringTable.clear();
                // TODO: optimize this
                while (len--) {
                    mStringTable += readByte();
                }
                if (mStringTable.size()) {
                    if (mStringTable[mStringTable.size() - 1] != 0) {
                        // must have an ending \0
                        //TODO:the byte array returned by SCTS does not contain '\0' at the
                        //end,should this be fixed accordingly?
#ifdef WBXML_DEBUG
                        printf("wbxml parser error: invalid string table\n");
#endif
                        longjmp(mJmpbuf, ERROR_INVALID_STRING_TABLE);
                    }
                }
                mState = EXPECT_BODY_START;
                if (mPublicId <= 0) {
                    const char * s = mStringTable.c_str() + (-mPublicId);
#ifdef SUPPORT_SYNCML
                    if (strcmp(s, "-//SYNCML//DTD SyncML 1.2//EN") == 0) {
                        mPublicId = PUBLICID_SYNCML_1_2;
                    } else if (strcmp(s, "-//SYNCML//DTD SyncML 1.1//EN") == 0) {
                        mPublicId = PUBLICID_SYNCML_1_1;
                    } else if (strcmp(s, "-//SYNCML//DTD SyncML 1.0//EN") == 0) {
                        mPublicId = PUBLICID_SYNCML_1_0;
                    }
#endif
                    if ((mPublicId <= 0) || !selectTokenMapping(mPublicId)) {
                        longjmp(mJmpbuf, ERROR_UNSUPPORTED_PUBID);
                    }
                }
                break;
            }

            case EXPECT_BODY_START:
                //TODO: handle possible PIs
                mState = EXPECT_ELEMENT_START;
                break;

            case EXPECT_ELEMENT_START:
            {
                int stag = readByte();
                const char * name;
                if ((stag & 0x3f) == TOKEN_LITERAL) {
                    name = resolveStrTableRef();
                } else {
                    if (stag == TOKEN_SWITCH_PAGE) {
                        mCurrTagPage = readByte();
                        stag = readByte();
                    }
                    name = lookupTagName(stag);
                }
                if (name == NULL) {
#ifdef WBXML_DEBUG
                    printf("wbxml parser error: unrecognized tag\n");
#endif
                    longjmp(mJmpbuf, ERROR_UNRECOGNIZED_TAG);
                }
                attribs.clear();
                if (stag & 0x80) {
                    // followed by 1 or more attributes
                    while (peekByte() != TOKEN_END) {
                        readAttribute(&attrib);
                        attribs.push_back(attrib);
                    }
                    if (!attribs.size()) {
#ifdef WBXML_DEBUG
                        printf("wbxml parser error: missing attributes\n");
#endif
                        longjmp(mJmpbuf, ERROR_MISSING_ATTR);
                    }
                    // TOKEN_END
                    readByte();
                }
                if (mContentHandler) {
                    mContentHandler->startElement(name, attribs);
                }
                if (stag & 0x40) {
                    mState = EXPECT_CONTENT;
                } else {
                    mState = ELEMENT_END;
                }
                tagName = name;
                mStartElemStack.push_back(name);
                break;
            }

            case EXPECT_CONTENT:
            {
                int byte = peekByte();
                if (byte == TOKEN_SWITCH_PAGE) {
                    readByte();
                    mCurrTagPage = readByte();
                    byte = peekByte();
                }
                if (isTagStart(byte) || byte == TOKEN_END) {
                    if (characters.size() && mContentHandler) {
                        mContentHandler->characters(characters.c_str(), characters.size());
                        characters.clear();
                    }
                    if (byte == TOKEN_END) {
                        mState = EXPECT_ELEMENT_END;
                    } else {
                        mState = EXPECT_ELEMENT_START;
                    }
                } else {
                    // TODO: handle extension and pi
                    switch (byte) {
                        case TOKEN_ENTITY:
                        case TOKEN_STR_I:
                        case TOKEN_STR_T:
                            readString(characters);
                            break;

                        case TOKEN_EXT_T_0:
                        {
                            readByte();
                            uint32_t valueToken = readMbuint32();
                            if (mPublicId == PUBLICID_IMPS_1_1
                                    || mPublicId == PUBLICID_IMPS_1_2
                                    || mPublicId == PUBLICID_IMPS_1_3) {
                                TokenData t = {valueToken, NULL};
                                const TokenData * res = (TokenData *)bsearch(&t,
                                        csp13ExtValueTokens, ARRAY_SIZE(csp13ExtValueTokens),
                                        sizeof(csp13ExtValueTokens[0]), compareTokenData);
                                if (res) {
                                    characters.append(res->tagName);
                                } else {
                                    longjmp(mJmpbuf, ERROR_INVALID_EXT_TOKEN);
                                }
                            } else {
                                printf ("Token 0x%x\n", byte);
                                longjmp(mJmpbuf, ERROR_NOT_SUPPORTED_YET);
                            }
                            break;
                        }

                        case TOKEN_OPAQUE:
                        {
                            readByte();
                            uint32_t opaqueDataLen = readMbuint32();
                            opaque.clear();
                            while (opaqueDataLen--) {
                                opaque += (char)readByte();
                            }
                            if (mContentHandler) {
                                mContentHandler->opaque(opaque.c_str(), opaque.size());
                            }
                            break;
                        }

                        default:
                            printf ("Token 0x%x\n", byte);
                            longjmp(mJmpbuf, ERROR_NOT_SUPPORTED_YET);
                            break;
                    }
                }
                break;
            }

            case EXPECT_ELEMENT_END:
                if (readByte() != TOKEN_END) {
#ifdef WBXML_DEBUG
                    printf("wbxml parser error: TOKEN_END expected\n");
#endif
                    longjmp(mJmpbuf, ERROR_MISSING_TOKEN_END);
                }
                mState = ELEMENT_END;
                break;

            case ELEMENT_END:
                assert(!mStartElemStack.empty());

                tagName = mStartElemStack.back();
                mStartElemStack.pop_back();
                if (mContentHandler) {
                    mContentHandler->endElement(tagName.c_str());
                }
                if (mStartElemStack.empty()) {
                    mState = EXPECT_BODY_END;
                } else {
                    mState = EXPECT_CONTENT;
                }
                break;

            case EXPECT_BODY_END:
                // TODO: handle possible PIs

                // we're done
                return WBXML_STATUS_OK;
                break;
        }
    }
}

/*
 * We don't make a copy of the data chunk for the current parse() until
 * it returns.
 * The remaining data will be saved in saveRemainingData() before parse()
 * returns.
 */
void WbxmlParser::appendData(const char * data, uint32_t len, bool end)
{
    mExternalChunk = data;
    mExternalChunkLen = len;
    mIsDataEnd = end;
}

void WbxmlParser::saveRemainingData()
{
    if (mDataOffset > mLastChunk.size()) {
        uint32_t offsetToExtChunk = mDataOffset - mLastChunk.size();
        assert(offsetToExtChunk <= mExternalChunkLen);
        mLastChunk.assign(mExternalChunk + offsetToExtChunk,
                mExternalChunkLen - offsetToExtChunk);
        mDataOffset = 0;
    } else {
        mLastChunk.append(mExternalChunk, mExternalChunkLen);
    }
    mExternalChunk = NULL;
    mExternalChunkLen = 0;
}

int WbxmlParser::readByte()
{
    if (mDataOffset < mLastChunk.size()) {
#ifdef WBXML_DEBUG
        printf ("rb 0x%x; ", (unsigned char)mLastChunk[mDataOffset]);
#endif
        return (unsigned char)mLastChunk[mDataOffset++];
    } else {
        uint32_t offsetToExtChunk = mDataOffset - mLastChunk.size();
        if (offsetToExtChunk < mExternalChunkLen) {
            mDataOffset++;
#ifdef WBXML_DEBUG
            printf ("rb 0x%x; ", (unsigned char)mExternalChunk[offsetToExtChunk]);
#endif
            return (unsigned char)mExternalChunk[offsetToExtChunk];
        }
        longjmp(mJmpbuf, ERROR_NEED_MORE_DATA);
    }
}

int WbxmlParser::peekByte()
{
    if (mDataOffset < mLastChunk.size()) {
        return (unsigned char)mLastChunk[mDataOffset];
    } else {
        uint32_t offsetToExtChunk = mDataOffset - mLastChunk.size();
        if (offsetToExtChunk < mExternalChunkLen) {
            return (unsigned char)mExternalChunk[offsetToExtChunk];
        }
        longjmp(mJmpbuf, ERROR_NEED_MORE_DATA);
    }
}

uint32_t WbxmlParser::readMbuint32()
{
    uint32_t value = 0;
    uint32_t byte;
    do {
        if ((value >> 25) != 0) {
            // would go overflow. not a valid uint32.
            longjmp(mJmpbuf, ERROR_INVALID_MBUINT);
        }
        byte = readByte();
        value = (value << 7) | (byte & 0x7f);
    } while (byte & 0x80);
    return value;
}

/**
 * Read STR_I | STR_T | ENTITY and *append* to str.
 * Yes this looks ugly...
 */
void WbxmlParser::readString(string & str)
{
    int byte = readByte();
    switch (byte) {
        case TOKEN_STR_I:
            //TODO: assuming UTF-8
            while ((byte = readByte()) != 0) {
                str += (char)byte;
            }
            break;

        case TOKEN_ENTITY:
        {
            uint32_t ch = readMbuint32();
            //TODO: assuming UTF-8 for now.
            if (ch <= 0x7f) {
                str += (char)ch;
            } else if (ch <= 0x7ff) {
                str += (char)((ch >> 6) | 0xc0);
                str += (char)((ch & 0x3f) | 0x80);
            } else if (ch <= 0xffff) {
                str += (char)((ch >> 12) | 0xe0);
                str += (char)(((ch >> 6) & 0x3f) | 0x80);
                str += (char)((ch & 0x3f) | 0x80);
            } else if (ch <= 0x10ffff) {
                // 010000 - 10FFFF
                str += (char)((ch >> 18) | 0xf0);
                str += (char)(((ch >> 12) & 0x3f) | 0x80);
                str += (char)(((ch >> 6) & 0x3f) | 0x80);
                str += (char)((ch & 0x3f) | 0x80);
            } else {
                // not a valid UCS-4 character
                longjmp(mJmpbuf, ERROR_INVALID_ENTITY);
            }
            break;
        }

        case TOKEN_STR_T:
        {
            const char * s = resolveStrTableRef();
            str.append(s, strlen(s));
            break;
        }

        default:
            // impossible
            printf ("Unknown token 0x%02x\n", byte);
            longjmp(mJmpbuf, ERROR_NOT_SUPPORTED_YET);
            break;
    }
}

const char * WbxmlParser::resolveStrTableRef(void)
{
    uint32_t offset = readMbuint32();
    if (offset >= mStringTable.size()) {
        longjmp(mJmpbuf, ERROR_INVALID_STRING_TABLE_REFERENCE);
    }
    return mStringTable.c_str() + offset;
}

bool WbxmlParser::selectTokenMapping(int publicId)
{
    switch (publicId) {
        case PUBLICID_IMPS_1_3:
        case PUBLICID_IMPS_1_2:
        case PUBLICID_IMPS_1_1:
            mTagPages = csp13TagPages;
            mNumTagPages = ARRAY_SIZE(csp13TagPages);
            mAttrPages = csp13AttrPages;
            mNumAttrPages = ARRAY_SIZE(csp13AttrPages);
            break;

#ifdef SUPPORT_SYNCML
        case PUBLICID_SYNCML_1_0:
        case PUBLICID_SYNCML_1_1:
        case PUBLICID_SYNCML_1_2:
        case PUBLICID_SYNCML_METINF_1_2:
            mTagPages = syncmlTagPages;
            mNumTagPages = ARRAY_SIZE(syncmlTagPages);
            mAttrPages = NULL;
            mNumAttrPages = 0;
            break;

        case PUBLICID_SYNCML_DEVINF_1_2:
            mTagPages = syncmlDevInfTagPages;
            mNumTagPages = ARRAY_SIZE(syncmlDevInfTagPages);
            mAttrPages = NULL;
            mNumAttrPages = 0;
            break;
#endif
        default:
            return false;
    }
    return true;
}

const char * WbxmlParser::lookupTagName(int tag) const
{
    tag = tag & 0x3f;

    // TODO: optimize this
    if (mCurrTagPage >= mNumTagPages) {
        return NULL;
    }
    const TagCodePage * page = &mTagPages[mCurrTagPage];
    if (page == NULL) {
        return NULL;
    }

    TokenData t = {tag, NULL};
    const TokenData * res = (TokenData *)bsearch(&t, page->tags, page->numTokens,
            sizeof(TokenData), compareTokenData);
    if (res) {
        return res->tagName;
    }

    return NULL;
}

const char * WbxmlParser::lookupAttrName(int token, const char **prefix) const
{
    // TODO: optimize this
    if (mCurrAttrPage >= mNumAttrPages) {
        return NULL;
    }
    const AttrCodePage * page = &mAttrPages[mCurrAttrPage];
    if (page == NULL) {
        return NULL;
    }

    AttrData t = {token, NULL, NULL};
    const AttrData * res = (AttrData *)bsearch(&t, page->attrs, page->numTokens,
            sizeof(AttrData), compareAttrData);
    if (res) {
        if (prefix) {
            *prefix = res->attrValuePrefix;
        }
        return res->attrName;
    }

    return NULL;
}

void WbxmlParser::readAttribute(Attribute * attrib)
{
    // attribute start: attrib start token, LITERAL or END
    int attrStart = readByte();
    const char * name;
    const char * valuePrefix = NULL;

    if (attrStart == TOKEN_LITERAL) {
        name = resolveStrTableRef();
    } else {
        if (attrStart == TOKEN_SWITCH_PAGE) {
            mCurrAttrPage = readByte();
            attrStart = readByte();
        }
        name = lookupAttrName(attrStart, &valuePrefix);
    }
    if (name == NULL) {
        longjmp(mJmpbuf, ERROR_UNRECOGNIZED_ATTR);
    }
    attrib->name = name;
    attrib->value = "";
    if (valuePrefix != NULL) {
        attrib->value = valuePrefix;
    }

    // now attribute value: zero or more value, string, entity or extension tokens
    for (;;) {
        int valueToken = peekByte();
        if (isAttrStart(valueToken) || valueToken == TOKEN_END) {
            // An attribute start token, a LITERAL token or the END token
            // indicates the end of an attribute value.
            return;
        }
        switch (valueToken) {
            case TOKEN_ENTITY:
            case TOKEN_STR_I:
            case TOKEN_STR_T:
                readString(attrib->value);
                break;

            case TOKEN_EXT_I_0:
            case TOKEN_EXT_I_1:
            case TOKEN_EXT_I_2:
            case TOKEN_EXT_0:
            case TOKEN_EXT_1:
            case TOKEN_EXT_2:
                //TODO: document type specific
                printf ("Unsupported Token 0x%x\n", valueToken);
                longjmp(mJmpbuf, ERROR_NOT_SUPPORTED_YET);
                break;

            default:
                //TODO
                printf ("Unknown Token 0x%x\n", valueToken);
                longjmp(mJmpbuf, ERROR_NOT_SUPPORTED_YET);
                break;
        }
    }
}

