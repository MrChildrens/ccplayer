package com.cc.ccplaye;

/**
 * @author: Ciel
 * @date: 2019/12/5 17:42
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;

import androidx.annotation.NonNull;

import com.cc.ccplaye.ijkplayer.IjkPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author: Ciel
 * @date: 2019/12/5
 */

public class CCVideoView extends FrameLayout
        implements MediaController.MediaPlayerControl {
    private static final String TAG = CCVideoView.class.getSimpleName()
            ;

    // settable by the client
    private Uri mUri;
    private Map<String, String> mHeaders;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = IMediaPlayer.STATE_IDLE;
    private int mTargetState = IMediaPlayer.STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    private IMediaPlayer mMediaPlayer = null;
    private int mAudioSession;

    private int mVideoWidth;

    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private SurfaceRenderView mSurfaceView;
    private MediaController mMediaController;

    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;

    private int mCurrentBufferPercentage;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    private AudioManager mAudioManager;
    private int mAudioFocusType = AudioManager.AUDIOFOCUS_GAIN; // legacy focus gain
    private AudioAttributes mAudioAttributes;

    private Context mContext;

    public CCVideoView(Context context) {
        this(context, null);
    }

    public CCVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CCVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CCVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        mVideoWidth = 0;
        mVideoHeight = 0;

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();

//        LayoutInflater.from(mContext).inflate(R.layout.cc_playview, this, true);
//        mSurfaceView = findViewById(R.id.cc_surfaceview);

        mSurfaceView = new SurfaceRenderView(mContext);
        mSurfaceView.getHolder().addCallback(mSHCallback);
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        mSurfaceView.setLayoutParams(lp);
        addView(mSurfaceView);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        mCurrentState = IMediaPlayer.STATE_IDLE;
        mTargetState = IMediaPlayer.STATE_IDLE;
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return CCVideoView.class.getName();
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    /**
     * Sets which type of audio focus will be requested during the playback, or configures playback
     * to not request audio focus. Valid values for focus requests are
     * {@link AudioManager#AUDIOFOCUS_GAIN}, {@link AudioManager#AUDIOFOCUS_GAIN_TRANSIENT},
     * {@link AudioManager#AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK}, and
     * {@link AudioManager#AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE}. Or use
     * {@link AudioManager#AUDIOFOCUS_NONE} to express that audio focus should not be
     * requested when playback starts. You can for instance use this when playing a silent animation
     * through this class, and you don't want to affect other audio applications playing in the
     * background.
     *
     * @param focusGain the type of audio focus gain that will be requested, or
     *                  {@link AudioManager#AUDIOFOCUS_NONE} to disable the use audio focus during playback.
     */
    public void setAudioFocusRequest(int focusGain) {
        if (focusGain != AudioManager.AUDIOFOCUS_NONE
                && focusGain != AudioManager.AUDIOFOCUS_GAIN
                && focusGain != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                && focusGain != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                && focusGain != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) {
            throw new IllegalArgumentException("Illegal audio focus type " + focusGain);
        }
        mAudioFocusType = focusGain;
    }

    /**
     * Sets the {@link AudioAttributes} to be used during the playback of the video.
     *
     * @param attributes non-null <code>AudioAttributes</code>.
     */
    public void setAudioAttributes(@NonNull AudioAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Illegal null AudioAttributes");
        }
        mAudioAttributes = attributes;
    }

    /**
     * Adds an external subtitle source file (from the provided input stream.)
     * <p>
     * Note that a single external subtitle source may contain multiple or no
     * supported tracks in it. If the source contained at least one track in
     * it, one will receive an {@link MediaPlayer#MEDIA_INFO_METADATA_UPDATE}
     * info message. Otherwise, if reading the source takes excessive time,
     * one will receive a {@link MediaPlayer#MEDIA_INFO_SUBTITLE_TIMED_OUT}
     * message. If the source contained no supported track (including an empty
     * source file or null input stream), one will receive a {@link
     * MediaPlayer#MEDIA_INFO_UNSUPPORTED_SUBTITLE} message. One can find the
     * total number of available tracks using {@link MediaPlayer#getTrackInfo()}
     * to see what additional tracks become available after this method call.
     *
     * @param is     input stream containing the subtitle data.  It will be
     *               closed by the media framework.
     * @param format the format of the subtitle track(s).  Must contain at least
     *               the mime type ({@link MediaFormat#KEY_MIME}) and the
     *               language ({@link MediaFormat#KEY_LANGUAGE}) of the file.
     *               If the file itself contains the language information,
     *               specify "und" for the language.
     */
    public void addSubtitleSource(InputStream is, MediaFormat format) {
//        if (mMediaPlayer == null) {
//            mPendingSubtitleTracks.add(Pair.create(is, format));
//        } else {
//            try {
//                mMediaPlayer.addSubtitleSource(is, format);
//            } catch (IllegalStateException e) {
//                mInfoListener.onInfo(
//                        mMediaPlayer, MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE, 0);
//            }
//        }
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = IMediaPlayer.STATE_IDLE;
            mTargetState = IMediaPlayer.STATE_IDLE;
            mAudioManager.abandonAudioFocus(null);
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);

        if (mAudioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            // TODO this should have a focus listener
//            mAudioManager.requestAudioFocus(null, mAudioAttributes, mAudioFocusType, 0 /*flags*/);
        }

        try {
//            mMediaPlayer = new AndroidMediaPlayer();
            mMediaPlayer = new IjkPlayer(mContext);

            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioAttributes(mAudioAttributes);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = IMediaPlayer.STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = IMediaPlayer.STATE_ERROR;
            mTargetState = IMediaPlayer.STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = IMediaPlayer.STATE_ERROR;
            mTargetState = IMediaPlayer.STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } finally {

        }
    }

    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            mMediaController.setAnchorView(this);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height) {
                    Log.d(TAG, "onVideoSizeChanged: width: " + width + ", height: " + height);
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        if (mSurfaceView != null) {
                            mSurfaceView.setVideoSize(mVideoWidth, mVideoHeight);
                        }
                        requestLayout();
                    }
                }
            };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            mCurrentState = IMediaPlayer.STATE_PREPARED;

            // Get the capabilities of the player for this stream
