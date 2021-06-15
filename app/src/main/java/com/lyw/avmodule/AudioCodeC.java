package com.lyw.avmodule;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 功能描述:音频采集并编码
 * Created on 2021/6/1.
 *
 * @author lyw
 */
public class AudioCodeC extends Thread{
    private MediaCodec mMediaCodec;
    private AudioRecord mAudioRecord;

    private int bufferSize;

    boolean isRecording;

    private long startTime;

    private ScreenLive mScreenLive;

    public  AudioCodeC(ScreenLive mScreenLive){
        this.mScreenLive = mScreenLive;
    }


    public void startLive() {
        try {
            MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 500_000);


            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);



            //44100表示采样频率
            bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //开始run方法
        start();
    }

    @Override
    public void run() {
        mMediaCodec.start();

        isRecording = true;
        mAudioRecord.startRecording();

        RTMPPackage rtmpPackage = new RTMPPackage();
        byte[] audioDecoderSpecificInfo = {0x12,0x08};
        rtmpPackage.setBuffer(audioDecoderSpecificInfo);
        rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_AUDIO_HEAD);
        mScreenLive.addPackage(rtmpPackage);


        byte[] buffer = new byte[bufferSize];
        MediaCodec.BufferInfo bufferInfo =  new MediaCodec.BufferInfo();

        while (isRecording){
            int len = mAudioRecord.read(buffer,0,1024);
            if(len <= 0){
                continue;
            }

            //得到有效输入缓冲器   把audio数据写入mediacode
            int index = mMediaCodec.dequeueInputBuffer(0);
            if(index >= 0){
                ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(index);
                inputBuffer.clear();////避免缓冲队列有冗余数据，先clear一下

                inputBuffer.put(buffer,0,len);
                //填充数据再加入队列
                mMediaCodec.queueInputBuffer(index,0,len,System.nanoTime()/1000,0);
            }

            //将数据从MediaCodec取出并封包成rtmp包
            index = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (index >= 0 && isRecording) {
                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(index);
                byte[] outData = new byte[bufferInfo.size];

                if(startTime == 0){
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }
                outputBuffer.get(outData);

//                FileUtils.writeBytes(outData,"music.pcm");

                rtmpPackage = new RTMPPackage();
                rtmpPackage.setBuffer(outData);
                long ems = (bufferInfo.presentationTimeUs / 1000) - startTime;
                rtmpPackage.setTms(ems);
                rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_AUDIO_DATA);

                mScreenLive.addPackage(rtmpPackage);

                mMediaCodec.releaseOutputBuffer(index,false);
            }
        }

        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;

        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
        startTime = 0;
        isRecording = false;
    }

    public void stopLive(){
        isRecording = false;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
