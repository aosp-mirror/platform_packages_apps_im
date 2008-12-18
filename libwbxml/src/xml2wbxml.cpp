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

#include "xml2wbxml.h"
#include "wbxml_stl.h"
#include "imps_encoder.h"

struct PublicIdData {
    const char * pubid;
    const char * xmlns;
    PublicId idVal;
};

// http://www.openmobilealliance.org/tech/omna/omna-wbxml-public-docid.htm
static const PublicIdData knownPublicId[] = {
    { "-//OMA//DTD IMPS-CSP 1.3//EN",
        "http://www.openmobilealliance.org/DTD/IMPS-CSP1.3",
        PUBLICID_IMPS_1_3 },
    { "-//OMA//DTD WV-CSP 1.2//EN",
        "http://www.openmobilealliance.org/DTD/WV-CSP1.2",
        PUBLICID_IMPS_1_2 },
    { "-//WIRELESSVILLAGE//DTD CSP 1.1//EN",
        "http://www.wireless-village.org/CSP1.1",
        PUBLICID_IMPS_1_1 },
    /*
    { "-//OMA//DRM 2.1//EN", XXX,           0x13 },
    { "-//OMA//DTD DRMREL 1.0//EN", XXX,    0x0e },
    { "-//SYNCML//DTD DevInf 1.2//EN", XXX, 0x1203 },
    { "-//SYNCML//DTD MetaInf 1.2//EN", XXX,0x1202 },
    { "-//SYNCML//DTD SyncML 1.2//EN", XXX, 0x1201 }
    */
};

static WbxmlEncoder * makeEncoder(int publicid)
{
    switch (publicid) {
        case PUBLICID_IMPS_1_3:
        case PUBLICID_IMPS_1_2:
        case PUBLICID_IMPS_1_1:
            return new ImpsWbxmlEncoder(publicid);

        default:
            return NULL;
    }
}

Xml2WbxmlEncoder::Xml2WbxmlEncoder() :
    mWbxmlHandler(NULL),
    mEncoder(NULL),
    mPublicId(-1),
    mDepth(0),
    mErrorCode(NO_ERROR)
{
    mExpatParser = new ExpatParser();
    mExpatParser->setContentHandler(this);
}

Xml2WbxmlEncoder::~Xml2WbxmlEncoder()
{
    delete(mExpatParser);
    delete(mEncoder);
}

void Xml2WbxmlEncoder::setWbxmlHandler(WbxmlHandler * handler)
{
    mWbxmlHandler = handler;
}

void Xml2WbxmlEncoder::startElement(const char *name, const char **atts)
{
    if (getErrorCode() != NO_ERROR)
        return;

    if (!mDepth) {
        if (!isPublicIdSet()) {
            for (int i = 0; atts[i]; i += 2) {
                // TODO: for now we don't handle xmlns:<prefix> yet
                if (strcmp(atts[i], "xmlns") == 0) {
                    if (detectPublicIdByXmlns(atts[i + 1]))
                        break;
                }
            }
        }
        if (isPublicIdSet()) {
            // TODO: at present fixed to WBXML 1.3, UTF-8, no string table
            mEncoder = makeEncoder(mPublicId);
            if (mEncoder == NULL) {
                setError(ERROR_UNSUPPORTED_DOCTYPE);
                return;
            }
            mEncoder->setWbxmlHandler(mWbxmlHandler);
        } else {
            setError(ERROR_NO_PUBLIC_ID);
            return;
        }
    }
    setError(mEncoder->startElement(name, atts));
    mDepth++;
}

void Xml2WbxmlEncoder::endElement(const char *name)
{
    if (getErrorCode() != NO_ERROR)
        return;

    setError(mEncoder->endElement());
    mDepth--;
}

void Xml2WbxmlEncoder::characters(const char *data, int len)
{
    if (getErrorCode() != NO_ERROR)
        return;

    setError(mEncoder->characters(data, len));
}

void Xml2WbxmlEncoder::startDoctype(const char *doctypeName,
        const char *sysid,
        const char *pubid,
        int has_internal_subset)
{
    if (!isPublicIdSet()) {
        detectPublicId(pubid);
    }
}

bool Xml2WbxmlEncoder::detectPublicId(const char * pubid)
{
    for (size_t i = 0; i < sizeof(knownPublicId) / sizeof(knownPublicId[0]); i++) {
        if (strcmp(pubid, knownPublicId[i].pubid) == 0) {
            //printf ("pubid %s => 0x%x\n", pubid, knownPublicId[i].idVal);
            setPublicId(knownPublicId[i].idVal);
            return true;
        }
    }
    return false;
}

bool Xml2WbxmlEncoder::detectPublicIdByXmlns(const char * xmlnsUri)
{
    for (size_t i = 0; i < sizeof(knownPublicId) / sizeof(knownPublicId[0]); i++) {
        if (strcmp(xmlnsUri, knownPublicId[i].xmlns) == 0) {
            //printf ("xmlns %s => 0x%x\n", xmlnsUri, knownPublicId[i].idVal);
            setPublicId(knownPublicId[i].idVal);
            return true;
        }
    }
    return false;
}

int Xml2WbxmlEncoder::encode(const char * data, uint32_t dataLen, bool end)
{
    if (data == NULL) {
        return WBXML_STATUS_ERROR;
    }
    if (mExpatParser->parse(data, (int)dataLen, end) != XML_STATUS_OK) {
        //printf ("Expat error: %s\n", XML_ErrorString(XML_GetErrorCode(mExpat)));
        return WBXML_STATUS_ERROR;
    }
    return getErrorCode() == NO_ERROR ? WBXML_STATUS_OK : WBXML_STATUS_ERROR;
}

