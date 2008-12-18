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

#ifndef WBXML_CONST_H
#define WBXML_CONST_H

enum WbxmlStatus
{
    WBXML_STATUS_ERROR = 0,
    WBXML_STATUS_OK = 1,
};

enum IanaCharset
{
    CHARSET_UNKNOWN = 0,
    CHARSET_UTF8 = 0x6a,
};

enum PublicId
{
    PUBLICID_IMPS_1_3 = 0x12,
    PUBLICID_IMPS_1_2 = 0x11,
    PUBLICID_IMPS_1_1 = 0x10,
    PUBLICID_SYNCML_1_0 = 0xFD1,
    PUBLICID_SYNCML_1_1 = 0xFD3,
    PUBLICID_SYNCML_1_2 = 0x1201,
    PUBLICID_SYNCML_METINF_1_2 = 0x1202,
    PUBLICID_SYNCML_DEVINF_1_2 = 0x1203,
};

enum WbxmlToken {
    TOKEN_SWITCH_PAGE   = 0,
    TOKEN_END           = 1,
    TOKEN_ENTITY        = 2,
    TOKEN_STR_I         = 3,
    TOKEN_LITERAL       = 4,
    TOKEN_EXT_I_0       = 0x40,
    TOKEN_EXT_I_1       = 0x41,
    TOKEN_EXT_I_2       = 0x42,
    TOKEN_PI            = 0x43,
    TOKEN_LITERAL_C     = 0x44,
    TOKEN_EXT_T_0       = 0x80,
    TOKEN_EXT_T_1       = 0x81,
    TOKEN_EXT_T_2       = 0x82,
    TOKEN_STR_T         = 0x83,
    TOKEN_LITERAL_A     = 0x84,
    TOKEN_EXT_0         = 0xC0,
    TOKEN_EXT_1         = 0xC1,
    TOKEN_EXT_2         = 0xC2,
    TOKEN_OPAQUE        = 0xC3,
    TOKEN_LITERAL_AC    = 0xC4,
};

#endif

