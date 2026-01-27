package kr.rtustudio.fieldzone.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Wand로 선택한 점들의 정보
 */
public record WandPos(String world, List<BlockPos> positions) {

    public WandPos(String world) {
        this(world, new ArrayList<>());
    }

    public WandPos(String world, List<BlockPos> positions) {
        this.world = world;
        this.positions = new ArrayList<>(positions);
    }

    /**
     * 새로운 점 추가
     */
    public WandPos addPosition(BlockPos pos) {
        List<BlockPos> newPositions = new ArrayList<>(positions);
        newPositions.add(pos);
        return new WandPos(world, newPositions);
    }

    /**
     * 마지막 점 제거
     */
    public WandPos removeLastPosition() {
        if (positions.isEmpty()) return this;
        List<BlockPos> newPositions = new ArrayList<>(positions);
        newPositions.remove(newPositions.size() - 1);
        return new WandPos(world, newPositions);
    }

    /**
     * 모든 점 제거
     */
    public WandPos clear() {
        return new WandPos(world);
    }

    /**
     * 점이 충분한지 확인 (최소 3개 필요)
     */
    public boolean isValid() {
        return positions.size() >= 3;
    }

    /**
     * Point 리스트로 변환 (2D 평면)
     */
    public List<Point> toPoints() {
        return positions.stream().map(BlockPos::toPoint).toList();
    }

}
