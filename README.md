# LINKLE_Backend
Spring Boot 기반 백엔드 서버
<<<<<<< HEAD
=======

## 동네 친구 SNS (Local Friends SNS)

주변 사람들과 일상을 공유하고, 다양한 활동을 함께 즐길 수 있는 실시간 지도 / 채팅 / 포스팅 통합 플랫폼입니다.  
Spring Boot와 React를 기반으로, 백엔드-프론트엔드-데이터베이스-배포까지 풀스택 개발과 최신 기술 스택을 활용한 프로젝트입니다.

---

## 📌 프로젝트 개요

- **프로젝트 주제**: 지역기반 소통 플랫폼
- **목표**: 주변 사람들과의 소통과 활동 참여를 촉진하고, 실시간 정보와 다양한 부가 기능을 제공
- **전략**: Divide and Conquer (모듈화 및 기능별 병렬 개발)
  - 기능별 모듈화 (지도, 채팅, 포스트 등 도메인 분리)
  - 병렬 개발을 위한 API 명세
- **성과 목표**: 개인 역량 강화 + 실사용 가능한 플랫폼 완성

---

## 📋 주요 기능

| 기능 카테고리   | 세부 기능                                                                    |
|-----------|--------------------------------------------------------------------------|
| **지도·위치** | 카카오맵 API 연동, 내 위치 지도, 주변 명소 추천, 실시간 번개톡 연동, 클래스톡 연동, 링커 생성을 통한 다양한 참여 활동 |
| **채팅**    | 1:1 DM, 실시간 번개톡 생성, 클래스톡 생성, 채팅 목록/기록 CRUD, 채팅 알림                        |
| **포스트**   | 사진과 태그를 활용한 개인 이미지 히스토리                                                  |
| **검색**    | #해시태그 검색, 친구 검색, 장소 검색                                                   |
| **결제·구독** | 결제 API 연동                                                                |
| **AI/음성** | TTS 화면 읽기, AI 비서                                                         |
| **프로필**   | 프로필 및 배경 설정, 닉네임 설정                                                      |

---


## ⚙ 사용 기술 스택(미정)
| 영역    | 기술                                            |
|-------|-----------------------------------------------|
| 백엔드   | Spring Boot, Spring Security, JPA, QueryDSL   |
| 프론트엔드 | React, Axios, Tailwind (or styled-components) |
| DB    | MariaDB                                       |
| 인증    | JWT                                           |
| 배포    | Podman, Red Hat Quay                          |
| 기타    | TTS API, Toss Payments                        |

---

## 🛠 개발 환경

| 구분            | 상세                                                        |
|---------------|-----------------------------------------------------------|
| **언어**        | Java 17.0.6, Python 3.11                                  |
| **Framework** | Spring Boot 3.5.4, React 19.1.0                           |
| **DB**        | MariaDB 11.8.2                                            |
| **IDE**       | IntelliJ 2025.2, VSCode 1.102.3, DBeaver 25.1.0, Anaconda |
| **패키지 관리자**   | Gradle 8.14.3                                             |
| **버전 관리**     | Git, GitHub                                               |
| **배포 환경**     | Podman, Red Hat Quay.io                                   |

---

## 📦 설치 방법

```bash
# 저장소 클론
git clone https://github.com/LINKLE-2025/LINKLE_Backend.git
cd LINKLE
```

# 백엔드 실행 (Spring Boot)
```
./gradlew bootRun
```

## 🧱 아키텍처
```
plaintext
User → React → Spring Boot REST API → MariaDB
                    ↳ JWT Auth
                    ↳ Payment API
                    ↳ KaKaoMap API
                    ↳ Python API
```
---
## 🪪 라이선스
이 프로젝트는 MIT 라이선스를 따릅니다. [LICENSE](./LICENSE) 파일을 확인해주세요.
>>>>>>> c383a9a (초기 프로젝트 설정: Spring Boot init)
