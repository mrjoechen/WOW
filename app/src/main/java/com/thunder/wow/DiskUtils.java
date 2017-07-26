package com.thunder.wow;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DiskUtils {
    public static final String DISK_KEY = "disk_rema_size";

    private DiskUtils() {

    }

    /**
     * 获取当前挂载的所有外置存储的挂载路径
     *
     * @return
     * @throws Exception
     */
    public static List<String> getExStoragePaths() throws Exception {
        List<String> paths = new ArrayList<>();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        //首先判断一下外置SD卡的状态，处于挂载状态才能获取的到
        if (extFileStatus.equals(Environment.MEDIA_MOUNTED)
                && extFile.exists() && extFile.isDirectory()
                && extFile.canWrite()) {
            //外置SD卡的路径
            paths.add(extFile.getAbsolutePath());
        }
        // obtain executed result of command line code of 'mount', to judge
        // whether tfCard exists by the result
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("mount");
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        int mountPathIndex = 1;
        while ((line = br.readLine()) != null) {
            // format of sdcard file system: vfat/fuse
            // L.w("DeviceInit", line);
            if ((!line.contains("fat") && !line.contains("fuse") && !line
                    .contains("storage") && !line.contains("mnt"))
                    || line.contains("secure")
                    || line.contains("asec")
                    || line.contains("firmware")
                    || line.contains("shell")
                    || line.contains("obb")
                    || line.contains("legacy")) {
                continue;
            }
            String[] parts = line.split(" ");
            int length = parts.length;
            if (mountPathIndex >= length) {
                continue;
            }
            String mountPath = parts[mountPathIndex];

            // L.w("DeviceInit", "mountPath:"+mountPath);

            if (!mountPath.contains("/") || mountPath.contains("com/data")
                    || mountPath.contains("Data")) {
                continue;
            }
            File mountRoot = new File(mountPath);
            if (!mountRoot.exists() || !mountRoot.isDirectory()
                    || !mountRoot.canWrite()) {
                continue;
            }
            boolean equalsToPrimarySD = mountPath.equals(extFile
                    .getAbsolutePath());
            if (equalsToPrimarySD) {
                continue;
            }
            //扩展存储卡即TF卡或者SD卡路径
            paths.add(mountPath);
        }

        return paths;
    }


    /**
     * 获取指定路径所在空间的剩余可用容量字节数,耗时操作
     *
     * @param filePath
     * @return 可用空间，单位byte
     */
    public static long getAvailableBytes(String filePath) {
        if (filePath==null || filePath.equals("") || !(new File(filePath).exists())) {
            return 0;
        }
        StatFs stat = new StatFs(filePath);
        long availableBlocks = stat.getAvailableBlocksLong();
        return stat.getBlockSizeLong() * availableBlocks;
    }


    /**
     * 从指定的路径列表中选择可用容量最大的一个返回
     *
     * @param paths
     * @return
     */
    public static String getMaxAvailableSpacePath(List<String> paths) {
        long maxSize = 0, size;
        String result = null;

        for (String path :
                paths) {
            size = getAvailableBytes(path);
            if (size > maxSize) {
                maxSize = size;
                result = path;
            }
        }

        return result;
    }

    /**
     * 找到包含subPath的设备目录
     *
     * @param subPath
     * @return
     */
    public static File findContainingRoot(String subPath) {
        List<String> extSDCardPath = null;
        try {
            extSDCardPath = getExStoragePaths();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        for (int i = extSDCardPath.size() - 1; i >= 0; i--) {
            File dir = new File(extSDCardPath.get(i));
            if (!dir.exists() || !dir.isDirectory())
                continue;

            File dbFile = new File(dir.getAbsolutePath() + '/' + subPath);
            if (dbFile.exists()) {
                return dir;
            }
        }
        return null;
    }

}
