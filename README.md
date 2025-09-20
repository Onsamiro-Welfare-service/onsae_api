# 온새 API (Onsae API)

복지관 케어 시스템 백엔드 API 서버입니다.

## 🏗️ 기술 스택

- **Framework**: Spring Boot 3.2.0
- **Language**: Kotlin 1.9.20
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA + Hibernate
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle

## 📊 주요 기능

- **멀티테넌시**: 여러 복지관의 데이터 완전 분리
- **사용자 관리**: 시스템 관리자, 기관 관리자, 복지관 이용자
- **질문/설문 시스템**: 동적 질문 생성 및 할당
- **응답 수집**: 매일 답변 수집 및 분석
- **파일 업로드**: 이미지, 음성, 비디오 등 다양한 파일 지원

## 🚀 시작하기

### 1. 환경 설정

```bash
# .env 파일 생성
cp .env.example .env

# 환경 변수 설정
vim .env
```

### 2. 데이터베이스 설정

```sql
-- PostgreSQL에서 데이터베이스 생성
CREATE DATABASE welfare_care;
CREATE USER welfare_care WITH PASSWORD 'welfare_care';
GRANT ALL PRIVILEGES ON DATABASE welfare_care TO welfare_care;
```

### 3. 애플리케이션 실행

```bash
# 로컬 환경에서 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 개발 환경에서 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 📁 프로젝트 구조

```
src/main/kotlin/com/onsae/api/
├── config/           # 설정 클래스들
├── controller/       # REST 컨트롤러
├── service/          # 비즈니스 로직
├── repository/       # 데이터 접근 계층
├── entity/           # JPA 엔티티
├── dto/              # 데이터 전송 객체
├── exception/        # 예외 처리
├── security/         # 보안 관련
└── util/             # 유틸리티
```

## 🔐 보안

### 인증 방식
- **시스템 관리자**: 이메일 + 비밀번호
- **기관 관리자**: 이메일 + 비밀번호
- **복지관 이용자**: 기관코드 + 사용자코드

### 권한 체계
- `ROLE_SYSTEM_ADMIN`: 시스템 전체 관리
- `ROLE_ADMIN`: 기관 내 모든 권한
- `ROLE_STAFF`: 기관 내 일반 관리자
- `ROLE_USER`: 복지관 이용자

### 멀티테넌시
- JWT 토큰에 `institution_id` 포함
- 모든 API에서 기관별 데이터 격리
- 자동 데이터 접근 제어

## 📊 데이터베이스 스키마

### 주요 테이블 (14개)

#### 기관 및 관리자 (3개)
- `institutions`: 복지관 정보
- `system_admins`: 시스템 관리자
- `admins`: 기관 관리자

#### 사용자 및 그룹 (3개)
- `users`: 복지관 이용자
- `user_groups`: 사용자 그룹
- `user_group_members`: 그룹 멤버십

#### 질문 및 설문 (4개)
- `categories`: 질문 카테고리
- `questions`: 질문
- `question_assignments`: 질문 할당
- `question_responses`: 설문 응답

#### 업로드 (2개)
- `uploads`: 업로드 정보
- `upload_files`: 업로드 파일

#### 템플릿 (2개)
- `assignment_templates`: 할당 템플릿
- `template_questions`: 템플릿-질문 연결

## 🌍 환경별 설정

### Local (개발)
- H2 Console 활성화
- SQL 로그 출력
- 자동 DDL 업데이트

### Dev (개발 서버)
- PostgreSQL 연결
- 연결 풀 설정
- 기본 로깅

### Prod (운영)
- 성능 최적화 설정
- 보안 강화
- 최소 로깅

## 📝 API 문서

API 문서는 추후 Swagger/OpenAPI로 제공될 예정입니다.

### 주요 엔드포인트

```
POST /api/auth/login              # 로그인
POST /api/auth/refresh            # 토큰 갱신

GET  /api/admin/users             # 사용자 목록
POST /api/admin/questions         # 질문 생성
POST /api/admin/assignments       # 질문 할당

GET  /api/user/questions          # 할당된 질문 조회
POST /api/user/responses          # 응답 제출
POST /api/user/uploads            # 파일 업로드
```

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "UserServiceTest"
```

## 📦 배포

```bash
# JAR 빌드
./gradlew bootJar

# Docker 이미지 빌드 (추후 지원)
docker build -t onsae-api .
```

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스

This project is proprietary software.