/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cc.ccplaye;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.cc.ccplaye.utils.ScreenUtil;

import java.util.Formatter;
import java.util.Locale;

/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 * <p>
 * The way to use this class is to instantiate it programmatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 * <p>
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 * <p>
 * MediaController will hide and
 * show the buttons according to these rules:
 * <ul>
 * <li> The "previous" and "next" buttons are hidden until setPrevNextListeners()
 * has been called
 * <li> The "previous" and "next" buttons are visible but disabled if
 * setPrevNextListeners() was called with null listeners
 * <li> The "rewind" and "fastforward" buttons are shown unless requested
 * otherwise by using the MediaController(Context, boolean) constructor
 * with the boolean set to false
 * </ul>
 */
public abstract class MediaController extends FrameLayout implements IMediaController, View.OnTouchListener {

    private static final String TAG = MediaController.class.getSimpleName();

    private static final int DEFAULT_TIMEOUT = 10000;

    private IMediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private Activity mActivity;

    protected View mRoot;
    protected View mVideoRoot;
    protected View mProgress;
    protected View mEndTime;
    protected View mCurrentTime;
    protected View mPauseButton;
    protected View mFfwdButton;
    protected View mRewButton;
    protected View mNextButton;
    protected View mPrevButton;
    protected View mExitButton;
    protected View mFullScreenButton;

    private boolean mShowing;
    private boolean mDragging;
    private boolean mIsFullScreen;

    private int mVideoOldWidth;
    private int mVideoOldHeight;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private CharSequence mPlayDescription;
    private CharSequence mPauseDescription;

    protected abstract int getRootViewId();

    protected abstract int getPauseId();

    protected abstract int getPauseIconId();

    protected abstract int getPlayIconId();

    protected abstract int getFfwdId();

    protected abstract int getRewId();

    protected abstract int getNextId();

    protected abstract int getPrevId();

    protected abstract int getProgressId();

    protected abstract int getEndTimeId();

    protected abstract int getCurrentTimeId();

    protected abstract int getExitId();

    protected abstract int getFullScreenId();

    protected abstract int getFullIconId();

    protected abstract int getScreenIconId();

    public MediaController(@NonNull Context context) {
        this(context, null);
    }

