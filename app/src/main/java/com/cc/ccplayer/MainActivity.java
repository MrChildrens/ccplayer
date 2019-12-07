package com.cc.ccplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.cc.ccplaye.BaseMediaController;
import com.cc.ccplaye.CCMediaController;
import com.cc.ccplaye.CCVideoView;
import com.cc.ccplaye.IMediaPlayer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CCVideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mVideoView = findViewById(R.id.videoview_play);
//        CCMediaController mediaController = new CCMediaController(this);
        MediaController mediaController = new MediaController(this);
        mVideoView.setMediaController(mediaController);
//        mVideoView.setBaseMediaController(new BaseMediaController(this));

        String uri = ("https://mov.bn.netease.com/open-movie/nos/mp4/2016/01/11/SBC46Q9DV_hd.mp4");
        mVideoView.setVideoURI(Uri.parse(uri));
        mVideoView.start();


        Button button = findViewById(R.id.btn_next);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = ("https://mov.bn.netease.com/open-movie/nos/mp4/2018/01/12/SD70VQJ74_sd.mp4");
                mVideoView.setVideoURI(Uri.parse(uri));
                mVideoView.start();
            }
        });

        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                Log.d(TAG, "onPrepared: ");
            }
        });

        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                Log.d(TAG, "onCompletion: ");
            }
        });

        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onError() called with: mp = [" + mp + "], what = [" + what + "], extra = [" + extra + "]");
                return false;
            }
        });

        mVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onInfo() called with: mp = [" + mp + "], what = [" + what + "], extra = [" + extra + "]");
                return false;
            }
        });
    }


}
