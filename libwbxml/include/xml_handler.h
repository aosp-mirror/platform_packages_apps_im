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

#ifndef XML_HANDLER_H
#define XML_HANDLER_H

class XmlContentHandler
{
public:
    virtual ~XmlContentHandler() {}

    virtual void startElement(const char *name, const char **atts) = 0;
    virtual void endElement(const char *name) = 0;
    virtual void characters(const char *data, int len) = 0;
    virtual void startDoctype(const char *doctypeName, const char *sysid, const char *pubid,
            int has_internal_subset) = 0;
    virtual void endDoctype(void) = 0;
};

#endif

