# Global Domain

전역적으로 사용되는 공통 기능들을 담당하는 도메인입니다.

## 주요 기능

- **전역 예외 처리**: 모든 도메인의 예외를 일괄 처리
- **API 응답 표준화**: 성공/실패 응답 형식 통일
- **로깅 및 모니터링**: 전역 로깅, 성능 모니터링

## 구성 요소

### Exception
- `GlobalExceptionHandler`: 전역 예외 처리기
- `ErrorResponse`: 에러 응답 DTO
- `ValidationErrorResponse`: 검증 오류 응답 DTO

## 처리하는 예외 타입

### 1. **비즈니스 예외**
- 각 도메인별 특화 예외들
- `BusinessException` 기반 예외들

### 2. **시스템 예외**
- `Exception`: 예상치 못한 시스템 오류
- `MethodArgumentNotValidException`: 검증 실패

### 3. **도메인별 예외**
- **auth**: 인증/인가 관련 예외
- **user**: 사용자 관리 예외
- **institution**: 기관 관리 예외
- **survey**: 설문/질문 관리 예외
- **file**: 파일 업로드 예외
- **notification**: 알림 관련 예외
- **dashboard**: 대시보드/통계 예외

## 응답 형식

### 성공 응답
```json
{
  "success": true,
  "data": {},
  "timestamp": 1234567890
}
```

### 에러 응답
```json
{
  "message": "에러 메시지",
  "code": "ERROR_CODE",
  "timestamp": 1234567890
}
```

### 검증 오류 응답
```json
{
  "message": "Validation failed",
  "code": "VALIDATION_FAILED",
  "timestamp": 1234567890,
  "errors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "이메일 형식이 올바르지 않습니다"
    }
  ]
}
```

## 로깅 전략

- **INFO**: 정상적인 비즈니스 플로우
- **WARN**: 비즈니스 예외, 복구 가능한 오류
- **ERROR**: 시스템 오류, 예상치 못한 예외
- **DEBUG**: 상세한 디버깅 정보 (개발 환경)