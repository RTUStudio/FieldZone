package kr.rtustudio.fieldzone.configuration;

import kr.rtustudio.configurate.model.ConfigurationPart;
import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import lombok.Getter;

@Getter
@SuppressWarnings({
        "unused",
        "CanBeFinal",
        "FieldCanBeLocal",
        "FieldMayBeFinal",
        "InnerClassMayBeStatic"
})
public class GlobalConfig extends ConfigurationPart {

    @Comment("""
            Whether to delete unregistered flags if their owner plugin is enabled
            소유자 플러그인이 로드되었음에도 등록되지 않은 경우 해당 지역 플래그를 삭제할지 여부
            """)
    private boolean cleanUnregisteredFlags = false;

    @Comment("""
            Wand settings
            도구 설정
            """)
    private Wand wand;

    @Getter
    public class Wand extends ConfigurationPart {
        @Comment("""
                Item ID (CustomItems format)
                아이템 ID (CustomItems 형식)
                """)
        private String item = "minecraft:blaze_rod";

        @Comment("""
                Raycast maximum range
                레이캐스트 최대 거리
                """)
        private int raycastMaxRange = 200;

        @Comment("""
                Particle settings
                파티클 설정
                """)
        private Particle particle;

        @Getter
        public class Particle extends ConfigurationPart {
            @Comment("""
                    Wave particles
                    파도 파티클
                    """)
            public Wave wave;
            @Comment("""
                    Render interval (ticks, lower is smoother). Recommended: 1-5
                    표시 간격 (틱, 낮을수록 부드러움). 권장: 1-5
                    """)
            private int interval = 2;
            @Comment("""
                    Density (spacing per block, lower is denser). Recommended: 0.05-0.3
                    밀도 (블록당 간격, 낮을수록 촘촘함, 권장: 0.05-0.3)
                    """)
            private double density = 0.05;
            @Comment("""
                    Wave gap (blocks between horizontal bands)
                    파도 간격 (블록 단위, 수평 밴드 간격)
                    """)
            private int waveGap = 10;
            @Comment("""
                    Wave phase step (height added per tick, e.g., 0.1)
                    파도 상승 단계 (틱마다 증가하는 높이, 블록 단위, 예: 0.1)
                    """)
            private double wavePhaseStep = 0.1;
            @Comment("""
                    Vertical range from player Y (± blocks)
                    플레이어 Y 기준 수직 표시 범위(±블록)
                    """)
            private int verticalRange = 64;
            @Comment("""
                    Pillar particle type
                    기둥 파티클
                    """)
            private org.bukkit.Particle pillar = MinecraftVersion.isSupport("1.20.5") ? org.bukkit.Particle.FIREWORK : org.bukkit.Particle.valueOf("FIREWORKS_SPARK");

            @Getter
            public class Wave extends ConfigurationPart {
                @Comment("""
                        Default
                        기본
                        """)
                private org.bukkit.Particle normal = MinecraftVersion.isSupport("1.20.5") ? org.bukkit.Particle.HAPPY_VILLAGER : org.bukkit.Particle.valueOf("VILLAGER_HAPPY");
                @Comment("""
                        Intersecting edges
                        교차
                        """)
                private org.bukkit.Particle intersect = org.bukkit.Particle.WAX_ON;
            }
        }
    }

}
