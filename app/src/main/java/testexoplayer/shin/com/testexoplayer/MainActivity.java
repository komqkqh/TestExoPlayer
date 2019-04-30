package testexoplayer.shin.com.testexoplayer;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import net.chilicat.m3u8.Element;
import net.chilicat.m3u8.Playlist;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    private static final String TAG = "MainActivity";
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private TextView resolutionTextView;
    private TextView sample_app_title;

    DefaultTrackSelector trackSelector;

    private DebugTextViewHelper debugViewHelper;

    private TrackSelectionHelper mTrackSelectionHelper;
    private int rendererIndex;      // Video 탭 선택 index

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        resolutionTextView = new TextView(this);
        resolutionTextView = (TextView) findViewById(R.id.resolution_textView);
        sample_app_title = findViewById(R.id.sample_app_title);
        resolutionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTrackSelectionHelper.showPlayerList(mContext, rendererIndex);
            }
        });

        getm3u8Parser();

//// I. ADJUST HERE:
////CHOOSE CONTENT: LiveStream / SdCard
//
////LIVE STREAM SOURCE: * Livestream links may be out of date so find any m3u8 files online and replace:
//
////        Uri mp4VideoUri =Uri.parse("http://81.7.13.162/hls/ss1/index.m3u8"); //random 720p source
////        Uri mp4VideoUri =Uri.parse("http://54.255.155.24:1935//Live/_definst_/amlst:sweetbcha1novD235L240P/playlist.m3u8"); //Radnom 540p indian channel
//        Uri mp4VideoUri =Uri.parse("http://cbsnewshd-lh.akamaihd.net/i/CBSNHD_7@199302/index_700_av-p.m3u8"); //CNBC
//        Uri mp4VideoUri =Uri.parse("http://live.field59.com/wwsb/ngrp:wwsb1_all/playlist.m3u8"); //ABC NEWS
        Uri mp4VideoUri =Uri.parse("http://flv14.afreecatv.com/afflv14/_definst_/smil:save/afreeca/station/2019/0407/00/1554564704767570.smil/playlist.m3u8"); //afreeca


////        Uri mp4VideoUri =Uri.parse("FIND A WORKING LINK ABD PLUg INTO HERE"); //PLUG INTO HERE<------------------------------------------
//
//
////VIDEO FROM SD CARD: (2 steps. set up file and path, then change videoSource to get the file)
////        String urimp4 = "path/FileName.mp4"; //upload file to device and add path/name.mp4
////        Uri mp4VideoUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+urimp4);


        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        LoadControl loadControl = new DefaultLoadControl();

        // 2. Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        simpleExoPlayerView = new SimpleExoPlayerView(this);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        int h = simpleExoPlayerView.getResources().getConfiguration().screenHeightDp;
        int w = simpleExoPlayerView.getResources().getConfiguration().screenWidthDp;
        Log.v(TAG, "height : " + h + " weight: " + w);
        ////Set media controller
        simpleExoPlayerView.setUseController(false);//set to true or false to see controllers
        simpleExoPlayerView.requestFocus();
        // Bind the player to the view.
        simpleExoPlayerView.setPlayer(player);

        // Measures bandwidth during playback. Can be null if not required.
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeter);
        // This is the MediaSource representing the media to be played.
//        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(liveStreamUri);

        //// II. ADJUST HERE:

        ////        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
        ////Produces Extractor instances for parsing the media data.
        //        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        //This is the MediaSource representing the media to be played:
        //FOR SD CARD SOURCE:
        //        MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);

        //FOR LIVESTREAM LINK:
        MediaSource videoSource = new HlsMediaSource(mp4VideoUri, dataSourceFactory, 1, null, null);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);


        MergingMediaSource mMergingMediaSource = new MergingMediaSource(loopingSource);

        // Prepare the player with the source.
        player.prepare(mMergingMediaSource);

        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged... ");
                updateButtonVisibilities();
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState+"|||isDrawingCacheEnabled():"+simpleExoPlayerView.isDrawingCacheEnabled());
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                player.stop();
                player.prepare(loopingSource);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });
        player.setPlayWhenReady(true); //run file/link when ready to play.
        player.setVideoDebugListener(videoRendererEventListener);

        debugViewHelper = new DebugTextViewHelper(player, sample_app_title);
        debugViewHelper.start();

        mTrackSelectionHelper = new TrackSelectionHelper(trackSelector);
        mTrackSelectionHelper.initPlayerList();
    }

    private void updateButtonVisibilities() {
        if (player == null) {
            return;
        }

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

        if (mappedTrackInfo == null) {
            return;
        }

        for (int i = 0; i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);

            if (trackGroups.length != 0) {

//                Button button = new Button(this);
                String label;
                switch (player.getRendererType(i)) {
//                    case C.TRACK_TYPE_AUDIO:
//                        label = "audio";
//                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = "video";
                        rendererIndex = i;
                        break;
//                    case C.TRACK_TYPE_TEXT:
//                        label = "text";
//                        break;
                    default:
                        continue;
                }
                Log.i("TEST","[" + i +"]label:::"+label);

                for(int j = 0;j < trackGroups.length; j++){
                    TrackGroup track = trackGroups.get(j);
                    Log.i("TEST","track:::"+track);
                    for (int trackIndex = 0; trackIndex < track.length; trackIndex++) {
//                        Log.i("TEST",label + " : [" + buildTrackName(track.getFormat(trackIndex)) +"] "+ track.getFormat(trackIndex));
                        Log.i("TEST",label + " : [" + TrackSelectionHelper.buildTrackNameK(track.getFormat(trackIndex)) +"] "+ track.getFormat(trackIndex));
                    }
                }

//                button.setText(label);
//                button.setTag(i);
//                button.setOnClickListener(this);
//                debugRootView.addView(button, debugRootView.getChildCount() - 1);
            }
        }
    }

    VideoRendererEventListener videoRendererEventListener = new VideoRendererEventListener() {
        @Override
        public void onVideoEnabled(DecoderCounters counters) {

        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onVideoInputFormatChanged(Format format) {

        }

        @Override
        public void onDroppedFrames(int count, long elapsedMs) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            Log.v(TAG, "onVideoSizeChanged [" + " width: " + width + " height: " + height + "]");
            resolutionTextView.setText("RES:(WxH):" + width + "X" + height + "\n           " + height + "p");//shows video info
        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {

        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {

        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()...");
        player.release();
        debugViewHelper.stop();
        debugViewHelper = null;
    }


    /**
     * M3U8 파서 테스트
     */
    private void getm3u8Parser(){
        M3u8Parser.getM3u8FromUrl("http://flv14.afreecatv.com/afflv14/_definst_/smil:save/afreeca/station/2019/0407/00/1554564704767570.smil/playlist.m3u8", new M3u8Parser.M3u8ParserCallback() {
            @Override
            public void onSuccess(Playlist playlist) {
                if(null != playlist.getElements() && 0 < playlist.getElements().size()){
                    for(int i=0;i<playlist.getElements().size();i++){
                        Element element = playlist.getElements().get(i);
                        Log.i("TEST",i + "[element:::" + element +"]");
                    }
                }
            }

            @Override
            public void onError(Exception e) {

                Log.i("TEST","[error:::" + e +"]");
            }
        });
    }
}
