package kr.rtustudio.fieldzone.bukkit.mapfrontiers;

import kr.rtustudio.fieldzone.bukkit.configuration.MapFrontiersConfig;
import kr.rtustudio.fieldzone.common.region.Region;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;


public final class MapFrontiersBridge implements AutoCloseable {

    public static final String CHANNEL_FRONTIER_CREATED = "mapfrontiers:packet_frontier_created";
    public static final String CHANNEL_FRONTIER_DELETED = "mapfrontiers:packet_frontier_deleted";
    // Minecraft BlockPos packed long layout:
    // X: bits 38..63 (26 bits), Z: bits 12..37 (26 bits), Y: bits 0..11 (12 bits)
    private static final int PACKED_HORIZONTAL_LENGTH = 26;        // for X and Z
    private static final int PACKED_Y_LENGTH = 64 - 2 * PACKED_HORIZONTAL_LENGTH; // = 12
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_HORIZONTAL_LENGTH; // = 38
    private static final int Z_OFFSET = PACKED_Y_LENGTH;                                 // = 12
    private static final long PACKED_X_MASK = 0x3FFFFFFL; // 26 bits
    private static final long PACKED_Z_MASK = 0x3FFFFFFL; // 26 bits
    private static final long PACKED_Y_MASK = 0xFFFL;     // 12 bits
    private final RSPlugin plugin;
    private final MapFrontiersConfig mfConfig;

