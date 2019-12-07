package com.cc.ccplaye.ijkplayer;

import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.text.TextUtils;
import android.view.SurfaceHolder;

import com.cc.ccplaye.BaseMediaPlayer;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @author: Ciel
 * @date: 2019/12/6 09:44
 */
public class IjkPlayer extends BaseMediaPlayer {

    private IjkMediaPlayer mIjkMediaPlayer;
    private Settings mSettings;

    public IjkPlayer(Context context) {
        mIjkMediaPlayer = new IjkMediaPlayer();
        mSettings = new Settings(context);
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);

        if (mSettings.getUsingMediaCodec()) {
            mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            if (mSettings.getUsingMediaCodecAutoRotate()) {
                mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
            } else {
                mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
            }
            if (mSettings.getMediaCodecHandleResolutionChange()) {
                mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            } else {
                mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);
            }
        } else {
            mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
        }

        if (mSettings.getUsingOpenSLES()) {
            mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
        } else {
            mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
        }

        String pixelFormat = mSettings.getPixelFormat();
        if (TextUtils.isEmpty(pixelFormat)) {
            mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        } else {
            mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", pixelFormat);
        }
        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);

        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

        mIjkMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                notifyOnPrepared();
            }
        });

        mIjkMediaPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                notifyOnVideoSizeChanged(width, height);
            }
        });

        mIjkMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                notifyOnCompletion();
            }
        });

        mIjkMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                notifyOnError(what, extra);
                return false;
            }
        });

        mIjkMediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                notifyOnInfo(what, extra);
                return false;
            }
        });

        mIjkMediaPlayer.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                notifyOnBufferingUpdate(percent);
            }
        });
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        mIjkMediaPlayer.setDisplay(sh);
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mIjkMediaPlayer.setDataSource(context, uri, headers);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mIjkMediaPlayer.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        mIjkMediaPlayer.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        mIjkMediaPlayer.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        mIjkMediaPlayer.pause();
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        mIjkMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    @Override
    public boolean isPlaying() {
        return mIjkMediaPlayer.isPlaying();
    }

    @Override
    public int getVideoWidth() {
        return mIjkMediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mIjkMediaPlayer.getVideoHeight();
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        mIjkMediaPlayer.seekTo(msec);
    }

    @Override
    public int getCurrentPosition() {
        return (int) mIjkMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return (int) mIjkMediaPlayer.getDuration();
    }

    @Override
    public void release() {
        mIjkMediaPlayer.release();
    }

    @Override
    public void reset() {
        mIjkMediaPlayer.reset();
    }

    @Override
    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {

    }

    @Override
    public void setAudioSessionId(int sessionId) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public int getAudioSessionId() {
        return mIjkMediaPlayer.getAudioSessionId();
    }
}
