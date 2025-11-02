package kr.rtustudio.fieldzone.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FieldZone Fabric Client
 * 클라이언트 측 지역 시각화 및 UI 기능 제공
 */
public class FieldZoneClient implements ClientModInitializer {

    public static final String MOD_ID = "fieldzone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("FieldZone Client initialized!");
        // TODO: 클라이언트 측 기능 구현
        // - 지역 경계선 렌더링
        // - 지역 정보 HUD
        // - 미니맵 연동
    }

}
