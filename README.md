# FieldZone

다각형 기반 지역 관리 플러그인. 자유 형태의 다각형, 사각형, 레이캐스트 모드로 지역을 생성하고, 플래그를 설정하여 경계 경고 시스템 등을 구성할 수 있습니다.

> **의존성**: RSFramework

---

## 사용자 가이드

### 명령어

기본 명령어: `/fieldzone` (별칭: `/지역`)

| 명령어 | 설명 |
|---|---|
| `/fieldzone wand` | 지역 선택 도구를 받습니다 |
| `/fieldzone create <이름>` | 선택한 점들로 지역을 생성합니다 |
| `/fieldzone remove <지역>` | 지역을 제거합니다 |
| `/fieldzone teleport <지역>` | 지역의 중심으로 이동합니다 |
| `/fieldzone list` | 모든 지역 목록을 확인합니다 |
| `/fieldzone info <지역>` | 지역의 상세 정보를 확인합니다 (면적, 둘레, 중심, 플래그 등) |
| `/fieldzone clear` | 선택한 모든 점을 초기화합니다 |
| `/fieldzone particle` | 파티클 표시를 켜거나 끕니다 |
| `/fieldzone flag set <지역> <플래그> [true\|false]` | 지역에 플래그 값을 설정합니다 (기본: true) |
| `/fieldzone flag clear <지역> <플래그>` | 지역에서 플래그를 해제합니다 (NONE 상태로 복귀) |
| `/fieldzone flag list <지역>` | 지역의 플래그 목록을 확인합니다 |
| `/fieldzone reload` | 설정을 다시 불러옵니다 |

### 선택 모드

도구를 들고 **우클릭(Shift)**으로 모드를 전환할 수 있습니다.

| 모드 | 설명 |
|---|---|
| **자유 (FREE)** | 클릭한 위치마다 점이 추가됩니다. 자유로운 다각형 생성에 적합합니다. |
| **사각형 (SQUARE)** | 두 지점을 선택하면 자동으로 사각형 영역이 생성됩니다. |
| **레이캐스트 (RAYCAST)** | 바라보는 방향의 블록에 점을 추가합니다. (최대 거리: 설정 가능) |

**도구 조작:**
- **좌클릭**: 점 추가 (자유/레이캐스트 모드) 또는 첫 번째/두 번째 지점 선택 (사각형 모드)
- **우클릭**: 마지막 점 제거
- **Shift + 우클릭**: 선택 모드 전환

### 플래그

플래그는 `namespace:key` 형식을 사용합니다. 내장 플래그는 `fieldzone` 네임스페이스를 사용하며, 외부 플러그인은 자신만의 네임스페이스로 커스텀 플래그를 등록할 수 있습니다.

| 플래그 | 설명 |
|---|---|
| `fieldzone:warning` | 플레이어가 지역 경계에 가까이 가면 빨간색 파티클로 경계면을 표시합니다 |

> 명령어에서 `fieldzone:` 접두사는 생략 가능합니다. (예: `/fz flag set spawn warning`)

### 설정 파일

#### Global.yml — 전역 설정

| 항목 | 기본값 | 설명 |
|---|---|---|
| `clean-unregistered-flags` | `false` | 소유자 플러그인이 로드되었지만 플래그를 등록하지 않았을 때 해당 플래그를 자동 삭제할지 여부 |
| `wand.item` | `minecraft:blaze_rod` | 도구 아이템 (CustomItems 형식) |
| `wand.raycast-max-range` | `200` | 레이캐스트 최대 거리 |
| `wand.particle.interval` | `2` | 파티클 표시 간격 (틱) |
| `wand.particle.density` | `0.05` | 파티클 밀도 (낮을수록 촘촘) |
| `wand.particle.wave-gap` | `10` | 파도 간격 (블록) |
| `wand.particle.wave-phase-step` | `0.1` | 파도 상승 단계 |
| `wand.particle.vertical-range` | `64` | 수직 표시 범위 (±블록) |

#### Flag.yml — 경고 시스템 설정

| 항목 | 기본값 | 설명 |
|---|---|---|
| `warning.distance` | `2.75` | 경고 감지 거리 (블록) |
| `warning.particle.type` | `OMINOUS_SPAWNING` | 경고 파티클 종류 |
| `warning.particle.cooldown` | `29` | 파티클 쿨다운 (틱) |
| `warning.particle.interval` | `0.25` | 샘플링 간격 (블록) |

### PlaceholderAPI

