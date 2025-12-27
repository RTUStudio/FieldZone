package kr.rtustudio.fieldzone.bukkit.manager;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.configuration.GlobalConfig;
import kr.rtustudio.fieldzone.bukkit.data.BlockPos;
import kr.rtustudio.fieldzone.bukkit.data.WandMode;
import kr.rtustudio.fieldzone.bukkit.data.WandPos;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.core.scheduler.ScheduledTask;
import kr.rtustudio.framework.bukkit.api.player.PlayerChat;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class WandManager {

    private final FieldZone plugin;
    private final PlayerChat chat;
    private final MessageTranslation message;
    private final GlobalConfig config;
    private final Map<UUID, WandPos> map = new HashMap<>();
    private final Map<UUID, Boolean> particleToggle = new HashMap<>();
    private final Map<UUID, ScheduledTask> particleTasks = new HashMap<>();
    private final Map<UUID, WandMode> modes = new HashMap<>();
    private final Map<UUID, BlockPos> squareFirst = new HashMap<>();
    private final Map<UUID, BlockPos> squareSecond = new HashMap<>();

    public WandManager(FieldZone plugin) {
        this.plugin = plugin;
        this.chat = PlayerChat.of(plugin);
        this.message = plugin.getConfiguration().getMessage();
        this.config = plugin.getConfiguration(GlobalConfig.class);
    }

    public WandPos get(UUID uuid) {
        return map.get(uuid);
    }

    public WandMode getMode(UUID uuid) {
        return modes.getOrDefault(uuid, WandMode.FREE);
    }

    public WandMode toggleMode(UUID uuid) {
        WandMode current = getMode(uuid);
        WandMode next = switch (current) {
            case FREE -> WandMode.SQUARE;
            case SQUARE -> WandMode.RAYCAST;
            case RAYCAST -> WandMode.FREE;
        };
        modes.put(uuid, next);
        squareFirst.remove(uuid);
        squareSecond.remove(uuid);
        return next;
    }

    public void remove(UUID uuid) {
        map.remove(uuid);
        stopParticle(uuid);
        particleToggle.remove(uuid);
        modes.remove(uuid);
        squareFirst.remove(uuid);
        squareSecond.remove(uuid);
    }

    public boolean toggleParticle(UUID uuid) {
        boolean current = particleToggle.getOrDefault(uuid, true);
        particleToggle.put(uuid, !current);
        if (!current) {
            WandPos wandPos = map.get(uuid);
            if (wandPos != null) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null) startParticle(player, wandPos);
            }
        } else {
            stopParticle(uuid);
        }
        return !current;
    }

    public boolean isParticleEnabled(UUID uuid) {
        return particleToggle.getOrDefault(uuid, true);
    }

    private void stopParticle(UUID uuid) {
        ScheduledTask task = particleTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    public void addPosition(Player player, Location location) {
        UUID uuid = player.getUniqueId();
        WandMode mode = getMode(uuid);
        if (mode == WandMode.SQUARE) {
            addSquareFirst(player, location);
            return;
        }

        String world = location.getWorld().getName();
        WandPos wandPos = map.getOrDefault(uuid, new WandPos(world));

        if (!wandPos.world().equals(world)) {
            wandPos = new WandPos(world);
        }

        BlockPos newPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (wandPos.positions().size() >= 2 && willIntersect(wandPos.positions(), newPos)) {
            chat.announce(player, message.get(player, "wand.intersect"));
        }

        WandPos newWandPos = wandPos.addPosition(newPos);
        map.put(uuid, newWandPos);
        stopParticle(uuid);
        if (isParticleEnabled(uuid)) {
            startParticle(player, newWandPos);
        }
    }

    public void removeLastPosition(Player player) {
        UUID uuid = player.getUniqueId();
        if (getMode(uuid) == WandMode.SQUARE) {
            squareFirst.remove(uuid);
            squareSecond.remove(uuid);
            map.remove(uuid);
            stopParticle(uuid);
            return;
        }

        WandPos wandPos = map.get(uuid);
        if (wandPos == null || wandPos.positions().isEmpty()) return;

        WandPos newWandPos = wandPos.removeLastPosition();
        map.put(uuid, newWandPos);
        stopParticle(uuid);
        if (!newWandPos.positions().isEmpty() && isParticleEnabled(uuid)) {
            startParticle(player, newWandPos);
        }
    }

    public void addSquareFirst(Player player, Location location) {
        UUID uuid = player.getUniqueId();
        String world = location.getWorld().getName();
        BlockPos first = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        squareFirst.put(uuid, first);
        WandPos result = buildSquareResult(world, squareFirst.get(uuid), squareSecond.get(uuid));
        updatePreview(player, uuid, result);
    }

    public void addSquareSecond(Player player, Location location) {
        UUID uuid = player.getUniqueId();
        String world = location.getWorld().getName();
        BlockPos second = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        squareSecond.put(uuid, second);

        WandPos result = buildSquareResult(world, squareFirst.get(uuid), squareSecond.get(uuid));
        updatePreview(player, uuid, result);
    }

    private WandPos buildSquareResult(String world, BlockPos first, BlockPos second) {
        if (first != null && second != null) {
            int minX = Math.min(first.x(), second.x());
            int maxX = Math.max(first.x(), second.x());
            int minZ = Math.min(first.z(), second.z());
            int maxZ = Math.max(first.z(), second.z());
            int y = first.y();

            WandPos rect = new WandPos(world);
            rect = rect.addPosition(new BlockPos(minX, y, minZ));
            rect = rect.addPosition(new BlockPos(maxX, y, minZ));
            rect = rect.addPosition(new BlockPos(maxX, y, maxZ));
            rect = rect.addPosition(new BlockPos(minX, y, maxZ));
            return rect;
        }

        BlockPos single = first != null ? first : second;
        if (single != null) {
            return new WandPos(world).addPosition(single);
        }
        return new WandPos(world);
    }

    private void updatePreview(Player player, UUID uuid, WandPos result) {
        map.put(uuid, result);
        stopParticle(uuid);
        if (isParticleEnabled(uuid)) startParticle(player, result);
    }

    public void clear(UUID uuid) {
        map.remove(uuid);
        squareFirst.remove(uuid);
        squareSecond.remove(uuid);
        stopParticle(uuid);
    }

    private void startParticle(Player player, WandPos wandPos) {
        UUID uuid = player.getUniqueId();
        List<BlockPos> positions = wandPos.positions();
        if (positions.isEmpty()) return;

        GlobalConfig.Wand.Particle particleConfig = this.config.getWand().getParticle();
        double density = particleConfig.getDensity();
        int interval = particleConfig.getInterval();
        stopParticle(uuid);

        World world = player.getWorld();
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight();

        final int waveGap = particleConfig.getWaveGap();
        final double wavePhaseStep = particleConfig.getWavePhaseStep();

        Columns columnsData = buildColumns(positions, density);
        final List<Column> columns = columnsData.regular();
        final List<Column> intersectColumns = columnsData.intersect();

        // 파도 애니메이션 위상 (수직 진행: 높이 기준)
        final double[] phaseY = new double[]{0.0};
        final Particle waveType = particleConfig.getWave().getNormal();
        final Particle pillarType = particleConfig.getPillar();
        final Particle intersectWaveType = particleConfig.getWave().getIntersect();

        ScheduledTask task = CraftScheduler.repeat(plugin, scheduledTask -> {
            if (!isParticleEnabled(uuid)) {
                scheduledTask.cancel();
                return;
            }

            Player p = plugin.getServer().getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                scheduledTask.cancel();
                return;
            }

            WandPos currentPos = map.get(uuid);
            if (currentPos == null) {
                scheduledTask.cancel();
                return;
            }

            List<BlockPos> currentPositions = currentPos.positions();
            int posCount = currentPositions.size();
            World worldNow = p.getWorld();
            if (!worldNow.getUID().equals(world.getUID())) {
                scheduledTask.cancel();
                return;
            }
            Location ploc = p.getLocation();
            double px = ploc.getX();
            double py = ploc.getY();
            double pz = ploc.getZ();
            double horizontalLimitSq = particleRangeSquared(p);
            double yMaxDelta = particleConfig.getVerticalRange();

            Location tmp = new Location(world, 0, 0, 0);

            if (posCount == 1) {
                BlockPos pos = currentPositions.get(0);
                double x = pos.x() + 0.5;
                double z = pos.z() + 0.5;
                if (inHorizontalRange(px, pz, x, z, horizontalLimitSq)) {
                    double yStart0 = Math.max(worldMinY, py - yMaxDelta);
                    double yEnd0 = Math.min(worldMaxY, py + yMaxDelta);
                    spawnPillar(p, tmp, x, z, yStart0, yEnd0, pillarType);
                }
            } else {
                phaseY[0] = (phaseY[0] + wavePhaseStep) % Math.max(0.0001, waveGap);
                double yStart = Math.max(worldMinY, py - yMaxDelta);
                double yEnd = Math.min(worldMaxY, py + yMaxDelta);
                double phase = (phaseY[0] % waveGap + waveGap) % waveGap;
                double y0 = worldMinY + phase;
                if (y0 < yStart) {
                    double k = Math.ceil((yStart - y0) / waveGap);
                    y0 += k * waveGap;
                }
                renderWaveBands(columns, waveType, p, tmp, px, pz, horizontalLimitSq, y0, yEnd, waveGap);
                renderWaveBands(intersectColumns, intersectWaveType, p, tmp, px, pz, horizontalLimitSq, y0, yEnd, waveGap);
                for (BlockPos pos : currentPositions) {
                    double x = pos.x() + 0.5;
                    double z = pos.z() + 0.5;
                    if (!inHorizontalRange(px, pz, x, z, horizontalLimitSq)) continue;
                    double yStart2 = Math.max(worldMinY, py - yMaxDelta);
                    double yEnd2 = Math.min(worldMaxY, py + yMaxDelta);
                    spawnPillar(p, tmp, x, z, yStart2, yEnd2, pillarType);
                }
            }
        }, 0, interval, true);

        particleTasks.put(uuid, task);
    }

    private Columns buildColumns(List<BlockPos> positions, double density) {
        if (positions.size() < 2) return new Columns(Collections.emptyList(), Collections.emptyList());

        Set<Integer> intersectingEdges = findIntersectingEdges(positions);
        List<Column> columns = new ArrayList<>();
        List<Column> intersectColumns = new ArrayList<>();
        Set<Long> used = new HashSet<>();

        for (int i = 0; i < positions.size(); i++) {
            BlockPos a = positions.get(i);
            BlockPos b = positions.get((i + 1) % positions.size());
            if (i == positions.size() - 1 && positions.size() < 3) break;

            double dx = b.x() - a.x();
            double dz = b.z() - a.z();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 1e-6) continue;

            boolean isIntersecting = intersectingEdges.contains(i);
            double sampleInterval = Math.max(0.5, density);
            int stepsByInterval = (int) Math.ceil(dist / sampleInterval);
            int stepsByBlock = (int) Math.ceil(dist);
            int steps = Math.max(stepsByInterval, stepsByBlock);

            for (int iStep = 0; iStep <= steps; iStep++) {
                double t = (double) iStep / Math.max(1, steps);
                double x = a.x() + dx * t;
                double z = a.z() + dz * t;
                long qx = Math.round((x + 0.5) * 32.0);
                long qz = Math.round((z + 0.5) * 32.0);
                long key = (qx << 32) ^ (qz & 0xffffffffL);
                if (!used.add(key)) continue;

                if (isIntersecting) intersectColumns.add(new Column(x, z));
                else columns.add(new Column(x, z));
            }
        }

        return new Columns(columns, intersectColumns);
    }

    private Set<Integer> findIntersectingEdges(List<BlockPos> positions) {
        Set<Integer> intersecting = new HashSet<>();
        if (positions.size() < 3) return intersecting;

        int n = positions.size();

        for (int i = 0; i < n; i++) {
            BlockPos a1 = positions.get(i);
            BlockPos a2 = positions.get((i + 1) % n);

            if (i == n - 1 && n < 3) continue;

            for (int j = i + 2; j < n; j++) {
                if (j == (i + 1) % n || i == (j + 1) % n) continue;

                BlockPos b1 = positions.get(j);
                BlockPos b2 = positions.get((j + 1) % n);

                if (j == n - 1 && n < 3) continue;

                if (linesIntersect(a1, a2, b1, b2)) {
                    intersecting.add(i);
                    intersecting.add(j);
                }
            }
        }

        return intersecting;
    }

    private boolean willIntersect(List<BlockPos> positions, BlockPos newPos) {
        if (positions.size() < 2) return false;

        BlockPos lastPos = positions.get(positions.size() - 1);

        for (int i = 0; i < positions.size() - 1; i++) {
            BlockPos p1 = positions.get(i);
            BlockPos p2 = positions.get(i + 1);

            if (p1.equals(lastPos) || p2.equals(lastPos)) continue;

            if (linesIntersect(p1, p2, lastPos, newPos)) {
                return true;
            }

            if (onSegment(p1, p2, newPos)) {
                return true;
            }

            if ((!p1.equals(lastPos) && onSegment(lastPos, newPos, p1)) || (!p2.equals(lastPos) && onSegment(lastPos, newPos, p2))) {
                return true;
            }

            if (nearParallelOverlap(lastPos, newPos, p1, p2)) {
                return true;
            }
        }

        if (positions.size() >= 3) {
            BlockPos firstPos = positions.get(0);

            for (int i = 1; i < positions.size() - 1; i++) {
                BlockPos p1 = positions.get(i);
                BlockPos p2 = positions.get(i + 1);

                if (p1.equals(firstPos) || p2.equals(firstPos)) continue;

                if (linesIntersect(p1, p2, newPos, firstPos)) {
                    return true;
                }

                if (onSegment(p1, p2, newPos) || onSegment(p1, p2, firstPos)) {
                    return true;
                }

                if ((!p1.equals(firstPos) && onSegment(newPos, firstPos, p1)) || (!p2.equals(firstPos) && onSegment(newPos, firstPos, p2))) {
                    return true;
                }

                if (nearParallelOverlap(newPos, firstPos, p1, p2)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean linesIntersect(BlockPos p1, BlockPos p2, BlockPos p3, BlockPos p4) {
        double d = (p1.x() - p2.x()) * (p3.z() - p4.z()) - (p1.z() - p2.z()) * (p3.x() - p4.x());
        if (Math.abs(d) < 0.0001) return false;

        double t = ((p1.x() - p3.x()) * (p3.z() - p4.z()) - (p1.z() - p3.z()) * (p3.x() - p4.x())) / d;
        double u = -((p1.x() - p2.x()) * (p1.z() - p3.z()) - (p1.z() - p2.z()) * (p1.x() - p3.x())) / d;

        return t > 0 && t < 1 && u > 0 && u < 1;
    }

    private boolean inHorizontalRange(double px, double pz, double x, double z, double viewBlocks2) {
        double dx = x - px, dz = z - pz;
        return (dx * dx + dz * dz) <= viewBlocks2;
    }

    private void spawnPillar(Player p, Location tmp, double x, double z, double yStart, double yEnd, Particle type) {
        for (double y = yStart; y <= yEnd; y += 1.0) {
            tmp.setX(x);
            tmp.setY(y);
            tmp.setZ(z);
            p.spawnParticle(type, tmp, 1, 0, 0, 0, 0);
        }
    }

    private void renderWaveBands(List<Column> columns, Particle type, Player player, Location tmp,
                                 double px, double pz, double horizontalLimitSq,
                                 double yStart, double yEnd, double waveGap) {
        if (columns.isEmpty()) return;
        for (double y = yStart; y <= yEnd; y += waveGap) {
            for (Column column : columns) {
                double cx = column.x() + 0.5;
                double cz = column.z() + 0.5;
                if (!inHorizontalRange(px, pz, cx, cz, horizontalLimitSq)) continue;
                tmp.setX(cx);
                tmp.setY(y);
                tmp.setZ(cz);
                player.spawnParticle(type, tmp, 1, 0, 0, 0, 0);
            }
        }
    }

    private double particleRangeSquared(Player player) {
        int viewChunks = plugin.getServer().getViewDistance();
        double viewBlocks = Math.max(8, viewChunks * 16);
        return viewBlocks * viewBlocks;
    }

    private boolean onSegment(BlockPos a, BlockPos b, BlockPos p) {
        double ax = a.x(), az = a.z();
        double bx = b.x(), bz = b.z();
        double px = p.x(), pz = p.z();
        double vx = bx - ax, vz = bz - az;
        double wx = px - ax, wz = pz - az;
        double cross = Math.abs(vx * wz - vz * wx);
        double len = Math.hypot(vx, vz);
        if (len < 1e-9) return false;
        double colTol = 1e-6 * len + 1e-6;
        if (cross > colTol) return false;
        double minx = Math.min(ax, bx) - 1e-9, maxx = Math.max(ax, bx) + 1e-9;
        double minz = Math.min(az, bz) - 1e-9, maxz = Math.max(az, bz) + 1e-9;
        return px >= minx && px <= maxx && pz >= minz && pz <= maxz;
    }

    private boolean nearParallelOverlap(BlockPos a1, BlockPos a2, BlockPos b1, BlockPos b2) {
        double ax = a2.x() - a1.x();
        double az = a2.z() - a1.z();
        double bx = b2.x() - b1.x();
        double bz = b2.z() - b1.z();
        double la = Math.hypot(ax, az);
        double lb = Math.hypot(bx, bz);
        if (la < 1e-9 || lb < 1e-9) return false;
        double sinTheta = Math.abs(ax * bz - az * bx) / (la * lb);
        final double ANGLE_EPS = 0.02;
        if (sinTheta > ANGLE_EPS) return false;

        final double DIST_EPS = 0.1;
        double d = segmentDistance(a1.x(), a1.z(), a2.x(), a2.z(), b1.x(), b1.z(), b2.x(), b2.z());
        return d < DIST_EPS;
    }

    private double segmentDistance(double x1, double z1, double x2, double z2,
                                   double x3, double z3, double x4, double z4) {
        double d1 = pointToSegmentDistance(x1, z1, x3, z3, x4, z4);
        double d2 = pointToSegmentDistance(x2, z2, x3, z3, x4, z4);
        double d3 = pointToSegmentDistance(x3, z3, x1, z1, x2, z2);
        double d4 = pointToSegmentDistance(x4, z4, x1, z1, x2, z2);
        return Math.min(Math.min(d1, d2), Math.min(d3, d4));
    }

    private double pointToSegmentDistance(double px, double pz, double x1, double z1, double x2, double z2) {
        double vx = x2 - x1;
        double vz = z2 - z1;
        double wx = px - x1;
        double wz = pz - z1;
        double c1 = vx * wx + vz * wz;
        if (c1 <= 0) return Math.hypot(px - x1, pz - z1);
        double c2 = vx * vx + vz * vz;
        if (c2 <= c1) return Math.hypot(px - x2, pz - z2);
        double t = c1 / c2;
        double projx = x1 + t * vx;
        double projz = z1 + t * vz;
        return Math.hypot(px - projx, pz - projz);
    }

    private record Column(double x, double z) {
    }

    private record Columns(List<Column> regular, List<Column> intersect) {
    }

}
