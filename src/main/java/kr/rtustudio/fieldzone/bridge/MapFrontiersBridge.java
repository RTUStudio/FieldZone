package kr.rtustudio.fieldzone.bridge;

import kr.rtustudio.fieldzone.configuration.MapFrontiersConfig;
import kr.rtustudio.fieldzone.region.Region;
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

    private static final int X_OFFSET = 38;
    private static final int Z_OFFSET = 12;
    private static final long COORD_MASK = 0x3FFFFFFL; // 26 bits
    private static final long Y_MASK = 0xFFFL;         // 12 bits
    private static final int HEX_COLOR_PATTERN_LENGTH = 7; // #RRGGBB

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
        out.writeBoolean(true);  // Frontier
        out.writeBoolean(false); // AnnounceInChat
        out.writeBoolean(false); // AnnounceInTitle
        for (int i = 0; i < 27; i++) out.writeBoolean(true); // Fullscreen(9) + Minimap(9) + Webmap(9)
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
        String str = (s == null) ? "" : (s.length() > maxChars ? s.substring(0, maxChars) : s);
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > maxBytes) {
            int cutIndex = 0;
            int byteLen = 0;
            while (cutIndex < str.length()) {
                int cp = str.codePointAt(cutIndex);
                int cpBytes = Character.toString(cp).getBytes(StandardCharsets.UTF_8).length;
                if (byteLen + cpBytes > maxBytes) break;
                byteLen += cpBytes;
                cutIndex += Character.charCount(cp);
            }
            bytes = str.substring(0, cutIndex).getBytes(StandardCharsets.UTF_8);
        }
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    public static long packBlockPosAsLong(double x, int y, double z) {
        return (((long) x & COORD_MASK) << X_OFFSET)
                | (((long) z & COORD_MASK) << Z_OFFSET)
                | ((long) y & Y_MASK);
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
        World world = getRegionWorld(region);
        if (world == null) return;
        List<Location> vertices = region.pos().points().stream()
                .map(p -> new Location(world, p.x(), 70, p.z()))
                .toList();
        try {
            sendFrontierCreatedLikeMapFrontiers(player, worldToDimensionKey(world), false,
                    region.name(), "", vertices, region.uuid(), 0, resolveRegionColor(region.name()));
        } catch (IOException ignored) {}
    }

    public void sendRegionDeletedToPlayer(Player player, Region region) {
        World world = getRegionWorld(region);
        if (world == null) return;
        try {
            sendFrontierDeletedLikeMapFrontiers(player, worldToDimensionKey(world), region.uuid(), false, 0);
        } catch (IOException ignored) {}
    }

    public void broadcastRegionCreated(Region region) {
        World world = getRegionWorld(region);
        if (world != null) world.getPlayers().forEach(p -> sendRegionCreatedToPlayer(p, region));
    }

    public void broadcastRegionDeleted(Region region) {
        World world = getRegionWorld(region);
        if (world != null) world.getPlayers().forEach(p -> sendRegionDeletedToPlayer(p, region));
    }

    private World getRegionWorld(Region region) {
        return Bukkit.getWorld(region.pos().world());
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

    private int resolveRegionColor(String regionName) {
        String hex = (mfConfig.getColors() != null) ? mfConfig.getColors().get(regionName) : null;
        if (!isValidHexColor(hex)) hex = mfConfig.getDefaultColorHex();
        return isValidHexColor(hex) ? 0xFF000000 | Integer.parseInt(hex.substring(1), 16) : 0xFFFFFFFF;
    }

    private static boolean isValidHexColor(String hex) {
        return hex != null && hex.length() == HEX_COLOR_PATTERN_LENGTH && hex.charAt(0) == '#'
                && hex.substring(1).chars().allMatch(c -> Character.isDigit(c) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'));
    }
}
