FitQuest 백엔드
개요  
FitQuest 백엔드는 달리기 기록을 기반으로 퀘스트 진행 및 보상 시스템을 제공하는 백엔드 API 서버입니다.  
사용자 인증, 러닝 기록 관리, 퀘스트 진행 상태 관리, 보상 지급 로직을 중심으로 구현되었습니다.  
본 레포지토리는 백엔드 서버 구현 및 API 제공을 목적으로 하며,
프론트엔드(Android 앱)와 REST API 방식으로 연동됩니다.


기술 스택 :  
Python 3.x  
Django  
Django REST Framework  
SQLite3 (개발 환경)  
Docker / Docker Compose (운영 환경)  
Gunicorn  
Nginx  
GitHub Actions (CI/CD)  
AWS EC2 (Ubuntu)  


프로젝트 구조 :  
.  
├── config/                 # Django 프로젝트 설정  
├── fitquest/               # 인증 및 사용자 관련 기능  
├── workout/                # 러닝, 퀘스트, 보상 도메인  
├── manage.py  
├── requirements.txt  
└── README.md  

config  
Django 프로젝트 전반 설정  
URL 라우팅 및 환경 설정 관리  

fitquest  
회원가입 및 로그인  
JWT 기반 인증  
Kakao OAuth 로그인 처리  
사용자 정보 조회 API  

workout  
달리기 기록(Running Session) 생성 및 조회  
러닝 통계 API (일간 / 주간 / 최근 기록)  
퀘스트 목록 조회  
사용자 퀘스트 진행 상태 관리  
퀘스트 완료 시 보상 지급 로직  



API 구성 요약 :  
인증(Auth)  
회원가입  
로그인 (JWT 토큰 발급)  
사용자 정보 조회  
Kakao OAuth 로그인  
러닝(Running)  
러닝 기록 생성  
러닝 기록 조회  
러닝 통계 조회  
퀘스트(Quest)  
활성 퀘스트 목록 조회  
사용자 퀘스트 진행 현황 조회  
퀘스트 보상 수령  



환경 변수 설정 :  
DJANGO_SECRET_KEY  
DJANGO_DEBUG  
ALLOWED_HOSTS  
Kakao OAuth 관련:  
KAKAO_REST_API_KEY  
KAKAO_REDIRECT_URI  
※ 실제 키 값은 GitHub에 커밋하지 않으며,  
운영 환경에서는 GitHub Secrets를 통해 관리합니다.  




로컬 개발 환경 :   
pip install -r requirements.txt  

python manage.py makemigrations  
python manage.py migrate  
python manage.py runserver  
Django 개발 서버를 사용하여 로컬 테스트를 진행했습니다.  
API 테스트는 Postman을 이용해 수행했습니다.  



운영 환경 :  
AWS EC2 (Ubuntu)  
Docker 및 Docker Compose 기반 실행  
Gunicorn을 통한 Django 서비스 구동  
Nginx를 Reverse Proxy로 사용  
GitHub Actions를 이용한 CI/CD 자동 배포  
운영 서버에서는 GitHub Actions를 통해 자동 배포되며,  
직접 컨테이너를 수동 실행하지 않습니다.  



현재 구현 상태 :  
사용자 인증 API 구현 완료  
러닝 기록 및 통계 API 구현 완료  
퀘스트 및 보상 시스템 구현 완료  
프론트엔드(Android 앱)와 REST API 연동 테스트 완료  
CI/CD 및 서버 배포 환경 구성 진행 중  



본 프로젝트는 캡스톤 디자인 과제로 제작된 교육용 프로젝트입니다.
