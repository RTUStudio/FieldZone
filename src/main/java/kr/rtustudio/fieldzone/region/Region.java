package kr.rtustudio.fieldzone.region;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import kr.rtustudio.fieldzone.data.PolygonPos;

import java.util.Map;
import java.util.UUID;

public record Region(UUID uuid, String name, PolygonPos pos, Map<RegionFlag, Boolean> flags) {

    public Region(String name, PolygonPos pos) {
        this(UUID.randomUUID(), name, pos, new Object2BooleanOpenHashMap<>());
    }

    public Region(UUID uuid, String name, PolygonPos pos) {
        this(uuid, name, pos, new Object2BooleanOpenHashMap<>());
    }

    public Region(UUID uuid, String name, PolygonPos pos, Map<RegionFlag, Boolean> flags) {
        this.uuid = uuid;
        this.name = name;
        this.pos = pos;
        this.flags = new Object2BooleanOpenHashMap<>(flags);
    }

    /**
     * Returns the state of a flag on this region.
     *
     * @return {@link FlagState#TRUE} if explicitly set to true,
     *         {@link FlagState#FALSE} if explicitly set to false,
     *         {@link FlagState#NONE} if not set at all
     */
    public FlagState hasFlag(RegionFlag flag) {
        Boolean value = flags.get(flag);
        if (value == null) return FlagState.NONE;
        return FlagState.of(value);
    }

    public Region withFlag(RegionFlag flag, boolean value) {
        Map<RegionFlag, Boolean> newFlags = new Object2BooleanOpenHashMap<>(flags);
        newFlags.put(flag, value);
        return new Region(uuid, name, pos, newFlags);
    }

    public Region withoutFlag(RegionFlag flag) {
        Map<RegionFlag, Boolean> newFlags = new Object2BooleanOpenHashMap<>(flags);
        newFlags.remove(flag);
        return new Region(uuid, name, pos, newFlags);
    }
}
