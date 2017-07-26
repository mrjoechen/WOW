package com.thunder.wow;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



/**
 *
 */

public class FileUtils {
    private static String TAG = FileUtils.class.getName();

    private FileUtils() {
    }

    private static final byte[] COPY_ASSETS_BUF = new byte[1024];

    /**
     * 复制文件
     *
     * @param srcPath
     * @param dstPath
     * @param overwrite 若目标文件存在，是否覆盖
     * @return 复制成功，或无须复制
     */
    public static synchronized boolean copy(@NonNull String srcPath, @NonNull String dstPath, boolean overwrite) {

        File src = new File(srcPath);
        if (!src.exists()) { // short cut
            return false;
        }
        File dst = new File(dstPath);
        File parent = dst.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            return false;
        }

        if (dst.exists()) {
            if (!overwrite) {
                return true;
            } else {
                dst.delete();
            }
        }

        File tmp = new File(dstPath + ".tmp");

        byte[] buffer = new byte[64 * 1024];
        boolean success;
        int read;
        try (InputStream is = new FileInputStream(src); OutputStream os = new FileOutputStream(tmp)) {
            while ((read = is.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            success = false;
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }

        if (!success) {
            tmp.delete();
        } else {
            tmp.renameTo(dst);
        }

        return success;
    }

    /**
     * 移动文件
     *
     * @param srcPath
     * @param dstPath
     * @return 移动成功，注意返回true表示复制到目标路径成功，但未检查源文件是否删除成功
     */
    public static boolean move(@NonNull String srcPath, @NonNull String dstPath) {
        boolean copyed = copy(srcPath, dstPath, true);
        if (copyed) {
            new File(srcPath).delete();
            return true;
        }
        return false;
    }

    /**
     * 删除某个文件或目录（递归删除）
     *
     * @param file
     * @return
     */
    public static boolean delete(File file) {
        if (!file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File subFile :
                    files) {
                if (!delete(subFile)) {
                    return false;
                }
            }
            return file.delete();
        } else {
            return file.delete();
        }
    }

    public static boolean deleteExcept(File file, File exceptFile) {
        if (!file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File subFile :
                    files) {
                if (!deleteExcept(subFile, exceptFile)) {
                    return false;
                }
            }
            return file.delete();
        } else {
            if (!exceptFile.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath())) {
                return file.delete();
            } else {
                return true;
            }
        }
    }

    /**
     * 删除某个文件或目录（递归删除）
     *
     * @param path
     * @return
     */
    public static boolean delete(String path) {
        return delete(new File(path));
    }


    /**
     * 将制定文件读入为字符串
     *
     * @param fileName
     * @return
     */
    public static String readTextFromFile(String fileName) {
        return readTextFromFile(new File(fileName));
    }

    /**
     * 将制定文件读入为字符串
     *
     * @param file
     * @return
     */
    public static String readTextFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder stringBuilder = new StringBuilder();
            String tmpStr;
            while ((tmpStr = reader.readLine()) != null) {
                stringBuilder.append(tmpStr).append("\r\n");
            }
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字符串写入文件，若文件存在则覆盖原文件内容
     *
     * @param file
     * @param content
     * @return
     */
    public static boolean writeToFile(File file, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将字符串写入文件，若文件存在则覆盖原文件内容
     *
     * @param fileName
     * @param content
     */
    public static boolean writeToFile(String fileName, String content) {
        return writeToFile(new File(fileName), content);
    }

        
    public static <C extends Closeable> C closeQuietly(C closable) {
        if (closable != null) {
            try {
                closable.close();
                closable = null;
            } catch (IOException e) {
            }
        }
        return closable;
    }

    /**
     * 将asset/{src}下的文件（不包含src），递归复制到制定目录
     * 不存在的目录会自动创建，已经存在的文件将忽略
     * @param src
     * @param dst
     * @return
     */
    public static boolean copyAssets(Context context, String src, String dst) {
        AssetManager assetManager = context.getAssets();
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            String[] paths = assetManager.list(src);
            // consider it a directory if paths.length > 0, may be there is a better way to determine that
            if (paths.length == 0) {
                File dstFile = new File(dst);
                if (dstFile.exists()) {
                    return true;
                }
                File tmpFile = new File(dst + ".tmp");
                File parent = dstFile.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    return false;
                }
                fos = new FileOutputStream(tmpFile);
                is = assetManager.open(src);
                int read = 0;
                while ((read = is.read(COPY_ASSETS_BUF)) > 0) {
                    fos.write(COPY_ASSETS_BUF, 0, read);
                }
                fos.flush();
                tmpFile.renameTo(dstFile);
                return true;
            } else {
                File dstFile = new File(dst);
                if (!dstFile.exists() && !dstFile.mkdirs()) {
                    return false;
                }
                for (String path :
                        paths) {
                    if (!copyAssets(context, src + "/" + path, dst + "/" + path)) {
                        return false;
                    }
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeQuietly(fos);
            closeQuietly(is);
        }
    }


}

