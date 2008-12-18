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

#ifndef WBXML_STL_H
#define WBXML_STL_H

#ifdef PLATFORM_ANDROID

#include <utils/String8.h>
#include <utils/Vector.h>
using android::String8;
using android::Vector;

class string: public String8
{
public:
    string() {}
    string(const string& o) :
        String8(o)
    {
    }
    string(const char* o) :
        String8(o)
    {
    }
    string(const char* o, size_t len) :
        String8(o, len)
    {
    }
    string & operator=(const char* other)
    {
        setTo(other);
        return *this;
    }
    string & assign(const char* other, int len)
    {
        setTo(other, len);
        return *this;
    }
    bool empty(void) const
    {
        return size() == 0;
    }
    const char *c_str(void) const
    {
        return String8::string();
    }
    string & operator+=(const string & o)
    {
        append(o);
        return *this;
    }
    string & operator+=(const char * other)
    {
        append(other);
        return *this;
    }
    string & operator+=(char ch)
    {
        char c[2] = {ch, 0};    // temporary workaround for String8.append(str, len) bug
        append(c, 1);
        return *this;
    }
    void clear(void)
    {
        setTo("");
    }
};

template <class T>
class vector: public Vector<T>
{
public:
    T & back(void)
    {
        return Vector<T>::editTop();
    }
    const T & back(void) const
    {
        return Vector<T>::top();
    }
    void push_back(const T& val)
    {
        Vector<T>::push(val);
    }
    void pop_back(void)
    {
        Vector<T>::pop();
    }
    bool empty(void) const
    {
        return Vector<T>::isEmpty();
    }
};

#else

#include <string>
#include <vector>
using std::string;
using std::vector;

#endif

#endif

