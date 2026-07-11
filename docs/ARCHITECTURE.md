# ARCHITECTURE — 모듈 구조

## 설계 원칙
1. **순수 로직 / Android 로직 분리**: 알고리즘(파싱 규칙·정규화·해시·중복판정·게시판매칭·이미지검증·상태머신)은 Android 의존성이 없는 순수 Kotlin 으로 두어 단위 테스트가 가능하게 함.
2. **검증 로직 이식**: v0.1.1 에서 검증된 보안/접근성/저장 로직은 규칙 변경 없이 이식하고, 확장은 부가(side-effect) 수준으로만 추가.
3. **빌드 의존성 최소화**: 이력 DB는 Room/어노테이션 프로세서 대신 SQLiteOpenHelper 사용 → v0.1.1 과 동일한 의존성 집합 유지(CI 빌드 실패 위험 최소화).

## 패키지 구성
```
com.gptgongjakso.naverwriterhelper
├─ GptGongjaksoApp            앱 초기화(Application)
├─ MainActivity              4영역 UI + 처리 파이프라인 오케스트레이션
├─ model/                    순수 데이터 모델
│  ├─ ImageModels            ImageRole, ParsedImage
│  ├─ NaverPostData          자료 1세트(+metadata/해시)
│  ├─ PostMetadata           metadata 스키마 2.1
│  ├─ BoardProfile           게시판 프로필
│  ├─ PipelineState          18개 상태 enum
│  └─ DuplicateModels        판정/이력 레코드
├─ parser/                   [순수] TagNormalizer, MetadataMapper
│  ├─ PackageParser          [Android/org.json] ZIP 파싱(v0.1.1 이식 + SHA256/metadata)
│  └─ MetadataParser         [Android/org.json] JSON→Map 브릿지
├─ dedup/  [순수]            ContentFingerprint(SHA256/지문), DuplicateChecker(판정)
├─ board/  [순수]            BoardMatcher(매칭), BoardProfileRepository(9개 프로필)
├─ image/  [순수]            ImageValidator(가변 장수 검사)
├─ statemachine/ [순수]      PipelineStateMachine
├─ helper/ [Android]         ImageSaveHelper, ClipboardInputHelper, NaverLaunchHelper, PermissionGuideHelper (v0.1.1 이식)
├─ service/ [Android]        NaverAccessibilityService(이식+타임아웃), FloatingControlService(이식+상태), TagInputController(이식), SafetyMonitor(신규)
├─ store/ [Android]          SessionRepository(이식+확장), AutomationLogStore(이식)
│  └─ db/                    HistoryStore(SQLiteOpenHelper 이력 DB)
├─ selector/ [Android]       SelectorRules (기초)
├─ instruction/ [Android]    InstructionManager (기초)
└─ diagnostics/ [Android]    DiagnosticsExporter (기초)
```

## 처리 파이프라인 (상태머신)
```
RECEIVED → VALIDATING → DUPLICATE_CHECKING → PARSING → STORING → SAVING_IMAGES
        → OPENING_NAVER → SELECTING_CATEGORY → INPUTTING_TITLE → INPUTTING_BODY
        → INPUTTING_TAGS → OPENING_PHOTO_PICKER → WAITING_PHOTO_CONFIRM → READY_FOR_USER
비정상: PAUSED(재개지점 보관) / FAILED / CANCELLED / COMPLETED_BY_USER
```
- 자동 진행은 **절대 발행/임시저장으로 이어지지 않고 READY_FOR_USER 에서 멈춥니다.**
- 화면 꺼짐 → PAUSED. 앱 전환/전화(다른 앱 전면) → 접근성 패키지 제한이 자동 중단.

## 데이터 흐름
1. ZIP 수신(파일 선택 / 공유 / 열기) → `PackageParser` 파싱 + ZIP/본문 SHA-256.
2. `metadata.json` → `MetadataParser`(org.json) → `MetadataMapper`(순수) → `PostMetadata`.
3. `BoardMatcher` 로 게시판 자동 매칭.
4. `HistoryStore.all()` + `DuplicateChecker` → 판정(동일글이면 진행 차단).
5. `ImageValidator` 로 가변 이미지 검사.
6. 이력 저장(HistoryStore) → READY_FOR_USER.
7. 사용자가 이미지 저장/네이버 열기/플로팅으로 입력 보조를 직접 실행. 발행은 사용자가 직접.

## 스레딩
- 파싱/DB I/O 는 `Dispatchers.IO`. UI/상태 갱신은 메인 스레드.
- 전역 상태(SessionRepository/AutomationLogStore)는 리스너 구독으로 UI 에 반영.
