package com.cc.ccplaye;

import android.view.View;
import android.widget.FrameLayout;

/**
 * @author: Ciel
 * @date: 2019/12/6 13:25
 */
public interface IMediaController {
    void setMediaPlayer(MediaPlayerControl player);

    void setAnchorView(FrameLayout view);

    void show();

    void show(int timeout);

    boolean isShowing();

    void hide();

    void setEnabled(boolean enabled);

    int getDefaultTimeout();

    boolean getCanRotateScreen();

    boolean getUsePause();

    boolean getUseFastForward();

    boolean getUsePrevNext();

    boolean getUseTime();

    boolean getUseSeek();

    void toggleFullScreen();

    void adjustVideoViewSize(boolean isFullScreen);

    void toggleScreenOrExit();

    void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev);

    interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        /**
         * Get the audio session id for the player used by this VideoView. This can be used to
         * apply audio effects to the audio track of a video.
         *
         * @return The audio session, or 0 if there was an error.
         */
        int getAudioSessionId();

        boolean next();

        boolean previous();

        void changeAspectRatio(int aspectRaito);
    }
}
