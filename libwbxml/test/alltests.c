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

#include <embUnit.h>

extern TestRef ImpsParserTest_tests(void);
extern TestRef ImpsEncoderTest_tests(void);
#ifdef SUPPORT_SYNCML
extern TestRef SyncmlParserTest_tests(void);
#endif

int main (int argc, const char* argv[])
{
    TestRunner_start();
        TestRunner_runTest(ImpsParserTest_tests());
        TestRunner_runTest(ImpsEncoderTest_tests());
#ifdef SUPPORT_SYNCML
        TestRunner_runTest(SyncmlParserTest_tests());
#endif
    TestRunner_end();
    return 0;
}
