-- =====================================================
-- TrayStorage 전체 테이블 생성 SQL (통합본)
-- 이 파일 하나만 실행하세요
-- =====================================================

-- 1. users 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    phone_number VARCHAR(20),
    signup_type INTEGER DEFAULT 0,
    status INTEGER DEFAULT 0,
    gender INTEGER,
    birthday VARCHAR(20),
    email VARCHAR(255),
    profile_image TEXT,
    access_token VARCHAR(255),
    password VARCHAR(255),
    is_agree INTEGER DEFAULT 0,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    exit_reg_time TIMESTAMP WITH TIME ZONE,
    stop_remark TEXT
);

-- 2. categories 테이블 생성
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color INTEGER DEFAULT 0,
    icon VARCHAR(50) DEFAULT 'folder',
    sort_order INTEGER DEFAULT 0,
    document_count INTEGER DEFAULT 0,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    update_time TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. documents 테이블 생성
CREATE TABLE IF NOT EXISTS documents (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    images TEXT,
    label INTEGER DEFAULT 0,
    tags TEXT,
    code VARCHAR(100),
    create_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    reg_time TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_access_token ON users(access_token);
CREATE INDEX IF NOT EXISTS idx_categories_user_id ON categories(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_category_id ON documents(category_id);

-- 5. 테스트 유저 생성 (id=999)
INSERT INTO users (id, name, email, access_token, status, is_agree)
SELECT 999, '테스트유저', 'test@test.com', 'test_token_123', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 999);

-- 6. 테스트 유저용 기본 카테고리 생성
INSERT INTO categories (user_id, name, color, sort_order)
SELECT 999, '기본', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE user_id = 999);

-- 7. 시퀀스 업데이트
SELECT setval('users_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM users), 999));
SELECT setval('categories_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM categories), 1));
SELECT setval('documents_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM documents), 1));
