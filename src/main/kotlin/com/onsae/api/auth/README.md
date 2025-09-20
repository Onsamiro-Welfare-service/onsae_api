# Auth Domain

인증 및 인가 관련 기능을 담당하는 도메인입니다.

## 주요 기능

- **로그인**: SystemAdmin, Admin, User 로그인
- **JWT 토큰**: 토큰 생성, 검증, 갱신
- **권한 검증**: 역할 기반 접근 제어

## 구성 요소

### Controller
- `AuthController`: 로그인, 토큰 갱신 API

### Service
- `AuthService`: 인증 비즈니스 로직
- `JwtService`: JWT 토큰 관리

### Exception
- `InvalidCredentialsException`: 잘못된 인증 정보
- `InvalidTokenException`: 유효하지 않은 토큰
- `TokenExpiredException`: 토큰 만료
- `AccountNotActivatedException`: 계정 비활성화
- `AccountSuspendedException`: 계정 정지

## API 엔드포인트

```
POST /api/auth/login              # 로그인
POST /api/auth/refresh            # 토큰 갱신
POST /api/auth/logout             # 로그아웃
```