package kr.rtustudio.fieldzone.common.region;

import kr.rtustudio.fieldzone.common.data.PolygonPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record Region(UUID uuid, String name, PolygonPos pos, Set<RegionFlag> flags) {

    public Region(String name, PolygonPos pos) {
        this(UUID.randomUUID(), name, pos, new HashSet<>());
    }

    public Region(UUID uuid, String name, PolygonPos pos) {
        this(uuid, name, pos, new HashSet<>());
    }

    public Region(UUID uuid, String name, PolygonPos pos, Set<RegionFlag> flags) {
        this.uuid = uuid;
        this.name = name;
        this.pos = pos;
        this.flags = new HashSet<>(flags);
    }

    public boolean hasFlag(RegionFlag flag) {
        return flags.contains(flag);
    }

    public Region withFlag(RegionFlag flag) {
        Set<RegionFlag> newFlags = new HashSet<>(flags);
        newFlags.add(flag);
        return new Region(uuid, name, pos, newFlags);
    }

    public Region withoutFlag(RegionFlag flag) {
        Set<RegionFlag> newFlags = new HashSet<>(flags);
        newFlags.remove(flag);
        return new Region(uuid, name, pos, newFlags);
    }

}
