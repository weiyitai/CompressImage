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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qianbajin.compress.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LayoutInflater mInflater;
    private LinearLayout mLlContainer;
    private File mFileSrc;
    private File mFileJpeg;
    private File mFileWebp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mInflater = LayoutInflater.from(this);
        addChild(0, "original img");
        addChild(1, "JPEG");
        addChild(2, "WEBP");

    }

    private void addChild(int index, String name) {
        LinearLayout item = (LinearLayout) mInflater.inflate(R.layout.item_image, null);
        ImageView img = item.findViewById(R.id.iv_src);
        ViewGroup.LayoutParams params = img.getLayoutParams();
        Log.d("MainActivity", "params.width:" + params.width + "  params.height:" + params.height);
        params.width = params.height * 9 / 16;
        img.setLayoutParams(params);
        mLlContainer.addView(item, index);
        img.setOnClickListener(v -> {
            v.setTag(index);
            onClick(v);
        });
        setImage(index, name, null);
    }

    private void setImage(int index, String name, File file) {
        View childAt = mLlContainer.getChildAt(index);
        ImageView imageView = childAt.findViewById(R.id.iv_src);
        if (file != null) {
//            BitmapFactory.decodeFile()
            // 用BitmapFactory.decodeFile(),入过原图较大会内存溢出,使用setImageURI则不会
            imageView.setImageURI(Uri.fromFile(file));
        }
        TextView tvName = childAt.findViewById(R.id.tv_name);
        TextView tvSize = childAt.findViewById(R.id.tv_size);
        tvName.setText(name);
        tvSize.setText(file != null ? getReadableFileSize(file.length()) : "0 MB");
    }

    private void initView() {
        mLlContainer = findViewById(R.id.ll_container);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null) {
            try {
                mFileSrc = FileUtil.from(this, data.getData());
                Log.d("MainActivity", mFileSrc.getAbsolutePath());
                setImage(0, "original", mFileSrc);
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
                File file;
                if (index == 0) {
                    file = mFileSrc;
                } else if (index == 1) {
                    file = mFileJpeg;
                } else {
                    file = mFileWebp;
                }
                if (file != null) {
                    Intent intent = new Intent();
                    intent.setDataAndType(Uri.fromFile(file), "image/*");
                    try {
                        intent.setPackage("com.alensw.PicFolder");
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        startActivity(intent);
                        Log.d("MainActivity", e.getMessage());
                    }
                } else {
                    Toast.makeText(this, "file invalid", Toast.LENGTH_SHORT).show();
                }
                break;
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
        if (mFileSrc != null) {
            try {
                mFileJpeg = ImageUtil.get(this)
                        .setMaxWidth(720)
                        .setMaxHeight(1280)
                        .compressToFile(mFileSrc);
                setImage(1, "JPEG", mFileJpeg);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.choice_pic_first, Toast.LENGTH_SHORT).show();
        }
    }

    public void btn3(View view) {
        if (mFileSrc != null) {
            try {
                mFileWebp = ImageUtil.get(this)
                        .setMaxHeight(1280)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToFile(mFileSrc);
                setImage(2, "WEBP", mFileJpeg);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.choice_pic_first, Toast.LENGTH_SHORT).show();
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
