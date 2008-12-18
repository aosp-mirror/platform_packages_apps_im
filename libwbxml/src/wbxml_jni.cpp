/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
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

#define LOG_TAG "wbxml"

#include <jni.h>
#include <cutils/jstring.h>

#include "wbxml_parser.h"
#include "imps_encoder.h"
#include "csp13_hash.h"

// FIXME: dalvik seems to have problem throwing non-system exceptions. Use IAE for now.
#if 1
#define PARSER_EXCEPTION_CLASS      "java/lang/IllegalArgumentException"
#define SERIALIZER_EXCEPTION_CLASS  "java/lang/IllegalArgumentException"
#else
#define PARSER_EXCEPTION_CLASS      "com/android/im/imps/ParserException"
#define SERIALIZER_EXCEPTION_CLASS  "com/android/im/imps/SerializerException"
#endif

class JniWbxmlContentHandler;
class JniWbxmlDataHandler;

struct WbxmlParsingContext
{
    /** The JNI enviromenet. */
    JNIEnv * env;
    /** The Java parser object. */
    jobject object;
    /** The wbxml parser. */
    WbxmlParser * parser;
    /** The content handler.*/
    JniWbxmlContentHandler * contentHandler;
};

struct WbxmlEncodingContext
{
    /** The JNI enviroment. */
    JNIEnv * env;
    /** The Java encoder object. */
    jobject object;
    /** The wbxml encoder. */
    WbxmlEncoder * encoder;
    /** The handler to get encoding result. */
    JniWbxmlDataHandler * handler;
};

static jmethodID parserCallbackStartElement;
static jmethodID parserCallbackEndElement;
static jmethodID parserCallbackCharacters;
static jmethodID encoderCallbackWbxmlData;
static jclass stringClass;

/**
 * Copies UTF-8 characters into the buffer.
 */
static jcharArray bytesToCharArray(JNIEnv * env, const char *data, int length, size_t * out_len)
{
    // TODO: store the buffer in ParsingContext to reuse it instead create a
    // new one everytime.
    jcharArray buffer = env->NewCharArray(length);
    if(buffer == NULL) {
        return NULL;
    }

    // Get a native reference to the buffer.
    jboolean copy;
    jchar *nativeBuffer = env->GetCharArrayElements(buffer, &copy);
    if (copy) {
        jclass clazz = env->FindClass("java/lang/AssertionError");
        env->ThrowNew(clazz, "Unexpected copy");
        return NULL;
    }

    // Decode UTF-8 characters into the buffer.
    strcpylen8to16((char16_t *) nativeBuffer, data, length, out_len);

    // Release our native reference.
    env->ReleaseCharArrayElements(buffer, nativeBuffer, JNI_ABORT);

    return buffer;
}

class JniWbxmlContentHandler : public DefaultWbxmlContentHandler
{
public:
    JniWbxmlContentHandler(WbxmlParsingContext * context):mContext(context)
    {
    }

    void reset(void)
    {
        mCurTag.clear();
    }

    void startElement(const char * name, const vector<Attribute> & atts)
    {
        JNIEnv * env = mContext->env;

        if(env->ExceptionCheck()) return;

        mCurTag = name;

        //TODO cache jstrings for later use?
        jstring localName = env->NewStringUTF(name);
        jobjectArray attrNames = NULL;
        jobjectArray attrValues = NULL;
        int count = atts.size();
        if(count > 0) {
            attrNames = env->NewObjectArray(count, stringClass, NULL);
            attrValues = env->NewObjectArray(count, stringClass, NULL);
        }

        for(int i = 0; i < count; i++) {
            jstring attrName = env->NewStringUTF(atts[i].name.c_str());
            jstring attrValue = env->NewStringUTF(atts[i].value.c_str());
            env->SetObjectArrayElement(attrNames, i, attrName);
            env->SetObjectArrayElement(attrValues, i, attrValue);
        }

        jobject javaParser = mContext->object;
        env->CallVoidMethod(javaParser, parserCallbackStartElement, localName, attrNames, attrValues);
        env->DeleteLocalRef(localName);
        for(int i = 0; i < count; i++) {
            env->DeleteLocalRef(env->GetObjectArrayElement(attrNames, i));
            env->DeleteLocalRef(env->GetObjectArrayElement(attrValues, i));
        }
    }

    void endElement(const char * name)
    {
        JNIEnv * env = mContext->env;

        if(env->ExceptionCheck()) return;

        mCurTag.clear();

        jstring localName = env->NewStringUTF(name);
        jobject javaParser = mContext->object;

        env->CallVoidMethod(javaParser, parserCallbackEndElement, localName);
        env->DeleteLocalRef(localName);
    }

