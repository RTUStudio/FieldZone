package kr.rtustudio.fieldzone.common.region;

public enum RegionFlag {
    /**
     * 경고 플래그: 지역 경계에 가까이 가면 빨간색 파티클로 경계면 표시
     */
    WARNING;

    public String getKey() {
        return name().toLowerCase();
    }
}
