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

#ifndef CSP13_HASH_H
#define CSP13_HASH_H

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @param str Tag name
 * @return integer value with code page in bit 8-15 and tag token in bit 0-7; -1 if not a CSP 1.3 tag
 */
int csp13TagNameToKey(const char * str);

/**
 * @param str Tag name
 * @return 1 if <code>str</code> is a CSP 1.3 tag of Integer type; 0 if not
 */
int csp13IsIntegerTag(const char * str);

/**
 * @param str Value token string
 * @param len Length of the value token
 * @return token value ; -1 if not a valid CSP 1.3 value token
 */
int csp13ValueTokenToKey(const char * str, int len);

#ifdef __cplusplus
}
#endif

#endif