    void characters(const char * data, int len)
    {
        JNIEnv * env = mContext->env;

        if(env->ExceptionCheck()) return;

        size_t utf16length;
        jcharArray buffer = bytesToCharArray(env, data, len, &utf16length);
        jobject javaParser = mContext->object;
        env->CallVoidMethod(javaParser, parserCallbackCharacters, buffer, utf16length);
        env->DeleteLocalRef(buffer);
    }

    void opaque(const char * data, int len)
    {
        JNIEnv * env = mContext->env;

        if(env->ExceptionCheck()) return;

        int val = 0;
        if (csp13IsIntegerTag(mCurTag.c_str())) {
            while (len--){
                val <<= 8;
                val |= (unsigned char)*data++;
            }

            char buf[20];
            sprintf(buf, "%d", val);
            size_t utf16length;
            jcharArray buffer = bytesToCharArray(env, buf, strlen(buf), &utf16length);
            jobject javaParser = mContext->object;
            env->CallVoidMethod(javaParser, parserCallbackCharacters, buffer, utf16length);
            env->DeleteLocalRef(buffer);
        }
        // FIXME: handle DateTime and binary data too
    }
private:
    WbxmlParsingContext * mContext;
    string mCurTag;
};

static void parserStaticInitialize(JNIEnv * env, jclass clazz)
{
    parserCallbackStartElement = env->GetMethodID((jclass) clazz, "startElement",
        "(Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V");

    parserCallbackEndElement = env->GetMethodID((jclass) clazz, "endElement",
        "(Ljava/lang/String;)V");

    parserCallbackCharacters = env->GetMethodID((jclass) clazz, "characters",
        "([CI)V");

    stringClass = env->FindClass("java/lang/String");
}

static jint parserCreate(JNIEnv * env, jobject thisObj, jstring encoding)
{
    // TODO: encoding
    WbxmlParsingContext * context = NULL;
    WbxmlParser * parser = NULL;
    JniWbxmlContentHandler * handler = NULL;

    context = new WbxmlParsingContext();
    if (!context) {
        goto error;
    }

    parser = new WbxmlParser(0);
    if (!parser) {
        goto error;
    }

    handler = new JniWbxmlContentHandler(context);
    if (!handler) {
        goto error;
    }

    parser->setContentHandler(handler);
    context->parser = parser;
    context->contentHandler = handler;
    context->env = env;
    context->object = thisObj;

    return (jint)context;

error:
    // C++ is OK with deleting NULL ptr.
    delete context;
    delete parser;
    return 0;
}

static void parserDelete(JNIEnv * env, jobject thisObj, jint nativeParser)
{
    WbxmlParsingContext * context = (WbxmlParsingContext *)nativeParser;
    delete context->parser;
    delete context->contentHandler;
    delete context;
}

static void parserReset(JNIEnv * env, jobject thisObj, jint nativeParser)
{
    WbxmlParsingContext * context = (WbxmlParsingContext *)nativeParser;
    WbxmlParser * parser = context->parser;
    JniWbxmlContentHandler * handler = context->contentHandler;

    parser->reset();
    handler->reset();
    parser->setContentHandler(handler);
}

static void parserParse(JNIEnv * env, jobject thisObj, jint nativeParser,
        jbyteArray buf, jint len, jboolean isEnd)
{
    WbxmlParsingContext * context = (WbxmlParsingContext *)nativeParser;
    WbxmlParser * parser = context->parser;
    context->env = env;

    jboolean copy;
    jbyte * bytes = env->GetByteArrayElements(buf, &copy);
    if(copy) {
        jclass clazz = env->FindClass("java/lang/AssertionError");
        env->ThrowNew(clazz, "Unexpected copy");
        return;
    }

    // make sure the context is updated because we may get called from
    // a different thread
    context->env = env;
    context->object = thisObj;

    if (parser->parse((char*)bytes, len, isEnd) != WBXML_STATUS_OK) {
        LOGW("WbxmlParser parse error %d\n", parser->getError());
        jclass clazz = env->FindClass(PARSER_EXCEPTION_CLASS);
        if (clazz == NULL) {
            LOGE("Can't find class " PARSER_EXCEPTION_CLASS "\n");
            return;
        }
        env->ThrowNew(clazz, NULL);
    }

    jthrowable exception = env->ExceptionOccurred();
    if (exception) {
        env->ExceptionClear();
        env->ReleaseByteArrayElements(buf, bytes, JNI_ABORT);
        env->Throw(exception);
    } else {
        env->ReleaseByteArrayElements(buf, bytes, JNI_ABORT);
    }
    context->env = NULL;
}

class JniWbxmlDataHandler : public WbxmlHandler
{
public:
    JniWbxmlDataHandler(WbxmlEncodingContext * context) : mContext(context)
    {
    }

    void reset()
    {
        // nothing to do
    }

