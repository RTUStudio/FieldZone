package kr.rtustudio.fieldzone.configuration;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class MapFrontiersConfig extends ConfigurationPart {

    @Comment("""
            Default color (#RRGGBB)
            기본 색상 (#RRGGBB)
            """)
    private String defaultColorHex = "#FFFFFF"; // 흰색을 기본값으로 사용

    @Comment("""
            Region-specific colors
            지역 별 색상
            """)
    private Map<String, String> colors = make(new HashMap<>(), map -> {
        map.put("example_region", "#000000");
    });

    @Comment("""
            Initial sync delay on player join (ticks)
            플레이어 접속 시 초기 동기화 지연 시간 (틱)
            """)
    private int joinSyncDelayTicks = 40;

}
