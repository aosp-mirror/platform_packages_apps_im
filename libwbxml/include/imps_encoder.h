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

#ifndef IMPS_ENCODER_H
#define IMPS_ENCODER_H

#include "wbxml_encoder.h"
#include "wbxml_stl.h"

class ImpsWbxmlEncoder : public WbxmlEncoder
{
public:
    ImpsWbxmlEncoder(int publicid) :
        mPublicId(publicid)
    {
        reset();
    }

    /**
     * Reset the encoder so it can be used for encoding next document.
     */
    void reset();

    EncoderError startElement(const char *name, const char **atts);

    EncoderError characters(const char *chars, int len);

    /**
     * Send OPAQUE data to the encoder. Should only be used for ContentData.
     * The application should choose to encode ContentData either by characters()
     * when the ContentType is text (e.g. text/plain) or by opaque() if the
     * ContentType is of some binary types.
     * WBXML Integer and DateTime are automatically converted to opaque data
     * in characters() so there's no need for the application to use opaque()
     * for these types.
     */
    EncoderError opaque(const char *chars, int len);

    EncoderError endElement();

private:
    int mPublicId;
    int mTagCodePage;
    string mResult;
    string mCurrElement;
    int mDepth;

    EncoderError encodeInteger(const char *chars, int len);
    EncoderError encodeDatetime(const char *chars, int len);
    EncoderError encodeString(const char *chars, int len);
    EncoderError encodeAttrib(const char *name, const char *value);
    void encodeInlinedStr(const char *s, int len);
    void encodeMbuint(uint32_t i);

    void appendResult(int ch)
    {
        mResult += (char)ch;
    }

    void appendResult(const char *s, int len)
    {
        mResult.append(s, len);
    }
};

#endif

