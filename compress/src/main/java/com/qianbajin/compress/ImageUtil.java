package com.qianbajin.compress;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
/**
 * @author Administrator
 * @created at 2018/8/25 0025  17:00
 * @des
 */

public class ImageUtil {

    private static volatile ImageUtil INSTANT;

    /**
     * 最大宽度，默认为640
     */
    private int maxWidth = 640;
    /**
     * 最大高度,默认为480
     */
    private int maxHeight = 480;
    /**
     * 默认压缩后的方式为JPEG,为有损压缩，
     * PNG无损图片格式,调用bitmap.compress(CompressFormat format, int quality, OutputStream stream)时
     * 如果传入 CompressFormat.PNG,则图片不会被压缩,相反,保存后的图片可能比原来的图片还要大
     * CompressFormat.WEBP 为谷歌推荐的图片格式,若传入该格式,图片压缩比会更大,即压缩后的图片要比原图小很多,如果对图片传输大小有
     * 较高要求,推荐使用这种方式,因为压缩出来的图片很小且不是很模糊
     */
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;

    /**
     * 默认的图片处理方式是ARGB_8888,为32位图,一个像素占4个字节
     * 假如一张 2560 x 1440 的图片,使用ARGB_8888格式(默认)加载到内存,占用的内存为
     * 2560 x 1440 x 4 = 14745600 (字节) = 14.0625 MB 所以大图加载到内存和容易发生oom
     */
    private Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
    /**
     * 默认压缩质量为80,压缩质量在 70-80之间质量压缩效果是比较明显的,再低图片大小降得不明显反而图片变得模糊
     */
    private int quality = 80;
    /**
     * 存储路径
     */
    private String destinationDirPath;

    private ImageUtil(Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        destinationDirPath = externalCacheDir != null ? externalCacheDir.getAbsolutePath() : context.getCacheDir().getAbsolutePath();
    }

    public static ImageUtil get(Context context) {
        if (INSTANT == null) {
            synchronized (ImageUtil.class) {
                if (INSTANT == null) {
                    INSTANT = new ImageUtil(context);
                }
            }
        }
        return INSTANT;
    }

    /**
     * 压缩成文件
     *
     * @param file 原始文件
     * @return 压缩后的文件
     */
    public File compressToFile(File file) throws IOException {
        return BitmapUtil.compressImage(file, maxWidth, maxHeight, compressFormat,
                bitmapConfig, quality, destinationDirPath);
    }

    public Bitmap compress2Bitmap(File file) throws IOException {
        return BitmapUtil.decodeBitmapFromFile(file, maxWidth, maxHeight, bitmapConfig);
    }

    public ImageUtil setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public ImageUtil setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public ImageUtil setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
        return this;
    }

    public ImageUtil setBitmapConfig(Bitmap.Config bitmapConfig) {
        this.bitmapConfig = bitmapConfig;
        return this;
    }

    public ImageUtil setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    public ImageUtil setDestinationDirPath(String destinationDirPath) {
        this.destinationDirPath = destinationDirPath;
        return this;
    }
}
