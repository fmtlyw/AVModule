package com.lyw.avmodule;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 功能描述:视频采集并编码（编码层---生产者）
 * Created on 2021/6/1.
 *
 * @author lyw
 */
public class VideoCodeC extends Thread{


    private MediaProjection mMediaProjection;

    //编码器
    private MediaCodec mMediaCodec;
    //虚拟画布
    private VirtualDisplay mVirtualDisplay;

    //正在直播
    private boolean isLiving;

    //时间戳，每个包的时间戳
    private long timeStamp;
    private long startTime;

    private ScreenLive mScreenLive;


    public VideoCodeC(ScreenLive screenLive) {
        this.mScreenLive = screenLive;
    }


    public void startLive(MediaProjection mediaProjection){
        this.mMediaProjection = mediaProjection;

        //创建一个编码器
        try {
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 640, 480);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            //码流
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,500_000);
            //帧数
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,15);
            //关键帧
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2);


            //CONFIGURE_FLAG_ENCODE表示编码操作
            mMediaCodec.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

            //注意：mMediaCodec创建的画布会自动把mMediaProjection录制的数据进行编码
            Surface mSurface = mMediaCodec.createInputSurface();
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("lyw",640,480,1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,mSurface,null,null);

            //开始执行 run()方法
            start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        //开始编码
        mMediaCodec.start();
        isLiving = true;
        Log.d("lyw","run-->开始编码");

        //取数据

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (isLiving){
            if (timeStamp != 0) {
                //这段代码的用处？？？？？
                if (System.currentTimeMillis() - timeStamp >= 2000) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME,0);
                    mMediaCodec.setParameters(bundle);
                    timeStamp = System.currentTimeMillis();
                }
            }else {
                timeStamp = System.currentTimeMillis();
            }

            /***************************开始取数据封包********************************/


            //99号技师有没有空
            int index = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10);

            //有空带出去
            if (index >= 0) {
                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(index);

                //转成byte字节数据，再封装成rtmp包
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                //把h264流数据写入文件，可通过ffmpeg的ffplay或者vlc播放
                //FileUtils.writeBytes(outData);

                if (startTime == 0) {
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }

                RTMPPackage rtmpPackage = new RTMPPackage();
                rtmpPackage.setBuffer(outData);
                long tms = bufferInfo.presentationTimeUs / 1000 - startTime;
                rtmpPackage.setTms(tms);
                rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_VIDEO);

                mScreenLive.addPackage(rtmpPackage);

                //释放
                mMediaCodec.releaseOutputBuffer(index,false);
            }
        }
        isLiving = false;
        startTime = 0;
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        mMediaProjection.stop();
        mMediaProjection = null;
    }

    public void stopLive(){
        isLiving = false;
        try {
            //阻塞上一线程，让线程按顺序执行
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
