package testexoplayer.shin.com.testexoplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 트랙 선택 클래스
 */
public class TrackSelectionHelper {

    private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private DefaultTrackSelector trackSelector;

    TrackSelection.Factory videoTrackSelectionFactory;

    private boolean isDisabled;
    private MappingTrackSelector.SelectionOverride override;

    int videoSelectorId = -1;   // 이전 선택 셀렉터

    public TrackSelectionHelper(DefaultTrackSelector trackSelector){
        this.trackSelector = trackSelector;
    }

    /**
     * 플레이 리스트 초기화
     */
    public void initPlayerList(){
        videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
    }

    /**
     * 커스텀 플레이 리스트
     * @param context
     * @param rendererIndex
     */
    public void showPlayerList(final Context context, final int rendererIndex){
//        trackSelector, videoTrackSelectionFactory

//        this, ((Button) view).getText(), trackSelector.getCurrentMappedTrackInfo(), (int) view.getTag()
        MappingTrackSelector.MappedTrackInfo trackInfo = trackSelector.getCurrentMappedTrackInfo();
        final TrackGroupArray trackGroups = trackInfo.getTrackGroups(rendererIndex);
        boolean[] trackGroupsAdaptive = new boolean[trackGroups.length];


        // 트랙그룹안에 내용이 있는지 확인
        for (int i = 0; i < trackGroups.length; i++) {
            trackGroupsAdaptive[i] = videoTrackSelectionFactory != null
                    && trackInfo.getAdaptiveSupport(rendererIndex, i, false) != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED
                    && trackGroups.get(i).length > 1;
        }

        isDisabled = trackSelector.getRendererDisabled(rendererIndex);
        override = trackSelector.getSelectionOverride(rendererIndex, trackGroups);

        if(!isDisabled && null == override){
            videoSelectorId = 0;    // 선택 초기화
        }

        String[][] trackName = new String[trackGroups.length][];

        final ArrayList<TrackData> tracks = new ArrayList<>();
        tracks.add(new TrackData("자동", -1,-1));

        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
            TrackGroup group = trackGroups.get(groupIndex);
            boolean groupIsAdaptive = trackGroupsAdaptive[groupIndex];
//            haveAdaptiveTracks |= groupIsAdaptive;
//            trackViews[groupIndex] = new CheckedTextView[group.length];
            trackName[groupIndex] = new String[group.length];

            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                if (trackInfo.getTrackFormatSupport(rendererIndex, groupIndex, trackIndex) == RendererCapabilities.FORMAT_HANDLED) {
                    // 지원 가능
                    trackName[groupIndex][trackIndex] = buildTrackNameK(group.getFormat(trackIndex));
                    tracks.add(new TrackData(buildTrackNameK(group.getFormat(trackIndex)), groupIndex, trackIndex));
                } else {
                    // 지원 불가능
                }
            }
        }

        final CharSequence[] items2 = new CharSequence[tracks.size()];
        for(int i=0; i<tracks.size(); i++){
            items2[i] = tracks.get(i).getTrackName();
        }

        AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(context);

        // 제목셋팅
        alertDialogBuilder2.setTitle("옵션 선택 목록 대화상자");
        alertDialogBuilder2.setSingleChoiceItems(items2, videoSelectorId,
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
//                        https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/trackselection/DefaultTrackSelector.html
                        if(id == 0){
                            override = null;
                        } else {
                            override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, tracks.get(id).getGroupIndex(), tracks.get(id).getTrackIndex());
                        }

                        videoSelectorId = id;

                        // check start
                        trackSelector.setRendererDisabled(rendererIndex, isDisabled);
                        if (override != null) {
                            trackSelector.setSelectionOverride(rendererIndex, trackGroups, override);
                        } else {
                            trackSelector.clearSelectionOverrides(rendererIndex);
                        }

                        // 프로그램을 종료한다
                        Toast.makeText(context, items2[id] + " 선택했습니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

        // 다이얼로그 생성
        AlertDialog alertDialog2 = alertDialogBuilder2.create();

        // 다이얼로그 보여주기
        alertDialog2.show();
    }


    public static String buildTrackNameK(Format format) {
        String trackName = "";
        if (MimeTypes.isVideo(format.sampleMimeType)) {
            trackName = buildKString(format);
        } else if (MimeTypes.isAudio(format.sampleMimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildAudioPropertyString(format)), buildBitrateString(format)),
                    buildTrackIdString(format));
        } else {
            trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        }
        return trackName.length() == 0 ? "unknown" : trackName;
    }


    public static String buildTrackName(Format format) {
        String trackName;
        if (MimeTypes.isVideo(format.sampleMimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(buildResolutionString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        } else if (MimeTypes.isAudio(format.sampleMimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildAudioPropertyString(format)), buildBitrateString(format)),
                    buildTrackIdString(format));
        } else {
            trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        }
        return trackName.length() == 0 ? "unknown" : trackName;
    }



    private static String joinWithSeparator(String first, String second) {
        return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
    }


    private static String buildResolutionString(Format format) {
        return format.width == Format.NO_VALUE || format.height == Format.NO_VALUE
                ? "" : format.width + "x" + format.height;
    }

    private static String buildLanguageString(Format format) {
        return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
                : format.language;
    }


    private static String buildBitrateString(Format format) {
        return format.bitrate == Format.NO_VALUE ? ""
                : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
    }

    private static String buildKString(Format format) {
        return format.bitrate == Format.NO_VALUE ? ""
                : String.format(Locale.US, "%.0fk", format.bitrate / 1000f);
    }

    private static String buildTrackIdString(Format format) {
        return format.id == null ? "" : ("id:" + format.id);
    }

    private static String buildAudioPropertyString(Format format) {
        return format.channelCount == Format.NO_VALUE || format.sampleRate == Format.NO_VALUE
                ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
    }
}
