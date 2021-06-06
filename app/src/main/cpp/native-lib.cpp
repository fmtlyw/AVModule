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


extern "C" {
JNIEXPORT jstring JNICALL
Java_com_lyw_avmodule_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

//JNIEXPORT jboolean JNICALL
//Java_com_lyw_avmodule_ScreenLive_sendData(JNIEnv *env, jobject thiz, jbyteArray data, jint len,
//                                          jlong tms) {
//    //推流
//
//
//
//
//
//
//
//
//}

JNIEXPORT jboolean JNICALL
Java_com_lyw_avmodule_ScreenLive_connect(JNIEnv *env, jobject thiz, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);
    int ret;
    //轮循链接
    do{
        //相当于java的 live = new Live()
        live = (Live *) malloc(sizeof(live));
        //内存初始化
        memset(live, 0, sizeof(live));
        live->rtmp = RTMP_Alloc();

        //librtmp初始化
        RTMP_Init(live->rtmp);
        //设置超时时间
        live->rtmp->Link.timeout = 10;

        LOGI("connect %s",url);
        if(!(ret = RTMP_SetupURL(live->rtmp,(char *) url))) break;
        RTMP_EnableWrite(live->rtmp);

        LOGI("rtmp_connect");
        if(!(ret = RTMP_Connect(live->rtmp,0))) break;

        LOGI("rtmp_connectStream");
        if(!(ret = RTMP_ConnectStream(live->rtmp,0))) break;
        LOGI("connect_success");
    }while (0);

    if(!ret && live){
        free(live);
        live = nullptr;
    }
    env->ReleaseStringUTFChars(url_, url);
    return ret;
}
}