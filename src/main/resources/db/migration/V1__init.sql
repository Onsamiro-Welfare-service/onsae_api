-- 01-schema.sql
-- 복지관 케어 시스템 데이터베이스 스키마

-- 데이터베이스 생성 (이미 생성되어 있지만 확인차)
-- CREATE DATABASE welfare_care_dev;

-- ENUM 타입 정의 제거 (VARCHAR + CHECK 제약조건으로 대체)

-- =============================================================================
-- 1. 기관 및 관리자 관리
-- =============================================================================

-- 복지관(기관) 테이블
CREATE TABLE institutions (
                              id BIGSERIAL PRIMARY KEY,

    -- 기관 기본 정보
                              name VARCHAR(200) NOT NULL,
                              business_number VARCHAR(50) UNIQUE, -- 사업자번호
                              registration_number VARCHAR(50), -- 사회복지법인 등록번호

    -- 연락처 정보
                              address TEXT,
                              phone VARCHAR(20),
                              email VARCHAR(100),
                              website VARCHAR(200),

    -- 담당자 정보
                              director_name VARCHAR(100), -- 관장/센터장
                              contact_person VARCHAR(100), -- 담당자
                              contact_phone VARCHAR(20),
                              contact_email VARCHAR(100),

    -- 기관 상태
                              is_active BOOLEAN DEFAULT true,

    -- 시스템 정보
                              timezone VARCHAR(50) DEFAULT 'Asia/Seoul',
                              locale VARCHAR(10) DEFAULT 'ko_KR',

                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 시스템 관리자 테이블
CREATE TABLE system_admins (
                               id BIGSERIAL PRIMARY KEY,
                               email VARCHAR(100) UNIQUE NOT NULL,
                               password VARCHAR(255) NOT NULL,
                               name VARCHAR(50) NOT NULL,
                               is_active BOOLEAN DEFAULT true,
                               last_login TIMESTAMP,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 기관 관리자 테이블
CREATE TABLE admins (
                        id BIGSERIAL PRIMARY KEY,
                        institution_id BIGINT NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,

                        email VARCHAR(100) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        name VARCHAR(50) NOT NULL,

    -- 권한 및 승인 시스템
                        role VARCHAR(20) DEFAULT 'STAFF', -- ADMIN, STAFF
                        status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')), -- 계정 승인 상태

    -- 승인 관련 정보
                        approved_by BIGINT REFERENCES admins(id),
                        approved_at TIMESTAMP,
                        rejection_reason TEXT,

    -- 개인 정보
                        phone VARCHAR(20),

    -- 계정 상태
                        is_active BOOLEAN DEFAULT true,
                        last_login TIMESTAMP,

                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        UNIQUE(institution_id, email)
);

-- =============================================================================
-- 2. 사용자 관리
-- =============================================================================

-- 사용자 테이블
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       institution_id BIGINT NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,

    -- 로그인 정보 (username & pw)
                       username VARCHAR(100) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       phone VARCHAR(20),
                       address TEXT,
                       birth_date DATE,

    -- 장애 관련 정보
                       severity VARCHAR(20) NOT NULL CHECK (severity IN ('MILD', 'SEVERE')),

    -- 보호자 정보 (주 보호자)
                       guardian_name VARCHAR(50),
                       guardian_relationship VARCHAR(20), -- 부모, 형제, 배우자, 법정후견인 등
                       guardian_phone VARCHAR(20),
                       guardian_email VARCHAR(100),
                       guardian_address TEXT,

    -- 비상 연락처 (JSON 배열로 여러 연락처 저장)
                       emergency_contacts JSONB,

    -- 케어 정보
                       care_notes TEXT, -- 케어 주의사항 및 특이사항

    -- 시스템 정보
                       is_active BOOLEAN DEFAULT true,
                       last_login TIMESTAMP,
                       fcm_token VARCHAR(500), -- 푸시 알림용

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       UNIQUE(institution_id, username)
);

-- =============================================================================
-- 3. 사용자 그룹 관리
-- =============================================================================

-- 사용자 그룹 테이블
CREATE TABLE user_groups (
                             id BIGSERIAL PRIMARY KEY,
                             institution_id BIGINT NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,

                             name VARCHAR(100) NOT NULL, -- 예: "고혈압 그룹", "당뇨병 그룹"
                             description TEXT,

    -- 시스템 정보
                             is_active BOOLEAN DEFAULT true,
                             member_count INTEGER DEFAULT 0, -- 캐시용

                             created_by BIGINT REFERENCES admins(id),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             UNIQUE(institution_id, name)
);

-- 그룹 멤버십 테이블
CREATE TABLE user_group_members (
                                    id BIGSERIAL PRIMARY KEY,
                                    group_id BIGINT REFERENCES user_groups(id) ON DELETE CASCADE,
                                    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,

                                    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    is_active BOOLEAN DEFAULT true,
                                    added_by BIGINT REFERENCES admins(id),

                                    UNIQUE(group_id, user_id)
);

-- =============================================================================
-- 4. 질문 및 설문 관리
-- =============================================================================

-- 질문 카테고리 테이블
CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            institution_id BIGINT NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,

                            name VARCHAR(100) NOT NULL, -- 예: "고혈압", "당뇨병", "건강상태"
                            description TEXT,
                            image_path VARCHAR(500), -- 카테고리 이미지 경로
                            is_active BOOLEAN DEFAULT true,

                            created_by BIGINT REFERENCES admins(id),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            UNIQUE(institution_id, name)
);

