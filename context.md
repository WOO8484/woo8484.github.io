# 🐙 먹물왕 낚시일지 — 프로젝트 컨텍스트

> 새 채팅창에서 작업 재개 시 이 파일을 첨부하세요.

---

## 📁 파일 구성

| 파일 | 용도 |
|------|------|
| `index_single.html` | Claude 미리보기 확인용 (단일 파일) |
| `index.html` | GitHub 배포용 (모듈화) |
| `species-svg.js` | 어종 SVG 데이터 (7종) |
| `sw.js` | PWA 서비스워커 |
| `manifest.json` | PWA 앱 정보 |
| `icon-192.png` | PWA 홈화면 아이콘 (192x192) |
| `icon-512.png` | PWA 홈화면 아이콘 (512x512) |

### 작업 첨부 규칙
- **기능 수정** → `index_single.html` 첨부
- **어종 추가** → `index_single.html` + `species-svg.js` 첨부
- **작업 완료 후** → 단일/모듈화 두 버전 동시 출력

---

## 🔖 현재 버전: v2.1

| 항목 | 내용 |
|------|------|
| 배포 URL | woo8484.github.io |
| 배포 방식 | GitHub Pages |
| 외부 의존성 | Leaflet.js (CDN), Open-Meteo API (무료·무키) |

---

## 🗂 탭 구성 (하단 네비 5개)

| 탭 | page id | 내용 |
|----|---------|------|
| 🌤 날씨/물때 | `page-home` | 지역 선택, 실시간 날씨, 물때, 🌬날씨검색(Leaflet+Open-Meteo, 지도 상시표시) |
| 📅 시즌/금어기 | `page-season` | 이달의 시즌 스트립, 연간 캘린더, 금어기 카드 (오늘 날짜 기준 알림 배너) |
| 🐙 어종도감 | `page-species` | 7종 SVG 카드 그리드, 탭 클릭 → 상세 모달 |
| ⚓ 채비 | `page-tackle` | 어종별 채비 카드 (2열 그리드) + 색상 가이드 |
| 📍 정보 | `page-info` | 선사정보, 낚시방법(통합), 도보 포인트, 용어집 |

---

## 🐟 등록 어종 (7종) — species-svg.js

| id | 이름 | badge | 피크 | 금어기 |
|----|------|-------|------|--------|
| `octopus` | 문어 | 연중 | 9~11월 | 외투장 6cm 이하 방류 |
| `squid-han` | 한치 | 여름 | 7~9월 야간 | 없음 |
| `webfoot` | 주꾸미 | 가을~겨울 | 9~11월 | 5/11~8/31 |
| `bigfin` | 무늬오징어 | 봄·가을 | 3~5월, 9~11월 | 없음 (산란기 자제) |
| `cuttlefish` | 갑오징어 | 가을~겨울 | 9~12월 | 없음 |
| `arrow` | 화살촉오징어 | 여름~가을 | 6~11월 | 5/1~5/31 / 15cm 이하 방류 |
| `saury` | 살오징어 | 봄~가을 | 6~9월 먼바다 | 4/1~5/31 |

---

## 🗺 날씨검색 기능 (🌬 버튼)

- **Leaflet 지도** (OSM 타일) — 포인트 핀 표시, **패널 열리면 바로 상시 표시**
- **Open-Meteo API** — 현재 날씨 + 3일치 시간별 테이블 (3시간 간격)
- **🐙 Windy 보기 버튼** — 해당 좌표 Windy 앱으로 오픈
- **포인트 저장** — localStorage (`fishing_points_v2` 키)
- **조황 메모** — 포인트별 조황 기록 localStorage (`fishing_memo_v1` 키), 📝 버튼
- **바람 화살표 오버레이** — 포인트 선택 시 Open-Meteo 풍향·풍속 화살표 지도 표시
- **기본 포인트 5개** (삭제 불가): 거제 장승포, 통영 서호항, 사천 삼천포, 부산 대변항, 울산 방어진

---

## 🌐 지역 구성 (5개 고정) + 실시간 날씨 좌표

| 칩 | data-region | 좌표 |
|----|-------------|------|
| 🏝 거제 | 거제 | 34.8799, 128.6211 |
| ⛵ 통영 | 통영 | 34.8544, 128.4332 |
| 🎣 사천 | 사천 | 34.9381, 128.0644 |
| 🌉 부산 | 부산 | 35.1796, 129.0756 |
| 🐋 울산 | 울산 | 35.5384, 129.3114 |

---

## 🎨 디자인 토큰 (CSS 변수)

```css
--sky:    #E8F4FD   /* 배경 */
--ocean:  #4BA3CC   /* 주색 */
--deep:   #1A6E99   /* 진한 파랑 */
--foam:   #FFFFFF   /* 카드 배경 */
--mint:   #2ECC8F   /* 액센트 */
--coral:  #FF6B6B   /* 경고/강조 */
--ink:    #2C3E50   /* 본문 텍스트 */
--muted:  #8899AA   /* 보조 텍스트 */
--border: #D4E6F1   /* 구분선 */
--radius: 14px
--radius-sm: 8px
```

---

## 🏗 핵심 JS 구조

