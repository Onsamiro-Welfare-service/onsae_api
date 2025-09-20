# File Domain

파일 업로드 및 관리 관련 기능을 담당하는 도메인입니다.

## 주요 기능

- **Upload**: 파일 업로드 요청 관리
- **File Processing**: 이미지, 음성, 비디오 처리
- **Thumbnail Generation**: 썸네일 생성
- **Admin Response**: 관리자 확인 및 응답

## Entity

- `Upload`: 업로드 정보
- `UploadFile`: 업로드 파일

## Enum

- `FileType`: IMAGE, AUDIO, VIDEO, DOCUMENT, TEXT

## 구성 요소

### Controller
- `UploadController`: 파일 업로드 API
- `FileController`: 파일 조회/다운로드 API

### Service
- `UploadService`: 업로드 비즈니스 로직
- `FileStorageService`: 파일 저장 관리
- `FileProcessingService`: 파일 처리 (썸네일, 메타데이터)

### Repository
- `UploadRepository`
- `UploadFileRepository`

## API 엔드포인트

```
# Upload
POST /api/user/uploads            # 파일 업로드
GET  /api/user/uploads            # 내 업로드 목록
GET  /api/admin/uploads           # 전체 업로드 목록
PUT  /api/admin/uploads/{id}/response # 관리자 응답

# File
GET  /api/files/{id}              # 파일 조회
GET  /api/files/{id}/download     # 파일 다운로드
GET  /api/files/{id}/thumbnail    # 썸네일 조회
```

## 파일 처리

### 지원 파일 타입
- **이미지**: JPG, PNG, GIF, WEBP
- **음성**: MP3, WAV, M4A
- **비디오**: MP4, AVI, MOV
- **문서**: PDF, DOC, DOCX
- **텍스트**: TXT, MD

### 처리 과정
1. 파일 업로드 검증 (타입, 크기)
2. 파일 저장 (원본)
3. 메타데이터 추출
4. 썸네일 생성 (이미지/비디오)
5. 압축 처리 (필요시)