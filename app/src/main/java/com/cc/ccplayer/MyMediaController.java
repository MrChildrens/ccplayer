package com.cc.ccplayer;

import android.content.Context;
import android.util.AttributeSet;

import com.cc.ccplaye.MediaController;

/**
 * @author: Ciel
 * @date: 2019/12/9 17:01
 */
public class MyMediaController extends MediaController {

    public MyMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyMediaController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public MyMediaController(Context context) {
        super(context);
    }
    
    @Override
    protected int getRootViewId() {
        return R.layout.apstar_controller;
    }

    @Override
    protected int getPauseId() {
        return R.id.iv_play_or_pause;
    }

    @Override
    protected int getPauseIconId() {
        return R.drawable.play_icon_pause;
    }

    @Override
    protected int getPlayIconId() {
        return R.drawable.play_icon_play;
    }

    @Override
    protected int getFfwdId() {
        return R.id.iv_forward;
    }

    @Override
    protected int getRewId() {
        return R.id.iv_rewind;
    }

    @Override
    protected int getNextId() {
        return R.id.iv_next_episode;
    }

    @Override
    protected int getPrevId() {
        return R.id.iv_previous_episode;
    }

    @Override
    protected int getProgressId() {
        return R.id.seekbar_progress;
    }

    @Override
    protected int getEndTimeId() {
        return R.id.tv_endtime;
    }

    @Override
    protected int getCurrentTimeId() {
        return R.id.tv_starttime;
    }

    @Override
    protected int getExitId() {
        return R.id.iv_exit;
    }

    @Override
    protected int getFullScreenId() {
        return R.id.iv_switch_screen;
    }

    @Override
    protected int getFullIconId() {
        return R.drawable.play_icon_switch_full_screen;
    }

    @Override
    protected int getScreenIconId() {
        return R.drawable.play_icon_switch_window;
    }
}
