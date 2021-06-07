#include <jni.h>
#include <string>
#include "librtmp/rtmp.h"
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"lyw",__VA_ARGS__)


//缓存sps、pps（基础配置信息，宽高等信息），发送I帧都带上（没有sps、pps的I帧数据，不能显示画面）
//非关键帧: 0x27
//关键帧：  0x17  0x01
//sps、pps:0x17  0x00 0x00 0x00 0x00 0x01  3个字节  0xff 0xe1






//相当于javabeen
typedef struct {
    RTMP *rtmp;

    //缓存sps、pps
    int16_t sps_len;
    int8_t *sps;

    int16_t pps_len;
    int8_t *pps;

} Live;

Live *live = NULL;

/**
 * 创建sps、pps包
 */
RTMPPacket *createVideoPackage(Live *live) {
    int body_size = 16 + live->sps_len + live->pps_len;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));

    //实例化数据包
    RTMPPacket_Alloc(packet, body_size);
    int i = 0;
    packet->m_body[i++] = 0x17;
    packet->m_body[i++] = 0x00;

    packet->m_body[i++] = 0x00;

    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;

    packet->m_body[i++] = 0x01;

    packet->m_body[i++] = live->sps[1];
    packet->m_body[i++] = live->sps[2];
    packet->m_body[i++] = live->sps[3];


    packet->m_body[i++] = 0xFF;
    packet->m_body[i++] = 0xE1;


    packet->m_body[i++] = (live->sps_len >> 8) & 0xFF;

    packet->m_body[i++] = live->sps_len & 0xff;

    //拷贝sps的内容
    memcpy(&packet->m_body[i], live->sps, live->sps_len);
    i += live->sps_len;
    packet->m_body[i++] = 0x01;

    packet->m_body[i++] = (live->pps_len >> 8) & 0xff;
    packet->m_body[i++] = live->pps_len & 0xff;
    //拷贝pps的内容
    memcpy(&packet->m_body[i], live->pps, live->pps_len);

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;

    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}


/**
 * 创建关键帧、非关键帧包
 */
RTMPPacket *createVideoPackage(int8_t *buf, int len, long tms, Live *live) {
    buf += 4;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    int body_size = len + 9;
    RTMPPacket_Alloc(packet, body_size);

    if (buf[0] == 0x65) {
        packet->m_body[0] = 0x17;
        LOGI("rtmp_发送关键帧 data");
    } else {
        packet->m_body[0] = 0x27;
        LOGI("rtmp_发送非关键帧 data");
    }


    //固定大小
    packet->m_body[1] = 0x01;
    packet->m_body[2] = 0x00;
    packet->m_body[3] = 0x00;
    packet->m_body[4] = 0x00;


    //长度
    packet->m_body[5] = (len >> 24) & 0xff;
    packet->m_body[6] = (len >> 16) & 0xff;
    packet->m_body[7] = (len >> 8) & 0xff;
    packet->m_body[8] = (len) & 0xff;

    //数据
    memcpy(&packet->m_body[9], buf, len);
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;

    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = tms;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}


/**
 * 发送关键帧
 * @param pPacket
 * @return
 */
int sendPacket(RTMPPacket *pPacket) {
    int r = RTMP_SendPacket(live->rtmp, pPacket, 1);
    RTMPPacket_Free(pPacket);
    free(pPacket);
    return r;
}


//    分隔符                     sps                                 pps
//00 00 00 01 67     64001FACB405A0500290506060606DBA135    00 00 00 00 01 68 FF06F2C0
void prepareVideo(int8_t *data, int len, Live *live) {
    for (int i = 0; i < len; i++) {
        //防止越界
        if (i + 4 < len) {
            if (data[i] == 0x00
                && data[i + 1] == 0x00
                && data[i + 2] == 0x00
                && data[i + 3] == 0x01) {
                if(data[i + 4] == 0x68){
                    live->sps_len = i - 4;
                    //new 一个数组
                    live->sps = static_cast<int8_t *>(malloc(live->sps_len));
                    //sps解析出来了
                    memcpy(live->sps, data + 4, live->sps_len);

                    //解析pps
                    live->pps_len = len - (4 + live->sps_len) - 4;
                    //实例pps的数组
                    live->pps = static_cast<int8_t *>(malloc(live->pps_len));

                    //rtmp协议

                    memcpy(live->pps, data + 4 + live->sps_len + 4, live->pps_len);
                    LOGI("rtmp_sps:%d  pps:%d", live->sps_len, live->pps_len);
                    break;
                }
            }
        }
    }
}

int sendVideo(int8_t *buf, int len, long tms) {
    int ret = 0;

    //判断帧类型
    int type = buf[4] & 0x1F;
    //sps、pps
    if (buf[4] == 0x67) {
        if (live && (!live->pps || !live->sps)) {
            LOGI("rtmp_prepareVideo");

            //缓存，没有推流
            prepareVideo(buf, len, live);
        }
        return ret;

    }
    //I帧  （关键帧）
    if (buf[4] == 0x65) {
        LOGI("rtmp_发送sps、pps");
        //组装包，要带上sps、pps
        //先发sps、pps
        RTMPPacket *packet = createVideoPackage(live);
        sendPacket(packet);

        //再发关键帧
    }

    //i 帧 还是非i帧
    RTMPPacket *packet2 = createVideoPackage(buf, len, tms, live);
    ret = sendPacket(packet2);
    return ret;
}


extern "C" {
JNIEXPORT jstring JNICALL
Java_com_lyw_avmodule_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jboolean JNICALL
Java_com_lyw_avmodule_ScreenLive_sendData(JNIEnv *env, jobject thiz, jbyteArray data_, jint len,
                                          jlong tms) {
    //推流
    int ret;
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    ret = sendVideo(data, len, tms);

    env->ReleaseByteArrayElements(data_, data, 0);

    return ret;
}

JNIEXPORT jboolean JNICALL
Java_com_lyw_avmodule_ScreenLive_connect(JNIEnv *env, jobject thiz, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);
    int ret;
    //轮循链接
    do {
        //相当于java的 live = new Live()
        live = (Live *) malloc(sizeof(live));
        //内存初始化
        memset(live, 0, sizeof(live));
        live->rtmp = RTMP_Alloc();

        //librtmp初始化
        RTMP_Init(live->rtmp);
        //设置超时时间
        live->rtmp->Link.timeout = 10;

        LOGI("rtmp_connect %s", url);
        if (!(ret = RTMP_SetupURL(live->rtmp, (char *) url))) break;
        RTMP_EnableWrite(live->rtmp);

        LOGI("rtmp_connect");
        if (!(ret = RTMP_Connect(live->rtmp, 0))) break;

        LOGI("rtmp_connectStream");
        if (!(ret = RTMP_ConnectStream(live->rtmp, 0))) break;
        LOGI("rtmp_connect_success");
    } while (0);

    if (!ret && live) {
        free(live);
        live = nullptr;
    }
    env->ReleaseStringUTFChars(url_, url);
    return ret;
}
}