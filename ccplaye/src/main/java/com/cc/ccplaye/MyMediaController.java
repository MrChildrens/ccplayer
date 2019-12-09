package com.cc.ccplaye;

import android.content.Context;
import android.util.AttributeSet;

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
        return R.layout.custom_media_controller;
    }

    @Override
    protected int getPauseId() {
        return R.id.pause;
    }

    @Override
    protected int getFfwdId() {
        return R.id.ffwd;
    }

    @Override
    protected int getRewId() {
        return R.id.rew;
    }

    @Override
    protected int getNextId() {
        return R.id.next;
    }

    @Override
    protected int getPrevId() {
        return R.id.prev;
    }

    @Override
    protected int getProgressId() {
        return R.id.mediacontroller_progress;
    }

    @Override
    protected int getEndTimeId() {
        return R.id.end;
    }

    @Override
    protected int getCurrentTimeId() {
        return R.id.time_current;
    }
}
