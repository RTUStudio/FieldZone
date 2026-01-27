package kr.rtustudio.fieldzone.data;

import java.util.ArrayList;
import java.util.List;

public record PolygonPos(String world, List<Point> points, BoundingBox boundingBox,
                         Boolean isAxisAlignedRectangle, Double cachedArea, Double cachedPerimeter,
                         Point cachedCenter) {

    /**
     * Build with copied points and derived caches
     * 포인트 복사 후 캐시 계산
     */
    public PolygonPos(String world, List<Point> points) {
        this(world, new ArrayList<>(points), calculateBoundingBox(points),
                checkAxisAlignedRectangle(points, calculateBoundingBox(points)), null, null, null);
    }

    /**
     * Compute bounding box from point list
     * 점 목록으로 경계 상자 계산
     */
    private static BoundingBox calculateBoundingBox(List<Point> points) {
        if (points.isEmpty()) return new BoundingBox(0, 0, 0, 0);

        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (Point point : points) {
            minX = Math.min(minX, point.x());
            minZ = Math.min(minZ, point.z());
            maxX = Math.max(maxX, point.x());
            maxZ = Math.max(maxZ, point.z());
        }

        return new BoundingBox(minX, minZ, maxX, maxZ);
    }

    /**
     * Check if polygon is axis-aligned rectangle (AABB)
     * 축 정렬 사각형 여부 판단
     */
    private static boolean checkAxisAlignedRectangle(List<Point> points, BoundingBox bbox) {
        if (points.size() != 4) return false;

        int minX = bbox.minX();
        int minZ = bbox.minZ();
        int maxX = bbox.maxX();
        int maxZ = bbox.maxZ();

        boolean hasCorner1 = false;
        boolean hasCorner2 = false;
        boolean hasCorner3 = false;
        boolean hasCorner4 = false;

        for (Point p : points) {
            if (p.x() == minX && p.z() == minZ) hasCorner1 = true;
            else if (p.x() == minX && p.z() == maxZ) hasCorner2 = true;
            else if (p.x() == maxX && p.z() == minZ) hasCorner3 = true;
            else if (p.x() == maxX && p.z() == maxZ) hasCorner4 = true;
            else return false;
        }

        return hasCorner1 && hasCorner2 && hasCorner3 && hasCorner4;
    }

    /**
     * Determine if point (x, z) is inside polygon
     * 점이 다각형 내부인지 판정
     */
    public boolean isIn(double x, double z) {
        if (points.size() < 3) return false;

        if (!boundingBox.contains(x, z)) return false;

        if (isAxisAlignedRectangle != null && isAxisAlignedRectangle) {
            return true;
        }

        boolean inside = false;
        int n = points.size();

        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = points.get(i);
            Point pj = points.get(j);

            double xi = pi.x() + 0.5;
            double zi = pi.z() + 0.5;
            double xj = pj.x() + 0.5;
            double zj = pj.z() + 0.5;

            if (((zi > z) != (zj > z)) && (x < (xj - xi) * (z - zi) / (zj - zi) + xi)) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * Distance from point to nearest edge
     * 가장 가까운 변까지 거리
     */
    public double distanceToNearestEdge(double x, double z) {
        if (points.size() < 2) return Double.MAX_VALUE;

        double bboxDist = boundingBox.distanceSquared(x, z);
        if (bboxDist > 10000) return Math.sqrt(bboxDist);

        double minDistanceSquared = Double.MAX_VALUE;
        int n = points.size();

        for (int i = 0; i < n; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % n);

            double distSquared = distanceSquaredToSegment(x, z,
                    p1.x() + 0.5, p1.z() + 0.5,
                    p2.x() + 0.5, p2.z() + 0.5);
            if (distSquared < minDistanceSquared) {
                minDistanceSquared = distSquared;
            }
        }

        return Math.sqrt(minDistanceSquared);
    }

    /**
     * Squared distance from point to line segment
     * 선분까지의 거리 제곱
     */
    private double distanceSquaredToSegment(double px, double pz, double x1, double z1, double x2, double z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        double lengthSquared = dx * dx + dz * dz;

        if (lengthSquared == 0) {
            double diffX = px - x1;
            double diffZ = pz - z1;
            return diffX * diffX + diffZ * diffZ;
        }

        double t = ((px - x1) * dx + (pz - z1) * dz) / lengthSquared;
        t = Math.max(0, Math.min(1, t));

        double closestX = x1 + t * dx;
        double closestZ = z1 + t * dz;
        double diffX = px - closestX;
        double diffZ = pz - closestZ;

        return diffX * diffX + diffZ * diffZ;
    }

    /**
     * Compute polygon centroid
     * 다각형 무게중심 계산
     */
    public Point center() {
        if (cachedCenter != null) return cachedCenter;
        if (points.isEmpty()) return new Point(0, 0);

        long sumX = 0;
        long sumZ = 0;
        for (Point point : points) {
            sumX += point.x();
            sumZ += point.z();
        }

        return new Point((int) (sumX / points.size()), (int) (sumZ / points.size()));
    }

    /**
     * Return bounding box
     * 경계 상자 반환
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Compute polygon perimeter
     * 둘레 길이 계산
     */
    public double perimeter() {
        if (cachedPerimeter != null) return cachedPerimeter;
        if (points.size() < 2) return 0;

        double perimeter = 0;
        for (int i = 0; i < points.size(); i++) {
            Point current = points.get(i);
            Point next = points.get((i + 1) % points.size());
            perimeter += current.distance(next);
        }

        return perimeter;
    }

    /**
     * Compute polygon area via Shoelace formula
     * 슈레이스 공식을 이용한 면적 계산
     */
    public double area() {
        if (cachedArea != null) return cachedArea;
        if (points.size() < 3) return 0;

        double area = 0;
        int n = points.size();

        for (int i = 0; i < n; i++) {
            Point current = points.get(i);
            Point next = points.get((i + 1) % n);
            area += (long) current.x() * next.z() - (long) next.x() * current.z();
        }

        return Math.abs(area) / 2.0;
    }

    public record BoundingBox(int minX, int minZ, int maxX, int maxZ) {
        /**
         * Check if point is inside bounding box
         * 점이 경계 상자 내부인지 확인
         */
        public boolean contains(double x, double z) {
            return x >= minX - 0.5 && x <= maxX + 0.5 && z >= minZ - 0.5 && z <= maxZ + 0.5;
        }

        /**
         * Squared distance from point to bounding box
         * 경계 상자까지의 거리 제곱
         */
        public double distanceSquared(double x, double z) {
            double dx = 0;
            double dz = 0;

            if (x < minX) dx = minX - x;
            else if (x > maxX) dx = x - maxX;

            if (z < minZ) dz = minZ - z;
            else if (z > maxZ) dz = z - maxZ;

            return dx * dx + dz * dz;
        }
    }

}
