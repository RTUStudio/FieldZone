package kr.rtustudio.fieldzone.bukkit.configuration;

import kr.rtustudio.configurate.objectmapping.ConfigSerializable;
import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;
import org.bukkit.Particle;

@Getter
@ConfigSerializable
public class GlobalConfig extends ConfigurationPart {

    @Comment("PlaceholderAPI - 지역이 없을 때 표시할 텍스트")
    private final String noRegionText = "빈 공간";

    @Comment("Wand 설정")
    private final Wand wand = new Wand();

    @Comment("경고 시스템 설정")
    private final Warning warning = new Warning();

    @Getter
    @ConfigSerializable
    public static class Wand {
        @Comment("아이템 ID (CustomItems 형식)")
        private final String item = "minecraft:blaze_rod";

        @Comment("파티클 설정")
        private final Particle particle = new Particle();

        @Getter
        @ConfigSerializable
        public static class Particle {
            @Comment("표시 간격 (틱, 낮을수록 부드러움). 권장: 1-5")
            private final int interval = 2;

            @Comment("밀도 (블록당 간격, 낮을수록 촘촘함, 권장: 0.05-0.3)")
            private final double density = 0.05;

            @Comment("파도 간격 (블록 단위, 수평 밴드 간격)")
            private final int waveGap = 10;

            @Comment("파도 상승 단계 (틱마다 증가하는 높이, 블록 단위, 예: 0.1)")
            private final double wavePhaseStep = 0.1;

            @Comment("플레이어 Y 기준 수직 표시 범위(±블록)")
            private final int verticalRange = 64;

            @Comment("파도 파티클 타입")
            private final org.bukkit.Particle waveType = org.bukkit.Particle.HAPPY_VILLAGER;

            @Comment("기둥 파티클 타입")
            private final org.bukkit.Particle pillarType = org.bukkit.Particle.FIREWORK;
        }
    }
    @Getter
    @ConfigSerializable
    public static class Warning {
        @Comment("감지 거리 (블록, 플레이어가 경계에서 이 거리 내에 있을 때 파티클 표시)")
        private final double distance = 2.75;
        @Comment("파티클 설정")
        private final Particle particle = new Particle();

        public double getDistanceSquared() {
            return distance * distance;
        }

        @Getter
        @ConfigSerializable
        public static class Particle {
            @Comment("타입 (예: OMINOUS_SPAWNING, FLAME, REDSTONE, SOUL_FIRE_FLAME 등)")
            private final org.bukkit.Particle type = org.bukkit.Particle.OMINOUS_SPAWNING;

            @Comment("쿨다운 (틱, 같은 위치에 파티클이 다시 표시되기까지의 시간)")
            private final int cooldown = 29;

            @Comment("샘플링 간격 (블록, 낮을수록 촘촘함, 권장: 0.15-0.5)")
            private final double interval = 0.25;
        }
    }

}
