package com.thunder.wow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnTouchListener {


    private AdvertisementViewFlipper viewFlipper;
    private String scan_path = "wow";
    private UsbReceiver usbReceiver;
    private float touchDownX;  // 手指按下的X坐标
    private float touchUpX;  //手指松开的X坐标

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        viewFlipper = (AdvertisementViewFlipper) findViewById(R.id.viewFlipper);


        new ScanTask().execute("");
        viewFlipper.setFlipInterval(5000);
        viewFlipper.setOnPageFlipperListener(new AdvertisementViewFlipper.OnPageFlipListener() {
            @Override
            public void onPageFlip(ViewFlipper flipper, int whichChild) {
                //Log.d("onPageFlip", "onPageFlip:" + whichChild);


            }
        });

        viewFlipper.setOnTouchListener(this);
        usbReceiver = new UsbReceiver(this);


        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");

        registerReceiver(usbReceiver, filter);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // 取得左右滑动时手指按下的X坐标
            touchDownX = event.getX();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // 取得左右滑动时手指松开的X坐标
            touchUpX = event.getX();
            // 从左往右，看前一个View
            if (touchUpX - touchDownX > 100) {
                // 显示上一屏动画
                viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
                        R.anim.fade_in));
                viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
                        R.anim.fade_out));
                // 显示上一屏的View
                viewFlipper.showPrevious();
                // 从右往左，看后一个View
            } else if (touchDownX - touchUpX > 100) {
                //显示下一屏的动画
                viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
                        R.anim.fade_in));
                viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
                        R.anim.fade_out));
                // 显示下一屏的View
                viewFlipper.showNext();
            }
            return true;
        }
        return false;
    }


    public class  ScanTask extends AsyncTask<String, Void, List<String>>{

        @Override
        protected List<String> doInBackground(String... strings) {


            ArrayList<String> paths = new ArrayList<>();
            String path = strings[0];
            if (path!=null && !path.equals("")){
                File file = new File(path);
                if (file.exists() && file.canRead()){
                    File containingRoot = DiskUtils.findContainingRoot(scan_path);
                    if (containingRoot!=null){
                        File resource = new File(containingRoot.getAbsolutePath() + "/" + scan_path);
                        File dest = new File(Environment.getExternalStorageDirectory() + "/" + scan_path);

                        //删除目标文件
                        if (dest.exists() && dest.isDirectory()){

                            File[] files = dest.listFiles();
                            for (int i=0;i<files.length;i++){
                                files[i].delete();
                            }

                        }else {
                            dest.mkdirs();
                        }

                        //复制文件
                        File[] fs = resource.listFiles();
                        for (int i=0;i<fs.length;i++){
                            FileUtils.copy(fs[i].getAbsolutePath(), dest.getAbsolutePath()+"/"+fs[i].getName(), true);
                        }

                        //读取所有文件路径
                        if (dest.exists() && dest.isDirectory()){
                            File[] files = dest.listFiles();

                            for (int i=0;i<files.length;i++){
                                paths.add(files[i].getAbsolutePath());
                            }
                        }

                    }
                }
            }else {
                File f = new File(Environment.getExternalStorageDirectory() + "/" + scan_path);
                if (f.exists() && f.isDirectory()){
                    File[] files = f.listFiles();

                    if (files.length<=0){
                        FileUtils.copyAssets(MainActivity.this, "wow",Environment.getExternalStorageDirectory() + "/" + scan_path );
                        File f1 = new File(Environment.getExternalStorageDirectory() + "/" + scan_path);
                        File[] fs = f1.listFiles();
                        for (int i=0;i<fs.length;i++){
                            paths.add(fs[i].getAbsolutePath());
                        }
                    }else {
                        for (int i = 0; i < files.length; i++) {
                            paths.add(files[i].getAbsolutePath());
                        }
                    }
                }else {
                    FileUtils.copyAssets(MainActivity.this, "wow",Environment.getExternalStorageDirectory() + "/" + scan_path );
                    File f1 = new File(Environment.getExternalStorageDirectory() + "/" + scan_path);
                    File[] files = f1.listFiles();
                    for (int i=0;i<files.length;i++){
                        paths.add(files[i].getAbsolutePath());
                    }
                }
            }
            return paths;
        }


        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);


            if (strings!=null && strings.size()>0){

                viewFlipper.removeAllViews();
                for (int i=0;i<strings.size();i++){


                    ImageView imageView = new ImageView(MainActivity.this);
                    Glide.with(MainActivity.this).load(strings.get(i))
                            .asBitmap().into(imageView);
                    viewFlipper.addView(imageView);

                }
                viewFlipper.setFlipInterval(5000);
                viewFlipper.startFlipping();

            }else {
                Toast.makeText(MainActivity.this, "扫描失败，请把图片资源放入根目录wow文件夹下", Toast.LENGTH_LONG).show();
            }


        }


    }


    public void notifyImg(String path){
        new ScanTask().execute(path);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(usbReceiver);
    }
}
