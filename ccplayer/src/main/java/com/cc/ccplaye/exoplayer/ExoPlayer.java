package com.cc.ccplaye.exoplayer;

import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;

import com.cc.ccplaye.BaseMediaPlayer;
import com.cc.ccplaye.IMediaPlayer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author: Ciel
 * @date: 2019/12/14 23:08
 */
public class ExoPlayer extends BaseMediaPlayer {

    private static final String TAG = "ExoPlayer";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private Context mContext;
    private Handler mMainHandler = null;
    private EventLogger mEventLogger = null;

    private SimpleExoPlayer mMediaPlayer;
    private DataSource.Factory mDataSourceFactory;
    private MediaSource mMediaSource;

    private boolean mIsPrepared;

    public ExoPlayer(Context context) {
        mContext = context;
        mMainHandler = new MyHandler();

        mMediaPlayer = ExoPlayerFactory.newSimpleInstance(context);
        mMediaPlayer.addListener(mPlayerEventListener);
        mMediaPlayer.addVideoListener(mVideoListener);
        mMediaPlayer.addAudioListener(mAudioListener);
        mMediaPlayer.addMetadataOutput(mMetadataOutputListener);
        mMediaPlayer.addTextOutput(mTextOutputListener);
    }

    private static class MyHandler extends Handler {

    }

    private Player.EventListener mPlayerEventListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
            Log.d(TAG, "[Ciel_Debug] #onTimelineChanged()#: timeline: " + timeline + ", manifest: " + manifest + ", reason: " + reason);
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray
                trackSelections) {
            Log.d(TAG, "[Ciel_Debug] #onTracksChanged()#: trackGroups: " + trackGroups + ", trackSelections: " + trackSelections);
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.d(TAG, "[Ciel_Debug] #onPlayerStateChanged()#: playWhenReady: " + playWhenReady);
            switch (playbackState) {
                case Player.STATE_IDLE:
                    Log.d(TAG, "[Ciel_Debug] #onPlayerStateChanged()#: STATE_IDLE");
                    break;
                case Player.STATE_BUFFERING:
                    Log.d(TAG, "[Ciel_Debug] #onPlayerStateChanged()#: STATE_BUFFERING");
                    int percent = mMediaPlayer.getBufferedPercentage();
                    notifyOnBufferingUpdate(percent);
                    notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_START, percent);
                    break;
                case Player.STATE_READY:
                    Log.d(TAG, "[Ciel_Debug] #onPlayerStateChanged()#: STATE_READY");
                    notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_END, 100);
                    if (!mIsPrepared) {
                        notifyOnPrepared();
                        mIsPrepared = true;
                    }
                    break;
                case Player.STATE_ENDED:
                    Log.d(TAG, "[Ciel_Debug] #onPlayerStateChanged()#: STATE_ENDED");
                    notifyOnCompletion();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Log.d(TAG, "[Ciel_Debug] #onRepeatModeChanged()#: repeatMode: " + repeatMode);
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Log.d(TAG, "[Ciel_Debug] #onShuffleModeEnabledChanged()#: shuffleModeEnabled: " + shuffleModeEnabled);
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d(TAG, "[Ciel_Debug] #onPlayerError()#: error: " + error);
            notifyOnError(IMediaPlayer.MEDIA_ERROR_UNKNOWN, IMediaPlayer.MEDIA_ERROR_UNKNOWN);
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.d(TAG, "[Ciel_Debug] #onPositionDiscontinuity()#: reason: " + reason);
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.d(TAG, "[Ciel_Debug] #onPlaybackParametersChanged()#: playbackParameters: " + playbackParameters);
        }

        @Override
        public void onSeekProcessed() {
            Log.d(TAG, "[Ciel_Debug] #onSeekProcessed()#: ");
        }
    };

    private VideoListener mVideoListener = new VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            Log.d(TAG, "[Ciel_Debug] #onVideoSizeChanged()#: width: " + width + ", height: " + height + ", unappliedRotationDegrees: " + unappliedRotationDegrees + ", pixelWidthHeightRatio: " + pixelWidthHeightRatio);
            notifyOnVideoSizeChanged(width, height);
        }

        @Override
        public void onSurfaceSizeChanged(int width, int height) {
            Log.d(TAG, "[Ciel_Debug] #onSurfaceSizeChanged()#: width: " + width + ", height: " + height);
        }

        @Override
        public void onRenderedFirstFrame() {
            Log.d(TAG, "[Ciel_Debug] #onRenderedFirstFrame()#: ");
        }
    };

    private AudioListener mAudioListener = new AudioListener() {
        @Override
        public void onAudioSessionId(int audioSessionId) {
            Log.d(TAG, "[Ciel_Debug] #onAudioSessionId()#: audioSessionId: " + audioSessionId);
        }

        @Override
        public void onAudioAttributesChanged(com.google.android.exoplayer2.audio.AudioAttributes audioAttributes) {
            Log.d(TAG, "[Ciel_Debug] #onAudioAttributesChanged()#: audioAttributes: " + audioAttributes);
        }

        @Override
        public void onVolumeChanged(float volume) {
            Log.d(TAG, "[Ciel_Debug] #onVolumeChanged()#: volume: " + volume);
        }
    };

    private MetadataOutput mMetadataOutputListener = new MetadataOutput() {
        @Override
        public void onMetadata(Metadata metadata) {
            Log.d(TAG, "[Ciel_Debug] #onMetadata()#: metadata: " + metadata);
        }
    };

    private TextOutput mTextOutputListener = new TextOutput() {
        @Override
        public void onCues(List<Cue> cues) {
            Log.d(TAG, "[Ciel_Debug] #onCues()#: cues: " + cues);
        }
    };

    @Override
    public void setDisplay(SurfaceHolder sh) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVideoSurfaceHolder(sh);
        }
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "yourApplicationName"));
        // This is the MediaSource representing the media to be played.
