##  프로젝트 개요 (Overview)

FitQuest는 운동 초보자도 재미있고 꾸준하게 달리기를 실천할 수 있도록 설계된  
**퀘스트 기반 러닝 동기부여 플랫폼**입니다.  
사용자는 러닝 기록을 남기고, 기록을 기반으로 퀘스트를 완료하며 경험치와 보상을 얻습니다.  
이러한 반복 과정을 통해 자연스럽게 운동 습관을 만들 수 있도록 돕는 것이 핵심 목표입니다.

본 레포지토리는 FitQuest의 **Backend 서버(Django REST Framework)** 전체 기능을 포함하며,  
Android 앱과 **JWT 인증**을 기반으로 통신합니다.  
또한 **카카오 OAuth 로그인**, Running Summary/Statistics API, Quest Progress 시스템 등  
서비스 핵심 기능이 모두 구현되어 있습니다.

---

## 주요 기능 요약 (Key Features)

### 1. 인증(Authentication)
- 이메일 기반 회원가입 및 로그인 기능 제공
- JWT Access/Refresh Token 발급 및 인증 유지
- `/auth/me` 를 통한 사용자 프로필 조회 지원
- 카카오 OAuth 연동  
  → 프론트에서 전달받은 카카오 Access Token 검증  
  → 신규 유저면 자동 회원가입  
  → 기존 유저면 즉시 로그인(JWT 재발급)

### 2. Running 기능 (Running Session API)
- 러닝 기록 **생성 / 조회 / 수정 / 삭제(CRUD)** 지원
- Running 세션 데이터: 거리, 시간, 페이스, 시작/종료 시간 등
- 사용자별 요약 제공:
  - **오늘(today)** 러닝 기록 요약
  - **이번 주(week)** 누적 데이터
  - **최근 7일(7days)** 간 트래킹
- 전체 기록 기반 **Running 통계 API** 제공

### 3. Quest 시스템 (Quest Progress System)
- 전체 퀘스트 목록 조회
- 사용자별 퀘스트 진행도 반환
- Running 기록과 연동하여 퀘스트 자동 진행 업데이트
- 보상 지급(경험치 EXP, 포인트 Point)
- `/claim/<id>/` API를 통한 보상 지급 후 진행도 업데이트

### 4. Frontend 연동 구조
- 모든 요청은 JWT 인증 기반으로 처리
- Android Kotlin 앱에서 Axios/Volley로 서버와 통신
- 카카오 SDK로 받은 토큰을 백엔드에 전달하면  
  → 백엔드가 카카오 서버와 검증 후 JWT 발급

---

## 기술 스택 (Tech Stack)

- **Backend Framework**: Django 5, Django REST Framework  
- **Auth**: JWT(SimpleJWT), Kakao OAuth  
- **Database**: SQLite3 (개발 환경)  
- **Infrastructure**: Docker, Docker Compose  
- **Language**: Python 3.10  
- **Version Control**: GitHub

---

## 프로젝트 구조 (Project Structure)
capstone25-fit-quest/
├── config/ # Django settings, URLs, WSGI

├── fitquest/ # Auth, User, Kakao 로그인 관련 도메인

├── workout/ # Running + Quest 시스템

├── docker-compose.yml

├── Dockerfile

└── manage.py


---

##  프로젝트 목적 (Goal of the Project)

본 프로젝트는 단순한 운동 기록 앱이 아니라,  
**“운동을 게임처럼 재미있게 만드는 동기부여 시스템”**을 만드는 것을 목표로 한다.

- 퀘스트 · 보상 · 경험치 시스템 도입  
- 러닝 데이터를 기반으로 진행도 자동 업데이트  
- 사회적 경쟁 대신 개별 성장에 초점  
- 초보자도 쉽게 접근할 수 있는 UX 설계  

백엔드는 이러한 서비스 전반을 안정적으로 지원하는 핵심 로직을 담당하며,  
안드로이드 앱 및 카카오 플랫폼과 안정적으로 연동되도록 설계되어 있다.
