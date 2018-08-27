package com.qianbajin.compress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
/**
 * @author Administrator
 * @Created at 2018/8/25 0025  17:00
 * @des
 */

public class BitmapUtil {

    static File compressImage(File srcFile, int reqWidth, int reqHeight,
                              Bitmap.CompressFormat compressFormat, Bitmap.Config bitmapConfig,
                              int quality, String destinationDirPath) throws IOException {
        FileOutputStream fos = null;
        File desFile = new File(destinationDirPath, srcFile.getName());
        File parentFile = desFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            fos = new FileOutputStream(desFile);
            decodeBitmapFromFile(srcFile, reqWidth, reqHeight, bitmapConfig).compress(compressFormat, quality, fos);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        return desFile;
    }

    static Bitmap decodeBitmapFromFile(File imageFile, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        long length = imageFile.length();
        Log.d("BitmapUtil", "length:" + length + "  imageFile.length():" + getReadableFileSize(length));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        Log.d("BitmapUtil", "options.outWidth:" + options.outWidth + "  options.outHeight:" + options.outHeight);
        Log.d("BitmapUtil", "reqWidth:" + reqWidth + "  reqHeight:" + reqHeight);
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        int byteCount = bitmap.getAllocationByteCount();
        Log.d("BitmapUtil", "byteCount:" + byteCount + "  scaledBitmap:" + getReadableFileSize(byteCount));

        int[] ints = calculateConfig(options, reqWidth, reqHeight);
        int width = ints[0];
        int height = ints[1];

        Bitmap scaledBitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        Matrix scaleMatrix = new Matrix();
        float ratioX = width / (float) options.outWidth;
        float ratioY = height / (float) options.outHeight;
        scaleMatrix.setScale(ratioX, ratioY, 0, 0);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        bitmap.recycle();

        // 检查图片的方向并进行旋转
        ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        Matrix matrix = new Matrix();
        if (orientation == 1) {
            return scaledBitmap;
        } else if (orientation == 6) {
            matrix.postRotate(90);
        } else if (orientation == 3) {
            matrix.postRotate(180);
        } else if (orientation == 8) {
            matrix.postRotate(270);
        }
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return scaledBitmap;
    }

    private static int[] calculateConfig(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int[] widthHeight = new int[]{width, height};
        if (width > reqWidth && height > reqHeight) {
//            float widthRatio = (float) reqWidth / width;
//            float heightRatio = (float) reqHeight / height;
//            Log.d("ImageUtil", "widthRatio:" + widthRatio + "  heightRatio:" + heightRatio);
//            if (widthRatio > heightRatio) {
//                widthHeight[0] = reqWidth;
//                widthHeight[1] = height * reqWidth / width;
//            } else {
//                widthHeight[1] = reqHeight;
//                widthHeight[0] = width * reqHeight / height;
//            }
            // bitmap 的长边
            int maxSize = Math.max(width, height);
            int minSize = Math.min(width, height);
            // 要求的长边
            int maxReq = Math.max(reqWidth, reqHeight);
            int minReq = Math.min(reqWidth, reqHeight);
            // 取最接近的边长作为固定边长,另一边等比缩放
            float maxRatio = (float) maxReq / maxSize;
            float minRatio = (float) minReq / minSize;
            Log.d("BitmapUtil", "maxRatio:" + maxRatio + "  minRatio:" + minRatio);
            // 以长边作为基准,为了保留质量,图片更清晰

            
            if (maxRatio < minRatio) {
                if (reqWidth == maxReq) {
                    widthHeight[0] = reqWidth;
                    widthHeight[1] = reqWidth * height / width;
                } else {
                    widthHeight[1] = reqHeight;
                    widthHeight[0] = reqHeight * width / height;
                }
            } else {
                // 以短边作为基准
                if (reqWidth == maxReq) {
                    widthHeight[0] = reqWidth;
                    widthHeight[1] = reqWidth * height / width;
                } else {
                    widthHeight[1] = reqHeight;
                    widthHeight[0] = reqHeight * width / height;
                }
            }
        }
        Log.d("BitmapUtil", "ints:" + widthHeight[0] + "  " + widthHeight[1]);
        return widthHeight;
    }

    /**
     * 计算inSampleSize,在质量压缩中,inSampleSize是一个最大的影响参数,调用Bitmap bitmap = BitmapFactory.decodeFile(String pathName, Options opts)时
     * opts 如果传入null 假如一张 2560 x 1440 的图片,使用ARGB_8888格式(默认)加载到内存,占用的内存为 2560 x 1440 x 4 = 14745600 (字节) = 14.0625 MB,
     * 图片尺寸太大,如果opts不做限制,很容易发生oom,如果inSampleSize = 2,那么生成的bitmap的宽高分别为原来的 1/2,那么图片占用的像素为原来的 1/4,极大的缩小了
     * 占用的内存.
     * inSampleSize 只能为2的n次幂,假如 inSampleSize = 7,那么系统会向下取整为4 , inSampleSize最小只能为1,小于1会被视为1处理
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int longSize = Math.max(options.outWidth, options.outHeight);
        int shortSize = Math.min(options.outWidth, options.outHeight);
        int longReqSize = Math.max(reqWidth, reqHeight);
        int shortReqSize = Math.min(reqWidth, reqHeight);
        int inSampleSize = 1;

        if (longSize > longReqSize && shortSize > shortReqSize) {
            // 长边对长边,短边对短边,取比例的最小值
            int longRatio = longSize / longReqSize;
            int shortRatio = shortSize / shortReqSize;

            inSampleSize = Math.min(longRatio, shortRatio);
        }
        Log.d("BitmapUtil", "inSampleSize:" + inSampleSize);
        inSampleSize = calculateBitmapSize(options, inSampleSize);
        Log.d("BitmapUtil", "inSampleSize:" + inSampleSize);

        return inSampleSize;
    }

    /**
     * 计算当前inSampleSize下生成的bitmap是否大于 16 MB,如果大于则向上取整,防止oom
     *
     * @param options      options
     * @param inSampleSize 采样率
     */
    private static int calculateBitmapSize(BitmapFactory.Options options, int inSampleSize) {
        int width = options.outWidth;
        int height = options.outHeight;
        // 因为BitmapFactory.Options 是我们刚才创建的,默认为ARGB_8888,一个像素占4个字节
        // 生成Bitmap占用的内存 单位字节
        int pixel = width * height * 4;
        // 允许最大占用内存 16 MB
        int maxPixel = 16 * 1024 * 1024;
        while (pixel / inSampleSize > maxPixel) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    private static String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
