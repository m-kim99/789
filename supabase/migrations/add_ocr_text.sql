-- documents 테이블에 OCR 텍스트 저장 컬럼 추가
ALTER TABLE documents ADD COLUMN IF NOT EXISTS ocr_text TEXT;

-- 컬럼 설명 추가
COMMENT ON COLUMN documents.ocr_text IS '이미지 OCR 전사 텍스트 (여러 이미지인 경우 순차적으로 구분자로 합침)';
