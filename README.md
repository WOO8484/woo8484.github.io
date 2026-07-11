# GPT 공작소 v1.0.1

네이버 블로그 앱에서 **제목·본문·태그 입력을 보조**하고, 자료 ZIP 처리·이미지 저장·중복 글 방지를 돕는 Android 앱입니다.

- 패키지명: `com.gptgongjakso.naverwriterhelper`
- versionCode: `101` / versionName: `1.0.1`
- minSdk 30 / targetSdk 34 / compileSdk 34
- 빌드: AGP 8.5.2 · Kotlin 1.9.24 · Gradle 8.9

## v1.0.1 핵심 변경
- `네이버 블로그 앱 열기` 버튼은 `com.nhn.android.blog`만 실행합니다.
- 네이버 일반 앱과 모바일 브라우저 fallback을 제거했습니다.
- 블로그 앱 미설치 시 Play 스토어 설치 안내를 표시합니다.
- 접근성 허용 대상을 네이버 블로그 앱 1개로 제한했습니다.
- 글쓰기 화면 진입과 발행·임시저장은 사용자가 직접 수행합니다.

## 안전 원칙
- **발행·임시저장 버튼을 자동으로 누르지 않습니다.**
- **INTERNET 권한이 없습니다.** 앱 자체가 글·이미지·계정정보를 외부 서버로 전송하지 않습니다.
- 접근성 조작 대상은 `com.nhn.android.blog` 한 개로 제한합니다.
- 자동 입력 실패 시 클립보드 복사와 사용자 안내로 대체합니다.
- 로그인 정보와 서명키를 소스에 저장하지 않습니다.

## 주요 기능
1. 자료 ZIP 파싱과 ZIP 보안 검사
2. 제목·본문·태그 입력 보조
3. 이미지 갤러리 저장
4. SHA-256/지문 기반 중복 검사
5. 게시판 프로필 자동 매칭
6. ZIP 공유·열기 수신

## 빌드
자세한 절차는 [`docs/BUILD_LOCAL_AND_CI.md`](docs/BUILD_LOCAL_AND_CI.md)를 확인하세요.

```bash
./gradlew clean test assembleDebug
```

디버그 APK 경로:
`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions는 `.github/workflows/android-build.yml`에서 테스트·디버그 APK 빌드·로그 업로드를 수행합니다.

## 현재 검증 상태
- v1.0.0 대비 변경 범위와 보존 범위는 소스 비교로 확인했습니다.
- v1.0.1 Gradle 테스트와 APK 빌드는 아직 실행 성공 확인 전입니다.
- 네이버 블로그 앱 실제 입력 동작은 APK 설치 후 실기 검증이 필요합니다.
- 상세 결과: [`REVIEW_RESULT_GPT.md`](REVIEW_RESULT_GPT.md), [`TEST_RESULT.md`](TEST_RESULT.md)
