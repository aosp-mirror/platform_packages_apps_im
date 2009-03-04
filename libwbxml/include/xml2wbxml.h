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

#ifndef XML_TO_WBXML_ENCODER_H
#define XML_TO_WBXML_ENCODER_H

#include <expat.h>
#include "wbxml_encoder.h"
#include "wbxml_stl.h"
#include "expat_parser.h"

class Xml2WbxmlEncoder: public XmlContentHandler
{
public:
    Xml2WbxmlEncoder();
    ~Xml2WbxmlEncoder();

    void setWbxmlHandler(WbxmlHandler * handler);

    int encode(const char * data, uint32_t len, bool end);

    void startElement(const char *name, const char **atts);
    void endElement(const char *name);
    void characters(const char *data, int len);
    void startDoctype(const char *doctypeName, const char *sysid, const char *pubid,
            int has_internal_subset);
    void endDoctype(void) {}

private:
    WbxmlHandler * mWbxmlHandler;
    WbxmlEncoder * mEncoder;
    ExpatParser * mExpatParser;

    int mPublicId;
    int mDepth;
    int mErrorCode;

    bool detectPublicId(const char * pubid);
    bool detectPublicIdByXmlns(const char * xmlnsUri);
    bool isPublicIdSet(void) const
    {
        return mPublicId != -1;
    }
    void setPublicId(int id)
    {
        mPublicId = id;
    }
    int getErrorCode(void) const
    {
        return mErrorCode;
    }
    void setError(int err)
    {
        mErrorCode = err;
    }
};

#endif

