// supabase/functions/app/index.ts
// TrayStorage App API - Categories & Documents CRUD
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
    const supabaseUrl = Deno.env.get('SUPABASE_URL')!;
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!;
    const supabase = createClient(supabaseUrl, supabaseKey);

    const url = new URL(req.url);
    const pathParts = url.pathname.split('/').filter(p => p);
    // Expected path: /app/{action} e.g., /app/insert_category
    const action = pathParts[pathParts.length - 1];

    const formData = await req.formData().catch(() => null);
    const jsonData = formData ? null : await req.json().catch(() => ({}));
    
    const getParam = (key: string): string | null => {
      if (formData) return formData.get(key) as string;
      if (jsonData) return jsonData[key];
      return null;
    };

    const getIntParam = (key: string): number | null => {
      const val = getParam(key);
      return val ? parseInt(val) : null;
    };

    // Verify user by access_token
    const access_token = getParam('access_token');
    let userId: number | null = null;
    
    if (access_token) {
      const { data: user } = await supabase
        .from('users')
        .select('id')
        .eq('access_token', access_token)
        .single();
      userId = user?.id || null;
    }

    // =====================================================
    // Category APIs
    // =====================================================

    if (action === 'get_category_list') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }
      
      const { data: categories, error } = await supabase
        .from('categories')
        .select('*')
        .eq('user_id', userId)
        .order('sort_order', { ascending: true });

      if (error) throw error;

      // Get document counts for each category
      for (const cat of categories || []) {
        const { count } = await supabase
          .from('documents')
          .select('*', { count: 'exact', head: true })
          .eq('category_id', cat.id);
        cat.document_count = count || 0;
      }

      return jsonResponse({
        result: 0,
        msg: '',
        data: { category_list: categories || [] }
      });
    }

    if (action === 'insert_category') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }

      const name = getParam('name');
      const color = getIntParam('color') || 0;

      const { data: category, error } = await supabase
        .from('categories')
        .insert({
          user_id: userId,
          name: name,
          color: color,
          icon: 'folder',
          sort_order: 0,
          document_count: 0
        })
        .select()
        .single();

      if (error) throw error;

      return jsonResponse({
        result: 0,
        msg: '',
        data: { category: category }
      });
    }

    if (action === 'update_category') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }

      const id = getIntParam('id');
      const name = getParam('name');
      const color = getIntParam('color');

      const { error } = await supabase
        .from('categories')
        .update({
          name: name,
          color: color,
          update_time: new Date().toISOString()
        })
        .eq('id', id)
        .eq('user_id', userId);

      if (error) throw error;

      return jsonResponse({ result: 0, msg: '' });
    }

    if (action === 'delete_category') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }

      const id = getIntParam('id');

      const { error } = await supabase
        .from('categories')
        .delete()
        .eq('id', id)
        .eq('user_id', userId);

      if (error) throw error;

      return jsonResponse({ result: 0, msg: '' });
    }

    // =====================================================
    // Document APIs
    // =====================================================

    if (action === 'get_document_list') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }

      const categoryId = getIntParam('category_id');
      const keyword = getParam('keyword');

      let query = supabase
        .from('documents')
        .select('*')
        .eq('user_id', userId)
        .order('create_time', { ascending: false });

      if (categoryId) {
        query = query.eq('category_id', categoryId);
      }

      if (keyword) {
        query = query.or(`title.ilike.%${keyword}%,content.ilike.%${keyword}%,tags.ilike.%${keyword}%`);
      }

      const { data: documents, error } = await query;

      if (error) throw error;

      // Parse tags and images
      const docList = (documents || []).map(doc => ({
        ...doc,
        tag_list: doc.tags ? doc.tags.split(',').map((t: string) => t.trim()).filter((t: string) => t) : [],
        image_list: doc.images ? doc.images.split(',').map((i: string) => i.trim()).filter((i: string) => i) : []
      }));

      return jsonResponse({
        result: 0,
        msg: '',
        data: { document_list: docList }
      });
    }

    if (action === 'insert_document') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }

      const title = getParam('title') || '';
      const content = getParam('content') || '';
      const label = getIntParam('label') || 0;
      const tags = getParam('tags') || '';
      const images = getParam('images') || '';
      const categoryId = getIntParam('category_id');
      const ocrText = getParam('ocr_text') || '';

      const { data: document, error } = await supabase
        .from('documents')
        .insert({
          user_id: userId,
          category_id: categoryId,
          title: title,
          content: content,
          label: label,
          tags: tags,
          images: images
        })
        .select()
        .single();

      if (error) throw error;

      // Update category document count
      if (categoryId) {
        await updateCategoryDocCount(supabase, categoryId);
      }

      const doc = {
        ...document,
        tag_list: tags ? tags.split(',').map(t => t.trim()).filter(t => t) : [],
        image_list: images ? images.split(',').map(i => i.trim()).filter(i => i) : []
      };

      return jsonResponse({
        result: 0,
        msg: '',
        data: { document: doc }
      });
    }

    if (action === 'get_document_detail') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }

      const id = getIntParam('id');

      const { data: document, error } = await supabase
        .from('documents')
        .select('*')
        .eq('id', id)
        .eq('user_id', userId)
        .single();

      if (error || !document) {
        return jsonResponse({ result: 401, msg: 'Document not found' });
      }

      const doc = {
        ...document,
        tag_list: document.tags ? document.tags.split(',').map((t: string) => t.trim()).filter((t: string) => t) : [],
        image_list: document.images ? document.images.split(',').map((i: string) => i.trim()).filter((i: string) => i) : []
      };

      return jsonResponse({
        result: 0,
        msg: '',
        data: { document: doc }
      });
    }

    if (action === 'update_document') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }

      const id = getIntParam('id');
      const title = getParam('title') || '';
      const content = getParam('content') || '';
      const label = getIntParam('label') || 0;
      const tags = getParam('tags') || '';
      const images = getParam('images') || '';
      const categoryId = getIntParam('category_id');

      // Get old category_id
      const { data: oldDoc } = await supabase
        .from('documents')
        .select('category_id')
        .eq('id', id)
        .single();

      const oldCategoryId = oldDoc?.category_id;

      const { error } = await supabase
        .from('documents')
        .update({
          title: title,
          content: content,
          label: label,
          tags: tags,
          images: images,
          category_id: categoryId,
          reg_time: new Date().toISOString()
        })
        .eq('id', id)
        .eq('user_id', userId);

      if (error) throw error;

      // Update category document counts
      if (oldCategoryId && oldCategoryId !== categoryId) {
        await updateCategoryDocCount(supabase, oldCategoryId);
      }
      if (categoryId) {
        await updateCategoryDocCount(supabase, categoryId);
      }

      return jsonResponse({ result: 0, msg: '' });
    }

    if (action === 'delete_document_item') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }

      const id = getIntParam('id');

      // Get category_id before delete
      const { data: doc } = await supabase
        .from('documents')
        .select('category_id')
        .eq('id', id)
        .single();

      const categoryId = doc?.category_id;

      const { error } = await supabase
        .from('documents')
        .delete()
        .eq('id', id)
        .eq('user_id', userId);

      if (error) throw error;

      // Update category document count
      if (categoryId) {
        await updateCategoryDocCount(supabase, categoryId);
      }

      return jsonResponse({ result: 0, msg: '' });
    }

    // =====================================================
    // User APIs
    // =====================================================

    if (action === 'login' || action === 'login_email') {
      const email = getParam('email');
      const password = getParam('password');

      const { data: user, error } = await supabase
        .from('users')
        .select('*')
        .eq('email', email)
        .eq('password', password)
        .single();

      if (error || !user) {
        return jsonResponse({ result: 401, msg: '이메일 또는 비밀번호가 올바르지 않습니다.' });
      }

      // Generate access token if not exists
      if (!user.access_token) {
        const token = crypto.randomUUID();
        await supabase
          .from('users')
          .update({ access_token: token })
          .eq('id', user.id);
        user.access_token = token;
      }

      return jsonResponse({
        result: 0,
        msg: '',
        data: { user: user }
      });
    }

    if (action === 'signup') {
      const email = getParam('email');
      const password = getParam('password');
      const name = getParam('name') || '';

      // Check if email exists
      const { data: existing } = await supabase
        .from('users')
        .select('id')
        .eq('email', email)
        .single();

      if (existing) {
        return jsonResponse({ result: 400, msg: '이미 존재하는 이메일입니다.' });
      }

      const token = crypto.randomUUID();

      const { data: user, error } = await supabase
        .from('users')
        .insert({
          email: email,
          password: password,
          name: name,
          access_token: token,
          signup_type: 0,
          status: 0,
          is_agree: 1
        })
        .select()
        .single();

      if (error) throw error;

      return jsonResponse({
        result: 0,
        msg: '',
        data: { user: user }
      });
    }

    if (action === 'get_user_info') {
      if (!userId) {
        return jsonResponse({ result: 401, msg: 'Unauthorized' });
      }

      const { data: user, error } = await supabase
        .from('users')
        .select('*')
        .eq('id', userId)
        .single();

      if (error) throw error;

      return jsonResponse({
        result: 0,
        msg: '',
        data: { user: user }
      });
    }

    // Unknown action
    return jsonResponse({ result: 404, msg: `Unknown action: ${action}` });

  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.error('Error:', message);
    return jsonResponse({ result: 500, msg: message });
  }
});

function jsonResponse(data: any) {
  return new Response(JSON.stringify(data), {
    headers: { ...corsHeaders, 'Content-Type': 'application/json' }
  });
}

async function updateCategoryDocCount(supabase: any, categoryId: number) {
  const { count } = await supabase
    .from('documents')
    .select('*', { count: 'exact', head: true })
    .eq('category_id', categoryId);

  await supabase
    .from('categories')
    .update({ document_count: count || 0 })
    .eq('id', categoryId);
}
