# Survey Domain

설문/질문 관리 관련 기능을 담당하는 도메인입니다.

## 주요 기능

- **Category**: 질문 카테고리 관리
- **Question**: 질문 생성 및 관리
- **Assignment**: 질문 할당 (개별/그룹)
- **Response**: 응답 수집 및 관리
- **Template**: 질문 템플릿 관리

## Entity

- `Category`: 질문 카테고리
- `Question`: 질문
- `QuestionAssignment`: 질문 할당
- `QuestionResponse`: 설문 응답
- `AssignmentTemplate`: 할당 템플릿
- `TemplateQuestion`: 템플릿-질문 연결

## Enum

- `QuestionType`: SINGLE_CHOICE, MULTIPLE_CHOICE, TEXT, SCALE, YES_NO, DATE, TIME
- `TemplateVisibility`: PRIVATE, SYSTEM
- `TargetType`: USER, GROUP, ALL

## 구성 요소

### Controller
- `CategoryController`: 카테고리 API
- `QuestionController`: 질문 API
- `AssignmentController`: 질문 할당 API
- `ResponseController`: 응답 API
- `TemplateController`: 템플릿 API

### Service
- `CategoryService`: 카테고리 비즈니스 로직
- `QuestionService`: 질문 비즈니스 로직
- `AssignmentService`: 할당 비즈니스 로직
- `ResponseService`: 응답 비즈니스 로직
- `TemplateService`: 템플릿 비즈니스 로직

## API 엔드포인트

```
# Category
GET  /api/admin/categories        # 카테고리 목록
POST /api/admin/categories        # 카테고리 생성

# Question
GET  /api/admin/questions         # 질문 목록
POST /api/admin/questions         # 질문 생성
PUT  /api/admin/questions/{id}    # 질문 수정

# Assignment
POST /api/admin/assignments       # 질문 할당
GET  /api/admin/assignments       # 할당 목록

# Response
GET  /api/user/questions          # 할당된 질문 조회
POST /api/user/responses          # 응답 제출
GET  /api/admin/responses         # 응답 조회

# Template
GET  /api/admin/templates         # 템플릿 목록
POST /api/admin/templates         # 템플릿 생성
```