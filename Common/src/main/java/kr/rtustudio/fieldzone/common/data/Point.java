package kr.rtustudio.fieldzone.common.data;

public record Point(int x, int z) {

    public Point(long packed) {
        this((int) (packed >> 32), (int) (packed & 0xFFFFFFFFL));
    }

    public long getPointKey() {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public double distance(Point other) {
        int dx = x - other.x;
        int dz = z - other.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public Point add(int dx, int dz) {
        return new Point(x + dx, z + dz);
    }

}
