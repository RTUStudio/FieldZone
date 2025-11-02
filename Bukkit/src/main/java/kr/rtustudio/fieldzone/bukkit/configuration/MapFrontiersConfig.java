package kr.rtustudio.fieldzone.bukkit.configuration;

import kr.rtustudio.configurate.objectmapping.ConfigSerializable;
import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@ConfigSerializable
public class MapFrontiersConfig extends ConfigurationPart {

    @Comment("기본 색상 (지역별 색상이 없을 때 사용), 형식: #RRGGBB")
    private final String defaultColorHex = "#FFFFFF"; // 흰색을 기본값으로 사용

    @Comment("지역 별 MapFrontiers 색상")
    private final Map<String, String> colors = make(new HashMap<>(), map -> {
        map.put("example_region", "#000000");
    });

    @Comment("플레이어 접속 시 초기 동기화 지연 시간(틱)")
    private final int joinSyncDelayTicks = 40;

}
