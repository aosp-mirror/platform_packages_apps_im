LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-subdir-java-files) \
			src/com/android/im/IChatSession.aidl \
			src/com/android/im/IChatSessionListener.aidl \
			src/com/android/im/IInvitationListener.aidl \
			src/com/android/im/IChatSessionManager.aidl \
			src/com/android/im/IConnectionListener.aidl \
			src/com/android/im/IContactList.aidl \
			src/com/android/im/IContactListListener.aidl \
			src/com/android/im/IContactListManager.aidl \
			src/com/android/im/IImConnection.aidl \
			src/com/android/im/IChatListener.aidl \
			src/com/android/im/IRemoteImService.aidl \
			src/com/android/im/ISubscriptionListener.aidl \
			src/com/android/im/IConnectionCreationListener.aidl \

LOCAL_STATIC_JAVA_LIBRARIES := android-common

LOCAL_PACKAGE_NAME := IM

LOCAL_JNI_SHARED_LIBRARIES := libwbxml_jni

#Disable building the APK; we are checking in the pre-built version which
#contains the credential plug-in instead. Note the libwbxml_jni has to be
#enabled because so won't be extracted from the system APK
#include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
