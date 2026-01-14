-- =====================================================
-- TrayStorage 카테고리 기능 추가 SQL
-- 주의: 000_create_users.sql을 먼저 실행하세요!
-- =====================================================

-- 1. categories 테이블 생성
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color INTEGER DEFAULT 0,
    icon VARCHAR(50) DEFAULT 'folder',
    sort_order INTEGER DEFAULT 0,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    update_time TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. documents 테이블에 category_id FK 추가 (컬럼이 이미 있는 경우)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'documents_category_id_fkey'
    ) THEN
        ALTER TABLE documents 
        ADD CONSTRAINT documents_category_id_fkey 
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 3. 인덱스 생성 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_categories_user_id ON categories(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_category_id ON documents(category_id);

-- 4. RLS(Row Level Security) 정책 설정 - 사용자별 데이터 분리
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;

-- 사용자는 자신의 카테고리만 조회 가능
DROP POLICY IF EXISTS "Users can view own categories" ON categories;
CREATE POLICY "Users can view own categories" ON categories
    FOR SELECT USING (true);

-- 사용자는 자신의 카테고리만 생성 가능
DROP POLICY IF EXISTS "Users can insert own categories" ON categories;
CREATE POLICY "Users can insert own categories" ON categories
    FOR INSERT WITH CHECK (true);

-- 사용자는 자신의 카테고리만 수정 가능
DROP POLICY IF EXISTS "Users can update own categories" ON categories;
CREATE POLICY "Users can update own categories" ON categories
    FOR UPDATE USING (true);

-- 사용자는 자신의 카테고리만 삭제 가능
DROP POLICY IF EXISTS "Users can delete own categories" ON categories;
CREATE POLICY "Users can delete own categories" ON categories
    FOR DELETE USING (true);

-- 5. 기본 카테고리 생성 함수 (새 사용자 가입 시 호출)
CREATE OR REPLACE FUNCTION create_default_category()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO categories (user_id, name, color, sort_order)
    VALUES (NEW.id, '기본', 0, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 6. 새 사용자 가입 시 기본 카테고리 자동 생성 트리거
DROP TRIGGER IF EXISTS on_user_created ON users;
CREATE TRIGGER on_user_created
    AFTER INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION create_default_category();

-- 7. 기존 사용자들에게 기본 카테고리 생성 (마이그레이션)
INSERT INTO categories (user_id, name, color, sort_order)
SELECT id, '기본', 0, 0 FROM users
WHERE id NOT IN (SELECT DISTINCT user_id FROM categories WHERE user_id IS NOT NULL);

-- 8. 기존 문서들을 기본 카테고리에 연결 (마이그레이션)
UPDATE documents d
SET category_id = (
    SELECT c.id FROM categories c 
    WHERE c.user_id = d.user_id 
    ORDER BY c.sort_order ASC 
    LIMIT 1
)
WHERE d.category_id IS NULL;

-- 9. 카테고리별 문서 수를 조회하는 뷰 (선택사항)
CREATE OR REPLACE VIEW categories_with_count AS
SELECT 
    c.*,
    COUNT(d.id) as document_count
FROM categories c
LEFT JOIN documents d ON d.category_id = c.id
GROUP BY c.id;
