// supabase/functions/ai-chat/index.ts
// TrayStorage 개인용 문서 관리 앱을 위한 AI 챗봇 Edge Function
import { serve } from 'https://deno.land/std@0.168.0/http/server.ts';
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders });
  }

  try {
    const { message, userId, access_token, history = [] } = await req.json();
    
    const GEMINI_API_KEY = Deno.env.get('GEMINI_API_KEY');
    const supabaseUrl = Deno.env.get('SUPABASE_URL');
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY');
    
    if (!GEMINI_API_KEY) throw new Error('GEMINI_API_KEY not configured');
    
    // 1. 사용자 문서 검색
    let documentContext = '';
    let searchResults: any[] = [];
    let totalDocCount = 0;
    
    if (supabaseUrl && supabaseKey) {
      const supabase = createClient(supabaseUrl, supabaseKey);
      
      // 전체 문서 수 조회
      const { count } = await supabase
        .from('documents')
        .select('*', { count: 'exact', head: true })
        .eq('user_id', userId);
      
      totalDocCount = count || 0;
      
      // 키워드 기반 문서 검색 (제목, 내용, 태그에서 검색)
      const keywords = message.split(/\s+/).filter((k: string) => k.length > 1);
      
      let query = supabase
        .from('documents')
        .select('id, title, content, tags, label, create_time')
        .eq('user_id', userId)
        .order('create_time', { ascending: false });
      
      // 키워드가 있으면 검색 조건 추가
      if (keywords.length > 0) {
        const searchConditions = keywords.map((k: string) => 
          `title.ilike.%${k}%,content.ilike.%${k}%,tags.ilike.%${k}%`
        ).join(',');
        
        const { data: searchDocs } = await supabase
          .from('documents')
          .select('id, title, content, tags, label, create_time')
          .eq('user_id', userId)
          .or(searchConditions)
          .limit(5);
        
        if (searchDocs && searchDocs.length > 0) {
          searchResults = searchDocs.map(d => ({
            id: String(d.id),
            name: d.title,
            storageLocation: d.tags || null
          }));
          
          documentContext = searchDocs.map(d => 
            `- "${d.title}" (태그: ${d.tags || '없음'}, 등록일: ${d.create_time?.split(' ')[0] || '알 수 없음'})\n  내용: ${(d.content || '').substring(0, 100)}...`
          ).join('\n');
        }
      }
      
      // 검색 결과가 없으면 최근 문서 5개 가져오기
      if (!documentContext) {
        const { data: recentDocs } = await supabase
          .from('documents')
          .select('id, title, content, tags, label, create_time')
          .eq('user_id', userId)
          .order('create_time', { ascending: false })
          .limit(5);
        
        if (recentDocs && recentDocs.length > 0) {
          documentContext = '최근 등록된 문서:\n' + recentDocs.map(d => 
            `- "${d.title}" (태그: ${d.tags || '없음'}, 등록일: ${d.create_time?.split(' ')[0] || '알 수 없음'})`
          ).join('\n');
        }
      }
    }
    
    // 2. 시스템 프롬프트 구성
    const systemPrompt = `당신은 TrayStorage의 AI 어시스턴트 "트로이"입니다.
사용자의 개인 문서 관리를 도와주는 친절한 어시스턴트입니다.

## 역할
- 사용자가 저장한 문서를 검색하고 찾아주기
- 문서 관리에 대한 조언 제공
- 친절하고 간결하게 답변

## 현재 사용자 정보
- 총 저장된 문서 수: ${totalDocCount}건

## 관련 문서 정보
${documentContext || '검색된 문서가 없습니다.'}

## 답변 지침
1. 한국어로 친절하게 답변하세요
2. 문서를 찾았다면 제목과 태그를 알려주세요
3. 문서를 찾지 못했다면 다른 키워드로 검색을 제안하세요
4. **중요한 내용**은 볼드 처리하세요
5. 이모지를 적절히 사용하세요 😊`;

    // 3. Gemini API 호출 (스트리밍)
    const historyContents = history
      .filter((h: any) => h.content && h.content.trim())
      .map((h: any) => ({
        role: h.role === 'user' ? 'user' : 'model',
        parts: [{ text: h.content }],
      }));
    
    const contents = [
      { role: 'user', parts: [{ text: systemPrompt }] },
      { role: 'model', parts: [{ text: '네, 알겠습니다. TrayStorage AI 어시스턴트 트로이로서 문서 검색과 관리를 도와드리겠습니다.' }] },
      ...historyContents,
      { role: 'user', parts: [{ text: message }] },
    ];
    
    const streamUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:streamGenerateContent?alt=sse&key=${GEMINI_API_KEY}`;
    
    const geminiResponse = await fetch(streamUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ contents }),
    });
    
    if (!geminiResponse.ok || !geminiResponse.body) {
      const errorText = await geminiResponse.text();
      console.error('Gemini API error:', errorText);
      throw new Error('Gemini API failed');
    }
    
    // 4. SSE 스트리밍 응답 반환
    const encoder = new TextEncoder();
    const decoder = new TextDecoder();
    
    const stream = new ReadableStream({
      async start(controller) {
        const reader = geminiResponse.body!.getReader();
        let buffer = '';
        let fullText = '';
        
        try {
          while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            
            buffer += decoder.decode(value, { stream: true });
            buffer = buffer.replace(/\r\n/g, '\n');
            
            let boundary = buffer.indexOf('\n\n');
            while (boundary !== -1) {
              const eventStr = buffer.slice(0, boundary);
              buffer = buffer.slice(boundary + 2);
              
              const lines = eventStr.split('\n');
              for (const line of lines) {
                if (!line.trim() || line.startsWith(':') || !line.startsWith('data:')) continue;
                
                const dataStr = line.slice(5).trim();
                if (!dataStr || dataStr === '[DONE]') continue;
                
                try {
                  const parsed = JSON.parse(dataStr);
                  const candidates = parsed.candidates ?? [];
                  for (const candidate of candidates) {
                    const parts = candidate.content?.parts ?? [];
                    for (const part of parts) {
                      const delta = part.text || '';
                      if (delta) {
                        fullText += delta;
                        // SSE 형식으로 전송
                        const sseData = JSON.stringify({ text: fullText });
                        controller.enqueue(encoder.encode(`data: ${sseData}\n\n`));
                      }
                    }
                  }
                } catch (e) {
                  console.error('Parse error:', e);
                }
              }
              
              boundary = buffer.indexOf('\n\n');
            }
          }
          
          // 검색 결과가 있으면 마지막에 추가
          if (searchResults.length > 0) {
            const finalData = JSON.stringify({ 
              text: fullText, 
              searchResults: searchResults 
            });
            controller.enqueue(encoder.encode(`data: ${finalData}\n\n`));
          }
          
          controller.enqueue(encoder.encode('data: [DONE]\n\n'));
        } finally {
          controller.close();
        }
      },
    });
    
    return new Response(stream, {
      headers: { 
        ...corsHeaders, 
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive'
      },
    });
    
  } catch (error) {
    console.error('Error:', error);
    return new Response(
      JSON.stringify({ error: error.message }),
      { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    );
  }
});
