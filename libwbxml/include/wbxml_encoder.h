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

#ifndef WBXML_ENCODER_H
#define WBXML_ENCODER_H

#include <stdint.h>
#include "wbxml_const.h"

class WbxmlHandler
{
public:
    virtual ~WbxmlHandler() {}
    virtual void wbxmlData(const char *data, uint32_t len) = 0;
};

enum EncoderError {
    NO_ERROR = 0,
    ERROR_NO_PUBLIC_ID = 1,
    ERROR_UNSUPPORTED_DOCTYPE,
    ERROR_UNSUPPORTED_TAG,
    ERROR_UNSUPPORTED_ATTR,
    ERROR_INVALID_DATA,
    ERROR_INVALID_INTEGER_VALUE,
    ERROR_INVALID_DATETIME_VALUE,
    ERROR_INVALID_END_ELEMENT,
};

class WbxmlEncoder
{
public:
    virtual ~WbxmlEncoder() {}

    void setWbxmlHandler(WbxmlHandler * handler)
    {
        mHandler = handler;
    }

    virtual EncoderError startElement(const char *name, const char **atts) = 0;
    virtual EncoderError characters(const char *chars, int len) = 0;
    virtual EncoderError endElement() = 0;

    /**
     * Reset the encoder so that it may be used again. The WbxmlHandler is
     * NOT cleared by reset().
     */
    virtual void reset() = 0;

protected:
    WbxmlHandler * mHandler;
};

#endif

