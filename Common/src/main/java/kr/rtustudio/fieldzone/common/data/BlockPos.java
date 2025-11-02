package kr.rtustudio.fieldzone.common.data;

// https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Data_types#Position
// x (-33554432 to 33554431), z (-33554432 to 33554431), y (-2048 to 2047)
public record BlockPos(int x, int y, int z) {

    public BlockPos(long packed) {
        this((int) (packed >> 38), (int) (packed << 52 >> 52), (int) (packed << 26 >> 38));
    }

    public long getBlockKey() {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | ((long) y & 0xFFF);
    }

    public long getChunkKey() {
        return ((long) (z >> 4) << 32) | ((x >> 4) & 0xFFFFFFFFL);
    }

    public Point toPoint() {
        return new Point(x, z);
    }

}
