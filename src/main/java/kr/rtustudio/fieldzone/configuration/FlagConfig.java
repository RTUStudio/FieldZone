package kr.rtustudio.fieldzone.configuration;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class FlagConfig extends ConfigurationPart {

    @Comment("""
            Warning system settings
            경고 시스템 설정
            """)
    private Warning warning = new Warning();

    @Getter
    public class Warning extends ConfigurationPart {
        @Comment("""
                Detection distance (blocks). Shows particles when the player is within this distance from the boundary
                감지 거리 (블록, 플레이어가 경계에서 이 거리 내에 있을 때 파티클 표시)
                """)
        private double distance = 2.75;
        @Comment("""
                Particle settings
                파티클 설정
                """)
        private Particle particle = new Particle();

        public double getDistanceSquared() {
            return distance * distance;
        }

        @Getter
        public class Particle extends ConfigurationPart {
            @Comment("""
                    Particle type (e.g., OMINOUS_SPAWNING, FLAME, REDSTONE, SOUL_FIRE_FLAME)
                    타입 (예: OMINOUS_SPAWNING, FLAME, REDSTONE, SOUL_FIRE_FLAME 등)
                    """)
            private org.bukkit.Particle type = MinecraftVersion.isSupport("1.20.5") ? org.bukkit.Particle.OMINOUS_SPAWNING : org.bukkit.Particle.SMALL_FLAME;

            @Comment("""
                    Cooldown (ticks) before particles can reappear at the same position
                    쿨다운 (틱, 같은 위치에 파티클이 다시 표시되기까지의 시간)
                    """)
            private int cooldown = 29;

            @Comment("""
                    Sampling interval (blocks, lower is denser). Recommended: 0.15-0.5
                    샘플링 간격 (블록, 낮을수록 촘촘함, 권장: 0.15-0.5)
                    """)
            private double interval = 0.25;
        }
    }

}