| Placeholder | 설명 |
|---|---|
| `%fieldzone_region%` | 현재 위치한 지역 이름 (없으면 "빈 공간") |
| `%fieldzone_count%` | 총 지역 개수 |
| `%fieldzone_area_<이름>%` | 특정 지역의 면적 |
| `%fieldzone_points_<이름>%` | 특정 지역의 점 개수 |

### 권한

| 권한 | 기본 | 설명 |
|---|---|---|
| `fieldzone.wand` | OP | 도구 관련 명령어 사용 |

---

## 개발자 가이드

### 의존성 추가

```kotlin
repositories {
    maven {
        name = "RTUStudio"
        url = uri("https://repo.codemc.io/repository/rtustudio/")
    }
}

dependencies {
    compileOnly("kr.rtustudio:fieldzone:1.2.0")
}
```

`plugin.yml`에 의존성을 추가하세요:
```yaml
depend:
  - FieldZone
```

### API 사용법

모든 API는 `FieldZoneAPI` 클래스의 static 메소드로 제공됩니다.

#### 지역 조회

```java
// Location으로 지역 조회
Region region = FieldZoneAPI.getRegion(player.getLocation());

// 이름으로 지역 조회
Region region = FieldZoneAPI.getRegion("spawn");

// 모든 지역 목록
List<Region> regions = FieldZoneAPI.getRegions();

// 총 지역 개수
int count = FieldZoneAPI.getRegionCount();
```

#### 위치 판정

```java
// 해당 위치가 지역 내부인지 확인
boolean inside = FieldZoneAPI.isInRegion(player.getLocation());

// 해당 위치에 특정 플래그의 상태 확인 (TRUE / FALSE / NONE)
FlagState state = FieldZoneAPI.hasFlag(player.getLocation(), RegionFlag.WARNING);
boolean active = state.toBoolean();  // NONE과 FALSE는 false
```

#### 지역 정보

```java
// 면적 (㎡)
double area = FieldZoneAPI.getArea("spawn");

// 둘레 (블록)
double perimeter = FieldZoneAPI.getPerimeter("spawn");

// 중심점
Point center = FieldZoneAPI.getCenter("spawn");
```

### Region 객체

`Region`은 record 타입이며 다음 필드를 가집니다:

| 필드 | 타입 | 설명 |
|---|---|---|
| `uuid()` | `UUID` | 고유 식별자 |
| `name()` | `String` | 지역 이름 |
| `pos()` | `PolygonPos` | 다각형 위치 데이터 (점 목록, 월드, 면적, 둘레, 중심) |
| `flags()` | `Map<RegionFlag, Boolean>` | 설정된 플래그와 값 |

```java
Region region = FieldZoneAPI.getRegion("spawn");
if (region != null) {
    String name = region.name();
    String world = region.pos().world();
    double area = region.pos().area();

    // hasFlag()는 FlagState를 반환 (TRUE / FALSE / NONE)
    FlagState state = region.hasFlag(RegionFlag.WARNING);
    if (state.toBoolean()) {
        // 경고 활성화됨
    }
}
```

---

### 커스텀 플래그 시스템

FieldZone은 외부 플러그인이 자체 플래그를 등록하고 활용할 수 있는 확장 가능한 플래그 시스템을 제공합니다.
WorldGuard처럼, 외부 플러그인은 자신만의 네임스페이스를 가진 플래그를 추가할 수 있습니다.

#### RegionFlag 구조

`RegionFlag`는 `namespace`(소유자)와 `key`(이름)로 구성된 레코드입니다:

```java
// FieldZone 내장 플래그
RegionFlag.WARNING           // → "fieldzone:warning"

// 커스텀 플래그 생성 (RegionFlag.create 사용 권장)
RegionFlag.create(this, "no_lightning")  // → "myplugin:no_lightning"
RegionFlag.create(this, "safe_zone")     // → "myplugin:safe_zone"
```

#### FlagState (3-상태)

`hasFlag()`는 `boolean`이 아닌 `FlagState` enum을 반환합니다:

| 상태 | 의미 | `toBoolean()` |
|---|---|---|
| `TRUE` | 플래그가 명시적으로 `true`로 설정됨 | `true` |
| `FALSE` | 플래그가 명시적으로 `false`로 설정됨 | `false` |
| `NONE` | 플래그가 설정되지 않음 (데이터에 저장되지 않음) | `false` |

#### 플래그 등록 및 해제

```java
// onEnable()에서 등록
RegionFlag myFlag = RegionFlag.create(this, "no_lightning");
FieldZoneAPI.registerFlag(myFlag);

// onDisable()에서 해제
FieldZoneAPI.unregisterFlag(myFlag);
```

