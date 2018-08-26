package com.qianbajin.compressimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qianbajin.compress.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private HorizontalScrollView mHsv;
    private LayoutInflater mInflater;
    private LinearLayout mLlContainer;
    private File mSrcFile;
    private File mDesFile;

//    private ImageView mIvSrc;
//    private TextView mOriginalSize;
//    private ImageView mIvImg1;
//    private TextView mImg1Size;
//    private ImageView mIvImg2;
//    private TextView mImg2Size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        String fileSize = getReadableFileSize(16 * 1024 * 1024);
        Log.d("MainActivity", "fileSize:" + fileSize);

        mInflater = LayoutInflater.from(this);
        addChild(0, "original img", null);
    }

    private void addChild(int index, String name, File file) {
        LinearLayout item = (LinearLayout) mInflater.inflate(R.layout.item_image, null);
        ImageView img = item.findViewById(R.id.iv_src);
        TextView tvName = item.findViewById(R.id.tv_name);
        TextView tvSize = item.findViewById(R.id.tv_size);
        if (file != null) {
            img.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        } /*else {
            img.setBackgroundColor(Color.GRAY);
        }*/
        tvName.setText(name);
        tvSize.setText(file != null ? getReadableFileSize(file.length()) : "0 MB");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(30);
        params.setMarginEnd(30);
        item.setLayoutParams(params);
        View childAt = mLlContainer.getChildAt(index);
        mLlContainer.removeView(childAt);
        int childCount = mLlContainer.getChildCount();
        if (index == childCount) {
            mLlContainer.addView(item, index, params);
        } else {
            mLlContainer.addView(item, index - 1, params);

        }
        img.setOnClickListener(v -> {
            v.setTag(index);
            onClick(v);
        });
    }

    private void initView() {
//        mIvSrc = findViewById(R.id.iv_src);
//        mIvSrc.setOnClickListener(this);
//        mOriginalSize = findViewById(R.id.original_size);
//        mIvImg1 = findViewById(R.id.iv_img1);
//        mIvImg1.setOnClickListener(this);
//        mImg1Size = findViewById(R.id.img1_size);
//        mIvImg2 = findViewById(R.id.iv_img2);
//        mIvImg2.setOnClickListener(this);
//        mImg2Size = findViewById(R.id.img2_size);
        mHsv = findViewById(R.id.hsv);
        mLlContainer = findViewById(R.id.ll_container);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null) {
            try {
                mSrcFile = FileUtil.from(this, data.getData());
                addChild(0, "original", mSrcFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_src:
                int index = (int) v.getTag();
                if (index == 0) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                } else {
                    if (mDesFile != null) {
                        Intent intent = new Intent();
                        intent.setDataAndType(Uri.fromFile(mDesFile), "image/*");
                        startActivity(intent);
                    }
                }
                break;
//            case R.id.iv_img1:
//                break;
//            case R.id.iv_img2:
//                break;
            default:
                break;
        }
    }

    public void btn1(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    public void btn2(View view) {
        if (mSrcFile != null) {
            try {
                mDesFile = ImageUtil.get(this)
                        .setMaxHeight(800)
                        .compressToFile(mSrcFile);
                addChild(1, "JPEG", mDesFile);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "发生异常" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
        }
    }

    public void btn3(View view) {
        if (mSrcFile != null) {
            try {
                mDesFile = ImageUtil.get(this)
                        .setMaxHeight(1280)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToFile(mSrcFile);
                addChild(2, "WEBP", mDesFile);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "发生异常" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
        }
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
