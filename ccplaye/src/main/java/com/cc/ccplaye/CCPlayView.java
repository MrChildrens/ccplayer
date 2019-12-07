package com.cc.ccplaye;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * @author: Ciel
 * @date: 2019/12/5 17:26
 */
public class CCPlayView extends FrameLayout {

    private Context mContext;
    private SurfaceView mVideoView;

    public CCPlayView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CCPlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CCPlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CCPlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.cc_playview, this, true);
    }


}
