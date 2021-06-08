package com.lyw.avmodule;

import android.os.Environment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 功能描述:文件工具类
 * Created on 2021/6/8.
 *
 * @author lyw
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 将h264数据写入文件里
     *
     * @param arrays
     */
    public static void writeBytes(byte[] arrays) {
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(Environment.getExternalStorageDirectory() + "/codec.h264", true);
            writer.write(arrays);
            writer.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