    public MapFrontiersBridge(RSPlugin plugin) {
        this.plugin = plugin;
        this.mfConfig = plugin.getConfiguration(MapFrontiersConfig.class);

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_FRONTIER_CREATED);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_FRONTIER_DELETED);
    }

    public static String worldToDimensionKey(World world) {
        return world.getKey().toString();
//        World.Environment env = world.getEnvironment();
//        return switch (env) {
//            case NORMAL -> "minecraft:overworld";
//            case NETHER -> "minecraft:the_nether";
//            case THE_END -> "minecraft:the_end";
//            default -> "minecraft:overworld";
//        };
    }

    // PacketFrontierCreated.encode 포맷 (간결화)
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

            boolean hasName = (name1 != null && !name1.isEmpty()) || (name2 != null && !name2.isEmpty());
            int colorToSend = (argbColor != null) ? argbColor : 0xFFFFFFFF;

            // Change flags: Name, Vertices, Banner, Shared, Visibility, Color
            out.writeBoolean(hasName);
            out.writeBoolean(true);
            out.writeBoolean(false);
            out.writeBoolean(false);
            out.writeBoolean(true);
            out.writeBoolean(true);

            // UUID (2 longs)
            out.writeLong(frontierId.getMostSignificantBits());
            out.writeLong(frontierId.getLeastSignificantBits());

            writeUtfVarInt(out, dimensionKey);
            out.writeBoolean(personal);

            // owner: hasUsername=false, hasUUID=false
            out.writeBoolean(false);
            out.writeBoolean(false);

            // Visibility (fixed 30 booleans)
            writeVisibilityDefaults(out);

            // Color
            out.writeInt(colorToSend);

            // Name (optional)
            if (hasName) {
                writeUtfVarIntCapped(out, name1 == null ? "" : name1, 17, 68);
                writeUtfVarIntCapped(out, name2 == null ? "" : name2, 17, 68);
            }

            // Vertices
            out.writeInt(vertices.size());
            for (Location loc : vertices) {
                out.writeLong(packBlockPosAsLong(loc.getBlockX() + 0.5, 70, loc.getBlockZ() + 0.5));
            }
            out.writeInt(0); // chunkCount
            out.writeInt(0); // mode = Vertex

            // copiedFrom/created/modified present flags
            out.writeBoolean(false);
            out.writeBoolean(false);
            out.writeBoolean(false);

            // playerId
            out.writeInt(playerId);

            return bout.toByteArray();
        }
    }

    private static void writeVisibilityDefaults(DataOutputStream out) throws IOException {
        // Frontier, (AnnounceInChat, AnnounceInTitle), Fullscreen(9), Minimap(9), Webmap(9)
        boolean[] seq = new boolean[30];
        int i = 0;
        seq[i++] = true;      // Frontier
        seq[i++] = false;     // AnnounceInChat
        seq[i++] = false;     // AnnounceInTitle
        for (int k = 0; k < 9; k++) seq[i++] = true;  // Fullscreen
        for (int k = 0; k < 9; k++) seq[i++] = true;  // Minimap
        for (int k = 0; k < 9; k++) seq[i++] = true;  // Webmap
        for (boolean b : seq) out.writeBoolean(b);
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

    private static void writeUtfVarInt(DataOutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static void writeUtfVarIntCapped(DataOutputStream out, String s, int maxChars, int maxBytes) throws IOException {
        if (s == null) s = "";
        if (s.length() > maxChars) {
            s = s.substring(0, maxChars);
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > maxBytes) {
            // 바이트 기준으로 잘라내기 (UTF-8 경계 유지)
            int len = 0;
            int i = 0;
            while (i < s.length() && len <= maxBytes) {
                int cp = s.codePointAt(i);
                int cpLen = Character.charCount(cp);
                byte[] cbytes = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8);
                if (len + cbytes.length > maxBytes) break;
                len += cbytes.length;
                i += cpLen;
            }
            s = s.substring(0, i);
            bytes = s.getBytes(StandardCharsets.UTF_8);
        }
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    public static long packBlockPosAsLong(double x, int y, double z) {
        return (((long) x & PACKED_X_MASK) << X_OFFSET)
                | (((long) z & PACKED_Z_MASK) << Z_OFFSET)
                | ((long) y & PACKED_Y_MASK);
    }

    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0L) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }

    @Override
    public void close() {
        this.plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(this.plugin, CHANNEL_FRONTIER_CREATED);
        this.plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(this.plugin, CHANNEL_FRONTIER_DELETED);
    }

    public void sendRegionCreatedToPlayer(Player player, Region region) {
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
                    0,
                    color
            );
        } catch (IOException ignored) {
        }
    }

    public void sendRegionDeletedToPlayer(Player player, Region region) {
        World world = Bukkit.getWorld(region.pos().world());
        if (world == null) return;
        String dim = worldToDimensionKey(world);
        try {
            sendFrontierDeletedLikeMapFrontiers(
                    player,
                    dim,
                    region.uuid(),
                    false,
                    0
            );
        } catch (IOException ignored) {
        }
    }

    public void broadcastRegionCreated(Region region) {
        World world = Bukkit.getWorld(region.pos().world());
        if (world == null) return;
        for (Player player : world.getPlayers()) {
            sendRegionCreatedToPlayer(player, region);
        }
    }

    public void broadcastRegionDeleted(Region region) {
        World world = Bukkit.getWorld(region.pos().world());
        if (world == null) return;
        for (Player player : world.getPlayers()) {
            sendRegionDeletedToPlayer(player, region);
        }
    }

    public void sendFrontierCreatedLikeMapFrontiers(
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
        byte[] payload = encodePacketFrontierCreatedFormat(dimensionKey, personal, name1, name2, vertices, frontierId, playerId, argbColor);
        player.sendPluginMessage(this.plugin, CHANNEL_FRONTIER_CREATED, payload);
    }

    public void sendFrontierDeletedLikeMapFrontiers(
            Player player,
            String dimensionKey,
            UUID frontierId,
            boolean personal,
            int playerId
    ) throws IOException {
        byte[] payload = encodePacketFrontierDeletedFormat(dimensionKey, frontierId, personal, playerId);
        player.sendPluginMessage(this.plugin, CHANNEL_FRONTIER_DELETED, payload);
    }

    private Integer resolveRegionColor(String regionName) {
        String hex = (mfConfig.getColors() != null) ? mfConfig.getColors().get(regionName) : null;
        if (hex == null || !hex.matches("^#[0-9A-Fa-f]{6}$")) {
            hex = mfConfig.getDefaultColorHex();
        }

        if (hex != null && hex.matches("^#[0-9A-Fa-f]{6}$")) {
            int rgb = Integer.parseInt(hex.substring(1), 16);
            return 0xFF000000 | rgb;
        }

        return 0xFFFFFFFF;
    }
}
