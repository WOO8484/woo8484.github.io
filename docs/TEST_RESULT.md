# TEST_RESULT — v1.0.0

## 1. 핵심 순수 로직 실행 검증 — 통과 52 / 실패 0
작성 환경에서 Kotlin 컴파일러(kotlinc 1.9.24)로 아래 모듈을 **실제 컴파일·실행**하여 확인했습니다.
(Android 프레임워크 미의존 순수 로직. 콘솔 한글 렌더링 제한과 무관하게 소스/결과는 정상.)

| 영역 | 케이스 | 통과 |
|---|---|---|
| TagNormalizer (v0.1.1 이식) | 붙은 해시태그 분리, 혼합 구분자, 중복/순서, placeholder(전체/개별), 특수문자, 빈 입력, 표시문자열, 초장문 폐기 | 9/9 |
| MetadataMapper | 2.1 필드(post_id/category/image_max/content_version/tags), 구버전 승격/별칭/문자열태그/keyword/기본버전 | 10/10 |
| ContentFingerprint | SHA-256 표준벡터(""·"abc"), 지문(공백·구두점 무시 동일 / 내용 다르면 상이) | 4/4 |
| DuplicateChecker | 동일 ZIP/본문→IDENTICAL, 지문→SUSPECT, 기간내/밖 주제, 신규→NORMAL, 수정본 예외 | 7/7 |
| BoardMatcher | 프로필 9개, 정확/별칭/공백무시 일치, 미매칭 null | 5/5 |
| ImageValidator | 정상5→NORMAL, 권장미만→CAUTION, 11장초과→ERROR, metadata불일치, 0바이트손상, 내용중복감지 | 6/6 |
| PipelineStateMachine | 초기/advance×2, pause/재개지점/resume, retry, fail, 종료전이불가, 정지·종료 플래그 | 11/11 |
| **합계** | | **52/52** |

## 2. CI 단위 테스트 (`./gradlew test`)
GitHub Actions 빌드에서 아래 JUnit 테스트가 재실행됩니다.

### v0.1.1 이식 테스트
- `PackageParserTest` : 정상 5장/3장, 엔트리 51개·텍스트 2MB·단일 25MB·전체 100MB 초과 거부, 중복 basename, 고압축률, 이미지 11개, 빈 ZIP, 손상 ZIP 안전중단, 제어문자 파일명 거부.
- `TagNormalizerTest` : 태그 정규화 규칙.
- `TagInputControllerTest` : 태그 1개씩 진행 상태.

### v1.0.0 신규 테스트
- `CoreLogicTest` : MetadataMapper(2.1/구버전), ContentFingerprint(표준벡터/지문), DuplicateChecker(4판정+수정본), BoardMatcher(정확/별칭/미매칭), ImageValidator(범위/손상), PipelineStateMachine(진행/정지/재개/실패).

> `PackageParserTest` 는 v0.1.1 과 동일하게 metadata.json 을 포함하지 않아 로컬 JVM 단위 테스트에서 org.json 을 호출하지 않습니다.

## 3. 빌드 검증
- `./gradlew clean test assembleDebug` 가 CI 에서 자동 실행되어 **컴파일 + 단위 테스트 + debug APK** 를 검증합니다.
- 산출물(APK)/테스트 리포트/빌드 로그는 Actions 아티팩트로 제공됩니다.

## 4. 미검증 (⛔ 실기기 필요)
- 실기기 설치 및 실행
- 네이버 실제 글쓰기 화면에서의 접근성 자동 입력 성공/타임아웃 동작
- Google Play Protect 통과
- 기기/네이버 앱 버전별 호환성

이 항목들은 실제 기기 검증 전까지 **미검증**으로 남습니다(BUILD_STATUS.md 참고).
