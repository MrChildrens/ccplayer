package com.cc.ccplaye;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cc.ccplaye.CCMediaController;

/**
 * @author: Ciel
 * @date: 2019/12/6 13:18
 */
public class BaseMediaController extends FrameLayout {

    protected View mRootView;
    protected MediaController.MediaPlayerControl mPlayer;

    private boolean mShowing = true;
    private boolean mDragging;

    private ProgressBar mProgress;
    private View mLayoutController;

    public BaseMediaController(@NonNull Context context) {
        this(context, null);
    }

    public BaseMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BaseMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mRootView = LayoutInflater.from(context).inflate(R.layout.custom_media_controller, this, true);
        mLayoutController = findViewById(R.id.layout_controller);
    }

//    protected abstract int getLayoutRes();

    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
//        updatePausePlay();
    }

    public void hide() {
        if (mShowing) {
            try {
                removeCallbacks(mShowProgress);
                setVisibility(View.GONE);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
    }

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mPlayer.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

//        if (mEndTime != null)
//            mEndTime.setText(stringForTime(duration));
//        if (mCurrentTime != null)
//            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    public interface MediaPlayerControl {
        void    start();
        void    pause();
        int     getDuration();
        int     getCurrentPosition();
        void    seekTo(int pos);
        boolean isPlaying();
        int     getBufferPercentage();
        boolean canPause();
        boolean canSeekBackward();
        boolean canSeekForward();

        /**
         * Get the audio session id for the player used by this VideoView. This can be used to
         * apply audio effects to the audio track of a video.
         * @return The audio session, or 0 if there was an error.
         */
        int     getAudioSessionId();
    }
}
