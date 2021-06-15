package com.lyw.avmodule;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;

/**
 * 功能描述:
 * Created on 2021/6/11.
 *
 * @author lyw
 */
public class PermissionUtil {

    public static final String[] PERMISSION_RECORD = new String[]{Manifest.permission.RECORD_AUDIO};
    public static final String[] PERMISSION_SD_WRITE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * sd卡
     */
    public static final int REQUEST_SD_WRITE = 101;
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


    public static int requestPermissions(Context context, String[] permissions, int request) {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permissions[i]);
                }
            }
            if (permissionList.size() > 0) {
                ActivityCompat.requestPermissions((Activity) context, permissionList.toArray(new String[permissionList.size()]), request);
                return permissionList.size();
            }

        }
        return 0;
    }


    /**
     * 文件权限读写
     *
     * @param context
     */

    public static void requestSDCardWrite(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!isHasSDCardWritePermission(context)) {
                ActivityCompat.requestPermissions((Activity) context, PERMISSION_SD_WRITE, REQUEST_SD_WRITE);
            }
        }
    }

}