//        mMediaSource = new ProgressiveMediaSource.Factory(mDataSourceFactory)
//                .createMediaSource(uri);
        mMediaSource = buildMediaSource(uri, null);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        // Prepare the player with the source.
        if (mMediaPlayer != null) {
            mMediaPlayer.prepare(mMediaSource);
        }
    }

    @Override
    public void start() throws IllegalStateException {
        if (mMediaPlayer != null && !mMediaPlayer.getPlayWhenReady()) {
            mMediaPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop(true);
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        if (mMediaPlayer != null && mMediaPlayer.getPlayWhenReady()) {
            mMediaPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {

    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer == null) {
            return false;
        }
        int state = mMediaPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                return mMediaPlayer.getPlayWhenReady();
            case Player.STATE_IDLE:
            case Player.STATE_ENDED:
            default:
                return false;
        }
    }

    @Override
    public int getVideoWidth() {
        if (mMediaPlayer == null) {
            return 0;
        }
        return mMediaPlayer.getVideoFormat().width;
    }

    @Override
    public int getVideoHeight() {
        if (mMediaPlayer == null) {
            return 0;
        }
        return mMediaPlayer.getVideoFormat().height;
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(msec);
        }
    }

    @Override
    public int getCurrentPosition() {
        if (mMediaPlayer == null) {
            return 0;
        }
        return (int) mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        if (mMediaPlayer == null) {
            return 0;
        }
        return (int) mMediaPlayer.getDuration();
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer.removeListener(mPlayerEventListener);
            mMediaPlayer.removeVideoListener(mVideoListener);
            mMediaPlayer.removeAudioListener(mAudioListener);
            mMediaPlayer.removeMetadataOutput(mMetadataOutputListener);
            mMediaPlayer.removeTextOutput(mTextOutputListener);
            mMediaPlayer = null;
        }
        mDataSourceFactory = null;
        mMediaSource = null;
    }

    @Override
    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {

    }

    @Override
    public void setAudioSessionId(int sessionId) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public int getAudioSessionId() {
        if (mMediaPlayer == null) {
            return 0;
        }
        return mMediaPlayer.getAudioSessionId();
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        Log.d(TAG, "[Ciel_Debug] #buildMediaSource()#: " + type);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
//        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
//                : Util.inferContentType("." + overrideExtension);
//        switch (type) {
//            case C.TYPE_SS:
//                return new SsMediaSource(uri, buildDataSourceFactory(),
//                        new DefaultSsChunkSource.Factory(mDataSourceFactory), mMainHandler, mEventLogger);
//            case C.TYPE_DASH:
//                return new DashMediaSource(uri, buildDataSourceFactory(),
//                        new DefaultDashChunkSource.Factory(mDataSourceFactory), mMainHandler, mEventLogger);
//            case C.TYPE_HLS:
//                return new HlsMediaSource(uri, mDataSourceFactory, mMainHandler, mEventLogger);
//            case C.TYPE_OTHER:
//                return new ExtractorMediaSource(uri, mDataSourceFactory, new DefaultExtractorsFactory(),
//                        mMainHandler, mEventLogger);
//            default: {
//                throw new IllegalStateException("Unsupported type: " + type);
//            }
//        }
    }

    private DataSource.Factory buildDataSourceFactory() {
        return new DefaultDataSourceFactory(mContext, BANDWIDTH_METER,
                buildHttpDataSourceFactory(BANDWIDTH_METER));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(mContext, "CC-Player"), bandwidthMeter);
    }
}
