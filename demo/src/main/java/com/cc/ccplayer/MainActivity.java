package com.cc.ccplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.cc.ccplaye.IMediaController;
import com.cc.ccplaye.IMediaPlayer;
import com.cc.ccplaye.VideoView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String VIDEO_URL_01 = "http://jiajunhui.cn/video/kaipao.mp4";
    public static final String VIDEO_URL_02 = "http://jiajunhui.cn/video/kongchengji.mp4";
    public static final String VIDEO_URL_03 = "http://jiajunhui.cn/video/allsharestar.mp4";
    public static final String VIDEO_URL_04 = "http://jiajunhui.cn/video/edwin_rolling_in_the_deep.flv";
    public static final String VIDEO_URL_05 = "http://jiajunhui.cn/video/crystalliz.flv";
    public static final String VIDEO_URL_06 = "http://jiajunhui.cn/video/big_buck_bunny.mp4";
    public static final String VIDEO_URL_07 = "http://jiajunhui.cn/video/trailer.mp4";
    public static final String VIDEO_URL_08 = "https://mov.bn.netease.com/open-movie/nos/mp4/2017/12/04/SD3SUEFFQ_hd.mp4";
    public static final String VIDEO_URL_09 = "https://mov.bn.netease.com/open-movie/nos/mp4/2017/05/31/SCKR8V6E9_hd.mp4";

    private VideoView mVideoView;
    private RecyclerView mRecyclerView;
    private IMediaController mIMediaController;
    private Path[] mData = new Path[]{
            new Path("你欠缺的也许并不是能力", "https://mov.bn.netease.com/open-movie/nos/mp4/2016/06/22/SBP8G92E3_hd.mp4"),
            new Path("坚持与放弃", "https://mov.bn.netease.com/open-movie/nos/mp4/2015/08/27/SB13F5AGJ_sd.mp4"),
            new Path("不想从被子里出来", "https://mov.bn.netease.com/open-movie/nos/mp4/2018/01/12/SD70VQJ74_sd.mp4"),
            new Path("不耐烦的中国人?", "https://mov.bn.netease.com/open-movie/nos/mp4/2017/05/31/SCKR8V6E9_hd.mp4"),
            new Path("神奇的珊瑚", "https://mov.bn.netease.com/open-movie/nos/mp4/2016/01/11/SBC46Q9DV_hd.mp4"),
            new Path("怎样经营你的人脉", "https://mov.bn.netease.com/open-movie/nos/mp4/2018/04/19/SDEQS1GO6_hd.mp4"),
            new Path("怎么才能不畏将来", "https://mov.bn.netease.com/open-movie/nos/mp4/2018/01/25/SD82Q0AQE_hd.mp4"),
            new Path("音乐和艺术如何改变世界", "https://mov.bn.netease.com/open-movie/nos/mp4/2017/12/04/SD3SUEFFQ_hd.mp4"),
            new Path("VIDEO_URL_01", VIDEO_URL_01),
            new Path("VIDEO_URL_02", VIDEO_URL_02),
            new Path("VIDEO_URL_03", VIDEO_URL_03),
            new Path("VIDEO_URL_04", VIDEO_URL_04),
            new Path("VIDEO_URL_05", VIDEO_URL_05),
            new Path("VIDEO_URL_06", VIDEO_URL_06),
            new Path("VIDEO_URL_07", VIDEO_URL_07),
            new Path("VIDEO_URL_08", VIDEO_URL_08),
            new Path("VIDEO_URL_09", VIDEO_URL_09)};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mVideoView = findViewById(R.id.videoview_play);
        mRecyclerView = findViewById(R.id.recyclerview_path);
        mIMediaController = new MyMediaController(this);
        mVideoView.setMediaController(mIMediaController);

        String[] paths = getPaths();
        mVideoView.setVideoPaths(paths);


        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                Log.d(TAG, "onPrepared: ");
            }
        });

        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                Log.d(TAG, "onCompletion: ");
            }
        });

        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onError() called with: mp = [" + mp + "], what = [" + what + "], extra = [" + extra + "]");
                return false;
            }
        });

        mVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onInfo() called with: mp = [" + mp + "], what = [" + what + "], extra = [" + extra + "]");
                return false;
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new PathViewHoler(View.inflate(parent.getContext(), R.layout.path_item, null));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                PathViewHoler pathViewHoler = (PathViewHoler)holder;
                pathViewHoler.mTextView.setText(mData[position].name);
                pathViewHoler.mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mVideoView.stopPlayback();
                        mVideoView.setVideoPaths(getPaths(), position);
                        mVideoView.start();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mData.length;
            }

            class PathViewHoler extends RecyclerView.ViewHolder {
                TextView mTextView;
                public PathViewHoler(@NonNull View itemView) {
                    super(itemView);
                    mTextView = itemView.findViewById(R.id.tv_name);
                }
            }
        });
    }

    private String[] getPaths() {
        String[] paths = new String[mData.length];
        for (int i = 0; i < mData.length; i++) {
            paths[i] = mData[i].path;
        }
        return paths;
    }

    public class Path {

        public Path(String name, String path) {
            this.name = name;
            this.path = path;
        }

        String name;
        String path;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();
    }

    @Override
    public void onBackPressed() {
        mIMediaController.toggleScreenOrExit();
    }
}
