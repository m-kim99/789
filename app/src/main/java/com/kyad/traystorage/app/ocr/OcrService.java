package com.kyad.traystorage.app.ocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OcrService {

    private static final String TAG = "OcrService";
    private static final String SUPABASE_URL = "https://rtlikunlsieloeprkbiv.supabase.co";
    private static final String API_URL = SUPABASE_URL + "/functions/v1/naver-ocr";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ0bGlrdW5sc2llbG9lcHJrYml2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjgyMDM0MDYsImV4cCI6MjA4Mzc3OTQwNn0.INjz8e36J1dpKV97xgACM0FiucZwlqXB2xKSVsWQJbo";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private Call currentCall;

    public interface OcrCallback {
        void onProgress(int current, int total);
        void onSuccess(String combinedText, List<OcrResult> results);
        void onError(String error);
    }

    public static class OcrResult {
        public int index;
        public String text;
        public String error;

        public OcrResult(int index, String text, String error) {
            this.index = index;
            this.text = text;
            this.error = error;
        }
    }

    public OcrService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void processImages(List<String> imagePaths, OcrCallback callback) {
        try {
            JsonObject requestBody = new JsonObject();
            JsonArray imagesArray = new JsonArray();

            for (int i = 0; i < imagePaths.size(); i++) {
                String path = imagePaths.get(i);
                callback.onProgress(i + 1, imagePaths.size());

                String base64 = imageToBase64(path);
                if (base64 != null) {
                    JsonObject imageObj = new JsonObject();
                    imageObj.addProperty("imageBase64", base64);
                    imageObj.addProperty("mimeType", getMimeType(path));
                    imagesArray.add(imageObj);
                }
            }

            requestBody.add("images", imagesArray);

            RequestBody body = RequestBody.create(requestBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .build();

            currentCall = client.newCall(request);
            currentCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (!call.isCanceled()) {
                        Log.e(TAG, "OCR API call failed", e);
                        callback.onError("네트워크 오류: " + e.getMessage());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = "";
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            errorBody = responseBody.string();
                        }
                        callback.onError("서버 오류: " + response.code() + "\n" + errorBody);
                        return;
                    }

                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        callback.onError("빈 응답");
                        return;
                    }

                    try {
                        String jsonStr = responseBody.string();
                        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();

                        if (json.has("error")) {
                            callback.onError(json.get("error").getAsString());
                            return;
                        }

                        String combinedText = json.has("combinedText") 
                                ? json.get("combinedText").getAsString() 
                                : "";

                        List<OcrResult> results = new ArrayList<>();
                        if (json.has("results")) {
                            JsonArray resultsArray = json.getAsJsonArray("results");
                            for (int i = 0; i < resultsArray.size(); i++) {
                                JsonObject resultObj = resultsArray.get(i).getAsJsonObject();
                                int index = resultObj.has("index") ? resultObj.get("index").getAsInt() : i;
                                String text = resultObj.has("text") ? resultObj.get("text").getAsString() : "";
                                String error = resultObj.has("error") ? resultObj.get("error").getAsString() : null;
                                results.add(new OcrResult(index, text, error));
                            }
                        }

                        callback.onSuccess(combinedText, results);

                    } catch (Exception e) {
                        Log.e(TAG, "Response parsing error", e);
                        callback.onError("응답 처리 오류: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "OCR request error", e);
            callback.onError("요청 생성 오류: " + e.getMessage());
        }
    }

    private String imageToBase64(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                Log.w(TAG, "Image file not found: " + imagePath);
                return null;
            }

            // 이미지 크기 제한 (네이버 OCR API 제한)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            int maxSize = 2048;
            int scale = 1;
            while (options.outWidth / scale > maxSize || options.outHeight / scale > maxSize) {
                scale *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

            if (bitmap == null) {
                Log.w(TAG, "Failed to decode bitmap: " + imagePath);
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String mimeType = getMimeType(imagePath);
            Bitmap.CompressFormat format = mimeType.contains("png") 
                    ? Bitmap.CompressFormat.PNG 
                    : Bitmap.CompressFormat.JPEG;
            bitmap.compress(format, 85, baos);
            bitmap.recycle();

            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);

        } catch (Exception e) {
            Log.e(TAG, "Image to Base64 error", e);
            return null;
        }
    }

    private String getMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "image/jpeg";
    }

    public void cancel() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }
}