    public MediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mRoot = this;
        mContext = context;
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }


    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * When VideoView calls this method, it will use the VideoView's parent
     * as the anchor.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */

    private boolean isAddToVideoView = false;

    @Override
    public void setAnchorView(FrameLayout view) {
        makeControllerView(view);
        show(10000);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     *
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView(FrameLayout view) {
        if (!isAddToVideoView) {
            mVideoRoot = view;
            mVideoRoot.setOnTouchListener(this);

            int rootId = getRootViewId();
            if (rootId <= 0) {
                rootId = R.layout.media_controller;
            }

            mRoot = LayoutInflater.from(mContext).inflate(rootId, this, true);
            initControllerView(mRoot);

            FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            mRoot.setLayoutParams(frameParams);

            view.addView(mRoot);
            isAddToVideoView = true;
        }

        return mRoot;
    }

    private void initControllerView(View v) {
        Resources res = mContext.getResources();
        mPlayDescription = res
                .getText(R.string.lockscreen_transport_play_description);
        mPauseDescription = res
                .getText(R.string.lockscreen_transport_pause_description);
        mPauseButton = v.findViewById(getPauseId());
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
            mPauseButton.setVisibility(getUsePause() ? View.VISIBLE : View.GONE);
        }

        mFfwdButton = v.findViewById(getFfwdId());
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
            mFfwdButton.setVisibility(getUseFastForward() ? View.VISIBLE : View.GONE);
        }

        mRewButton = v.findViewById(getRewId());
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
            mRewButton.setVisibility(getUseFastForward() ? View.VISIBLE : View.GONE);
        }

        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        mNextButton = v.findViewById(getNextId());
        if (mNextButton != null) {
            mNextButton.setVisibility(getUsePrevNext() ? View.VISIBLE : View.GONE);
        }

        mPrevButton = v.findViewById(getPrevId());
        if (mPrevButton != null) {
            mPrevButton.setVisibility(getUsePrevNext() ? View.VISIBLE : View.GONE);
        }

        mProgress = v.findViewById(getProgressId());
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            if (mProgress instanceof ProgressBar) {
                ((ProgressBar) mProgress).setMax(1000);
            }
            mProgress.setVisibility(getUseSeek() ? View.VISIBLE : View.GONE);
        }

        mEndTime = v.findViewById(getEndTimeId());
        if (mEndTime != null) {
            mEndTime.setVisibility(getUseTime() ? View.VISIBLE : View.GONE);
        }
        mCurrentTime = v.findViewById(getCurrentTimeId());
        if (mCurrentTime != null) {
            mCurrentTime.setVisibility(getUseTime() ? View.VISIBLE : View.GONE);
        }
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mExitButton = v.findViewById(getExitId());
        if (mExitButton != null) {
            mExitButton.setOnClickListener(mExitListener);
        }

        mFullScreenButton = v.findViewById(getFullScreenId());
        if (mFullScreenButton != null) {
            if (mFullScreenButton instanceof ImageView) {
                ((ImageView) mFullScreenButton).setImageResource(mIsFullScreen ? getScreenIconId() : getFullIconId());
            }
            mFullScreenButton.setOnClickListener(mFullScreenListener);
        }
        installPrevNextListeners();
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    @Override
    public void show() {
        show(getDefaultTimeout());
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mPauseButton != null && !mPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
            if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
            // TODO What we really should do is add a canSeek to the MediaPlayerControl interface;
            // this scheme can break the case when applications want to allow seek through the
            // progress bar but disable forward/backward buttons.
            //
            // However, currently the flags SEEK_BACKWARD_AVAILABLE, SEEK_FORWARD_AVAILABLE,
            // and SEEK_AVAILABLE are all (un)set together; as such the aforementioned issue
            // shouldn't arise in existing applications.
            if (mProgress != null && !mPlayer.canSeekBackward() && !mPlayer.canSeekForward()) {
                mProgress.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    @Override
    public void show(int timeout) {
        if (mRoot == null) {
            return;
        }
        if (!mShowing) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            mRoot.setVisibility(View.VISIBLE);
            mShowing = true;
        }
        updatePausePlay();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mRoot.post(mShowProgress);

        if (timeout != 0) {
            mRoot.removeCallbacks(mFadeOut);
            mRoot.postDelayed(mFadeOut, timeout);
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    @Override
    public void hide() {

        if (mShowing && mRoot != null) {
            try {
                mRoot.removeCallbacks(mShowProgress);
                mRoot.setVisibility(View.GONE);
            } catch (IllegalArgumentException ex) {
                Log.w("CCMediaController", "already removed");
            }
            mShowing = false;
        }
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mPlayer.isPlaying() && mRoot != null) {
                mRoot.postDelayed(mShowProgress, 1000 - (pos % 1000));
                updatePausePlay();
            }
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

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
                if (mProgress instanceof ProgressBar) {
                    ((ProgressBar) mProgress).setProgress((int) pos);
                }
            }
            int percent = mPlayer.getBufferPercentage();
            if (mProgress instanceof ProgressBar) {
                ((ProgressBar) mProgress).setSecondaryProgress(percent * 10);
            }
        }

        if (mEndTime != null && mEndTime instanceof TextView) {
            ((TextView) mEndTime).setText(stringForTime(duration));
        }
        if (mCurrentTime != null && mCurrentTime instanceof TextView) {
            ((TextView) mCurrentTime).setText(stringForTime(position));
        }

        return position;
    }

    /* 手指第一次触摸屏幕的X坐标 */
    private float mDownX;
    /* 手指第一次触摸屏幕的Y坐标 */
    private float mDownY;
    /* 是否需要快进快退的标志 */
    private boolean mNeedChangePosition;
    /* 是否需要调节音量的标志 */
    private boolean mNeedChangeVolume;
    /* 是否需要调节屏幕亮度的标志 */
    private boolean mNeedChangeBrightness;
    /* 规定在水平或垂直方向是移动的距离是否超过这个值 */
    private static final int THRESHOLD = 50;
    /* 在快进快退之前，记录上一次播放的位置 */
    private long mGestureDownPosition;
    /* 在调节亮度之前，记录上一次亮度的值 */
    private float mGestureDownBrightness;
    /* 在调节音量之前，记录上一次音量的值 */
    private int mGestureDownVolume;
    /* 快进快退需要seek的位置 */
    private int mNewPosition;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                mNeedChangePosition = false;
                mNeedChangeVolume = false;
                mNeedChangeBrightness = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                if (!mNeedChangePosition && !mNeedChangeVolume && !mNeedChangeBrightness) {
                    //只有播放、暂停、缓冲状态才能调节
                    if (!mPlayer.isPlaying()) {
                        return true;
                    }
                    if (absDeltaX >= THRESHOLD) {
                        //快进快退
                        mRoot.removeCallbacks(mShowProgress);
                        mNeedChangePosition = true;
                        mGestureDownPosition = mPlayer.getCurrentPosition();
                    } else if (absDeltaY >= THRESHOLD) {
                        if (mDownX < mVideoRoot.getWidth() * 0.5f) {
                            // 左侧改变亮度
                            mNeedChangeBrightness = true;
                            mGestureDownBrightness = mActivity.getWindow().getAttributes().screenBrightness;
                        } else {
                            // 右侧改变声音
                            mNeedChangeVolume = true;
                            mGestureDownVolume = mPlayer.getVolume();
                        }
                    }
                }
                if (mNeedChangePosition && getUseSeekByTouch()) {
                    int duration = mPlayer.getDuration();
                    if (duration != 0) {
                        int toPosition = (int) (mGestureDownPosition + duration * deltaX / mVideoRoot.getWidth());
                        mNewPosition = Math.max(0, Math.min(duration, toPosition));
                        int newPositionProgress = 1000 * mNewPosition / duration;
                        if (mCurrentTime instanceof TextView) {
                            ((TextView) mCurrentTime).setText(stringForTime(mNewPosition));
                        }
                        if (mProgress instanceof ProgressBar) {
                            ((ProgressBar) mProgress).setProgress(newPositionProgress);
                        }
                        show();
                        Log.d(TAG, "[Ciel_Debug] #onTouch()#: newPositionProgress: " + newPositionProgress);
                    }
                }
                if (mNeedChangeBrightness && getUseAdjustBrightness()) {
                    deltaY = -deltaY;
                    float deltaBrightness = deltaY * 3 / mVideoRoot.getHeight();
                    float newBrightness = mGestureDownBrightness + deltaBrightness;
                    newBrightness = Math.max(0, Math.min(newBrightness, 1));
                    float newBrightnessPercentage = newBrightness;
                    WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
                    params.screenBrightness = newBrightnessPercentage;
                    mActivity.getWindow().setAttributes(params);
                    int newBrightnessProgress = (int) (100f * newBrightnessPercentage);
                    hide();
                    Log.d(TAG, "[Ciel_Debug] #onTouch()#: newBrightnessPercentage: " + newBrightnessPercentage);
                }
                if (mNeedChangeVolume && getUseAdjustVolume()) {
                    deltaY = -deltaY;
                    int maxVolume = mPlayer.getMaxVolume();
                    int deltaVolume = (int) (maxVolume * deltaY * 3 / mVideoRoot.getHeight());
                    int newVolume = mGestureDownVolume + deltaVolume;
                    newVolume = Math.max(0, Math.min(maxVolume, newVolume));
                    mPlayer.setVolume(newVolume);
                    int newVolumeProgress = (int) (100f * newVolume / maxVolume);
                    hide();
                    Log.d(TAG, "[Ciel_Debug] #onTouch()#: newVolumeProgress: " + newVolumeProgress);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* 手指抬起后处理相关的动作 */
                if (mNeedChangePosition  && getUseSeekByTouch()) {
                    mNeedChangePosition = false;
                    mPlayer.seekTo(mNewPosition);
                    mRoot.post(mShowProgress);
                    return true;
                }
                if (mNeedChangeBrightness && getUseAdjustBrightness()) {
                    mNeedChangeBrightness = false;
                    return true;
                }
                if (mNeedChangeVolume && getUseAdjustVolume()) {
                    mNeedChangeVolume = false;
                    return true;
                }
                toggleMediaControlsVisiblity();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean getUseSeekByTouch() {
        return false;
    }

    @Override
    public boolean getUseAdjustVolume() {
        return false;
    }

    @Override
    public boolean getUseAdjustBrightness() {
        return false;
    }

    private void toggleMediaControlsVisiblity() {
        if (!mShowing) {
            show();
        } else {
            hide();
        }
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(getDefaultTimeout());
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(getDefaultTimeout());
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(getDefaultTimeout());
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(getDefaultTimeout());
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(getDefaultTimeout());
        return super.dispatchKeyEvent(event);
    }

    private final View.OnClickListener mPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doPauseResume();
            show(getDefaultTimeout());
        }
    };

    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null) {
            return;
        }
        int icPause = getPauseIconId();
        if (icPause <= 0) {
            icPause = android.R.drawable.ic_media_pause;
        }

        int icPlay = getPlayIconId();
        if (icPlay <= 0) {
            icPlay = android.R.drawable.ic_media_pause;
        }
        if (mPlayer.isPlaying()) {
            if (mPauseButton instanceof ImageView) {
                ((ImageView) mPauseButton).setImageResource(icPause);
                mPauseButton.setContentDescription(mPauseDescription);
            }
        } else {
            if (mPauseButton instanceof ImageView) {
                ((ImageView) mPauseButton).setImageResource(icPlay);
                mPauseButton.setContentDescription(mPlayDescription);
            }
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            if (mRoot != null) {
                mRoot.removeCallbacks(mShowProgress);
            }
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null && mCurrentTime instanceof TextView) {
                ((TextView) mCurrentTime).setText(stringForTime((int) newposition));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(getDefaultTimeout());

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            if (mRoot != null) {
                mRoot.post(mShowProgress);
            }
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    @Override
    public int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Override
    public boolean getCanRotateScreen() {
        return true;
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return MediaController.class.getName();
    }

    private final View.OnClickListener mRewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(getDefaultTimeout());
        }
    };

    private final View.OnClickListener mFfwdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos += 15000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(getDefaultTimeout());
        }
    };

    private final View.OnClickListener mExitListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleScreenOrExit();
        }
    };

    @Override
    public void toggleScreenOrExit() {
        if (mIsFullScreen) {
            toggleFullScreen();
        } else {
            if (mActivity != null) {
                mActivity.finish();
            }
        }
    }

    private final View.OnClickListener mFullScreenListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleFullScreen();
        }
    };

    @Override
    public void toggleFullScreen() {
        if (!getCanRotateScreen()) {
            mIsFullScreen = !mIsFullScreen;
        } else if (mActivity != null) {
            mIsFullScreen = ScreenUtil.toggleFullScreen(mActivity);
        }
        adjustVideoViewSize(mIsFullScreen);
        if (mFullScreenButton instanceof ImageView) {
            ((ImageView) mFullScreenButton).setImageResource(mIsFullScreen ? getScreenIconId() : getFullIconId());
        }
    }

    @Override
    public void adjustVideoViewSize(boolean isFullScreen) {
        if (isFullScreen) {
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mVideoOldWidth = mVideoRoot.getLayoutParams().width;
            mVideoOldHeight = mVideoRoot.getLayoutParams().height;
            mVideoRoot.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoRoot.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mVideoRoot.getLayoutParams().width = mVideoOldWidth;
            mVideoRoot.getLayoutParams().height = mVideoOldHeight;
        }
    }

    private void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mNextListener != null);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mPrevListener != null);
        }
    }

    private View.OnClickListener mPrevListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPlayer != null) {
                mPlayer.previous();
            }
        }
    };

    private View.OnClickListener mNextListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPlayer != null) {
                mPlayer.next();
            }
        }
    };

    @Override
    public void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;

        if (mRoot != null) {
            installPrevNextListeners();

            if (mNextButton != null) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            if (mPrevButton != null) {
                mPrevButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
