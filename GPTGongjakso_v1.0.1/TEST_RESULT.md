# TEST_RESULT — GPT 공작소 v1.0.1

## 1. 수정 파일 목록

- `app/src/main/java/com/gptgongjakso/naverwriterhelper/helper/NaverLaunchHelper.kt`
  - 네이버 일반 앱/브라우저 fallback 제거, `LaunchResult`를
    `OPENED_BLOG_APP / BLOG_APP_NOT_INSTALLED / FAILED` 3종으로 단순화
  - 설치 여부 판정(`decideByInstallState`)과 Play 스토어 실행
    (`openPlayStoreForBlogAppWith`)을 Context 없이 테스트 가능한 순수 함수로 분리
  - `market://details?id=com.nhn.android.blog` 우선, https Play 페이지는
    market 인텐트 실패 시에만 fallback
- `app/src/main/java/com/gptgongjakso/naverwriterhelper/MainActivity.kt`
  - `openNaver()`를 새 `LaunchResult` 3종 분기로 재작성
  - `BLOG_APP_NOT_INSTALLED` 시 설치 안내 팝업(`설치 화면 열기` / `취소`) 표시
  - 실행 성공/실패 Toast 및 로그 문구를 지시서 3.3 문구로 변경
  - 기존 파이프라인 상태 전환 구조(`PipelineState.OPENING_NAVER` 등)는 유지
- `app/src/main/res/layout/activity_main.xml`
  - 버튼 문구 `네이버 블로그 글쓰기 열기` → `네이버 블로그 앱 열기`
  - 상단 버전 표시 문구 `v1.0.0` → `v1.0.1` (버튼 크기/배치/카드 구조는 변경 없음)
- `app/src/main/AndroidManifest.xml`
  - `<queries>`에서 `com.nhn.android.blog`만 남기고
    `com.nhn.android.search`, `com.naver.whale`, 브라우저 패키지들, 범용 VIEW 인텐트 조회 제거
  - `INTERNET`/저장소/계정/전화 권한 추가 없음(기존 그대로)
- `app/src/main/res/xml/accessibility_service_config.xml`
  - `android:packageNames`를 `com.nhn.android.blog` 단일 패키지로 축소
- `app/src/main/java/com/gptgongjakso/naverwriterhelper/service/NaverAccessibilityService.kt`
  - `allowedPackages`를 `com.nhn.android.blog` 단일 패키지로 축소
  - 안내 문구를 `네이버 앱 화면` → `네이버 블로그 앱 화면`으로 명확화
  - 자동 입력 로직(제목/본문/태그/사진, 발행·임시저장 버튼 클릭 금지, 클립보드 fallback)은 무수정
- `app/build.gradle`
  - `versionCode 100→101`, `versionName "1.0.0"→"1.0.1"`
- `docs/CHANGELOG.md`, `CHANGELOG.md`(루트)
  - v1.0.1 변경 이력 추가
- 신규: `app/src/test/java/com/gptgongjakso/naverwriterhelper/NaverLaunchHelperTest.kt`

변경하지 않은 항목(지시서 5장 절대 변경 금지 범위): ZIP 파싱, metadata/HTML/Markdown/TXT 처리,
제목·본문·태그 추출, 태그 정규화·개별 Enter 입력, 중복 검사·지문 생성, 이미지 검증,
MediaStore 저장, 게시판 매칭, 플로팅 컨트롤 UI, 클립보드 fallback, 발행·임시저장 사용자 직접 수행
원칙, 로그인 정보 미저장, 외부 서버 전송 금지, ZIP 보안 검증 — 모두 원본 코드 그대로 유지했습니다.

## 2. 실행한 Gradle 명령 및 결과

```
./gradlew clean test assembleDebug
```

**실행하지 못했습니다.** 이 작업 환경은 네트워크 접근이 차단되어 있어 Gradle
배포판(`gradle-8.9-bin.zip`) 다운로드부터 `403 Forbidden`으로 실패했습니다
(`./gradlew --version` 시도 시 동일 오류 재현·확인). 따라서 `debug APK` 생성과
자동 테스트 실행 자체를 이 환경에서 수행할 수 없었습니다.

**대신 수행한 검증:**
- 수정한 4개 소스 파일 및 신규 테스트 파일을 전체 재검토하여 import/문법/참조 오류가
  없는지 육안 검토
- 삭제 대상 심볼(`openNaverBlogWrite`, `OPENED_NAVER_APP`, `OPENED_BROWSER`,
  `PKG_NAVER`, `com.nhn.android.search`, `URL_MOBILE_BLOG`)이 코드 전역에
  더 이상 참조되지 않음을 `grep`으로 확인
- 버튼 문구, 접근성 대상 패키지, manifest queries 등 변경 대상 문자열이 실제로
  치환되었는지 파일별로 재확인

