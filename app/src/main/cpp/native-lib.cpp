#include <jni.h>
#include <string>
#include "librtmp/rtmp.h"
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"lyw",__VA_ARGS__)

//相当于javabeen
typedef struct {
    RTMP *rtmp;
} Live;

Live *live = NULL;


extern "C" JNIEXPORT jstring JNICALL
Java_com_lyw_avmodule_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lyw_avmodule_ScreenLive_sendData(JNIEnv *env, jobject thiz, jbyteArray data, jint len,
                                          jlong tms) {
    //推流








}



extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lyw_avmodule_ScreenLive_connect(JNIEnv *env, jobject thiz, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);

    //相当于java的 live = new Live()
    live = (Live *) malloc(sizeof(live));
    //内存初始化
    memset(live, 0, sizeof(live));
    live->rtmp = RTMP_Alloc();

    //librtmp初始化
    RTMP_Init(live->rtmp);
    //设置超时时间
    live->rtmp->Link.timeout = 10;
    //设置URL
    RTMP_SetupURL(live->rtmp, (char *) url);
    RTMP_EnableWrite(live->rtmp);
    LOGI("rtmp_connect");
    //开始链接
    RTMP_Connect(Live.rtmp, 0);
    env->ReleaseStringUTFChars(url_, url);
}