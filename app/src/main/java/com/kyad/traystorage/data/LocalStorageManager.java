package com.kyad.traystorage.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kyad.traystorage.App;
import com.kyad.traystorage.data.model.ModelCategory;
import com.kyad.traystorage.data.model.ModelDocument;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 테스트 모드용 로컬 저장소 관리자
 * 카테고리와 문서를 SharedPreferences에 저장
 */
public class LocalStorageManager {
    private static final String PREF_NAME = "test_mode_storage";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_DOCUMENTS = "documents";
    private static final String KEY_CATEGORY_ID_SEQ = "category_id_seq";
    private static final String KEY_DOCUMENT_ID_SEQ = "document_id_seq";

    private static LocalStorageManager instance;
    private SharedPreferences prefs;
    private Gson gson;

    private LocalStorageManager() {
        prefs = App.get().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized LocalStorageManager get() {
        if (instance == null) {
            instance = new LocalStorageManager();
        }
        return instance;
    }

    // ==================== Categories ====================

    public List<ModelCategory> getCategories() {
        String json = prefs.getString(KEY_CATEGORIES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<ModelCategory>>(){}.getType();
        List<ModelCategory> categories = gson.fromJson(json, type);
        return categories != null ? categories : new ArrayList<>();
    }

    public void saveCategories(List<ModelCategory> categories) {
        String json = gson.toJson(categories);
        prefs.edit().putString(KEY_CATEGORIES, json).apply();
    }

    public ModelCategory insertCategory(String name, int color) {
        List<ModelCategory> categories = getCategories();
        
        int newId = prefs.getInt(KEY_CATEGORY_ID_SEQ, 1);
        prefs.edit().putInt(KEY_CATEGORY_ID_SEQ, newId + 1).apply();

        ModelCategory category = new ModelCategory();
        category.id = newId;
        category.user_id = 999;
        category.name = name;
        category.color = color;
        category.document_count = 0;
        category.create_time = getCurrentTime();
        category.update_time = getCurrentTime();

        categories.add(category);
        saveCategories(categories);

        return category;
    }

    public boolean updateCategory(int categoryId, String name, int color) {
        List<ModelCategory> categories = getCategories();
        for (ModelCategory cat : categories) {
            if (cat.id == categoryId) {
                cat.name = name;
                cat.color = color;
                cat.update_time = getCurrentTime();
                saveCategories(categories);
                return true;
            }
        }
        return false;
    }

    public boolean deleteCategory(int categoryId) {
        List<ModelCategory> categories = getCategories();
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).id == categoryId) {
                categories.remove(i);
                saveCategories(categories);
                // 해당 카테고리의 문서도 삭제
                deleteDocumentsByCategory(categoryId);
                return true;
            }
        }
        return false;
    }

    // ==================== Documents ====================

    public List<ModelDocument> getDocuments() {
        String json = prefs.getString(KEY_DOCUMENTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<ModelDocument>>(){}.getType();
        List<ModelDocument> documents = gson.fromJson(json, type);
        return documents != null ? documents : new ArrayList<>();
    }

    public List<ModelDocument> getAllDocuments(String keyword) {
        List<ModelDocument> allDocs = getDocuments();
        if (keyword == null || keyword.isEmpty()) {
            return allDocs;
        }
        List<ModelDocument> result = new ArrayList<>();
        for (ModelDocument doc : allDocs) {
            if (doc.title.contains(keyword) || 
                (doc.content != null && doc.content.contains(keyword)) ||
                (doc.tags != null && doc.tags.contains(keyword))) {
                result.add(doc);
            }
        }
        return result;
    }

    public List<ModelDocument> getDocumentsByCategory(int categoryId, String keyword) {
        List<ModelDocument> allDocs = getDocuments();
        List<ModelDocument> result = new ArrayList<>();
        
        for (ModelDocument doc : allDocs) {
            if (doc.category_id != null && doc.category_id == categoryId) {
                if (keyword == null || keyword.isEmpty() ||
                    doc.title.contains(keyword) || 
                    (doc.content != null && doc.content.contains(keyword))) {
                    result.add(doc);
                }
            }
        }
        return result;
    }

    public void saveDocuments(List<ModelDocument> documents) {
        String json = gson.toJson(documents);
        prefs.edit().putString(KEY_DOCUMENTS, json).apply();
    }

    public ModelDocument insertDocument(String title, String content, int label, 
                                         String tags, String images, Integer categoryId, String ocrText) {
        List<ModelDocument> documents = getDocuments();

        int newId = prefs.getInt(KEY_DOCUMENT_ID_SEQ, 1);
        prefs.edit().putInt(KEY_DOCUMENT_ID_SEQ, newId + 1).apply();

        ModelDocument doc = new ModelDocument();
        doc.id = newId;
        doc.user_id = 999;
        doc.category_id = categoryId;
        doc.title = title;
        doc.content = content;
        doc.label = label;
        doc.tags = tags;
        doc.images = images;
        doc.ocr_text = ocrText;
        doc.create_time = getCurrentTime();
        doc.reg_time = getCurrentTime();

        // tag_list와 image_list 초기화
        doc.tag_list = new ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            String[] tagArray = tags.split(",");
            for (String tag : tagArray) {
                doc.tag_list.add(tag.trim());
            }
        }
        
        doc.image_list = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            String[] imageArray = images.split(",");
            for (String img : imageArray) {
                doc.image_list.add(img.trim());
            }
        }

        documents.add(doc);
        saveDocuments(documents);

        // 카테고리 문서 수 업데이트
        updateCategoryDocumentCount(categoryId);

        return doc;
    }

    public ModelDocument getDocumentDetail(int docId) {
        List<ModelDocument> documents = getDocuments();
        for (ModelDocument doc : documents) {
            if (doc.id == docId) {
                return doc;
            }
        }
        return null;
    }

    public boolean updateDocument(int docId, String title, String content, int label,
                                   String tags, String images, Integer categoryId, String ocrText) {
        List<ModelDocument> documents = getDocuments();
        for (ModelDocument doc : documents) {
            if (doc.id == docId) {
                Integer oldCategoryId = doc.category_id;
                
                doc.title = title;
                doc.content = content;
                doc.label = label;
                doc.tags = tags;
                doc.images = images;
                doc.category_id = categoryId;
                doc.ocr_text = ocrText;
                doc.reg_time = getCurrentTime();

                // tag_list 업데이트
                doc.tag_list = new ArrayList<>();
                if (tags != null && !tags.isEmpty()) {
                    String[] tagArray = tags.split(",");
                    for (String tag : tagArray) {
                        doc.tag_list.add(tag.trim());
                    }
                }
                
                // image_list 업데이트
                doc.image_list = new ArrayList<>();
                if (images != null && !images.isEmpty()) {
                    String[] imageArray = images.split(",");
                    for (String img : imageArray) {
                        doc.image_list.add(img.trim());
                    }
                }

                saveDocuments(documents);

                // 카테고리 변경 시 문서 수 업데이트
                if (oldCategoryId != null && !oldCategoryId.equals(categoryId)) {
                    updateCategoryDocumentCount(oldCategoryId);
                }
                if (categoryId != null) {
                    updateCategoryDocumentCount(categoryId);
                }

                return true;
            }
        }
        return false;
    }

    public boolean deleteDocument(int docId) {
        List<ModelDocument> documents = getDocuments();
        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i).id == docId) {
                Integer categoryId = documents.get(i).category_id;
                documents.remove(i);
                saveDocuments(documents);
                
                // 카테고리 문서 수 업데이트
                if (categoryId != null) {
                    updateCategoryDocumentCount(categoryId);
                }
                return true;
            }
        }
        return false;
    }

    private void deleteDocumentsByCategory(int categoryId) {
        List<ModelDocument> documents = getDocuments();
        List<ModelDocument> remaining = new ArrayList<>();
        for (ModelDocument doc : documents) {
            if (doc.category_id == null || doc.category_id != categoryId) {
                remaining.add(doc);
            }
        }
        saveDocuments(remaining);
    }

    private void updateCategoryDocumentCount(Integer categoryId) {
        if (categoryId == null) return;
        
        List<ModelCategory> categories = getCategories();
        List<ModelDocument> documents = getDocuments();
        
        int count = 0;
        for (ModelDocument doc : documents) {
            if (doc.category_id != null && doc.category_id.equals(categoryId)) {
                count++;
            }
        }

        for (ModelCategory cat : categories) {
            if (cat.id == categoryId) {
                cat.document_count = count;
                break;
            }
        }
        saveCategories(categories);
    }

    // ==================== Utilities ====================

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    public void initDefaultCategory() {
        List<ModelCategory> categories = getCategories();
        if (categories.isEmpty()) {
            insertCategory("기본", 0);
        }
    }
}