**로컬/CI에서 실행이 필요합니다.** `docs/BUILD_LOCAL_AND_CI.md` 절차대로
`./gradlew clean test assembleDebug`를 실행해 실제 빌드·테스트 통과 여부를
확인해 주세요.

## 3. 단위 테스트 결과

Gradle을 실행하지 못해 **실제 테스트 실행 결과는 없습니다.** 신규 작성한
`NaverLaunchHelperTest.kt`는 다음 8개 케이스로 구성되어 있으며, 로컬에서
`./gradlew test`로 실행해야 통과 여부가 확정됩니다.

1. 블로그 앱 설치됨 → `OPENED_BLOG_APP`
2. 블로그 앱 미설치 → `BLOG_APP_NOT_INSTALLED`
3. 일반 네이버 앱만 설치된 상태에서도 `BLOG_APP_NOT_INSTALLED`만 반환(일반 앱 오픈 결과값 자체가 없음)
4. `LaunchResult`에 브라우저 오픈 결과값이 없음을 재확인
5. market/web 실행이 모두 예외를 던져도 예외 없이 `false` 반환
6. market 실패 → web fallback 성공 시 `true`
7. market 성공 시 web fallback을 시도하지 않음
8. market URI / Play 웹 URL이 `com.nhn.android.blog`를 가리킴

`isBlogAppInstalled`, `openNaverBlogApp`, `openPlayStoreForBlogApp`처럼
`Context`/`PackageManager`가 필요한 부분은 프로젝트에 Robolectric/Mockito 등
Android 프레임워크 목 라이브러리가 없어 순수 JVM 단위 테스트로 검증할 수
없었습니다(지시서 7.1에서 허용한 방식대로, 판정 로직과 Intent 실행 로직을
분리해 그 판정/순수 로직 부분만 테스트했습니다). 이 두 함수의 실제 동작은
실기 검증(4장)으로 확인해야 합니다.

기존 회귀 테스트(`TagInputControllerTest`, `TagNormalizerTest`,
`PackageParserTest`, `CoreLogicTest`)는 소스를 수정하지 않았으므로 로직상
회귀 요인은 없으나, 마찬가지로 이 환경에서 실행해 통과를 재확인하지 못했습니다.

## 4. APK 생성 경로

생성하지 못했습니다(2번 항목 사유와 동일). 로컬/CI에서
`./gradlew assembleDebug` 실행 후 `app/build/outputs/apk/debug/app-debug.apk`
경로에서 확인해 주세요.

## 5. 네이버 블로그 앱 설치/미설치 분기 결과

코드 검토 기준:
- 설치됨: `NaverLaunchHelper.openNaverBlogApp()` → `getLaunchIntentForPackage("com.nhn.android.blog")`로
  실행 인텐트 실행 → 성공 시 `OPENED_BLOG_APP`, 실행 자체가 실패하면 `FAILED`
- 미설치: `isBlogAppInstalled()`가 `false` → `BLOG_APP_NOT_INSTALLED` →
  `MainActivity`가 설치 안내 팝업(`설치 화면 열기`/`취소`) 표시
- `설치 화면 열기` 선택 시: `market://details?id=com.nhn.android.blog` 우선 실행,
  실패 시에만 `https://play.google.com/store/apps/details?id=com.nhn.android.blog`로 fallback

실기(에뮬레이터/실기기) 검증은 아직 수행하지 못했습니다 — 8장 실기 검증 순서 참고.

## 6. 브라우저 및 네이버 일반 앱 fallback 제거 확인

- `NaverLaunchHelper`에 `com.nhn.android.search` 실행 코드, 모바일 블로그 브라우저
  URL(`m.blog.naver.com`) 실행 코드가 더 이상 존재하지 않음(코드 삭제 확인)
- `AndroidManifest.xml`의 `<queries>`에서 해당 패키지/브라우저/범용 VIEW 조회 제거
- `accessibility_service_config.xml`, `NaverAccessibilityService.kt`의 허용
  패키지에서 `com.nhn.android.search` 제거
- `LaunchResult` enum에 `OPENED_NAVER_APP`, `OPENED_BROWSER` 값 자체가 없음
  (컴파일 타임에 해당 분기가 구조적으로 불가능함을 보장)

## 7. 아직 실기하지 못한 항목

- `./gradlew clean test assembleDebug` 실제 실행 및 결과 확인 (네트워크 차단으로 미실행)
- 8장 실기 검증 순서 1~14번 전체 (네이버 블로그 앱 설치/미설치 상태에서의 실제 기기 동작,
  Play 스토어 이동, 접근성 입력 실기 등)
- 제목·본문·태그·게시판·사진의 네이버 블로그 앱 실제 글쓰기 화면 입력 실기
  (9장 완료 판정 기준에 따라 네이버 블로그 앱 실행 성공만으로는 전체 완료로 판정하지 않음)
