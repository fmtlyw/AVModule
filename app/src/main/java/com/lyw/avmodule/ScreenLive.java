package com.lyw.avmodule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

import static android.app.Activity.RESULT_OK;

/**
 * 生产者消费者模式
 * 功能描述:屏幕管理（传输层---消费者）
 * Created on 2021/6/1.
 *
 * @author lyw
 */
public class ScreenLive extends Thread {

    private MediaProjection mMediaProjection;

    private MediaProjectionManager mProjectionManager;

    private boolean isLiving;

    //阻塞队列
    private LinkedBlockingQueue<RTMPPackage> mQueue = new LinkedBlockingQueue<>();

    private String url;

    private VideoCodeC videoCodeC;
    private AudioCodeC audioCodeC;

    /**
     * 开始直播
     *
     * @param mActivity
     * @param url
     */
    protected void startLive(Activity mActivity, String url) {
        this.url = url;
        //获取系统服务
        mProjectionManager = (MediaProjectionManager) mActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        //创建意图
        Intent screenCaptureIntent = mProjectionManager.createScreenCaptureIntent();
        //要求返回结果
        mActivity.startActivityForResult(screenCaptureIntent, 200);
    }


    /**
     * 结束直播
     */
    protected void stopLive(){
        addPackage(RTMPPackage.EMPTY_PACKGE);
        isLiving = false;
    }


    /**
     * 返回结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200 && resultCode == RESULT_OK) {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            start();
        }
    }


    /**
     * 生产者入口
     * @param rtmpPackage
     */
    public void addPackage(RTMPPackage rtmpPackage) {
        if (!isLiving) {
            return;
        }
        mQueue.add(rtmpPackage);
    }


    @Override
    public void run() {
        if (!connect(url)) {
            Log.d("lyw","run-->链接服务器失败");
            return;
        }
        isLiving = true;

        //视频采集编码
        videoCodeC = new VideoCodeC(this);
        videoCodeC.startLive(mMediaProjection);

        //音频采集编码
        audioCodeC = new AudioCodeC(this);
        audioCodeC.startLive();

        //循环取数据
        while (isLiving) {
            RTMPPackage rtmpPackage = null;
            try {
                rtmpPackage = mQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (rtmpPackage.getBuffer() != null && rtmpPackage.getBuffer().length != 0) {
                sendData(rtmpPackage.getBuffer(), rtmpPackage.getBuffer().length,rtmpPackage.getType(), rtmpPackage.getTms());
            }
        }

        isLiving = false;
        audioCodeC.stopLive();
        videoCodeC.stopLive();
        mQueue.clear();
        disConnect();
    }


    private native boolean sendData(byte[] data,int len,int type,long tms);

    private native boolean connect(String url);

    private native void disConnect();
}