    void wbxmlData(const char *data, uint32_t len)
    {
        JNIEnv * env = mContext->env;

        if (env->ExceptionCheck()) {
            return;
        }

        if (encoderCallbackWbxmlData == NULL) {
            jclass clazz = env->GetObjectClass(mContext->object);
            encoderCallbackWbxmlData = env->GetMethodID(clazz, "onWbxmlData", "([BI)V");
        }
        jbyteArray byteArray = env->NewByteArray(len);
        if (byteArray == NULL) {
            return;
        }

        env->SetByteArrayRegion(byteArray, 0, len, (const jbyte*)data);

        env->CallVoidMethod(mContext->object, encoderCallbackWbxmlData, byteArray, len);
    }

private:
    WbxmlEncodingContext * mContext;
};

static jint encoderCreate(JNIEnv * env, jobject thisObj, int publicId)
{
    WbxmlEncodingContext * context = NULL;
    WbxmlEncoder * encoder = NULL;
    JniWbxmlDataHandler * handler = NULL;

    context = new WbxmlEncodingContext();
    if (!context) {
        goto error;
    }

    encoder = new ImpsWbxmlEncoder(publicId);
    if (!encoder) {
        goto error;
    }

    handler = new JniWbxmlDataHandler(context);
    if (!handler) {
        goto error;
    }

    encoder->setWbxmlHandler(handler);

    context->encoder = encoder;
    context->handler = handler;
    context->env = env;
    context->object = thisObj;

    return (jint)context;

error:
    // C++ is OK with deleting NULL ptr.
    delete context;
    delete encoder;
    return 0;
}

static void encoderDelete(JNIEnv * env, jobject thisObj, jint nativeEncoder)
{
    WbxmlEncodingContext * context = (WbxmlEncodingContext *)nativeEncoder;
    delete context->encoder;
    delete context->handler;
    delete context;
}

static void encoderReset(JNIEnv * env, jobject thisObj, jint nativeEncoder)
{
    WbxmlEncodingContext * context = (WbxmlEncodingContext *)nativeEncoder;
    WbxmlEncoder * encoder = context->encoder;
    JniWbxmlDataHandler * handler = context->handler;

    encoder->reset();
    handler->reset();
    encoder->setWbxmlHandler(handler);
}

static void encoderStartElement(JNIEnv * env, jobject thisObj, jint nativeEncoder,
        jstring name, jobjectArray atts)
{
    WbxmlEncodingContext * context = (WbxmlEncodingContext *)nativeEncoder;
    WbxmlEncoder * encoder = context->encoder;

    const char ** c_atts = NULL;
    int count = atts ? env->GetArrayLength(atts) : 0;
    // TODO: handle out of memory.
    c_atts = new const char *[count + 1];
    if (atts != NULL) {
        for(int i = 0; i < count; i++) {
            jstring str = (jstring)env->GetObjectArrayElement(atts, i);
            c_atts[i] = env->GetStringUTFChars(str, NULL);
        }
    }
    c_atts[count] = NULL;
    const char * c_name = env->GetStringUTFChars(name, NULL);

    // make sure the context is updated because we may get called from
    // a different thread
    context->env = env;
    context->object = thisObj;

    EncoderError ret = encoder->startElement(c_name, c_atts);

    // encoder->startElement() may result in a call to WbxmlHandler.wbxmlData()
    // which in turns call back to the Java side. Therefore we might have
    // a pending Java exception here. Although current WbxmlEncoder always
    // call WbxmlHandler.wbxmlData() only after a successful complete parsing
    // but this may change later.

    if (env->ExceptionCheck() == JNI_FALSE && ret != NO_ERROR) {
        // only throw a new exception when there is no pending one
        LOGW("WbxmlEncoder startElement error:%d\n", ret);
        jclass clazz = env->FindClass(SERIALIZER_EXCEPTION_CLASS);
        if (clazz == NULL) {
            LOGE("Can't find class " SERIALIZER_EXCEPTION_CLASS);
            return;
        }
        env->ThrowNew(clazz, "Wbxml encode error");
    }

    jthrowable exception = env->ExceptionOccurred();
    if(exception) {
        env->ExceptionClear();
    }

    env->ReleaseStringUTFChars(name, c_name);
    for (int i = 0; i < count; i++) {
        jstring str = (jstring)env->GetObjectArrayElement(atts, i);
        env->ReleaseStringUTFChars(str, c_atts[i]);
    }
    delete[] c_atts;
    if (exception) {
        env->Throw(exception);
    }
}

