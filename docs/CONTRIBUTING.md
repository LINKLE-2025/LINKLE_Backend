# LINKLE_Contribute

## 코드 스타일 가이드(변경예정)
- Java: Google Java Style Guide 준수
- 클래스명은 UpperCamelCase, 변수/메서드는 lowerCamelCase
- 컨트롤러는 `@RestController` 또는 `@Controller`로 명확히 구분
- 예외 처리는 `GlobalExceptionHandler` 클래스에서 통합 관리

## 기여가이드
| 타입       | 설명                |
|----------|-------------------|
| feat     | 새로운 기능 추가         |
| fix      | 버그 수정             |
| docs     | 문서 수정             |
| style    | 코드 포맷팅, 세미콜론 누락 등 |
| refactor | 기능 변화 없는 코드 구조 변경 |
| test     | 테스트 코드 추가/수정      |

## 브랜치 관리
| 브랜치      | 기능                 | 설명                                                        |
|----------|--------------------|-----------------------------------------------------------|
| main     | 배포 가능하거나 안정된 버전    | .gitignore, 의존성 실험 새로운 기능 추가 등은 직접하지 않음                   | 
| develop  | 실제 개발 작업이 이뤄지는 브랜치 | 일반적으로 main과 develop을 병합할 때는 PR(Pull Request) 또는 Merge를 사용 |
| feature  | 새로운 기능 추가          | 로그인, 업로드 등 새로운 기능 개발                                      |
| fix      | 기존 기능 수정(버그 수정 포함) | 에러, 예외 오타, 로직 수정 등                                        |
| refactor | 리팩토링 전요            | 기능 변화 없이 내부 코드 구조만 개선                                     |
| chore    | 설정/빌드/배포 작업        | .gitignore, CI 설정, dependency 변경 등                        |

## PR 작성 가이드
- 제목: `feat: 채팅 기능 추가`, `fix: 회원가입 에러 수정`
- 본문: 변경사항 요약, 관련 이슈 (있다면), 테스트 방법 포함
- 리뷰어 지정, 라벨 붙이기 (optional)


- [🗂 버전 관리 정책](./docs/VERSIONING.md)