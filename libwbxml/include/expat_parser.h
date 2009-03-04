/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef EXPAT_PARSER_H
#define EXPAT_PARSER_H

#include <expat.h>
#include "xml_handler.h"

class ExpatParser
{
public:
    ExpatParser();
    virtual ~ExpatParser();

    void setContentHandler(XmlContentHandler *handler)
    {
        mHandler = handler;
    }
    int parse(const char * data, int len, bool end);

private:
    XML_Parser mExpat;
    XmlContentHandler * mHandler;

    static void startElementWrapper(void *userData, const char *name, const char **atts);
    static void endElementWrapper(void *userData, const char *name);
    static void charactersWrapper(void *userData, const char *data, int len);
    static void startDoctypeWrapper(void *userData,
            const char *doctypeName,
            const char *sysid,
            const char *pubid,
            int has_internal_subset);
    static void endDoctypeWrapper(void *userData);
};

#endif


