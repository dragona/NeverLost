package com.abc666.neverlost.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.blankj.ALog;
import com.abc666.neverlost.R;
import com.abc666.neverlost.base.BaseSoundPlayer;
import com.abc666.neverlost.base.BaseSwipeBackActivity;
import com.abc666.neverlost.base.SoundFileAdapter;
import com.abc666.neverlost.bean.SoundFile;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



public class SoundFileActivity extends BaseSwipeBackActivity {

    @BindView(R.id.recycle_view)
    RecyclerView recycleView;
    @BindView(R.id.bt_cancel)
    AppCompatButton btCancel;
    @BindView(R.id.bt_sure)
    AppCompatButton btSure;

    private List<SoundFile> soundFiles = new ArrayList<>();
    private SoundFileAdapter soundFileAdapter;
    public static final String RESULT_FILE_NAME = "RESULT_FILE_NAME";
    public static final String RESULT_FILE_DATA = "RESULT_FILE_DATA";
    public static final String IS_SOUND_LOOPING = "IS_SOUND_LOOPING";

    private BaseSoundPlayer baseSoundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_file);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放音乐实例
        baseSoundPlayer.release();
    }

    private void initView() {
        // 实例化对象
        baseSoundPlayer = new BaseSoundPlayer();

        // 获取手机音乐文件列表
        soundFiles = getSoundFiles();
        if (!soundFiles.isEmpty()) {
            // 设置RecycleView
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recycleView.setLayoutManager(layoutManager);
            soundFileAdapter = new SoundFileAdapter(recycleView, soundFiles, baseSoundPlayer,getIntent().getBooleanExtra(IS_SOUND_LOOPING, false));
            recycleView.setAdapter(soundFileAdapter);

        }
    }

    /**
     * @return 返回手机中的音乐文件列表SoundFileList
     */
    private List<SoundFile> getSoundFiles() {
        List<SoundFile> files = new ArrayList<>();
        SoundFile soundFile = new SoundFile();
        // 遍历查询音乐文件
        try {
            @SuppressLint("Recycle")
            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    String title = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String data = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    soundFile.setTitle(title);
                    soundFile.setData(data);
                    files.add(soundFile);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Exception e) {
            ALog.e(e);
        }
        return files;
    }

    @OnClick({R.id.bt_cancel, R.id.bt_sure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_cancel:
                finish();
                break;
            case R.id.bt_sure:
                // 确定设置音乐文件
                Intent intent = new Intent();
                if (soundFiles != null && soundFileAdapter != null) {
                    // 只有选择了列表文件才进行返回保存
                    if (soundFileAdapter.getSelectedPos() != -1) {
                        intent.putExtra(RESULT_FILE_NAME, soundFiles.get(soundFileAdapter.getSelectedPos()).getTitle());
                        intent.putExtra(RESULT_FILE_DATA, soundFiles.get(soundFileAdapter.getSelectedPos()).getData());
                        setResult(RESULT_OK, intent);
                    }
                } else {
                    setResult(RESULT_CANCELED, intent);
                }
                finish();
                break;
            default:
                break;
        }
    }
}
