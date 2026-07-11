# GPT 공작소 v1.0.0

네이버 블로그 글쓰기 화면에서 **제목·본문·태그 입력을 보조**하고, 자료 ZIP 처리·이미지 저장·중복 글 방지를 돕는 Android 앱입니다.
검증된 v0.1.1(Naver Writer Helper)의 ZIP 파싱·태그 처리·MediaStore 저장·접근성 제한·보안 로직을 **실제로 이식**하고, 모듈형 구조로 통합 재구축했습니다.

- 패키지명: `com.gptgongjakso.naverwriterhelper`
- versionCode: `100` / versionName: `1.0.0`
- minSdk 30 (Android 11) / targetSdk 34 / compileSdk 34
- 빌드: AGP 8.5.2 · Kotlin 1.9.24 · Gradle 8.9 (v0.1.1 검증 버전 유지)

## 안전 원칙 (변경 없음)
- **발행/임시저장 버튼은 절대 자동으로 누르지 않습니다.** 발행은 사용자가 직접 합니다.
- **INTERNET 권한 없음** → 앱이 어떤 서버로도 데이터를 전송할 수 없습니다(글/이미지/계정 외부 전송 원천 차단).
- 접근성 조작 대상은 네이버 앱 2개(`com.nhn.android.blog`, `com.nhn.android.search`)로만 제한.
- 자동 입력 실패 시 항상 **클립보드 복사 + 안내**로 안전하게 대체(fallback).
- 백업 전면 제외(allowBackup=false + data_extraction_rules).

## 주요 기능 (v1.0.0)
1. **ZIP 처리**: 자료 ZIP 파싱 + 자원 제한/압축폭탄/경로탈출/제어문자 방어(v0.1.1 이식) + ZIP/본문 SHA-256.
2. **자동 입력 보조**: 접근성 기반 제목/본문/태그(1개씩) 입력 + 단계별 타임아웃 + 상태머신.
3. **이미지 처리**: 갤러리(GPT공작소 앨범) 저장 + 가변 장수 검사(게시판별 최소/권장/최대).
4. **중복 방지**: SHA-256/지문 기반 정상/주의/중복의심/동일글 판정(이력 DB).
5. **게시판 프로필**: naver_category 자동 매칭(9개 기본 프로필).
6. **ZIP 공유 수신**: 다른 앱에서 '공유' 또는 파일앱 열기로 자료 ZIP 수신.

## 빌드 방법
자세한 내용은 [`docs/BUILD_LOCAL_AND_CI.md`](docs/BUILD_LOCAL_AND_CI.md) 참고.

### 로컬 (Android Studio)
1. Android Studio 로 프로젝트 루트를 엽니다.
2. Gradle Sync 후 `Build > Build Bundle(s)/APK(s) > Build APK(s)`.
3. 또는 터미널: `./gradlew clean test assembleDebug`
4. 산출물: `app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions (CI)
`.github/workflows/android-build.yml` 가 push/PR/수동 실행 시 `clean test assembleDebug` 를 자동 수행하고,
성공 시 **debug APK / 단위 테스트 결과 / 빌드 로그**를 아티팩트로 업로드합니다.
release 서명은 GitHub Secrets 로만 주입합니다(소스에 키/비밀번호 미포함).

## 상태 요약
- 핵심 순수 로직(태그 정규화·SHA256/지문·중복판정·게시판매칭·이미지검증·상태머신)은 **52/52 단위 검증 통과**.
- APK 빌드는 GitHub Actions 에서 수행/검증합니다(작성 환경에서는 Android SDK 부재로 직접 빌드 불가).
- 실기기 설치·접근성 실동작·Play Protect 통과는 **실제 기기 검증 전까지 미검증**입니다.
- 완료/기초/미검증 상세 구분은 [`docs/BUILD_STATUS.md`](docs/BUILD_STATUS.md) 를 반드시 확인하세요.
