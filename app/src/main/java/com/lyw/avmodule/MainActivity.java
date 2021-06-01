package com.lyw.avmodule;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //直播地址
    //rtmp://live-push.bilivideo.com/live-bvc/
    //?streamname=live_1792239063_87501789&key=b9a0bfebde52671720a2b933f05973b5&schedule=rtmp&pflag=1

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    private ScreenLive mScreenLive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        mScreenLive = new ScreenLive();
        /**
         * 开发步骤
         *
         * 一、采集数据
         * 1、采集视频数据
         *   1、1 获取系统投屏服务
         *   1、2 创建虚拟显示器
         *   1、3 创建画布
         *   1、4 画布中采集数据（直接从mediodecoc中取）
         *
         *
         * 2、采集音频数据
         *
         *   2、1 创建一个AudioRecord对象
         *   2、2 开始采集音频并读取数据
         *
         *
         * 二、对音视频进行编解码
         *
         *
         * 三、对编码后的数据进行rtmp封包
         *
         *
         * 四、rtmp封包推流
         *
         */

    }


    /**
     * 开始直播
     * @param view
     */
    public void onStartPlay(View view){
        String url = "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_1792239063_87501789&key=b9a0bfebde52671720a2b933f05973b5&schedule=rtmp&pflag=1";
        mScreenLive.startLive(this,url);
    }

    /**
     * 结束直播
     * @param view
     */
    public void onStopPlay(View view) {
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mScreenLive.onActivityResult(requestCode,resultCode,data);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


}
