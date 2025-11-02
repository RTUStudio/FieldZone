package kr.rtustudio.fieldzone.bukkit.mapfrontiers;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.configuration.MapFrontiersConfig;
import kr.rtustudio.fieldzone.common.data.Point;
import kr.rtustudio.fieldzone.common.region.Region;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class MapFrontiersBridge {

    public static final String CHANNEL_FRONTIER_CREATED = "mapfrontiers:packet_frontier_created";
    public static final String CHANNEL_FRONTIER_DELETED = "mapfrontiers:packet_frontier_deleted";
    public static final String CHANNEL_FRONTIERS = "mapfrontiers:packet_frontier"; // 다수 동기화용(미사용)

    private MapFrontiersBridge() {}

    public static void register(JavaPlugin plugin) {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_FRONTIER_CREATED);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_FRONTIER_DELETED);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_FRONTIERS);
    }

    public static void unregister(JavaPlugin plugin) {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL_FRONTIER_CREATED);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL_FRONTIER_DELETED);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL_FRONTIERS);
    }

    public static void sendRegionCreatedToPlayer(Player player, Region region) {
        World world = Bukkit.getWorld(region.pos().world());
        if (world == null) return;
        String dim = worldToDimensionKey(world);
        List<Location> vertices = region.pos().points().stream()
                .map(p -> new Location(world, p.x(), 70, p.z()))
                .toList();
        Integer color = resolveRegionColor(region.name());
        try {
            sendFrontierCreatedLikeMapFrontiers(
                    player,
                    dim,
                    false,
                    region.name(),
                    "",
                    vertices,
                    region.uuid(),
                    player.getEntityId(),
                    color
            );
        } catch (IOException ignored) {}
    }

    public static void sendRegionDeletedToPlayer(Player player, Region region) {
        World world = Bukkit.getWorld(region.pos().world());
        if (world == null) return;
        String dim = worldToDimensionKey(world);
        try {
            sendFrontierDeletedLikeMapFrontiers(
                    player,
                    dim,
                    region.uuid(),
                    false,
                    player.getEntityId()
            );
        } catch (IOException ignored) {}
    }

    public static void broadcastRegionCreated(FieldZone plugin, Region region) {
        World world = Bukkit.getWorld(region.pos().world());
        if (world == null) return;
        for (Player player : world.getPlayers()) {
            sendRegionCreatedToPlayer(player, region);
        }
    }

    public static void broadcastRegionDeleted(FieldZone plugin, Region region) {
        World world = Bukkit.getWorld(region.pos().world());
        if (world == null) return;
        for (Player player : world.getPlayers()) {
            sendRegionDeletedToPlayer(player, region);
        }
    }

    public static String worldToDimensionKey(World world) {
        World.Environment env = world.getEnvironment();
        return switch (env) {
            case NORMAL -> "minecraft:overworld";
            case NETHER -> "minecraft:the_nether";
            case THE_END -> "minecraft:the_end";
            default -> "minecraft:overworld";
        };
    }

    public static void sendFrontierCreatedLikeMapFrontiers(
            Player player,
            String dimensionKey,
            boolean personal,
            String name1,
            String name2,
            List<Location> vertices,
            UUID frontierId,
            int playerId,
            Integer argbColor
    ) throws IOException {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(dimensionKey, "dimensionKey");
        Objects.requireNonNull(vertices, "vertices");
        Objects.requireNonNull(frontierId, "frontierId");

        byte[] payload = encodePacketFrontierCreatedFormat(dimensionKey, personal, name1, name2, vertices, frontierId, playerId, argbColor);
        player.sendPluginMessage(JavaPlugin.getProvidingPlugin(MapFrontiersBridge.class), CHANNEL_FRONTIER_CREATED, payload);
    }

    public static void sendFrontierDeletedLikeMapFrontiers(
            Player player,
            String dimensionKey,
            UUID frontierId,
            boolean personal,
            int playerId
    ) throws IOException {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(dimensionKey, "dimensionKey");
        Objects.requireNonNull(frontierId, "frontierId");

        byte[] payload = encodePacketFrontierDeletedFormat(dimensionKey, frontierId, personal, playerId);
        player.sendPluginMessage(JavaPlugin.getProvidingPlugin(MapFrontiersBridge.class), CHANNEL_FRONTIER_DELETED, payload);
    }

    // PacketFrontierCreated.encode 포맷
    private static byte[] encodePacketFrontierCreatedFormat(
            String dimensionKey,
            boolean personal,
            String name1,
            String name2,
            List<Location> vertices,
            UUID frontierId,
            int playerId,
            Integer argbColor
    ) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bout)) {

            // FrontierData.Change 플래그 6개: Name, Vertices, Banner, Shared, Visibility, Color
            boolean hasName = (name1 != null && !name1.isEmpty()) || (name2 != null && !name2.isEmpty());
            // 색상이 미지정이면 눈에 띄는 불투명 기본색을 사용 (minimap 미표시 방지)
            int colorToSend = (argbColor != null) ? argbColor : 0xFFFFAA00; // Opaque orange
            boolean hasColor = true;
            boolean hasVisibility = true; // 미니맵 표시용 가시성 비트 명시 전송
            out.writeBoolean(hasName);   // Name
            out.writeBoolean(true);      // Vertices
            out.writeBoolean(false);     // Banner
            out.writeBoolean(false);     // Shared
            out.writeBoolean(hasVisibility); // Visibility
            out.writeBoolean(hasColor);  // Color

            // UUID (두 long)
            out.writeLong(frontierId.getMostSignificantBits());
            out.writeLong(frontierId.getLeastSignificantBits());

            // dimension (ResourceLocation)
            writeUtfVarInt(out, dimensionKey);

            // personal
            out.writeBoolean(personal);

            // owner SettingsUser.fromBytes: hasUsername(false), hasUUID(false)
            out.writeBoolean(false);
            out.writeBoolean(false);

            // if Visibility (FrontierData.VisibilityData.toBytes 순서 고정 30개)
            if (hasVisibility) {
                // Frontier
                out.writeBoolean(true);
                // AnnounceInChat, AnnounceInTitle
                out.writeBoolean(false);
                out.writeBoolean(false);
                // Fullscreen (+ name/owner/banner/day/night/underground/topo/biome)
                out.writeBoolean(true);  // Fullscreen
                out.writeBoolean(true);  // FullscreenName
                out.writeBoolean(true);  // FullscreenOwner
                out.writeBoolean(true);  // FullscreenBanner
                out.writeBoolean(true);  // FullscreenDay
                out.writeBoolean(true);  // FullscreenNight
                out.writeBoolean(true);  // FullscreenUnderground
                out.writeBoolean(true);  // FullscreenTopo
                out.writeBoolean(true);  // FullscreenBiome
                // Minimap (+ name/owner/banner/day/night/underground/topo/biome)
                out.writeBoolean(true);  // Minimap
                out.writeBoolean(true);  // MinimapName
                out.writeBoolean(true);  // MinimapOwner
                out.writeBoolean(true);  // MinimapBanner
                out.writeBoolean(true);  // MinimapDay
                out.writeBoolean(true);  // MinimapNight
                out.writeBoolean(true);  // MinimapUnderground
                out.writeBoolean(true);  // MinimapTopo
                out.writeBoolean(true);  // MinimapBiome
                // Webmap (+ name/owner/banner/day/night/underground/topo/biome)
                out.writeBoolean(true);  // Webmap
                out.writeBoolean(true);  // WebmapName
                out.writeBoolean(true);  // WebmapOwner
                out.writeBoolean(true);  // WebmapBanner
                out.writeBoolean(true);  // WebmapDay
                out.writeBoolean(true);  // WebmapNight
                out.writeBoolean(true);  // WebmapUnderground
                out.writeBoolean(true);  // WebmapTopo
                out.writeBoolean(true);  // WebmapBiome
            }

            // if Color
            if (hasColor) {
                out.writeInt(colorToSend);
            }

            // if Name
            if (hasName) {
                writeUtfVarInt(out, name1 == null ? "" : name1);
                writeUtfVarInt(out, name2 == null ? "" : name2);
            }

            // if Vertices
            out.writeInt(vertices.size());
            for (Location loc : vertices) {
                long packed = packBlockPosAsLong(loc.getBlockX(), 70, loc.getBlockZ());
                out.writeLong(packed);
            }
            out.writeInt(0); // chunkCount = 0
            out.writeInt(0); // mode ordinal = Vertex

            // copiedFrom present?
            out.writeBoolean(false);
            // created present?
            out.writeBoolean(false);
            // modified present?
            out.writeBoolean(false);

            // PacketFrontierCreated: playerID 마지막에 int
            out.writeInt(playerId);

            return bout.toByteArray();
        }
    }

    private static byte[] encodePacketFrontierDeletedFormat(
            String dimensionKey,
            UUID frontierId,
            boolean personal,
            int playerId
    ) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bout)) {
            writeUtfVarInt(out, dimensionKey);
            out.writeLong(frontierId.getMostSignificantBits());
            out.writeLong(frontierId.getLeastSignificantBits());
            out.writeBoolean(personal);
            out.writeInt(playerId);
            return bout.toByteArray();
        }
    }

    // 사용 안함
    private static void writeUtf(DataOutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static void writeUtfVarInt(DataOutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0L) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }

    private static long packBlockPosAsLong(int x, int y, int z) {
        long lx = ((long) x & 0x3FFFFFFL) << 38;
        long ly = ((long) y & 0xFFFL) << 26;
        long lz = ((long) z & 0x3FFFFFFL);
        return lx | ly | lz;
    }

    private static Integer resolveRegionColor(String regionName) {
        MapFrontiersConfig cfg = FieldZone.getInstance().getConfiguration(MapFrontiersConfig.class);
        if (cfg == null || cfg.getColors() == null) return null;
        String hex = cfg.getColors().get(regionName);
        if (hex == null) return null;
        if (!hex.matches("^#[0-9A-Fa-f]{6}$")) return null; // 형식 엄격히 제한
        int rgb = Integer.parseInt(hex.substring(1), 16);
        return 0xFF000000 | rgb;
    }

    private static void writeOwnerNone(DataOutputStream out) throws IOException {
        out.writeBoolean(false);
        out.writeBoolean(false);
    }

    private static void writeVisibilityAllOn(DataOutputStream out) throws IOException {
        // Frontier
        out.writeBoolean(true);
        // AnnounceInChat, AnnounceInTitle
        out.writeBoolean(false);
        out.writeBoolean(false);
        // Fullscreen (9)
        out.writeBoolean(true);  // Fullscreen
        out.writeBoolean(true);  // FullscreenName
        out.writeBoolean(true);  // FullscreenOwner
        out.writeBoolean(true);  // FullscreenBanner
        out.writeBoolean(true);  // FullscreenDay
        out.writeBoolean(true);  // FullscreenNight
        out.writeBoolean(true);  // FullscreenUnderground
        out.writeBoolean(true);  // FullscreenTopo
        out.writeBoolean(true);  // FullscreenBiome
        // Minimap (9)
        out.writeBoolean(true);  // Minimap
        out.writeBoolean(true);  // MinimapName
        out.writeBoolean(true);  // MinimapOwner
        out.writeBoolean(true);  // MinimapBanner
        out.writeBoolean(true);  // MinimapDay
        out.writeBoolean(true);  // MinimapNight
        out.writeBoolean(true);  // MinimapUnderground
        out.writeBoolean(true);  // MinimapTopo
        out.writeBoolean(true);  // MinimapBiome
        // Webmap (9)
        out.writeBoolean(true);  // Webmap
        out.writeBoolean(true);  // WebmapName
        out.writeBoolean(true);  // WebmapOwner
        out.writeBoolean(true);  // WebmapBanner
        out.writeBoolean(true);  // WebmapDay
        out.writeBoolean(true);  // WebmapNight
        out.writeBoolean(true);  // WebmapUnderground
        out.writeBoolean(true);  // WebmapTopo
        out.writeBoolean(true);  // WebmapBiome
    }

    private static void writeVertices(DataOutputStream out, List<Location> vertices) throws IOException {
        out.writeInt(vertices.size());
        for (Location loc : vertices) {
            long packed = packBlockPosAsLong(loc.getBlockX(), 70, loc.getBlockZ());
            out.writeLong(packed);
        }
        out.writeInt(0); // chunkCount
        out.writeInt(0); // mode = Vertex
    }
}