static void encoderCharacters(JNIEnv * env, jobject thisObj, jint nativeEncoder, jstring chars)
{
    WbxmlEncodingContext * context = (WbxmlEncodingContext *)nativeEncoder;
    WbxmlEncoder * encoder = context->encoder;

    const char * c_chars = env->GetStringUTFChars(chars, NULL);

    // make sure the context is updated because we may get called from
    // a different thread
    context->env = env;
    context->object = thisObj;

    EncoderError ret = encoder->characters(c_chars, env->GetStringUTFLength(chars));

    // encoder->characters() may result in a call to WbxmlHandler.wbxmlData()
    // which in turns call back to the Java side. Therefore we might have
    // a pending Java exception here. Although current WbxmlEncoder always
    // call WbxmlHandler.wbxmlData() only after a successful complete parsing
    // but this may change later.

    if (env->ExceptionCheck() == JNI_FALSE && ret != NO_ERROR) {
        // only throw a new exception when there is no pending one
        LOGE("WbxmlEncoder characters error:%d\n", ret);
        jclass clazz = env->FindClass(SERIALIZER_EXCEPTION_CLASS);
        if (clazz == NULL) {
            LOGE("Can't find class " SERIALIZER_EXCEPTION_CLASS);
            return;
        }
        env->ThrowNew(clazz, "Wbxml encode error");
    }
    jthrowable exception = env->ExceptionOccurred();
    if (exception) {
        env->ExceptionClear();
        env->ReleaseStringUTFChars(chars, c_chars);
        env->Throw(exception);
    } else {
        env->ReleaseStringUTFChars(chars, c_chars);
    }
}

static void encoderEndElement(JNIEnv * env, jobject thisObj, jint nativeEncoder)
{
    WbxmlEncodingContext * context = (WbxmlEncodingContext *)nativeEncoder;
    WbxmlEncoder * encoder = context->encoder;

    // make sure the context is updated because we may get called from
    // a different thread
    context->env = env;
    context->object = thisObj;

    EncoderError ret = encoder->endElement();

    // encoder->endElement() may result in a call to WbxmlHandler.wbxmlData()
    // which in turns call back to the Java side. Therefore we might have
    // a pending Java exception here.
    if (env->ExceptionCheck() == JNI_FALSE && ret != NO_ERROR) {
        // only throw a new exception when there is no pending one
        LOGE("WbxmlEncoder endElement error:%d\n", ret);
        jclass clazz = env->FindClass(SERIALIZER_EXCEPTION_CLASS);
        if (clazz == NULL) {
            LOGE("Can't find class " SERIALIZER_EXCEPTION_CLASS);
            return;
        }
        env->ThrowNew(clazz, "Wbxml encode error");
    }
}

/**
 * Table of methods associated with WbxmlParser.
 */
static JNINativeMethod parserMethods[] = {
    /* name, signature, funcPtr */
    { "nativeStaticInitialize", "()V",                      (void*) parserStaticInitialize },
    { "nativeCreate",           "(Ljava/lang/String;)I",    (void*) parserCreate },
    { "nativeRelease",          "(I)V",                     (void*) parserDelete },
    { "nativeReset",            "(I)V",                     (void*) parserReset },
    { "nativeParse",            "(I[BIZ)V",                 (void*) parserParse },
};

/**
 * Table of methods associated with WbxmlSerializer.
 */
static JNINativeMethod encoderMethods[] = {
    /* name, signature, funcPtr */
    { "nativeCreate",       "(I)I",                                     (void*) encoderCreate },
    { "nativeRelease",      "(I)V",                                     (void*) encoderDelete },
    { "nativeReset",        "(I)V",                                     (void*) encoderReset },
    { "nativeStartElement", "(ILjava/lang/String;[Ljava/lang/String;)V", (void*) encoderStartElement },
    { "nativeCharacters",   "(ILjava/lang/String;)V",                   (void*) encoderCharacters },
    { "nativeEndElement",   "(I)V",                                     (void*) encoderEndElement },
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* methods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'\n", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, methods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'\n", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods.
 */
static int registerNatives(JNIEnv* env)
{
    if (!registerNativeMethods(env, "com/android/im/imps/WbxmlParser",
            parserMethods, sizeof(parserMethods) / sizeof(parserMethods[0])))
        return JNI_FALSE;

    if (!registerNativeMethods(env, "com/android/im/imps/WbxmlSerializer",
            encoderMethods, sizeof(encoderMethods) / sizeof(encoderMethods[0])))
        return JNI_FALSE;

    return JNI_TRUE;
}

/*
 * Set some test stuff up.
 *
 * Returns the JNI version on success, -1 on failure.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("ERROR: GetEnv failed\n");
        goto bail;
    }
    assert(env != NULL);

    // LOGI("In WbxmlParser JNI_OnLoad\n");

    if (!registerNatives(env)) {
        LOGE("ERROR: WbxmlParser native registration failed\n");
        goto bail;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

bail:
    return result;
}