등록된 플래그는 자동으로 `/fieldzone flag set` 명령어의 탭 자동완성에 나타납니다.

#### 플래그 활용

```java
// 특정 위치에 해당 플래그가 설정되어 있는지 확인
FlagState state = FieldZoneAPI.hasFlag(location, myFlag);
if (state.toBoolean()) {
    // 플래그가 true로 설정된 지역 내부
}

// 3-상태를 활용한 세밀한 제어
FlagState state = FieldZoneAPI.hasFlag(location, myFlag);
switch (state) {
    case TRUE  -> // 명시적으로 활성화됨
    case FALSE -> // 명시적으로 비활성화됨
    case NONE  -> // 설정 없음, 기본 동작 적용
}
```

#### 미등록 플래그 보존 정책

`clean-unregistered-flags` 설정에 따라 미등록 플래그의 처리 방식이 달라집니다:

| 상황 | `false` (기본) | `true` |
|---|---|---|
| 소유자 플러그인 미로드 | 보존 ✅ | 보존 ✅ |
| 소유자 플러그인 로드됨 + 플래그 미등록 | 보존 ✅ | **삭제** 🗑️ |
| 소유자 플러그인 로드됨 + 플래그 등록됨 | 정상 사용 | 정상 사용 |

이는 플러그인 업데이트로 플래그가 제거된 경우를 감지하기 위한 것입니다.
소유자 플러그인이 활성 상태임에도 해당 플래그를 등록하지 않았다면, 더 이상 사용하지 않는 것으로 판단하고 정리합니다.

---

### 활용 예시: 번개 방지 플래그

다음은 번개를 발생시키는 플러그인이 FieldZone의 플래그 시스템을 활용하여
특정 지역에서 번개를 차단하는 완전한 예시입니다:

```java
package kr.example.lightning;

import kr.rtustudio.fieldzone.FieldZoneAPI;
import kr.rtustudio.fieldzone.region.FlagState;
import kr.rtustudio.fieldzone.region.RegionFlag;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 번개 플러그인 예시.
 * FieldZone에 'no_lightning' 플래그를 등록하고,
 * 해당 플래그가 true로 설정된 지역에서는 번개를 차단합니다.
 */
public class LightningPlugin extends JavaPlugin implements Listener {

    // 플래그를 필드로 선언
    private RegionFlag noLightning;

    @Override
    public void onEnable() {
        // RegionFlag.create(this, key) — 플러그인 이름이 자동으로 namespace가 됨
        noLightning = RegionFlag.create(this, "no_lightning");

        // FieldZone 레지스트리에 커스텀 플래그 등록
        // → /fz flag set <지역> LightningPlugin:no_lightning 으로 사용 가능
        // → /fz flag set <지역> LightningPlugin:no_lightning false 로 명시적 비활성화도 가능
        FieldZoneAPI.registerFlag(noLightning);

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("LightningPlugin enabled — registered 'lightning:no_lightning' flag");
    }

    @Override
    public void onDisable() {
        // 플러그인 종료 시 플래그 해제
        FieldZoneAPI.unregisterFlag(noLightning);
    }

    @EventHandler
    private void onLightningStrike(LightningStrikeEvent event) {
        Location location = event.getLightning().getLocation();

        // FlagState를 통한 3-상태 확인
        FlagState state = FieldZoneAPI.hasFlag(location, noLightning);

        // toBoolean()으로 간단히 확인 (TRUE만 true, FALSE와 NONE은 false)
        if (state.toBoolean()) {
            event.setCancelled(true);
        }
    }
}
```

**사용 흐름:**

1. 서버에 FieldZone과 LightningPlugin이 함께 설치되어 있습니다.
2. LightningPlugin이 활성화되면 `lightning:no_lightning` 플래그가 자동 등록됩니다.
3. 관리자가 `/fz flag set spawn LightningPlugin:no_lightning` 명령어를 실행합니다 → **TRUE** 설정.
4. 이제 `spawn` 지역에서는 번개가 차단됩니다.
5. `/fz flag set spawn LightningPlugin:no_lightning false` → 명시적으로 **FALSE** 설정 (차단 해제).
6. `/fz flag clear spawn LightningPlugin:no_lightning` → **NONE** 상태로 복귀 (데이터에서 삭제).
7. LightningPlugin을 제거하면:
   - `clean-unregistered-flags: false` → 플래그 데이터가 보존되어, 다시 설치하면 바로 작동합니다.
   - `clean-unregistered-flags: true` → 소유자 플러그인이 없으므로 데이터가 보존됩니다. (소유자가 로드되었는데 등록을 안 했을 때만 삭제)
