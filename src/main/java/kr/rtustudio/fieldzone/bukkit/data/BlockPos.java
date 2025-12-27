package kr.rtustudio.fieldzone.bukkit.data;

// https://minecraft.wiki/w/Java_Edition_protocol/Data_types#Position
// x (--33,554,432 to 33,554,431), z (-33,554,432 to 33,554,431), y (-2,048 to 2,047)
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
