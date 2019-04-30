package testexoplayer.shin.com.testexoplayer;

/**
 * 재 정렬을 위한 데이터 클래스
 */
public class TrackData {
    private String trackName;
    private int groupIndex;
    private int trackIndex;

    public TrackData(String trackName, int groupIndex, int trackIndex){
        this.trackName = trackName;
        this.groupIndex = groupIndex;
        this.trackIndex = trackIndex;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }
}
