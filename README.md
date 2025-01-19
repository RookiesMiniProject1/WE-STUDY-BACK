### We-Study: 학습 협업 웹 서비스 백엔드

---

### **프로젝트 개요**  
**We-Study**는 학생들과 취업 준비생들을 위한 통합 학습 협업 플랫폼입니다. 이 플랫폼은 효과적인 스터디 그룹 매칭, 멘토링, 과제 관리, 실시간 소통을 지원하며, 사용자들에게 체계적이고 협업 중심의 학습 환경을 제공합니다.

---

### **🛠 기술 스택**  
- **프로그래밍 언어 및 프레임워크**  
  - Java 17  
  - Spring Boot  

- **보안 및 인증**  
  - Spring Security  
  - JWT Authentication  
  - BCrypt 암호화  

- **데이터 관리**  
  - JPA/Hibernate  
  - H2 Database / mysql 

- **실시간 통신**  
  - WebSocket  

---

### **주요 기능 및 컨트롤러 구조**  

#### 1. **사용자 인증 (AuthController)**  
- **회원가입**: `/api/auth/register`  
- **로그인**: `/api/auth/login`  
- **JWT 토큰 기반 인증**  
- 멘토/멘티 구분 회원가입  

#### 2. **스터디 그룹 관리 (StudyGroupController)**  
- **그룹 생성**: `/api/groups`  
- **그룹 가입/탈퇴**: `/api/groups/{groupId}/join`  
- **멘토 매칭**: `/api/groups/match`  
- 그룹 멤버 관리 및 자동 매칭 시스템  

#### 3. **게시판 (BoardController)**  
- **게시글 CRUD**: `/api/posts`  
- 게시판 유형:  
  - 공지사항  
  - 자유게시판  
  - 포트폴리오  
- 권한별 게시판 접근 제어  

#### 4. **댓글 (CommentController)**  
- **댓글 작성/수정/삭제**: `/api/posts/{postId}/comments`  
- 게시글별 댓글 관리  

#### 5. **과제 관리**  
- **과제 관리 (TaskController)**  
  - 과제 생성 및 조회: `/api/groups/{groupId}/tasks`  
- **과제 제출 (TaskSubmissionController)**  
  - 과제 제출: `/api/groups/{groupId}/tasks/{taskId}/submissions`  
  - 멘토 피드백 및 피어 리뷰 시스템  

#### 6. **캘린더 (CalendarController)**  
- 개인/그룹 일정 관리: `/api/calendar`  
- 과제 일정 연동  
- 일정 CRUD  

#### 7. **칸반보드 (KanbanBoardController)**  
- 작업 상태 관리: `/api/groups/{groupId}/board`  
- 칸반 아이템 생성/수정/삭제  
- 진행 상황 추적  

#### 8. **채팅 (ChatController)**  
- WebSocket 기반 실시간 채팅: `/ws/chat`  
- 채팅방 생성/관리  
- 메시지 송수신  

---

### **주요 보안 및 인증 기능**  
- Spring Security 기반 권한 제어  
- JWT 토큰 인증  
- 비밀번호 BCrypt 암호화  
- 권한 기반 접근 제어  

---

### **데이터베이스 설계**  
- **데이터베이스**: H2 Database -> mysql -> 차후 클라우드로 완전 옮길 예정 
- **ORM**: JPA/Hibernate  
- **엔티티 간 복잡한 관계 매핑**  

---

### **프로젝트 구조**  

```plaintext
src/main/java/com/example/m_project1/
├── config/          # 설정 파일
├── controller/      # REST API 컨트롤러
├── service/         # 비즈니스 로직
├── repository/      # 데이터 접근 계층
├── entity/          # 도메인 모델
├── dto/             # 데이터 전송 객체
├── exception/       # 예외 처리
└── util/            # 유틸리티 클래스
```

---

### **개발팀**  
- 장은상 (프론트)  
- 박소하 (프론트)
- 엄준현 (백엔드) 
- 이현영 (기획 및 문서)

---

### **프로젝트 목표**  
1. 체계적인 학습 환경 제공  
2. 효과적인 스터디 그룹 매칭  
3. 멘토링을 통한 학습 성장 지원  

---

### **향후 계획**  
- AI 기반 매칭 고도화  
- 실시간 협업 기능 확장  
- 멀티 플랫폼 지원  

---
### API 명세서
[We-Study API 명세서 1 (Notion)](https://cooked-hockey-a64.notion.site/We-Study-API-Part-1-Ver-1-4-17ddc215b6d580899eb0e8ed14401001?pvs=4)

[We-Study API 명세서 2 (Notion)](https://cooked-hockey-a64.notion.site/We-Study-API-Part-2-17ddc215b6d5800baf8aedae1e164e4b?pvs=4)
