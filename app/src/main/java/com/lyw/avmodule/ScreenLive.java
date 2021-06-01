package com.lyw.avmodule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

import java.util.concurrent.LinkedBlockingQueue;

import static android.app.Activity.RESULT_OK;

/**
 * 功能描述:屏幕管理
 * Created on 2021/6/1.
 *
 * @author lyw
 */
public class ScreenLive implements Runnable{

    private MediaProjection mMediaProjection;

    private MediaProjectionManager mProjectionManager;

    private boolean isLiving;

    //阻塞队列
    private LinkedBlockingQueue<RTMPPackage> mQueue = new LinkedBlockingQueue<>();

    private String url;

    @Override
    public void run() {

    }

    /**
     * 开始直播
     * @param mActivity
     * @param url
     */
    protected void startLive(Activity mActivity,String url){
        this.url = url;
        //获取系统服务
        mProjectionManager = (MediaProjectionManager)mActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        //创建意图
        Intent screenCaptureIntent = mProjectionManager.createScreenCaptureIntent();
        //要求返回结果
        mActivity.startActivityForResult(screenCaptureIntent,200);
    }


    /**
     * 返回结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200 && resultCode == RESULT_OK) {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
//            VideoCodeC videoCodeC = new VideoCodeC(this);
//            videoCodeC.startLive(mMediaProjection);
        }
    }


    public void addPackage(RTMPPackage rtmpPackage){
        if (!isLiving) {
            return;
        }
        mQueue.add(rtmpPackage);


    }
}
