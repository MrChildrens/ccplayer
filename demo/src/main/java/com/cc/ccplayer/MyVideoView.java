package com.cc.ccplayer;

import android.content.Context;
import android.util.AttributeSet;

import com.cc.ccplaye.IMediaPlayer;
import com.cc.ccplaye.VideoView;

/**
 * @author: Ciel
 * @date: 2019/12/11 12:16
 */
public class MyVideoView extends VideoView {

    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int initAspectRatio() {
        return IMediaPlayer.AR_AUTO;
    }
}