```
SPECIES_DATA[]       — 7종 데이터 (species-svg.js / 단일파일은 인라인)
SEASON_DATA[]        — 캘린더용 (name, spId, months[12])
TIDE_MOCK{}          — 5개 지역 물때 mock (조석 API 미연동)
REGION_COORDS{}      — 5개 지역 실시간 날씨 좌표
BOAT_DATA{}          — 8개 지역 선상 출조 정보 (전화번호 없음)
WALK_SPOTS[]         — 도보 낚시 포인트
KNOT_DATA[]          — 매듭법
GLOSSARY[]           — 낚시 용어집
_wxCache{}           — 날씨 캐시 (5분 TTL)
MEMO_KEY             — localStorage 조황 메모 키 (fishing_memo_v1)

주요 함수:
  updateWeather(region)      — Open-Meteo 실시간 날씨 (async)
  _applyWeatherUI(w)         — 날씨 UI 업데이트
  _wmoToDesc(code)           — WMO 날씨코드 → 한글
  buildSeasonStrip()         — 이달의 시즌 스트립
  buildSeasonCal()           — 연간 캘린더
  checkProhibitedAlert()     — 오늘 날짜 기준 금어기 알림
  buildSpeciesGrid()         — 어종 도감 그리드
  openSpeciesModal(id)       — 어종 상세 모달 [전역]
  closeModal()               — 모달 닫기 [전역]
  buildTackleTabs()          — 채비 탭
  patchTackleCardIcons()     — 채비 카드 어종 SVG 교체
  patchProhibitedCardIcons() — 금어기 카드 어종 SVG 교체
  buildWalkSpots()           — 도보 포인트 (어종별 SVG 태그)
  toggleKnot(i)              — 매듭법 토글 [전역]
  buildBoats()               — 선상 출조 목록
  buildMethodInInfo()        — 정보 탭 내 낚시방법 동적 로드
  navigate(page)             — 탭 페이지 이동 [전역]
  setupWindySearch()         — Leaflet + Open-Meteo + Windy IIFE
  drawWindArrows(map,...)    — 풍향 화살표 오버레이
  openMemoSheet(pt)          — 조황 메모 시트 열기
  saveMemo()                 — 메모 저장 [전역]
  closeMemoSheet()           — 메모 시트 닫기 [전역]
  deleteMemo(name,idx)       — 메모 삭제 [전역]
  getSvg(sp)                 — SPECIES SVG 렌더링 헬퍼
  initPickMap()              — Leaflet 지도 초기화 (패널 열 때 자동)
```

---

## 🔧 버그 수정 / 변경 이력

| 버전 | 내용 |
|------|------|
| v1.0 | 최초 출시 |
| v1.1 | Script error 수정 (모달 함수 전역 노출) |
| v1.2 | 갑오징어 svgPath 백틱 누락 수정, 모달 화면 이탈 수정 |
| v1.3 | 버전 배지 인라인 이동, WEATHER_MOCK → Open-Meteo 실시간, Leaflet 지도 타이밍 fix |
| v1.4 | 채비 2열 그리드, 채비 값 줄바꿈, 어종 SVG 아이콘 (채비/금어기/도보), PWA, 단일/모듈화 분리 |
| v2.0 | PWA 아이콘 생성, 전화번호 삭제, 금어기 일자 정밀화+오늘날짜 알림, 지도 상시표시, 낚시방법 정보탭 통합, 조황 메모 기능, 바람 화살표 오버레이 |
| v2.1 | 지도 포인트 마커 클릭 연동, 조황 메모 통계 (포인트별 월별 집계) |

---

## ⚠️ 작업 시 주의사항

1. **svgPath 백틱** — 어종 추가 시 반드시 `</svg>` 닫힘 확인
2. **onclick 함수** — `navigate`, `openSpeciesModal`, `closeModal`, `toggleKnot`, `saveMemo`, `closeMemoSheet`, `deleteMemo` 전역 노출 필수
3. **SEASON_DATA 추가** 시 `spId`를 SPECIES_DATA의 `id`와 정확히 매칭
4. **TACKLE_LIST** 에도 새 어종 key/spId 추가 필요
5. **금어기 카드** (`page-season`) 에도 수동 추가 필요, `checkProhibitedAlert()`도 업데이트
6. **localStorage 키**: `fishing_points_v2` (포인트), `fishing_memo_v1` (조황 메모)
7. **항상 단일 파일 기준 작업** 후 모듈화 버전 분리 출력

---

## 📌 다음 작업 후보

### 우선순위 높음
- (없음 — v2.0에서 완료)

### 우선순위 중간
- [x] 조황 메모 통계 — 포인트별 월별 기록 집계 뷰 ✅ v2.1

### 우선순위 낮음 (검토 필요)
- [ ] 조석 예보 실시간 — 국립해양조사원 API 연동 (유료키 필요)
  - **7-A**: 음력 기반 간이 물때 계산 (무료, 정확도 낮음) — API 키 없이 가능
  - **7-B**: 국립해양조사원 실시간 연동 (정확) — 공공데이터포털 API 키 신청 필요
  - **권장 순서**: 7-A 먼저 → 키 발급 후 7-B 교체
  - 현재 TIDE_MOCK (가짜 고정값) 사용 중