//            Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
//                    MediaPlayer.BYPASS_METADATA_FILTER);
//
//            if (data != null) {
//                mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
//                        || data.getBoolean(Metadata.PAUSE_AVAILABLE);
//                mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
//                        || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
//                mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
//                        || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
//            } else {
            mCanPause = mCanSeekBack = mCanSeekForward = true;
//            }

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
//                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                Log.d(TAG, "onPrepared: suficaeWidth: " + mSurfaceWidth + ", surfaceHeight: " + mSurfaceHeight);
                Log.d(TAG, "onPrepared: mVideoWidth: " + mVideoWidth + ", mVideoHeight: " + mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the video here instead of in the callback.
                    if (mTargetState == IMediaPlayer.STATE_PLAYING) {
                        start();
                        if (mMediaController != null) {
                            Log.d(TAG, "onPrepared: isshow");
                            mMediaController.show();
                        }
                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null) {
                            // Show the media controls when we're paused into a video and make 'em stick.
                            Log.d(TAG, "onPrepared: isshow");
                            mMediaController.show(0);
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == IMediaPlayer.STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {
                    mCurrentState = IMediaPlayer.STATE_PLAYBACK_COMPLETED;
                    mTargetState = IMediaPlayer.STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                    if (mAudioFocusType != AudioManager.AUDIOFOCUS_NONE) {
                        mAudioManager.abandonAudioFocus(null);
                    }
                }
            };

    private IMediaPlayer.OnInfoListener mInfoListener =
            new IMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, arg1, arg2);
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = IMediaPlayer.STATE_ERROR;
                    mTargetState = IMediaPlayer.STATE_ERROR;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    /* Otherwise, pop up an error dialog so the user knows that
                     * something bad has happened. Only try and pop up the dialog
                     * if we're attached to a window. When we're going away and no
                     * longer have a window, don't bother showing the user an error.
                     */
                    if (getWindowToken() != null) {
                        Resources r = mContext.getResources();
                        int messageId;

                        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                            messageId = R.string.VideoView_error_text_invalid_progressive_playback;
                        } else {
                            messageId = R.string.VideoView_error_text_unknown;
                        }

                        new AlertDialog.Builder(mContext)
                                .setMessage(messageId)
                                .setPositiveButton(R.string.VideoView_error_button,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                /* If we get here, there is no onError listener, so
                                                 * at least inform them that the video is over.
                                                 */
                                                if (mOnCompletionListener != null) {
                                                    mOnCompletionListener.onCompletion(mMediaPlayer);
                                                }
                                            }
                                        })
                                .setCancelable(false)
                                .show();
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int w, int h) {
            Log.d(TAG, "surfaceChanged: suficaeWidth: " + w + ", surfaceHeight: " + h);
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == IMediaPlayer.STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            openVideo();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaController != null) {
                mMediaController.hide();
            }
            release(true);
        }
    };

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = IMediaPlayer.STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = IMediaPlayer.STATE_IDLE;
            }
            if (mAudioFocusType != AudioManager.AUDIOFOCUS_NONE) {
                mAudioManager.abandonAudioFocus(null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN
                && isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN
                && isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return super.onTrackballEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = IMediaPlayer.STATE_PLAYING;
        }
        mTargetState = IMediaPlayer.STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = IMediaPlayer.STATE_PAUSED;
            }
        }
        mTargetState = IMediaPlayer.STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != IMediaPlayer.STATE_ERROR &&
                mCurrentState != IMediaPlayer.STATE_IDLE &&
                mCurrentState != IMediaPlayer.STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

}
