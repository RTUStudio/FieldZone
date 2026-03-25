package kr.rtustudio.fieldzone.region;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import kr.rtustudio.fieldzone.data.PolygonPos;

import java.util.Set;
import java.util.UUID;

public record Region(UUID uuid, String name, PolygonPos pos, Set<RegionFlag> flags) {

    public Region(String name, PolygonPos pos) {
        this(UUID.randomUUID(), name, pos, new ObjectOpenHashSet<>());
    }

    public Region(UUID uuid, String name, PolygonPos pos) {
        this(uuid, name, pos, new ObjectOpenHashSet<>());
    }

    public Region(UUID uuid, String name, PolygonPos pos, Set<RegionFlag> flags) {
        this.uuid = uuid;
        this.name = name;
        this.pos = pos;
        this.flags = new ObjectOpenHashSet<>(flags);
    }

    public boolean hasFlag(RegionFlag flag) {
        return flags.contains(flag);
    }

    public Region withFlag(RegionFlag flag) {
        Set<RegionFlag> newFlags = new ObjectOpenHashSet<>(flags);
        newFlags.add(flag);
        return new Region(uuid, name, pos, newFlags);
    }

    public Region withoutFlag(RegionFlag flag) {
        Set<RegionFlag> newFlags = new ObjectOpenHashSet<>(flags);
        newFlags.remove(flag);
        return new Region(uuid, name, pos, newFlags);
    }

}
