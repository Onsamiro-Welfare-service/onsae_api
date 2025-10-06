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

## 📄 JSONB 컬럼 데이터 구조 예시

### 📍 원본 스키마 기반 (권장)

#### 1. users.emergency_contacts (비상 연락처)
```json
{
  "primary": {
    "name": "김영희",
    "relationship": "딸",
    "phone": "010-1111-2222",
    "email": "younghee@email.com",
    "isPrimary": true,
    "canPickup": true,
    "medicalDecision": true
  },
  "secondary": [
    {
      "name": "박사회복지사",
      "relationship": "담당자",
      "phone": "010-3333-4444",
      "email": "social@welfare.com",
      "canPickup": false,
      "medicalDecision": false,
      "notes": "담당 사회복지사"
    }
  ]
}
```

#### 2. questions.options (질문 옵션)

**객관식 (단일 선택)**
```json
{
  "type": "single",
  "options": [
    {"value": "1", "label": "매우 좋음"},
    {"value": "2", "label": "좋음"},
    {"value": "3", "label": "보통"},
    {"value": "4", "label": "나쁨"},
    {"value": "5", "label": "매우 나쁨"}
  ]
}
```

**객관식 (다중 선택)**
```json
{
  "type": "multiple",
  "options": [
    {"value": "exercise", "label": "운동"},
    {"value": "reading", "label": "독서"},
    {"value": "tv", "label": "TV 시청"},
    {"value": "cooking", "label": "요리"}
  ]
}
```

**척도형**
```json
{
  "type": "scale",
  "min": 1,
  "max": 10,
  "minLabel": "전혀 그렇지 않다",
  "maxLabel": "매우 그렇다"
}
```

**주관식**
```json
{
  "type": "text",
  "maxLength": 500,
  "placeholder": "자유롭게 입력해주세요"
}
```

**날짜/시간**
```json
{
  "type": "date",
  "minDate": "2024-01-01",
  "maxDate": "2024-12-31",
  "defaultToday": true
}
```

#### 3. question_responses.response_data (응답 데이터)

**객관식 응답 (기타 선택)**
```json
{
  "questionId": 123,
  "answer": "other",
  "otherText": "날씨가 흐려서 기분이 애매합니다"
}
```

**다중선택 응답 (기타 포함)**
```json
{
  "questionId": 124,
  "answers": ["exercise", "reading", "other"],
  "otherText": "친구들과 카페에서 수다떨기"
}
```

**주관식 응답**
```json
{
  "questionId": 125,
  "answer": "오늘은 날씨가 좋아서 산책을 했습니다. 기분이 상쾌해요!"
}
```

**척도형 응답**
```json
{
  "questionId": 126,
  "answer": 7,
  "note": "평소보다 조금 좋은 편"
}
```

**예/아니오 응답**
```json
{
  "questionId": 127,
  "answer": "yes",
  "note": "오전 9시에 측정했습니다"
}
```

#### 4. question_responses.device_info (기기 정보)
```json
{
  "platform": "android",
  "version": "13",
  "model": "SM-G973N",
  "appVersion": "1.2.3",
  "screenSize": {
    "width": 1080,
    "height": 2340
  },
  "networkType": "wifi",
  "batteryLevel": 85
}
```

#### 5. upload_files 관련 메타데이터 예시

**이미지 파일 정보**
```json
{
  "exif": {
    "camera": "Samsung Galaxy S21",
    "timestamp": "2024-09-20T14:30:00Z",
    "location": {
      "latitude": 37.5665,
      "longitude": 126.9780
    }
  },
  "processed": {
    "thumbnailGenerated": true,
    "compressionApplied": true,
    "originalSize": 2048000,
    "compressedSize": 512000
  }
}
```

**음성 파일 정보**
```json
{
  "audio": {
    "format": "mp3",
    "bitrate": 128,
    "sampleRate": 44100,
    "channels": 1
  },
  "processing": {
    "transcribed": false,
    "noiseReduced": true,
    "volumeNormalized": true
  }
}
```

---

### 🔄 확장된 API 포맷 (참고용)

질문 생성/응답 API에서 사용하는 확장된 JSON 구조입니다.

#### 질문 생성 예시 (POST /api/questions)

**객관식 (SINGLE_CHOICE)**
```json
{
  "title": "오늘 기분은 어떠신가요?",
  "content": "현재 기분 상태를 선택해주세요.",
  "questionType": "SINGLE_CHOICE",
  "categoryId": 1,
  "options": {
    "type": "single",
    "options": [
      {"value": "very_good", "label": "매우 좋음"},
      {"value": "good", "label": "좋음"},
      {"value": "normal", "label": "보통"},
      {"value": "bad", "label": "나쁨"},
      {"value": "very_bad", "label": "매우 나쁨"}
    ]
  },
  "allowOtherOption": true,
  "otherOptionLabel": "기타",
  "isRequired": true
}
```

**복수선택 (MULTIPLE_CHOICE)**
```json
{
  "title": "좋아하는 활동을 모두 선택해주세요",
  "content": "복지관에서 참여하고 싶은 활동들을 선택하세요.",
  "questionType": "MULTIPLE_CHOICE",
  "categoryId": 2,
  "options": {
    "type": "multiple",
    "options": [
      {"value": "cooking", "label": "요리교실"},
      {"value": "exercise", "label": "운동프로그램"},
      {"value": "culture", "label": "문화활동"},
      {"value": "education", "label": "교육프로그램"}
    ]
  },
  "allowOtherOption": true,
  "isRequired": true
}
```

**척도형 (SCALE)**
```json
{
  "title": "복지관 서비스 만족도",
  "content": "전반적인 서비스 만족도를 평가해주세요.",
  "questionType": "SCALE",
  "categoryId": 1,
  "options": {
    "type": "scale",
    "min": 1,
    "max": 5,
    "minLabel": "매우 불만족",
    "maxLabel": "매우 만족"
  },
  "isRequired": true
}
```

#### 질문 응답 예시 (POST /api/user/questions/responses)

**객관식 응답**
```json
{
  "assignmentId": 123,
  "answer": {
    "questionId": 123,
    "answer": "normal",
    "otherText": null
  }
}
```

**복수선택 응답**
```json
{
  "assignmentId": 124,
  "answer": {
    "questionId": 124,
    "answers": ["cooking", "exercise", "other"],
    "otherText": "음악치료"
  }
}
```

**척도형 응답**
```json
{
  "assignmentId": 125,
  "answer": {
    "questionId": 125,
    "answer": 4,
    "note": "만족합니다"
  }
}
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