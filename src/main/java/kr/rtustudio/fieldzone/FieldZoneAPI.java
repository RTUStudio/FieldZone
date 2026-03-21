package kr.rtustudio.fieldzone;

import kr.rtustudio.fieldzone.data.Point;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.fieldzone.region.RegionFlag;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 외부 플러그인에서 FieldZone의 지역 데이터에 접근하기 위한 API
 */
public class FieldZoneAPI {

    private static FieldZone plugin;

    private static RegionManager manager() {
        if (plugin == null) plugin = FieldZone.getInstance();
        return plugin.getRegionManager();
    }

    /**
     * 해당 위치가 속한 지역을 반환합니다.
     * @param location 위치
     * @return 지역이 없으면 null
     */
    @Nullable
    public static Region getRegion(Location location) {
        return manager().get(location);
    }

    /**
     * 이름으로 지역을 조회합니다.
     * @param name 지역 이름
     * @return 지역이 없으면 null
     */
    @Nullable
    public static Region getRegion(String name) {
        return manager().get(name);
    }

    /**
     * 등록된 모든 지역 목록을 반환합니다.
     */
    public static List<Region> getRegions() {
        return manager().getRegions();
    }

    /**
     * 해당 위치가 지역 내부인지 판정합니다.
     */
    public static boolean isInRegion(Location location) {
        return manager().get(location) != null;
    }

    /**
     * 해당 위치의 지역에 특정 플래그가 설정되어 있는지 확인합니다.
     */
    public static boolean hasFlag(Location location, RegionFlag flag) {
        Region region = manager().get(location);
        return region != null && region.hasFlag(flag);
    }

    /**
     * 지역의 면적을 반환합니다.
     * @return 지역이 없으면 0
     */
    public static double getArea(String name) {
        Region region = manager().get(name);
        return region != null ? region.pos().area() : 0;
    }

    /**
     * 지역의 둘레를 반환합니다.
     * @return 지역이 없으면 0
     */
    public static double getPerimeter(String name) {
        Region region = manager().get(name);
        return region != null ? region.pos().perimeter() : 0;
    }

    /**
     * 지역의 중심점을 반환합니다.
     * @return 지역이 없으면 null
     */
    @Nullable
    public static Point getCenter(String name) {
        Region region = manager().get(name);
        return region != null ? region.pos().center() : null;
    }

    /**
     * 등록된 총 지역 개수를 반환합니다.
     */
    public static int getRegionCount() {
        return manager().getRegions().size();
    }

}
