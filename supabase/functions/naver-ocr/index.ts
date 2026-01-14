import { serve } from 'https://deno.land/std@0.168.0/http/server.ts';

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};

interface OcrRequest {
  images: Array<{
    imageBase64: string;
    mimeType?: string;
  }>;
}

interface OcrResult {
  index: number;
  text: string;
  error?: string;
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders });
  }

  if (req.method !== 'POST') {
    return new Response(
      JSON.stringify({ error: '지원하지 않는 HTTP 메서드입니다. POST만 지원됩니다.' }),
      {
        status: 405,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      },
    );
  }

  try {
    const NAVER_CLOVA_OCR_API_URL = Deno.env.get('NAVER_CLOVA_OCR_API_URL');
    const NAVER_CLOVA_OCR_SECRET_KEY = Deno.env.get('NAVER_CLOVA_OCR_SECRET_KEY');

    if (!NAVER_CLOVA_OCR_API_URL || !NAVER_CLOVA_OCR_SECRET_KEY) {
      throw new Error('네이버 클로바 OCR 환경 변수가 설정되지 않았습니다.');
    }

    const body: OcrRequest = await req.json().catch(() => null);

    if (!body || !Array.isArray(body.images) || body.images.length === 0) {
      return new Response(
        JSON.stringify({ error: '유효하지 않은 요청입니다. images 배열은 필수입니다.' }),
        {
          status: 400,
          headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        },
      );
    }

    // 여러 이미지를 순차적으로 OCR 처리
    const results: OcrResult[] = [];

    for (let i = 0; i < body.images.length; i++) {
      const imageItem = body.images[i];
      
      if (!imageItem || typeof imageItem.imageBase64 !== 'string') {
        results.push({ index: i, text: '', error: '유효하지 않은 이미지 데이터' });
        continue;
      }

      try {
        const text = await processOcr(
          imageItem.imageBase64,
          imageItem.mimeType,
          NAVER_CLOVA_OCR_API_URL,
          NAVER_CLOVA_OCR_SECRET_KEY
        );
        results.push({ index: i, text });
      } catch (err) {
        const errorMsg = err instanceof Error ? err.message : '알 수 없는 오류';
        results.push({ index: i, text: '', error: errorMsg });
      }
    }

    // 전체 텍스트 합치기 (이미지 구분자 포함)
    const combinedText = results
      .filter(r => r.text)
      .map((r, idx) => {
        if (results.length > 1) {
          return `[이미지 ${r.index + 1}]\n${r.text}`;
        }
        return r.text;
      })
      .join('\n\n---\n\n');

    return new Response(
      JSON.stringify({ 
        results,
        combinedText,
        totalImages: body.images.length,
        successCount: results.filter(r => !r.error).length
      }), 
      {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      }
    );
  } catch (error) {
    console.error('naver-ocr function error:', error);
    const message = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.';

    return new Response(
      JSON.stringify({ error: `OCR 처리 중 오류가 발생했습니다: ${message}` }),
      {
        status: 500,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      },
    );
  }
});

async function processOcr(
  imageBase64Input: string,
  mimeType: string | undefined,
  apiUrl: string,
  secretKey: string
): Promise<string> {
  let base64Data = imageBase64Input;
  let format = 'jpg';

  // data URL("data:image/png;base64,...") 형식과 순수 base64 문자열 모두 지원
  const dataUrlMatch = imageBase64Input.match(/^data:(image\/[a-zA-Z0-9.+-]+);base64,(.*)$/);
  if (dataUrlMatch) {
    const [, mime, data] = dataUrlMatch;
    base64Data = data;
    format = mime.split('/')[1] || 'jpg';
  } else if (mimeType) {
    format = mimeType.split('/')[1] || 'jpg';
  }

  const payload = {
    version: 'V2',
    requestId:
      typeof crypto !== 'undefined' && 'randomUUID' in crypto
        ? crypto.randomUUID()
        : `${Date.now()}`,
    timestamp: Date.now(),
    lang: 'ko',
    images: [
      {
        format,
        name: 'image',
        data: base64Data,
      },
    ],
  };

  const ocrResponse = await fetch(apiUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-OCR-SECRET': secretKey,
    },
    body: JSON.stringify(payload),
  });

  if (!ocrResponse.ok) {
    const errorText = await ocrResponse.text();
    console.error('NAVER OCR API error:', ocrResponse.status, errorText);
    throw new Error(`네이버 OCR API 호출 실패: ${ocrResponse.status}`);
  }

  const ocrJson = await ocrResponse.json();

  const textPieces: string[] = [];

  if (Array.isArray(ocrJson?.images)) {
    for (const img of ocrJson.images) {
      if (!Array.isArray(img?.fields)) continue;

      let lineBuffer: string[] = [];

      for (const field of img.fields) {
        if (!field || typeof field.inferText !== 'string') continue;

        const inferText: string = field.inferText;
        const lineBreak: boolean = !!field.lineBreak;

        lineBuffer.push(inferText);

        if (lineBreak) {
          textPieces.push(lineBuffer.join(' '));
          lineBuffer = [];
        }
      }

      if (lineBuffer.length > 0) {
        textPieces.push(lineBuffer.join(' '));
      }
    }
  }

  return textPieces.join('\n').trim();
}
