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

#ifndef WBXML_TABLEDEF_H
#define WBXML_TABLEDEF_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct tagTokenData
{
    int token;
    const char * tagName;
} TokenData;

typedef struct tagAttrData
{
    int token;
    const char * attrName;
    const char * attrValuePrefix;
} AttrData;

typedef struct tagTagCodePage
{
    int page;
    int numTokens;
    const TokenData * tags;
} TagCodePage;

typedef struct tagAttrCodePage
{
    int page;
    int numTokens;
    const AttrData * attrs;
} AttrCodePage;

#define PAGE_DATA(page, dataArray)  \
    { page, sizeof(dataArray) / sizeof(dataArray[0]), dataArray }

#ifdef __cplusplus
}
#endif

#endif

