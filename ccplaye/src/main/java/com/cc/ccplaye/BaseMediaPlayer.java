package com.cc.ccplaye;

public abstract class BaseMediaPlayer implements IMediaPlayer {

    private OnPreparedListener mOnPreparedListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;

    @Override
    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        mOnPreparedListener = onPreparedListener;
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        mOnVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        mOnCompletionListener = onCompletionListener;
    }

    @Override
    public void setOnInfoListener(OnInfoListener onInfoListener) {
        mOnInfoListener = onInfoListener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener onErrorListener) {
        mOnErrorListener = onErrorListener;
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener onBufferingUpdateListener) {
        mOnBufferingUpdateListener = onBufferingUpdateListener;
    }

    protected final void notifyOnPrepared() {
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(this);
        }
    }

    protected final void notifyOnVideoSizeChanged(int width, int height) {
        if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener.onVideoSizeChanged(this, width, height);
        }
    }

    protected final void notifyOnCompletion() {
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(this);
        }
    }

    protected final boolean notifyOnError(int what, int extra) {
        return mOnErrorListener != null && mOnErrorListener.onError(this, what, extra);
    }

    protected final boolean notifyOnInfo(int what, int extra) {
        return mOnInfoListener != null && mOnInfoListener.onInfo(this, what, extra);
    }

    protected final void notifyOnBufferingUpdate(int percent) {
        if (mOnBufferingUpdateListener != null) {
            mOnBufferingUpdateListener.onBufferingUpdate(this, percent);
        }
    }

}
