package com.abc666.neverlost.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.abc666.neverlost.util.SharedUtils;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.skyfishjy.library.RippleBackground;
import com.abc666.neverlost.R;
import com.abc666.neverlost.base.BaseAppCompatActivity;
import com.abc666.neverlost.module.SoundPlayer;
import com.abc666.neverlost.service.AutoProtectService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
public class MainActivity extends BaseAppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener {


    public LocationClient mLocationClient;
    public String returnLocation="";


    @BindView(R.id.tb_lock)
    ToggleButton tbLock;
    @BindView(R.id.fab_setting)
    FloatingActionButton fabSetting;

    @BindView(R.id.ripple_back)
    RippleBackground rippleBack;

    private SoundPlayer soundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        List<String> permissionList =new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(! permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            requestLocation();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            if (resultCode == 1) {
            }
        }
    }
    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(5000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5*60*1000);
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String [] permissions, int [] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意使用所有权限才能发送安全位置短信",Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location){
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition.append("经度：").append(location.getLongitude()).append("\n");
            currentPosition.append("位置：").append(location.getAddrStr()).append("\n");
            currentPosition.append("位置描述：").append(location.getLocationDescribe());
            returnLocation = currentPosition.toString();
            Log.i("loc",returnLocation);
            SharedUtils.putString(MainActivity.this,"return_loc",returnLocation);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放音乐实例
        soundPlayer.release();
        mLocationClient.stop();
    }

    @SuppressLint("NewApi")
    private void initView() {
        // 实例化对象
        soundPlayer = new SoundPlayer(this);

        if(!tbLock.isChecked()){
            rippleBack.startRippleAnimation();
        }
        tbLock.setOnCheckedChangeListener(this);
        tbLock.setOnTouchListener(this);
    }



    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        // 播放防盗开启提示音
        soundPlayer.playOpenTone();
        Vibrator mvibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mvibrator.vibrate(2000);

        if (isChecked) {
            // 关闭波纹效果
            rippleBack.stopRippleAnimation();
            // 开启自动防盗服务
            boolean isProtectUSB = SharedUtils.getBoolean(this, "USBprotect", true);
            if (isProtectUSB){
                Intent startIntent = new Intent(this, AutoProtectService.class);
                startService(startIntent);
            }

            Intent poIntent=new Intent(MainActivity.this,PocketActivity.class);
            startActivity(poIntent);

        } else {
            // 显示波纹效果
            rippleBack.startRippleAnimation();
            // 关闭自动防盗
            soundPlayer.playOpenTone();
            Intent stopIntent = new Intent(this,AutoProtectService.class);
            stopService(stopIntent);//停止服务
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.tb_lock) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                rippleBack.stopRippleAnimation();
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                rippleBack.startRippleAnimation();
            }
        }
        return false;
    }

    /**
     * 绑定点击时间处理
     *
     * @param view 点击的view
     */
    @OnClick(R.id.fab_setting)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fab_setting:
                // 跳转到设置页面
                Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(settingIntent, 1);
                break;
            default:
                break;
        }
    }

}
