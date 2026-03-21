package kr.rtustudio.fieldzone.handler;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.configuration.FlagConfig;
import kr.rtustudio.fieldzone.data.Point;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.fieldzone.region.RegionFlag;
import kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class RegionWarning extends RSListener<FieldZone> {

    private static final int CLEANUP_INTERVAL = 100;
    private final RegionManager regionManager;
    private final FlagConfig config; // config는 클래스 변수로 보관 (섹션 보관 금지)
    // config 섹션을 클래스 변수로 보관하지 않음
    private final Object2ObjectOpenHashMap<UUID, PlayerWarningState> playerStates;

    public RegionWarning(FieldZone plugin) {
        super(plugin);
        this.regionManager = plugin.getRegionManager();
        this.config = plugin.getConfiguration(FlagConfig.class);
        this.playerStates = new Object2ObjectOpenHashMap<>();
    }

    /**
     * 좌표 양자화(0.01 단위) + 경량 64비트 해시
     * Quantize coords (0.01 units) + lightweight 64-bit hash
     */
    private static int fastRound2(double v) {
        return (int) (v * 100.0 + (v >= 0 ? 0.5 : -0.5));
    }

    private static long hashPosition(double x, double y, double z) {
        int ix = fastRound2(x);
        int iy = fastRound2(y);
        int iz = fastRound2(z);
        // 31 곱셈 기반 롤링 해시 / 31-multiplier rolling hash
        long h = 1L;
        h = 31L * h + ix;
        h = 31L * h + iy;
        h = 31L * h + iz;
        return h;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        startWarningTask(event.getPlayer());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        stopWarningTask(event.getPlayer());
    }

    private void startWarningTask(Player player) {
        UUID uuid = player.getUniqueId();
        stopWarningTask(player);

        PlayerWarningState state = playerStates.computeIfAbsent(uuid, k -> new PlayerWarningState());

        state.task = CraftScheduler.repeat(getPlugin(), scheduledTask -> {
            if (!player.isOnline()) {
                scheduledTask.cancel();
                playerStates.remove(uuid);
                return;
            }

            processPlayerWarning(player, state);
        }, 0, 1, true);
    }

    /**
     * 경고 처리 파이프라인
     * Warning pipeline: cleanup -> collect -> diff -> spawn -> update
     */
    private void processPlayerWarning(Player player, PlayerWarningState state) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        String worldName = world.getName();
        long currentTick = world.getGameTime();

        // 구성값 로드 (섹션은 보관하지 않고 즉시 사용)
        FlagConfig.Warning warn = this.config.getWarning();
        FlagConfig.Warning.Particle part = warn.getParticle();

        int cooldownTicks = part.getCooldown();
        Particle particleType = part.getType();
        double distLimit = warn.getDistance();
        double dist2Limit = warn.getDistanceSquared();
        double vInterval = part.getInterval();

        state.cleanup(currentTick, cooldownTicks);

        LongOpenHashSet currentHashes = new LongOpenHashSet();
        Long2ObjectOpenHashMap<Vec3> currentLocations = new Long2ObjectOpenHashMap<>();

        double playerX = loc.getX();
        double playerY = loc.getY();
        double playerZ = loc.getZ();
        double minY = world.getMinHeight();
        double maxY = world.getMaxHeight();

        // 지역 순회 및 파티클 수집 / Iterate regions and collect particle positions
        collectParticlesFromRegions(worldName, playerX, playerY, playerZ, minY, maxY,
                distLimit, dist2Limit, vInterval, currentHashes, currentLocations);

        boolean changed = state.hasChanged(currentHashes);

        // 변경 시 즉시, 동일 시 쿨다운 기반 생성 / Immediate on change, cooldown otherwise
        spawnParticles(player, currentHashes, currentLocations,
                state.particleCooldown, currentTick, changed, particleType, cooldownTicks);

        // 상태 업데이트 / Update state
        state.update(currentHashes, currentLocations);
    }

    /**
     * O(1) BBox 프리필터 + O(n) 경계거리 + 균일 선분 샘플링
     * O(1) BBox prefilter + O(n) edge distance + uniform segment sampling
     */
    private void collectParticlesFromRegions(String worldName, double playerX, double playerY, double playerZ,
                                             double minY, double maxY,
                                             double distLimit, double dist2Limit, double vInterval,
                                             LongOpenHashSet hashes, Long2ObjectOpenHashMap<Vec3> locations) {
        for (Region region : regionManager.getRegions()) {
            if (!region.pos().world().equals(worldName)) continue;
            if (!region.hasFlag(RegionFlag.WARNING)) continue;
            // BBox 빠른 배제 / Fast BBox rejection
            double bboxDist = region.pos().getBoundingBox().distanceSquared(playerX, playerZ);
            if (bboxDist > dist2Limit + 100) continue;
            // 경계까지 최소거리 / Min distance to polygon edges
            double distance = region.pos().distanceToNearestEdge(playerX, playerZ);
            if (distance > distLimit) continue;
            // 경계선 샘플링 / Sample along edges
            List<Point> points = region.pos().points();
            int n = points.size();

            for (int i = 0; i < n; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get((i + 1) % n);
                // 균일 선분 샘플링 / Uniform segment sampling
                sampleEdge(p1, p2, playerX, playerY, playerZ, minY, maxY,
                        dist2Limit, vInterval, hashes, locations);
            }
        }
    }

    /**
     * 2D 선분 균일 샘플링 후 수직(Y) 샘플링
     * Uniform 2D segment sampling, then vertical(Y) sampling
     */
    private void sampleEdge(Point from, Point to, double playerX, double playerY, double playerZ,
                            double minY, double maxY,
                            double dist2Limit, double stepInterval,
                            LongOpenHashSet hashes, Long2ObjectOpenHashMap<Vec3> locations) {
        double dx = to.x() - from.x();
        double dz = to.z() - from.z();
        double distSquared = dx * dx + dz * dz;

        if (distSquared < 0.01) return;

        double dist = Math.sqrt(distSquared);
        int steps = (int) Math.ceil(dist / Math.max(0.05, stepInterval));
        if (steps == 0) return;

        double stepRatio = 1.0 / steps;
        // 블록 중심(+.5)에서 샘플 / Sample at block center (+0.5)
        double fromX = from.x() + 0.5;
        double fromZ = from.z() + 0.5;

        // 선분 상 균일 보간 / Uniform interpolation along segment
        for (int i = 0; i <= steps; i++) {
            double ratio = i * stepRatio;
            double x = fromX + dx * ratio;
            double z = fromZ + dz * ratio;
            // 2D 제곱거리 선행 체크 / Early 2D squared-distance check
            double distX = x - playerX;
            double distZ = z - playerZ;
            double dist2DSquared = distX * distX + distZ * distZ;
            if (dist2DSquared > dist2Limit) continue;

            // 수직(Y) 샘플링 윈도우: 플레이어 기준 ±distLimit, 월드 경계로 클램프
            // Vertical(Y) window: player ±distLimit, clamped to world bounds
            double yStart = Math.max(minY, playerY - Math.sqrt(Math.max(0.0, dist2Limit - dist2DSquared)));
            double yEnd = Math.min(maxY, playerY + Math.sqrt(Math.max(0.0, dist2Limit - dist2DSquared)));

            // 3D 제곱거리 체크 / 3D squared-distance check
            for (double y = yStart; y <= yEnd; y += Math.max(0.05, stepInterval)) {
                double distY = y - playerY;
                double dist3DSquared = dist2DSquared + distY * distY;

                if (dist3DSquared <= dist2Limit) {
                    long hash = hashPosition(x, y, z);
                    hashes.add(hash);
                    locations.put(hash, new Vec3(x, y, z));
                }
            }
        }
    }

    /**
     * 변경 시 즉시 스폰, 무변경 시 쿨다운 스폰
     * Spawn immediately on change; otherwise obey per-point cooldown
     */
    private void spawnParticles(Player player, LongOpenHashSet hashes,
                                Long2ObjectOpenHashMap<Vec3> locations,
                                Long2LongOpenHashMap cooldown, long currentTick, boolean changed,
                                Particle particleType, int cooldownTicks) {
        for (long hash : hashes) {
            if (changed) {
                Vec3 vec = locations.get(hash);
                player.spawnParticle(particleType, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0);
                cooldown.put(hash, currentTick);
            } else {
                long lastSpawn = cooldown.get(hash);
                if (lastSpawn == -1L || currentTick - lastSpawn >= cooldownTicks) {
                    Vec3 vec = locations.get(hash);
                    player.spawnParticle(particleType, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0);
                    cooldown.put(hash, currentTick);
                }
            }
        }
    }

    private void stopWarningTask(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerWarningState state = playerStates.remove(uuid);
        if (state != null) {
            state.clear();
        }
    }

    private static class PlayerWarningState {
        final LongOpenHashSet previousHashes;
        final Long2ObjectOpenHashMap<Vec3> previousLocations;
        final Long2LongOpenHashMap particleCooldown;
        ScheduledTask task;
        long lastCleanupTick;

        PlayerWarningState() {
            this.previousHashes = new LongOpenHashSet();
            this.previousLocations = new Long2ObjectOpenHashMap<>();
            this.particleCooldown = new Long2LongOpenHashMap();
            this.particleCooldown.defaultReturnValue(-1L);
            this.lastCleanupTick = 0;
        }

        boolean hasChanged(LongOpenHashSet newHashes) {
            return !previousHashes.equals(newHashes);
        }

        void update(LongOpenHashSet newHashes, Long2ObjectOpenHashMap<Vec3> newLocations) {
            previousHashes.clear();
            previousHashes.addAll(newHashes);
            previousLocations.clear();
            previousLocations.putAll(newLocations);
        }

        void cleanup(long currentTick, int cooldownTicks) {
            // 메모리 상한: 오래된 기록 제거(쿨다운의 2배 이상)
            // Memory bound: evict entries older than 2x cooldown
            if (currentTick - lastCleanupTick > CLEANUP_INTERVAL) {
                particleCooldown.long2LongEntrySet().removeIf(entry ->
                        currentTick - entry.getLongValue() > cooldownTicks * 2L);
                lastCleanupTick = currentTick;
            }
        }

        void clear() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            previousHashes.clear();
            previousLocations.clear();
            particleCooldown.clear();
        }
    }

    private record Vec3(double x, double y, double z) {
    }

}
