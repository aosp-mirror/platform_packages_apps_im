LOCAL_PATH := $(call my-dir)

# Tags shared by all non-test wbxml modules
wbxml_module_tags := eng user

# wbxml core library: libwbxml.so
# ---------------------------------------
include $(CLEAR_VARS)

LOCAL_SRC_FILES :=          \
    src/wbxml_parser.cpp    \
    src/imps_encoder.cpp    \
    src/csp13tags_hash.c    \
    src/csp13values_hash.c  \
    src/csp13inttags_hash.c

LOCAL_C_INCLUDES :=         \
    $(LOCAL_PATH)/include

LOCAL_CFLAGS += -DPLATFORM_ANDROID

# SyncML support
LOCAL_CFLAGS += -DSUPPORT_SYNCML

LOCAL_SHARED_LIBRARIES +=   \
    libutils

LOCAL_MODULE_TAGS := $(wbxml_module_tags)

LOCAL_MODULE := libwbxml

include $(BUILD_SHARED_LIBRARY)

# xml2wbxml library: libxml2wbxml.so
# ---------------------------------------
include $(CLEAR_VARS)

# This requires expat

LOCAL_SRC_FILES :=          \
    src/xml2wbxml.cpp       \
    src/expat_parser.cpp

LOCAL_C_INCLUDES :=         \
    $(LOCAL_PATH)/include   \
    external/expat/lib

LOCAL_CFLAGS += -DPLATFORM_ANDROID

LOCAL_SHARED_LIBRARIES +=   \
    libutils                \
    libwbxml                \
    libexpat

LOCAL_MODULE_TAGS := $(wbxml_module_tags)

LOCAL_MODULE := libxml2wbxml

include $(BUILD_SHARED_LIBRARY)

# wbxml unit test: wbxml_test
# ---------------------------------------
include $(CLEAR_VARS)

LOCAL_SRC_FILES :=              \
    test/alltests.c             \
    test/imps_encoder_test.cpp  \
    test/imps_parser_test.cpp

LOCAL_C_INCLUDES :=             \
    $(LOCAL_PATH)/include       \
    external/embunit/inc         \
    external/expat/lib

LOCAL_CFLAGS += -DPLATFORM_ANDROID

# SyncML support
LOCAL_CFLAGS += -DSUPPORT_SYNCML
LOCAL_SRC_FILES += test/syncml_parser_test.cpp

LOCAL_SHARED_LIBRARIES +=   \
    libwbxml                \
    libxml2wbxml            \
    libembunit              \
    libutils                \
    libexpat

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := wbxmltest

include $(BUILD_EXECUTABLE)

# wbxml JNI: libwbxml_jni.so
# ------------------------------------------------
include $(CLEAR_VARS)

LOCAL_SRC_FILES :=      \
    src/wbxml_jni.cpp

LOCAL_C_INCLUDES :=         \
    $(LOCAL_PATH)/include   \
    external/expat/lib \
    $(JNI_H_INCLUDE)

LOCAL_CFLAGS += -DPLATFORM_ANDROID

LOCAL_SHARED_LIBRARIES +=   \
    libwbxml                \
    libutils                \
    libcutils                \
    libexpat

LOCAL_MODULE_TAGS := $(wbxml_module_tags)

LOCAL_MODULE := libwbxml_jni

include $(BUILD_SHARED_LIBRARY)

