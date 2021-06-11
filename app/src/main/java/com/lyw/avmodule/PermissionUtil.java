package com.lyw.avmodule;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

/**
 * 功能描述:
 * Created on 2021/6/11.
 *
 * @author lyw
 */
public class PermissionUtil {

    public static final String[] PERMISSION_RECORD = new String[]{Manifest.permission.RECORD_AUDIO};

    /**
     * 录音
     */
    public static final int REQUEST_RECORD = 104;

    /**
     * 判断是否有文件读写的权限
     *
     * @param context
     * @return
     */
    public static boolean isHasSDCardWritePermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 请求录音权限
     *
     * @param context
     */
    public static void requestRecordPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!isHasRecordPermission(context)) {
                ActivityCompat.requestPermissions((Activity) context,PERMISSION_RECORD, REQUEST_RECORD);
            }
        }
    }

    /**
     * 判断是否有录音权限
     * @param context
     * @return
     */
    public static boolean isHasRecordPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

}
