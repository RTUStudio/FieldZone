package kr.rtustudio.fieldzone.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.configuration.GlobalConfig;
import kr.rtustudio.fieldzone.data.Point;
import kr.rtustudio.fieldzone.data.PolygonPos;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.fieldzone.region.RegionFlag;
import kr.rtustudio.fieldzone.region.RegionFlagRegistry;
import kr.rtustudio.storage.JSON;
import kr.rtustudio.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RegionManager {

    private final FieldZone plugin;
    private final Storage storage;
    private final GlobalConfig globalConfig;
    private final Map<String, Region> map = new Object2ObjectOpenHashMap<>();
    private final Map<String, List<Region>> worldCache = new Object2ObjectOpenHashMap<>();

    public RegionManager(FieldZone plugin) {
        this.plugin = plugin;
        this.storage = plugin.getStorage("Region");
        this.globalConfig = plugin.getConfiguration(GlobalConfig.class);
        reload();
    }

    public void reload() {
        map.clear();
        worldCache.clear();
        storage.get(JSON.of()).thenAccept(result -> {
            for (JsonObject json : result) {
                Region region = parse(json);
                if (region == null) continue;
                map.put(region.name(), region);
                addCache(region);
            }
        }).join();
    }

    private void addCache(Region region) {
        worldCache.computeIfAbsent(region.pos().world(), k -> new ObjectArrayList<>()).add(region);
    }

    private void removeCache(Region region) {
        List<Region> regions = worldCache.get(region.pos().world());
        if (regions != null) {
            regions.remove(region);
            if (regions.isEmpty()) {
                worldCache.remove(region.pos().world());
            }
        }
    }

    private Region parse(JsonObject json) {
        String uuid = getString(json, "uuid");
        String name = getString(json, "name");
        String world = getString(json, "world");
        JsonArray pointsArray = getArray(json, "points");

        if (uuid == null || name == null || world == null || pointsArray == null) {
            plugin.console("<red>Corrupted region data detected. " + json + "</red>");
            plugin.console("<red>손상된 지역 데이터가 있습니다. " + json + "</red>");
            return null;
        }

        List<Point> points = new ObjectArrayList<>();
        for (JsonElement element : pointsArray) {
            if (element.isJsonPrimitive()) {
                points.add(new Point(element.getAsLong()));
            }
        }

        if (points.size() < 3) {
            plugin.console("<red>Region " + name + " has insufficient points. (Requires at least 3)</red>");
            plugin.console("<red>지역 " + name + "의 점이 부족합니다. (최소 3개 필요)</red>");
            return null;
        }

        Map<RegionFlag, Boolean> flags = parseFlags(json.get("flags"));
        PolygonPos pos = new PolygonPos(world, points);
        return new Region(UUID.fromString(uuid), name, pos, flags);
    }

    /**
     * Parses flags from either:
     * - JsonObject (new format): {"fieldzone:warning": true, "lightning:no_lightning": false}
     * - JsonArray (legacy format): ["warning", "fieldzone:warning"] — treated as all TRUE
     */
    private Map<RegionFlag, Boolean> parseFlags(@Nullable JsonElement flagsElement) {
        Map<RegionFlag, Boolean> flags = new Object2BooleanOpenHashMap<>();
        if (flagsElement == null) return flags;

        boolean cleanUnregistered = globalConfig.isCleanUnregisteredFlags();

        if (flagsElement.isJsonObject()) {
            JsonObject obj = flagsElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                RegionFlag flag = resolveFlag(entry.getKey(), cleanUnregistered);
                if (flag != null) {
                    flags.put(flag, entry.getValue().getAsBoolean());
                }
            }
        } else if (flagsElement.isJsonArray()) {
            // Legacy array format: all entries are implicitly TRUE
            for (JsonElement element : flagsElement.getAsJsonArray()) {
                if (!element.isJsonPrimitive()) continue;
                RegionFlag flag = resolveFlag(element.getAsString(), cleanUnregistered);
                if (flag != null) {
                    flags.put(flag, true);
                }
            }
        }

        return flags;
    }

    /**
     * Resolves a flag key to a RegionFlag instance.
     * If the flag is not registered, it may be preserved or cleaned based on config.
     *
     * @return the resolved flag, or null if it should be cleaned
     */
    @Nullable
    private RegionFlag resolveFlag(String fullKey, boolean cleanUnregistered) {
        String namespace;
        String key;

        if (fullKey.contains(":")) {
            String[] split = fullKey.split(":", 2);
            namespace = split[0];
            key = split[1];
        } else {
            namespace = "fieldzone";
            key = fullKey;
        }

        RegionFlag flag = RegionFlagRegistry.get(fullKey);
        if (flag != null) return flag;

        // Unregistered flag: clean if owner plugin is loaded but didn't register it
        if (cleanUnregistered && Bukkit.getPluginManager().isPluginEnabled(namespace)) {
            return null;
        }

        // Preserve unregistered flag data
        return new RegionFlag(namespace, key);
    }

    private String getString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive()) return null;
        return element.getAsString();
    }

    private JsonArray getArray(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonArray()) return null;
        return element.getAsJsonArray();
    }

    public List<Region> getRegions() {
        return List.copyOf(map.values());
    }

    @Nullable
    public Region get(Location location) {
        String worldName = location.getWorld().getName();
        List<Region> regions = worldCache.get(worldName);
        if (regions == null || regions.isEmpty()) return null;

        for (Region region : regions) {
            if (region.pos().isIn(location.getX(), location.getZ())) {
                return region;
            }
        }
        return null;
    }

    @Nullable
    public Region get(String regionName) {
        return map.get(regionName);
    }

    public CompletableFuture<Boolean> add(Region region) {
        return storage.get(JSON.of("name", region.name())).thenApply(result -> {
            if (result == null || result.isEmpty()) {
                JsonArray pointsArray = new JsonArray();
                for (Point point : region.pos().points()) {
                    pointsArray.add(point.getPointKey());
                }

                JSON json = JSON.of("uuid", region.uuid().toString())
                        .append("name", region.name())
                        .append("world", region.pos().world())
                        .append("points", pointsArray)
                        .append("flags", serializeFlags(region.flags()));
                storage.add(json);
                map.put(region.name(), region);
                addCache(region);
                plugin.getMapFrontiersBridge().broadcastRegionCreated(region);
                return true;
            }
            return false;
        });
    }

    public void updateFlags(String regionName, Map<RegionFlag, Boolean> flags) {
        Region region = map.get(regionName);
        if (region == null) return;

        Region updatedRegion = new Region(region.uuid(), region.name(), region.pos(), flags);
        map.put(regionName, updatedRegion);
        removeCache(region);
        addCache(updatedRegion);

        storage.set(JSON.of("name", regionName), JSON.of("flags", serializeFlags(flags)));
    }

    public boolean remove(String regionName) {
        Region region = map.get(regionName);
        if (region == null) return false;

        storage.set(JSON.of("name", regionName), JSON.of());
        map.remove(regionName);
        removeCache(region);
        plugin.getMapFrontiersBridge().broadcastRegionDeleted(region);
        return true;
    }

    private JsonObject serializeFlags(Map<RegionFlag, Boolean> flags) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<RegionFlag, Boolean> entry : flags.entrySet()) {
            obj.addProperty(entry.getKey().getKey(), entry.getValue());
        }
        return obj;
    }
}
