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
| `/fieldzone flag add <지역> <플래그>` | 지역에 플래그를 추가합니다 |
| `/fieldzone flag remove <지역> <플래그>` | 지역에서 플래그를 제거합니다 |
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

| 플래그 | 설명 |
|---|---|
| `WARNING` | 플레이어가 지역 경계에 가까이 가면 빨간색 파티클로 경계면을 표시합니다 |

### 설정 파일

#### Global.yml — 도구 및 파티클 설정

| 항목 | 기본값 | 설명 |
|---|---|---|
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
    compileOnly("kr.rtustudio:fieldzone:1.1.0")
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

// 해당 위치에 특정 플래그가 설정되어 있는지 확인
boolean hasWarning = FieldZoneAPI.hasFlag(player.getLocation(), RegionFlag.WARNING);
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
| `flags()` | `Set<RegionFlag>` | 설정된 플래그 목록 |

```java
Region region = FieldZoneAPI.getRegion("spawn");
if (region != null) {
    String name = region.name();
    String world = region.pos().world();
    double area = region.pos().area();
    boolean hasWarning = region.hasFlag(RegionFlag.WARNING);
}
```
