# 프로덕션 환경 변수 설정 가이드

## 필수 환경 변수

### 데이터베이스 설정
```bash
# PostgreSQL 데이터베이스 호스트
DB_HOST=your-db-host.com

# PostgreSQL 데이터베이스 포트
DB_PORT=5432

# 데이터베이스 이름
DB_NAME=onsae_prod

# 데이터베이스 사용자명
DB_USERNAME=onsae_user

# 데이터베이스 비밀번호
DB_PASSWORD=your-secure-password
```

### JWT 설정
```bash
# JWT 시크릿 키 (최소 256비트 권장)
JWT_SECRET=your-super-secure-jwt-secret-key-minimum-256-bits

# JWT 액세스 토큰 만료 시간 (밀리초, 기본값: 86400000 = 24시간)
JWT_ACCESS_TOKEN_EXPIRATION=86400000

# JWT 리프레시 토큰 만료 시간 (밀리초, 기본값: 604800000 = 7일)
JWT_REFRESH_TOKEN_EXPIRATION=604800000
```

### 서버 설정
```bash
# 서버 포트 (기본값: 8080)
SERVER_PORT=8080

# Spring 프로파일 (필수: prod)
SPRING_PROFILES_ACTIVE=prod
```

### 파일 업로드 설정
```bash
# 파일 업로드 경로
FILE_UPLOAD_PATH=/var/www/onsae/uploads

# 최대 파일 크기 (기본값: 10MB)
FILE_UPLOAD_MAX_SIZE=10MB

# 최대 요청 크기 (기본값: 50MB)
FILE_UPLOAD_MAX_REQUEST_SIZE=50MB
```

### CORS 설정
```bash
# 허용할 오리진 목록 (쉼표로 구분)
# 예: https://onsae.com,https://app.onsae.com
CORS_ALLOWED_ORIGINS=https://your-production-domain.com
```

### 로그 설정
```bash
# 로그 파일 경로
LOG_FILE_PATH=/var/log/onsae/application.log

# 로그 레벨 (WARN, INFO, DEBUG 등)
LOG_LEVEL=WARN
```

## 선택적 환경 변수

### 타임존 및 로케일
```bash
# 타임존 (기본값: Asia/Seoul)
TIMEZONE=Asia/Seoul

# 로케일 (기본값: ko_KR)
LOCALE=ko_KR
```

## 프로덕션 환경변수 파일 예시

`.env.production` 파일을 생성하고 다음과 같이 설정:

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database Configuration
DB_HOST=prod-db-server.example.com
DB_PORT=5432
DB_NAME=onsae_production
DB_USERNAME=onsae_prod_user
DB_PASSWORD=change-this-to-secure-password

# JWT Configuration
JWT_SECRET=change-this-to-a-secure-random-string-at-least-256-bits-long
JWT_ACCESS_TOKEN_EXPIRATION=86400000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Server Configuration
SERVER_PORT=8080

# File Upload Configuration
FILE_UPLOAD_PATH=/var/www/onsae/uploads
FILE_UPLOAD_MAX_SIZE=10MB
FILE_UPLOAD_MAX_REQUEST_SIZE=50MB

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://onsae.com,https://app.onsae.com

# Logging Configuration
LOG_FILE_PATH=/var/log/onsae/application.log
LOG_LEVEL=WARN

# Timezone and Locale
TIMEZONE=Asia/Seoul
LOCALE=ko_KR
```

## 보안 주의사항

1. **JWT_SECRET**: 최소 256비트 길이의 랜덤 문자열 사용 (예: `openssl rand -base64 32`)
2. **DB_PASSWORD**: 강력한 비밀번호 사용
3. **환경변수 파일 권한**: 프로덕션 환경변수 파일은 읽기 전용 권한으로 설정 (예: `chmod 400 .env.production`)
4. **설정 파일 보안**: 환경변수 파일을 Git에 커밋하지 마세요
5. **CORS 설정**: 프로덕션 도메인만 허용하도록 설정

## 배포 시 체크리스트

- [ ] 데이터베이스 연결 정보 설정
- [ ] JWT 시크릿 키 생성 및 설정
- [ ] CORS 허용 오리진 설정
- [ ] 파일 업로드 경로 생성 및 권한 설정
- [ ] 로그 파일 경로 생성 및 권한 설정
- [ ] 환경변수 파일 권한 설정
- [ ] 보안 설정 확인 (패스워드, 시크릿 키 등)

