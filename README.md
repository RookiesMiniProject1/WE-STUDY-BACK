# We-Study: 학습 협업 웹 서비스 백엔드

## 📋 프로젝트 개요
**We-Study**는 학생들과 취업 준비생들을 위한 통합 학습 협업 플랫폼입니다. 이 플랫폼은 효과적인 스터디 그룹 매칭, 멘토링, 과제 관리, 실시간 소통을 지원하며, 사용자들에게 체계적이고 협업 중심의 학습 환경을 제공합니다.

## 🛠 기술 스택
복잡한 엔티티 관계 매핑 (Study Group - User - Task 등)
### Backend
- **Framework & Language**
  - Java 17
  - Spring Boot 3.x
  - Spring Security
  - Spring Data JPA

### Database
- **Development & Test**
  - H2 Database (테스트용)
  - MySQL 8.0 (로컬 개발 환경)
- **Production** *(현재 개발중)*
  - AWS RDS (MySQL)
  > AWS RDS로 연결했으나, 상시 구동이 불가능한 상황 -> 다시 로컬 MySQL로 개발 진행중이며, 추후 AWS RDS로 완전히 전환 예정

### Authentication & Security
- JWT Token 기반 인증
- Spring Security
- BCrypt 암호화

### Real-time Communication
- WebSocket (STOMP)
- SockJS

## 💡 주요 기능

### 1. 사용자 관리
- 멘토/멘티 역할 기반 회원가입
- JWT 기반 인증
- 프로필 및 기술 스택 관리

### 2. 스터디 그룹
- 그룹 CRUD
- 멘토-멘티 매칭
- 기술 스택 기반 자동 매칭
- 그룹 권한 관리

### 3. 학습 관리
- 과제 출제 및 제출
- 멘토 피드백
- 동료 평가
- 진행 상황 트래킹

### 4. 협업 도구
- 실시간 채팅
- 일정 관리
- 칸반보드
- 게시판 시스템

## 💡 주요 기능 상세

### 1. 사용자 관리 시스템
- **회원 관리**
  - 역할 기반 회원가입 (멘토/멘티)
  - 이메일 기반 로그인
  - JWT 토큰 인증/인가
  - 비밀번호 BCrypt 암호화
- **프로필 관리**
  - 기술 스택 등록/수정
  - 멘토 경력 관리
  - 관심 분야 설정

### 2. 스터디 그룹 시스템
- **그룹 관리**
  - 스터디 그룹 CRUD
  - 그룹 멤버 권한 관리 (리더/멤버)
  - 그룹 가입 신청/승인/거절
  - 멤버 제한 및 모집 상태 관리
- **멘토링 시스템**
  - 멘토 매칭 신청/수락/거절
  - 멘토 권한 관리
  - 멘토-멘티 상호작용

### 3. 과제 관리 시스템
- **과제 관리**
  - 멘토의 과제 출제/수정/삭제
  - 과제 제출 기한 설정
  - 과제 진행 상태 추적
- **과제 제출**
  - 과제 제출/수정 기능
  - 파일 업로드 지원
  - 제출 현황 확인
- **피드백 시스템**
  - 멘토 피드백 및 점수 평가
  - 동료 평가 시스템
  - 평가 점수 집계

### 4. 실시간 소통 시스템
- **채팅 기능**
  - WebSocket 기반 실시간 채팅
  - 그룹별 채팅방 생성/관리
  - 채팅 이력 저장 및 조회
  - 실시간 알림

### 5. 게시판 시스템
- **게시판 종류**
  - 공지사항 게시판
  - 자유게시판
  - 포트폴리오 게시판
  - 그룹별 게시판
- **게시글 관리**
  - 게시글 CRUD
  - 댓글 시스템
  - 파일 첨부
  - 권한별 접근 제어

### 6. 일정 관리 시스템
- **캘린더 기능**
  - 개인 일정 관리
  - 그룹 일정 관리
  - 과제 일정 연동
  - 일정 알림 설정

### 7. 작업 관리 시스템
- **칸반보드**
  - 작업 상태 관리 (Todo/In Progress/Done)
  - 우선순위 설정
  - 담당자 지정
  - 과제 연동

