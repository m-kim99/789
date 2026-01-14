-- =====================================================
-- TrayStorage users 테이블 생성 SQL
-- 이 쿼리를 먼저 실행한 후 001_add_categories.sql 실행
-- =====================================================

-- 1. users 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    phone_number VARCHAR(20),
    signup_type INTEGER DEFAULT 0,  -- 0: 일반, 1: 카카오, 2: 네이버, 3: 페이스북
    status INTEGER DEFAULT 0,       -- 0: 정상, -1: 탈퇴
    gender INTEGER,                 -- 0: 남성, 1: 여성
    birthday VARCHAR(20),
    email VARCHAR(255) UNIQUE,
    profile_image TEXT,
    access_token VARCHAR(255),
    password VARCHAR(255),
    is_agree INTEGER DEFAULT 0,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    exit_reg_time TIMESTAMP WITH TIME ZONE,
    stop_remark TEXT
);

-- 2. documents 테이블 생성 (없는 경우)
CREATE TABLE IF NOT EXISTS documents (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    category_id INTEGER,  -- categories 테이블 생성 후 FK 추가
    title VARCHAR(255) NOT NULL,
    content TEXT,
    images TEXT,
    label INTEGER DEFAULT 0,
    tags TEXT,
    code VARCHAR(100),
    create_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    reg_time TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_access_token ON users(access_token);
CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents(user_id);

-- 4. 테스트용 유저 생성 (테스트 모드용, id=999)
INSERT INTO users (id, name, email, access_token, status, is_agree)
VALUES (999, '테스트유저', 'test@test.com', 'test_token_123', 0, 1)
ON CONFLICT (id) DO NOTHING;

-- 5. 시퀀스 조정 (id=999 다음부터 자동 생성되도록)
SELECT setval('users_id_seq', GREATEST((SELECT MAX(id) FROM users), 999));
