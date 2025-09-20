# User Domain

사용자 관리 관련 기능을 담당하는 도메인입니다.

## 주요 기능

- **SystemAdmin**: 시스템 관리자 관리
- **Admin**: 기관 관리자 관리 (승인/거부)
- **User**: 복지관 이용자 관리
- **UserGroup**: 사용자 그룹 관리

## Entity

- `SystemAdmin`: 시스템 관리자
- `Admin`: 기관 관리자
- `User`: 복지관 이용자
- `UserGroup`: 사용자 그룹
- `UserGroupMember`: 그룹 멤버십

## Enum

- `AdminRole`: ADMIN, STAFF
- `AccountStatus`: PENDING, APPROVED, REJECTED, SUSPENDED
- `SeverityLevel`: MILD, SEVERE

## 구성 요소

### Controller
- `SystemAdminController`: 시스템 관리자 API
- `AdminController`: 기관 관리자 API
- `UserController`: 사용자 API
- `UserGroupController`: 사용자 그룹 API

### Service
- `SystemAdminService`: 시스템 관리자 비즈니스 로직
- `AdminService`: 기관 관리자 비즈니스 로직
- `UserService`: 사용자 비즈니스 로직
- `UserGroupService`: 사용자 그룹 비즈니스 로직

### Repository
- `SystemAdminRepository`
- `AdminRepository`
- `UserRepository`
- `UserGroupRepository`
- `UserGroupMemberRepository`

## API 엔드포인트

```
# System Admin
GET  /api/system/admins           # 시스템 관리자 목록
POST /api/system/admins           # 시스템 관리자 생성

# Admin
GET  /api/admin/admins            # 기관 관리자 목록
POST /api/admin/admins            # 기관 관리자 생성
PUT  /api/admin/admins/{id}/approve  # 관리자 승인
PUT  /api/admin/admins/{id}/reject   # 관리자 거부

# User
GET  /api/admin/users             # 사용자 목록
POST /api/admin/users             # 사용자 생성
GET  /api/user/profile            # 사용자 프로필 조회
PUT  /api/user/profile            # 사용자 프로필 수정

# User Group
GET  /api/admin/groups            # 그룹 목록
POST /api/admin/groups            # 그룹 생성
POST /api/admin/groups/{id}/members # 그룹 멤버 추가
```