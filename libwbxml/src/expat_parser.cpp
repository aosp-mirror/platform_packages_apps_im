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

#include "expat_parser.h"

ExpatParser::ExpatParser() :
    mHandler(NULL)
{
    mExpat = XML_ParserCreate(NULL);
    if (mExpat) {
        XML_SetUserData(mExpat, this);
        XML_SetDoctypeDeclHandler(mExpat, startDoctypeWrapper, endDoctypeWrapper);
        XML_SetElementHandler(mExpat, startElementWrapper, endElementWrapper);
        XML_SetCharacterDataHandler(mExpat, charactersWrapper);
    }
}

ExpatParser::~ExpatParser()
{
    if (mExpat) {
        XML_ParserFree(mExpat);
    }
}

void ExpatParser::startElementWrapper(void *userData, const char *name, const char **atts)
{
    ExpatParser * self = (ExpatParser *)userData;
    if (self->mHandler) {
        self->mHandler->startElement(name, atts);
    }
}

void ExpatParser::endElementWrapper(void *userData, const char *name)
{
    ExpatParser * self = (ExpatParser *)userData;
    if (self->mHandler) {
        self->mHandler->endElement(name);
    }
}

void ExpatParser::charactersWrapper(void *userData, const char *data, int len)
{
    ExpatParser * self = (ExpatParser *)userData;
    if (self->mHandler) {
        self->mHandler->characters(data, len);
    }
}

void ExpatParser::startDoctypeWrapper(void *userData,
            const char *doctypeName,
            const char *sysid,
            const char *pubid,
            int has_internal_subset)
{
    ExpatParser * self = (ExpatParser *)userData;
    if (self->mHandler) {
        self->mHandler->startDoctype(doctypeName, sysid, pubid, has_internal_subset);
    }
}

void ExpatParser::endDoctypeWrapper(void *userData)
{
    ExpatParser * self = (ExpatParser *)userData;
    if (self->mHandler) {
        self->mHandler->endDoctype();
    }
}

int ExpatParser::parse(const char * data, int len, bool end)
{
    return XML_Parse(mExpat, data, len, end);
}

