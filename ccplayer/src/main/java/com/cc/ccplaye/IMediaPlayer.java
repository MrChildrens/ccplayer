package com.cc.ccplaye;

import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Map;

/**
 * @author: Ciel
 * @date: 2019/12/5 18:38
 */
public interface IMediaPlayer {

    // all possible internal states
    int STATE_ERROR = -1;
    int STATE_IDLE = 0;
    int STATE_PREPARING = 1;
    int STATE_PREPARED = 2;
    int STATE_PLAYING = 3;
    int STATE_PAUSED = 4;
    int STATE_PLAYBACK_COMPLETED = 5;

    int AR_16_9 = 0;
    int AR_4_3 = 1;
    int AR_ORIGINAL_RATIO = 2;
    int AR_FULL_SCREEN = 3;
    int AR_AUTO = 4;

    void setDisplay(SurfaceHolder sh);

    void setDataSource(Context context, Uri uri,
                       Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void prepareAsync() throws IllegalStateException;

    void start() throws IllegalStateException;

    void stop() throws IllegalStateException;

    void pause() throws IllegalStateException;


    void setScreenOnWhilePlaying(boolean screenOn);

    boolean isPlaying();

    int getVideoWidth();

    int getVideoHeight();

    void seekTo(int msec) throws IllegalStateException;

    int getCurrentPosition();

    int getDuration();

    void release();

    void reset();

    void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException;

    void setAudioSessionId(int sessionId) throws IllegalArgumentException, IllegalStateException;

    int getAudioSessionId();

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    interface OnPreparedListener {
        /**
         * Called when the media file is ready for playback.
         *
         * @param mp the MediaPlayer that is ready for playback
         */
        void onPrepared(IMediaPlayer mp);
    }

    /**
     * Interface definition of a callback to be invoked when the
     * video size is first known or updated
     */
    interface OnVideoSizeChangedListener {
        /**
         * Called to indicate the video size
         * <p>
         * The video size (width and height) could be 0 if there was no video,
         * no display surface was set, or the value was not determined yet.
         *
         * @param mp     the MediaPlayer associated with this callback
         * @param width  the width of the video
         * @param height the height of the video
         */
        void onVideoSizeChanged(IMediaPlayer mp, int width, int height);
    }

    /**
     * 3708
     * Interface definition for a callback to be invoked when playback of
     * a media source has completed.
     */
    interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         *
         * @param mp the MediaPlayer that reached the end of the file
         */
        void onCompletion(IMediaPlayer mp);
    }

    int MEDIA_ERROR_UNKNOWN = 1;
    int MEDIA_ERROR_SERVER_DIED = 100;
    int MEDIA_ERROR_IO = -1004;
    int MEDIA_ERROR_MALFORMED = -1007;
    int MEDIA_ERROR_UNSUPPORTED = -1010;
    int MEDIA_ERROR_TIMED_OUT = -110;

    /**
     * 4105
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation (other errors
     * will throw exceptions at method call time).
     */
    interface OnErrorListener {
        /**
         * Called to indicate an error.
         *
         * @param mp    the MediaPlayer the error pertains to
         * @param what  the type of error that has occurred:
         *              <ul>
         *              <li>{@link #MEDIA_ERROR_UNKNOWN}
         *              <li>{@link #MEDIA_ERROR_SERVER_DIED}
         *              </ul>
         * @param extra an extra code, specific to the error. Typically
         *              implementation dependent.
         *              <ul>
         *              <li>{@link #MEDIA_ERROR_IO}
         *              <li>{@link #MEDIA_ERROR_MALFORMED}
         *              <li>{@link #MEDIA_ERROR_UNSUPPORTED}
         *              <li>{@link #MEDIA_ERROR_TIMED_OUT}
         *              <li><code>MEDIA_ERROR_SYSTEM (-2147483648)</code> - low-level system error.
         *              </ul>
         * @return True if the method handled the error, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the OnCompletionListener to be called.
         */
        boolean onError(IMediaPlayer mp, int what, int extra);
    }

    int MEDIA_INFO_UNKNOWN = 1;
    int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    int MEDIA_INFO_BUFFERING_START = 701;
    int MEDIA_INFO_BUFFERING_END = 702;
    int MEDIA_INFO_BAD_INTERLEAVING = 800;
    int MEDIA_INFO_NOT_SEEKABLE = 801;
    int MEDIA_INFO_METADATA_UPDATE = 802;
    int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;

    /**
     * Interface definition of a callback to be invoked to communicate some
     * info and/or warning about the media or its playback.
     */
    interface OnInfoListener {
        /**
         * Called to indicate an info or a warning.
         *
         * @param mp    the MediaPlayer the info pertains to.
         * @param what  the type of info or warning.
         *              <ul>
         *              <li>{@link #MEDIA_INFO_UNKNOWN}
         *              <li>{@link #MEDIA_INFO_VIDEO_TRACK_LAGGING}
         *              <li>{@link #MEDIA_INFO_VIDEO_RENDERING_START}
         *              <li>{@link #MEDIA_INFO_BUFFERING_START}
         *              <li>{@link #MEDIA_INFO_BUFFERING_END}
         *              <li><code>MEDIA_INFO_NETWORK_BANDWIDTH (703)</code> -
         *              bandwidth information is available (as <code>extra</code> kbps)
         *              <li>{@link #MEDIA_INFO_BAD_INTERLEAVING}
         *              <li>{@link #MEDIA_INFO_NOT_SEEKABLE}
         *              <li>{@link #MEDIA_INFO_METADATA_UPDATE}
         *              <li>{@link #MEDIA_INFO_UNSUPPORTED_SUBTITLE}
         *              <li>{@link #MEDIA_INFO_SUBTITLE_TIMED_OUT}
         *              </ul>
         * @param extra an extra code, specific to the info. Typically
         *              implementation dependent.
         * @return True if the method handled the info, false if it didn't.
         * Returning false, or not having an OnInfoListener at all, will
         * cause the info to be discarded.
         */
        boolean onInfo(IMediaPlayer mp, int what, int extra);
    }

    /**
     * Interface definition of a callback to be invoked indicating buffering
     * status of a media resource being streamed over the network.
     */
    interface OnBufferingUpdateListener {
        /**
         * Called to update status in buffering a media stream received through
         * progressive HTTP download. The received buffering percentage
         * indicates how much of the content has been buffered or played.
         * For example a buffering update of 80 percent when half the content
         * has already been played indicates that the next 30 percent of the
         * content to play has been buffered.
         *
         * @param mp      the MediaPlayer the update pertains to
         * @param percent the percentage (0-100) of the content
         *                that has been buffered or played thus far
         */
        void onBufferingUpdate(IMediaPlayer mp, int percent);
    }

    void setOnPreparedListener(OnPreparedListener listener);

    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

    void setOnCompletionListener(OnCompletionListener listener);

    void setOnErrorListener(OnErrorListener listener);

    void setOnInfoListener(OnInfoListener listener);

    void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

}