-- 질문 테이블
CREATE TABLE questions (
                           id BIGSERIAL PRIMARY KEY,
                           institution_id BIGINT NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,

    -- 카테고리 연결 (NULL 허용 - 카테고리 없는 질문 가능)
                           category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,

    -- 핵심 질문 내용
                           title VARCHAR(200) NOT NULL, -- 질문 제목 (예: "오늘의 기분은 어떠세요?")
                           content TEXT NOT NULL, -- 질문 상세 설명
                           question_type VARCHAR(30) NOT NULL CHECK (question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TEXT', 'SCALE', 'YES_NO', 'DATE', 'TIME')), -- 질문 유형

    -- 질문 옵션 (객관식인 경우 선택지들)
                           options JSONB, -- {"options": [{"value": "1", "label": "좋음"}, {"value": "2", "label": "나쁨"}]}

    -- 기타 의견 입력 설정
                           allow_other_option BOOLEAN DEFAULT false, -- "기타" 옵션 허용 여부
                           other_option_label VARCHAR(50) DEFAULT '기타', -- "기타" 버튼 텍스트
                           other_option_placeholder VARCHAR(100), -- 기타 입력창 placeholder

    -- 필수 응답 설정
                           is_required BOOLEAN DEFAULT false, -- 필수 응답 여부

    -- 시스템 정보
                           is_active BOOLEAN DEFAULT true,

                           created_by BIGINT REFERENCES admins(id),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 5. 질문 할당 시스템 (매일 답변)
-- =============================================================================

-- 질문 할당 테이블
CREATE TABLE question_assignments (
                                      id BIGSERIAL PRIMARY KEY,
                                      institution_id BIGINT NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,

    -- 질문 정보
                                      question_id BIGINT REFERENCES questions(id),

    -- 할당 대상 (개별 또는 그룹)
                                      user_id BIGINT REFERENCES users(id),
                                      group_id BIGINT REFERENCES user_groups(id),

    -- 할당 설정
                                      priority INTEGER DEFAULT 5, -- 1(최고) ~ 10(최저)

    -- 시스템 정보
                                      assigned_by BIGINT REFERENCES admins(id),
                                      assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 제약 조건: 개별 할당 또는 그룹 할당 중 하나만
                                      CONSTRAINT check_assignment_target CHECK (
                                          (user_id IS NOT NULL AND group_id IS NULL) OR
                                          (user_id IS NULL AND group_id IS NOT NULL)
                                          )
);

-- =============================================================================
-- 6. 설문 응답 관리
-- =============================================================================

-- 설문 응답 테이블
CREATE TABLE question_responses (
                                    id BIGSERIAL PRIMARY KEY,
                                    assignment_id BIGINT REFERENCES question_assignments(id),
                                    user_id BIGINT REFERENCES users(id),
                                    question_id BIGINT REFERENCES questions(id),

    -- 응답 데이터 (기존 선택 + 기타 의견 모두 포함)
                                    response_data JSONB NOT NULL,
                                    response_text TEXT, -- 텍스트 응답 + 기타 의견 검색용

    -- 기타 의견 별도 필드 (검색 및 분석 편의성)
                                    other_response TEXT, -- "기타" 선택 시 입력한 텍스트

    -- 응답 메타데이터
                                    response_time_seconds INTEGER, -- 응답 소요 시간
                                    device_info JSONB, -- 기기 정보

    -- 시스템 정보
                                    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    ip_address INET,
                                    user_agent TEXT
);

-- =============================================================================
-- 7. 업로드 관리
-- =============================================================================

-- 업로드 테이블
CREATE TABLE uploads (
                         id BIGSERIAL PRIMARY KEY,
                         institution_id BIGINT NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
                         user_id BIGINT REFERENCES users(id),

    -- 콘텐츠 정보
                         title VARCHAR(200),
                         content TEXT,

    -- 관리자 처리 상태
                         admin_read BOOLEAN DEFAULT false,
                         admin_response TEXT,
                         admin_response_date TIMESTAMP,
                         admin_id BIGINT REFERENCES admins(id), -- 응답한 관리자

                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 업로드 파일 테이블
CREATE TABLE upload_files (
                              id BIGSERIAL PRIMARY KEY,
                              upload_id BIGINT REFERENCES uploads(id) ON DELETE CASCADE,

    -- 파일 정보
                              file_type VARCHAR(20) NOT NULL CHECK (file_type IN ('IMAGE', 'AUDIO', 'VIDEO', 'DOCUMENT', 'TEXT')),
                              file_name VARCHAR(255) NOT NULL,
                              original_name VARCHAR(255),
                              file_path VARCHAR(500) NOT NULL,
                              file_size BIGINT,
                              mime_type VARCHAR(100),

    -- 미디어 파일 메타데이터
                              duration_seconds INTEGER, -- 음성/비디오 재생 시간
                              image_width INTEGER, -- 이미지 너비
                              image_height INTEGER, -- 이미지 높이
                              thumbnail_path VARCHAR(500), -- 썸네일 경로

    -- 업로드 정보
                              upload_order INTEGER DEFAULT 1, -- 업로드 순서
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 8. 템플릿 시스템 (기본)
-- =============================================================================

-- 할당 템플릿 테이블
CREATE TABLE assignment_templates (
                                      id BIGSERIAL PRIMARY KEY,
                                      institution_id BIGINT REFERENCES institutions(id) ON DELETE CASCADE, -- NULL이면 시스템 공용

                                      name VARCHAR(100) NOT NULL,
                                      description TEXT,

    -- 템플릿 분류
                                      visibility VARCHAR(20) DEFAULT 'PRIVATE', -- PRIVATE, SYSTEM

    -- 대상 설정
                                      target_type VARCHAR(20) NOT NULL, -- 'USER', 'GROUP', 'ALL'

    -- 템플릿 설정
                                      is_active BOOLEAN DEFAULT true,
                                      tags TEXT[], -- 검색용 태그

                                      created_by BIGINT REFERENCES admins(id),
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 기관 내 템플릿명 중복 방지 (시스템 템플릿 제외)
                                      UNIQUE(institution_id, name)
);

-- 템플릿-질문 연결
CREATE TABLE template_questions (
                                    id BIGSERIAL PRIMARY KEY,
                                    template_id BIGINT REFERENCES assignment_templates(id) ON DELETE CASCADE,
                                    question_id BIGINT REFERENCES questions(id),

    -- 질문별 설정
                                    priority INTEGER DEFAULT 5, -- 1(높음) ~ 10(낮음)
                                    is_required BOOLEAN DEFAULT false,

    -- 질문 순서
                                    display_order INTEGER DEFAULT 1,

                                    UNIQUE(template_id, question_id)
);

-- =============================================================================
-- 인덱스 생성
-- =============================================================================

-- institutions 테이블 인덱스
CREATE INDEX idx_institutions_active ON institutions(is_active);
CREATE UNIQUE INDEX idx_institutions_business_number ON institutions(business_number) WHERE business_number IS NOT NULL;

-- admins 테이블 인덱스
CREATE INDEX idx_admins_institution ON admins(institution_id, is_active);
CREATE INDEX idx_admins_status ON admins(status, role);
CREATE INDEX idx_admins_pending ON admins(institution_id, status) WHERE status = 'PENDING';

-- users 테이블 인덱스
CREATE INDEX idx_users_institution ON users(institution_id, is_active);
CREATE INDEX idx_users_severity ON users(institution_id, severity);
CREATE INDEX idx_users_guardian ON users(guardian_phone) WHERE guardian_phone IS NOT NULL;

-- user_groups 테이블 인덱스
CREATE INDEX idx_user_groups_institution ON user_groups(institution_id, is_active);

-- user_group_members 테이블 인덱스
CREATE INDEX idx_group_members_group ON user_group_members(group_id, is_active);
CREATE INDEX idx_group_members_user ON user_group_members(user_id, is_active);

-- categories 테이블 인덱스
CREATE INDEX idx_categories_institution ON categories(institution_id, is_active);

-- questions 테이블 인덱스
CREATE INDEX idx_questions_institution ON questions(institution_id, is_active);
CREATE INDEX idx_questions_category ON questions(category_id, is_active);
CREATE INDEX idx_questions_type ON questions(institution_id, question_type);

-- question_assignments 테이블 인덱스
CREATE INDEX idx_assignments_institution ON question_assignments(institution_id);
CREATE INDEX idx_assignments_user ON question_assignments(user_id);
CREATE INDEX idx_assignments_group ON question_assignments(group_id);
CREATE INDEX idx_assignments_priority ON question_assignments(priority);

-- question_responses 테이블 인덱스
CREATE INDEX idx_responses_assignment ON question_responses(assignment_id);
CREATE INDEX idx_responses_user_date ON question_responses(user_id, submitted_at);
CREATE INDEX idx_responses_question ON question_responses(question_id);
CREATE INDEX idx_responses_daily ON question_responses(user_id, question_id, DATE(submitted_at));
-- CREATE INDEX idx_responses_other_text ON question_responses USING gin(to_tsvector('korean', other_response)) WHERE other_response IS NOT NULL;

-- uploads 테이블 인덱스
CREATE INDEX idx_uploads_institution ON uploads(institution_id, created_at DESC);
CREATE INDEX idx_uploads_user ON uploads(user_id, created_at DESC);
CREATE INDEX idx_uploads_admin ON uploads(institution_id, admin_read, created_at DESC);

-- upload_files 테이블 인덱스
CREATE INDEX idx_upload_files_upload ON upload_files(upload_id, upload_order);
CREATE INDEX idx_upload_files_type ON upload_files(file_type);

-- assignment_templates 테이블 인덱스
CREATE INDEX idx_templates_institution ON assignment_templates(institution_id, is_active);
CREATE INDEX idx_templates_visibility ON assignment_templates(visibility);

-- template_questions 테이블 인덱스
CREATE INDEX idx_template_questions_template ON template_questions(template_id, display_order);

-- =============================================================================
-- 완료 메시지
-- =============================================================================

DO $$
BEGIN
    RAISE NOTICE '=== 복지관 케어 시스템 데이터베이스 스키마 생성 완료 ===';
    RAISE NOTICE '총 테이블 수: 14개';
    RAISE NOTICE '- 기관/관리자: 3개 (institutions, system_admins, admins)';
    RAISE NOTICE '- 사용자/그룹: 3개 (users, user_groups, user_group_members)';
    RAISE NOTICE '- 질문/설문: 4개 (categories, questions, question_assignments, question_responses)';
    RAISE NOTICE '- 업로드: 2개 (uploads, upload_files)';
    RAISE NOTICE '- 템플릿: 2개 (assignment_templates, template_questions)';
    RAISE NOTICE '';
    RAISE NOTICE '다음 단계: 02-sample-data.sql 실행으로 샘플 데이터 생성';
END $$;