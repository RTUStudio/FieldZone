# FieldZone

자유도형 지역 플러그인 - 점과 점을 연결하여 다각형 지역을 생성할 수 있습니다.

## 기능

### 🎯 핵심 기능
- **자유도형 지역**: 사각형뿐만 아니라 삼각형, 오각형, 하트, 별 등 모든 다각형 지역 생성 가능
- **Point-in-Polygon 알고리즘**: Ray Casting 알고리즘을 사용한 정확한 지역 판별
- **시각화**: 파티클을 이용한 지역 경계선 실시간 표시
- **다중 서버 지원**: RSFramework ProtoWeaver를 통한 서버 간 지역 동기화

### 📋 명령어
- `/fieldzone wand` - 지역 선택 도구 받기
- `/fieldzone create <이름>` - 선택한 점들로 지역 생성
- `/fieldzone remove <지역>` - 지역 제거
- `/fieldzone teleport <지역>` - 지역 중심으로 이동
- `/fieldzone list` - 모든 지역 목록 확인
- `/fieldzone info <지역>` - 지역 상세 정보 확인
- `/fieldzone clear` - 선택한 점 초기화
- `/fieldzone reload` - 설정 다시 불러오기

### 🔧 사용 방법
1. `/fieldzone wand` 명령어로 Wand 아이템을 받습니다
2. **좌클릭**으로 지역의 꼭짓점을 순서대로 선택합니다 (최소 3개)
3. 선택한 점들이 파티클로 표시됩니다
4. `/fieldzone create <이름>` 명령어로 지역을 생성합니다
5. **우클릭**으로 마지막 점을 제거할 수 있습니다

### 📊 PlaceholderAPI 지원
- `%fieldzone_region%` - 현재 위치한 지역 이름
- `%fieldzone_region_count%` - 총 지역 개수
- `%fieldzone_region_<name>_area%` - 특정 지역의 면적
- `%fieldzone_region_<name>_points%` - 특정 지역의 점 개수

## 기술 스택

### 알고리즘
- **Ray Casting Algorithm**: 점이 다각형 내부에 있는지 판별
- **Shoelace Formula**: 다각형의 면적 계산
- **Distance Formula**: 다각형의 둘레 계산

### 데이터 구조
- `Point` - 2D 좌표 (X, Z)
- `BlockPos` - 3D 블록 좌표 (X, Y, Z)
- `PolygonPos` - 다각형 지역 정보 (점 리스트, Y 범위)
- `WandPos` - Wand로 선택한 점들

### 프레임워크
- **RSFramework 3.1.3**: 플러그인 기반 프레임워크
- **ProtoWeaver**: 서버 간 통신
- **Paper API 1.21.8**: 마인크래프트 서버 API

## 빌드

```bash
./gradlew shadowJar
```

빌드된 파일은 `builds/bukkit/` 폴더에 생성됩니다.

## 요구사항
- Minecraft 1.21.8 (Paper)
- Java 21
- RSFramework 3.1.3+

## 라이선스
Copyright (c) 2025 IPECTER
