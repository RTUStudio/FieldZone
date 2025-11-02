package kr.rtustudio.fieldzone.bukkit.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.common.data.Point;
import kr.rtustudio.fieldzone.common.data.PolygonPos;
import kr.rtustudio.fieldzone.common.region.Region;
import kr.rtustudio.fieldzone.common.region.RegionFlag;
import kr.rtustudio.framework.bukkit.api.platform.JSON;
import kr.rtustudio.framework.bukkit.api.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class RegionManager {

    private final FieldZone plugin;
    private final Map<String, Region> map = new HashMap<>();
    private final Map<String, List<Region>> worldCache = new HashMap<>();

    public void reload() {
        map.clear();
        worldCache.clear();
        Storage storage = plugin.getStorage();
        storage.get("Region", JSON.of()).thenAccept(result -> {
            for (JsonObject json : result) {
                Region region = parse(json);
                if (region != null) {
                    map.put(region.name(), region);
                    addCache(region);
                }
            }
        }).join();
    }

    private void addCache(Region region) {
        worldCache.computeIfAbsent(region.pos().world(), k -> new ArrayList<>()).add(region);
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
            plugin.console("<red>손상된 지역 데이터가 있습니다. " + json + "</red>");
            return null;
        }

        List<Point> points = new ArrayList<>();
        for (JsonElement element : pointsArray) {
            if (element.isJsonPrimitive()) {
                points.add(new Point(element.getAsLong()));
            }
        }

        if (points.size() < 3) {
            plugin.console("<red>지역 " + name + "의 점이 부족합니다. (최소 3개 필요)</red>");
            return null;
        }

        // Parse flags
        Set<RegionFlag> flags = new HashSet<>();
        JsonArray flagsArray = getArray(json, "flags");
        if (flagsArray != null) {
            for (JsonElement element : flagsArray) {
                if (element.isJsonPrimitive()) {
                    String key = element.getAsString();
                    try {
                        RegionFlag flag = RegionFlag.valueOf(key.toUpperCase());
                        flags.add(flag);
                    } catch (IllegalArgumentException ignored) {
                        // unknown flag key; skip
                    }
                }
            }
        }

        PolygonPos pos = new PolygonPos(world, points);
        return new Region(UUID.fromString(uuid), name, pos, flags);
    }

    private String getString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive()) return null;
        return element.getAsString();
    }

    private Integer getInt(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive()) return null;
        return element.getAsInt();
    }

    private JsonArray getArray(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonArray()) return null;
        return element.getAsJsonArray();
    }

    public List<Region> getRegions() {
        return List.copyOf(map.values());
    }

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
        Storage storage = plugin.getStorage();
        return storage.get("Region", JSON.of("name", region.name())).thenApply(result -> {
            if (result == null || result.isEmpty()) {
                JsonArray pointsArray = new JsonArray();
                for (Point point : region.pos().points()) {
                    pointsArray.add(point.getPointKey());
                }

                JsonArray flagsArray = new JsonArray();
                for (RegionFlag flag : region.flags()) {
                    flagsArray.add(flag.getKey());
                }

                JSON json = JSON.of("uuid", region.uuid().toString())
                        .append("name", region.name())
                        .append("world", region.pos().world())
                        .append("points", pointsArray)
                        .append("flags", flagsArray);
                storage.add("Region", json);
                map.put(region.name(), region);
                addCache(region);
                // MapFrontiers에 생성 브로드캐스트
                plugin.getMapFrontiersBridge().broadcastRegionCreated(region);
                return true;
            }
            return false;
        });
    }

    public void updateFlags(String regionName, Set<RegionFlag> flags) {
        Region region = map.get(regionName);
        if (region == null) return;

        Region updatedRegion = new Region(region.uuid(), region.name(), region.pos(), flags);
        map.put(regionName, updatedRegion);
        removeCache(region);
        addCache(updatedRegion);

        JsonArray flagsArray = new JsonArray();
        for (RegionFlag flag : flags) {
            flagsArray.add(flag.getKey());
        }

        Storage storage = plugin.getStorage();
        storage.set("Region", JSON.of("name", regionName), JSON.of("flags", flagsArray));
    }

    public boolean remove(String regionName) {
        Region region = map.get(regionName);
        if (region == null) return false;

        Storage storage = plugin.getStorage();
        storage.set("Region", JSON.of("name", regionName), JSON.of());
        map.remove(regionName);
        removeCache(region);
        // MapFrontiers에 삭제 브로드캐스트
        plugin.getMapFrontiersBridge().broadcastRegionDeleted(region);
        return true;
    }

}