### 8. 보안 및 권한 관리
- **인증 시스템**
  - JWT 기반 토큰 인증
  - Spring Security 통합
- **권한 관리**
  - 멘토/멘티 역할 기반 접근 제어
  - API 엔드포인트 보안
  - 리소스별 권한 검증


## 🔧 개발 환경 설정
### 📋 데이터베이스 설정
#### 로컬 개발 환경 (MySQL)

MySQL 설치 및 데이터베이스 생성:
CREATE DATABASE we_study CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

데이터베이스 사용자 생성

### 설정 파일

#### application.properties 파일:
##### #Application 설정
spring.application.name=m_project1

spring.profiles.active=dev

##### #MySQL Configuration (환경 변수 사용)
spring.datasource.url=${DB_URL}

spring.datasource.username=${DB_USERNAME}

spring.datasource.password=${DB_PASSWORD}

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect


##### #JPA/Hibernate Configuration
spring.jpa.show-sql=true

spring.jpa.hibernate.ddl-auto=update

spring.jpa.properties.hibernate.format_sql=true

logging.level.org.hibernate.SQL=info

##### #JWT Configuration (환경 변수 사용)
jwt.expiration=3600000

jwt.secret=${JWT_SECRET_KEY}

##### #Server Configuration
server.servlet.encoding.charset=UTF-8

server.servlet.encoding.force=true

##### #Jackson Configuration
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss


#### application-dev.properties 파일 (개발 환경 전용 예시):
##### #Database
DB_URL=jdbc:mysql://localhost:3306/we_study?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8(예시)

DB_USERNAME=설정

DB_PASSWORD=설정

##### #JWT
JWT_SECRET_KEY=랜덤 생성된 키 (예: zGhpcy1pcy1hLXN1...)

##### 랜덤 키 생성 로직
`JwtUtil` 클래스는 키가 사전에 설정되지 않았을 때, JJWT의 `Keys.secretKeyFor(SignatureAlgorithm.HS512)`를 사용해 키를 생성합니다. 이 키는 HMAC-SHA512 알고리즘에 적합하며, 강력한 암호화를 보장합니다.

#### ⚙️ 실행 방법
MySQL 실행 후 데이터베이스 생성 및 사용자 권한 부여.
application.properties 및 application-dev.properties 파일 설정.
프로젝트를 빌드하고 실행


## 📁 프로젝트 구조
```
src/main/java/com/example/m_project1/
├── config/          # 설정 파일
├── controller/      # REST API 컨트롤러
├── service/         # 비즈니스 로직
├── repository/      # 데이터 접근 계층
├── entity/          # 도메인 모델
├── dto/             # 데이터 전송 객체
├── exception/       # 예외 처리
└── util/           # 유틸리티 클래스
```

## 👥 개발팀
- 장은상 (프론트엔드)
- 박소하 (프론트엔드)
- 엄준현 (백엔드)
- 이현영 (기획/문서)

## 📌 API 문서
- [We-Study API 명세서 Part 1](https://cooked-hockey-a64.notion.site/We-Study-API-Part-1-Ver-1-4-17ddc215b6d580899eb0e8ed14401001?pvs=4)
- [We-Study API 명세서 Part 2](https://cooked-hockey-a64.notion.site/We-Study-API-Part-2-17ddc215b6d5800baf8aedae1e164e4b?pvs=4)

## 🚀 향후 계획
- 리팩토링 반드시 필요
- 프론트엔드 미구현 된 부분 구현 후 연동 필요
- AWS RDS 연동 및 안정화
- 실시간 채팅 기능 개선
- AI 기반 매칭 시스템 고도화
- 성능 최적화(리프레시 토큰 재도입 등) - 재도입 완료

## ⚙️ 실행 환경 설정
프로젝트를 로컬에서 실행하시려면 다음 단계를 따라주세요:

1. MySQL 설치 및 데이터베이스 생성
2. `application-dev.properties.example`을 참고하여 `application-dev.properties` 생성
3. 데이터베이스 접속 정보 설정
4. Gradle 프로젝트 빌드 및 실행

   
