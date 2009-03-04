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

#ifndef SYNCML_DATA_H
#define SYNCML_DATA_H

#include "wbxml_tabledef.h"

#ifdef __cplusplus
extern "C" {
#endif

/* SyncML code page (0x00) */
static const TokenData syncmlTagPage0[] = {
    {0x05, "Add"},
    {0x06, "Alert"},
    {0x07, "Archive"},
    {0x08, "Atomic"},
    {0x09, "Chal"},
    {0x0A, "Cmd"},
    {0x0B, "CmdID"},
    {0x0C, "CmdRef"},
    {0x0D, "Copy"},
    {0x0E, "Cred"},
    {0x0F, "Data"},
    {0x10, "Delete"},
    {0x11, "Exec"},
    {0x12, "Final"},
    {0x13, "Get"},
    {0x14, "Item"},
    {0x15, "Lang"},
    {0x16, "LocName"},
    {0x17, "LocURI"},
    {0x18, "Map"},
    {0x19, "MapItem"},
    {0x1A, "Meta"},
    {0x1B, "MsgID"},
    {0x1C, "MsgRef"},
    {0x1D, "NoResp"},
    {0x1E, "NoResults"},
    {0x1F, "Put"},
    {0x20, "Replace"},
    {0x21, "RespURI"},
    {0x22, "Results"},
    {0x23, "Search"},
    {0x24, "Sequence"},
    {0x25, "SessionID"},
    {0x26, "SftDel"},
    {0x27, "Source"},
    {0x28, "SourceRef"},
    {0x29, "Status"},
    {0x2A, "Sync"},
    {0x2B, "SyncBody"},
    {0x2C, "SyncHdr"},
    {0x2D, "SyncML"},
    {0x2E, "Target"},
    {0x2F, "TargetRef"},
    {0x31, "VerDTD"},
    {0x32, "VerProto"},
    {0x33, "NumberOfChanges"},
    {0x34, "MoreData"},
    {0x35, "Field"},
    {0x36, "Filter"},
    {0x37, "Record"},
    {0x38, "FilterType"},
    {0x39, "SourceParent"},
    {0x3A, "TargetParent"},
    {0x3B, "Move"},
    {0x3C, "Correlator"},
};

/* MetInf code page (0x01) */
static const TokenData syncmlTagPage1[] = {
    {0x05, "Anchor"},
    {0x06, "EMI"},
    {0x07, "Format"},
    {0x08, "FreeID"},
    {0x09, "FreeMem"},
    {0x0A, "Last"},
    {0x0B, "Mark"},
    {0x0C, "MaxMsgSize"},
    {0x0D, "Mem"},
    {0x0E, "MetInf"},
    {0x0F, "Next"},
    {0x10, "NextNonce"},
    {0x11, "SharedMem"},
    {0x12, "Size"},
    {0x13, "Type"},
    {0x14, "Version"},
    {0x15, "MaxObjSize"},
    {0x16, "FieldLevel"},
};

/* DevInf code page (0x00) */
static const TokenData syncmlDevInfTagPage[] = {
    {0x05, "CTCap"},
    {0x06, "CTType"},
    {0x07, "DataStore"},
    {0x08, "DataType"},
    {0x09, "DevID"},
    {0x0A, "DevInf"},
    {0x0B, "DevTyp"},
    {0x0C, "DisplayName"},
    {0x0D, "DSMem"},
    {0x0E, "Ext"},
    {0x0F, "FwV"},
    {0x10, "HwV"},
    {0x11, "Man"},
    {0x12, "MaxGUIDSize"},
    {0x13, "MaxID"},
    {0x14, "MaxMem"},
    {0x15, "Mod"},
    {0x16, "OEM"},
    {0x17, "ParamName"},
    {0x18, "PropName"},
    {0x19, "Rx"},
    {0x1A, "Rx-Pref"},
    {0x1B, "SharedMem"},
    {0x1C, "MaxSize"},
    {0x1D, "SourceRef"},
    {0x1E, "SwV"},
    {0x1F, "SyncCap"},
    {0x20, "SyncType"},
    {0x21, "Tx"},
    {0x22, "Tx-Pref"},
    {0x23, "ValEnum"},
    {0x24, "VerCT"},
    {0x25, "VerDTD"},
    {0x26, "Xnam"},
    {0x27, "Xval"},
    {0x28, "UTC"},
    {0x29, "SupportNumberOfChanges"},
    {0x2A, "SupportLargeObjs"},
    {0x2B, "Property"},
    {0x2C, "PropParam"},
    {0x2D, "MaxOccur"},
    {0x2E, "NoTruncate"},
    {0x30, "Filter-Rx"},
    {0x31, "FilterCap"},
    {0x32, "FilterKeyword"},
    {0x33, "FieldLevel"},
    {0x34, "SupportHierarchicalSync"},
};

static const TagCodePage syncmlTagPages[] = {
    PAGE_DATA(0, syncmlTagPage0),
    PAGE_DATA(1, syncmlTagPage1),
};

static const TagCodePage syncmlDevInfTagPages[] = {
    PAGE_DATA(0, syncmlDevInfTagPage),
};

#ifdef __cplusplus
}
#endif

#endif
