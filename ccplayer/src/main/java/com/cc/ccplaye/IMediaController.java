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

    void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev);

    /**
     * 控制条自动消失时间
     *
     * @return
     */
    int getDefaultTimeout();

    /**
     * 控制是否能旋转屏幕
     *
     * @return
     */
    boolean getCanRotateScreen();

    /**
     * 控制是否使用播放暂停功能
     *
     * @return
     */
    boolean getUsePause();

    /**
     * 控制是否使用快进和快退功能
     *
     * @return
     */
    boolean getUseFastForward();

    /**
     * 控制是否使用上一集和下一集功能
     *
     * @return
     */
    boolean getUsePrevNext();

    /**
     * 控制是否显示当前播放时间
     *
     * @return
     */
    boolean getUseTime();

    /**
     * 控制是否能在进度条上Seek
     *
     * @return
     */
    boolean getUseSeek();

    /**
     * 控制是否能用手指左右滑动去快进和快退
     *
     * @return
     */
    boolean getUseSeekByTouch();

    /**
     * 控制是否能用手指滑动去调节音量
     *
     * @return
     */
    boolean getUseAdjustVolume();

    /**
     * 控制是否能用手指滑动去调节亮度
     *
     * @return
     */
    boolean getUseAdjustBrightness();

    /**
     * 切换全屏或窗口
     */
    void toggleFullScreen();

    /**
     * 根据是否全屏调整播放器布局大小
     *
     * @param isFullScreen
     */
    void adjustVideoViewSize(boolean isFullScreen);

    /**
     * 用于退出按钮的点击处理，退出全屏或退出界面
     */
    void toggleScreenOrExit();

    interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        int getVolume();

        int getMaxVolume();

        void setVolume(int volume);

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

        /**
         * 切换下一集
         *
         * @return
         */
        boolean next();

        /**
         * 切换下一集
         *
         * @return
         */
        boolean previous();

        /**
         * 可调节视频显示的比例
         *
         * @param aspectRaito
         */
        void changeAspectRatio(int aspectRaito);
    }
}
