# CHANGELOG

## v1.0.1
- 네이버 블로그 전용 앱 실행으로 변경
- 네이버 일반 앱 및 모바일 브라우저 fallback 제거
- 앱 미설치 시 Play 스토어 설치 안내 추가
- 접근성 허용 대상을 네이버 블로그 앱으로 제한
- 기존 ZIP 파싱·이미지 저장·중복 검사 기능 유지

## v1.0.0 (GPT 공작소) — 통합 재구축
검증된 v0.1.1(Naver Writer Helper)의 로직을 실제로 이식하고 모듈형으로 통합 재구축.

### 이식(검증 로직 유지, 규칙 변경 없음)
- ZIP 파싱 + 보안 자원 제한/압축폭탄/경로탈출/제어문자/중복 방어 (PackageParser)
- 태그 정규화 (TagNormalizer), 태그 1개씩 입력 상태머신 (TagInputController)
- 접근성 입력 보조: 네이버 2개 패키지 제한, 금지단어 클릭 방지, 클립보드 fallback, IME_ENTER
- MediaStore 이미지 저장(GPT공작소 앨범), 클립보드 조건부 정리, 네이버 실행, 권한 안내
- 세션/로그 저장소, 플로팅 컨트롤, 백업 전면 제외

### 신규 (v1.0.0)
- 앱 리브랜딩: app_name "GPT 공작소", versionCode 100 / versionName 1.0.0
- metadata 스키마 2.1 파싱 + 구버전 변환 (MetadataParser/MetadataMapper)
- ZIP 원본/본문 SHA-256 및 본문 지문 (ContentFingerprint)
- 중복 글 방지 판정: 정상/주의/중복의심/동일글 + 수정본 예외 (DuplicateChecker)
- 이력 DB (SQLiteOpenHelper 기반, HistoryStore) — 지시서 18 저장 항목
- 게시판 프로필 9종 + 자동 매칭 (BoardProfileRepository/BoardMatcher)
- 가변 이미지 검사(게시판별 최소/권장/최대) (ImageValidator)
- 처리 상태머신 18상태 + 일시정지/재개/실패 (PipelineStateMachine)
- 안전 감시: 화면 꺼짐 시 일시정지 (SafetyMonitor)
- 단계별 타임아웃(태그 5s 등) (NaverAccessibilityService)
- ZIP 공유/열기 인텐트 수신 (ACTION_SEND/VIEW)
- 발행 상태 기록(발행완료/임시저장/취소), 시험 모드
- 기초: 선택자 규칙(SelectorRules), 지시문 관리(InstructionManager), 진단 내보내기(DiagnosticsExporter)

### 빌드/CI
- Gradle Wrapper 8.9 재사용, 의존성 집합 v0.1.1 유지(어노테이션 프로세서 미사용)
- GitHub Actions: clean test assembleDebug + debug APK/테스트결과/빌드로그 아티팩트
- release 서명은 GitHub Secrets 로만 주입(소스에 키/비밀번호 미포함)

### 검증
- 핵심 순수 로직 52/52 단위 검증 통과(kotlinc 실행). CI JUnit 에 동일 테스트 포함.
- 실기기 설치/접근성 실동작/Play Protect 는 미검증(BUILD_STATUS 참고).
