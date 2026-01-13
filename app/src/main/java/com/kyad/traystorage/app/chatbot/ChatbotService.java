package com.kyad.traystorage.app.chatbot;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kyad.traystorage.data.model.ModelChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class ChatbotService {

    private static final String TAG = "ChatbotService";
    // Supabase Edge Function URL
    private static final String SUPABASE_URL = "https://rtlikunlsieloeprkbiv.supabase.co";
    private static final String API_URL = SUPABASE_URL + "/functions/v1/ai-chat";
    // Supabase anon key
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ0bGlrdW5sc2llbG9lcHJrYml2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjgyMDM0MDYsImV4cCI6MjA4Mzc3OTQwNn0.INjz8e36J1dpKV97xgACM0FiucZwlqXB2xKSVsWQJbo";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private Call currentCall;
    private final Gson gson = new Gson();

    public interface ChatbotCallback {
        void onStreamUpdate(String text);
        void onComplete(String fullText, List<ModelChatMessage.SearchResult> searchResults);
        void onError(String error);
    }

    public ChatbotService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void sendMessage(String message, String userId, String accessToken, 
                           List<ModelChatMessage> history, ChatbotCallback callback) {
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("message", message);
            requestBody.put("userId", userId);
            requestBody.put("access_token", accessToken);

            JSONArray historyArray = new JSONArray();
            for (ModelChatMessage msg : history) {
                if (msg.content != null && !msg.content.isEmpty()) {
                    JSONObject historyItem = new JSONObject();
                    historyItem.put("role", msg.role);
                    historyItem.put("content", msg.content);
                    historyArray.put(historyItem);
                }
            }
            requestBody.put("history", historyArray);

            RequestBody body = RequestBody.create(requestBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Accept", "text/event-stream")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .build();

            currentCall = client.newCall(request);
            currentCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (!call.isCanceled()) {
                        Log.e(TAG, "API call failed", e);
                        callback.onError(e.getMessage());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError("서버 오류: " + response.code());
                        return;
                    }

                    ResponseBody responseBody = response.body();
                    if (responseBody == null) {
                        callback.onError("빈 응답");
                        return;
                    }

                    String contentType = response.header("Content-Type", "");
                    
                    if (contentType.contains("text/event-stream")) {
                        handleSSEResponse(responseBody, callback);
                    } else {
                        handleJsonResponse(responseBody, callback);
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
            callback.onError("요청 생성 오류");
        }
    }

    private void handleSSEResponse(ResponseBody responseBody, ChatbotCallback callback) {
        StringBuilder fullText = new StringBuilder();
        List<ModelChatMessage.SearchResult> searchResults = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();
                    
                    if (data.equals("[DONE]")) {
                        break;
                    }

                    try {
                        JsonObject json = JsonParser.parseString(data).getAsJsonObject();
                        
                        if (json.has("text")) {
                            String text = json.get("text").getAsString();
                            fullText.setLength(0);
                            fullText.append(text);
                            callback.onStreamUpdate(text);
                        }
                        
                        if (json.has("searchResults")) {
                            searchResults.clear();
                            json.getAsJsonArray("searchResults").forEach(element -> {
                                JsonObject resultObj = element.getAsJsonObject();
                                ModelChatMessage.SearchResult result = new ModelChatMessage.SearchResult(
                                        resultObj.has("id") ? resultObj.get("id").getAsString() : "",
                                        resultObj.has("name") ? resultObj.get("name").getAsString() : "",
                                        resultObj.has("storageLocation") ? resultObj.get("storageLocation").getAsString() : null
                                );
                                searchResults.add(result);
                            });
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to parse SSE data: " + data, e);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "SSE reading error", e);
            callback.onError("스트리밍 오류");
            return;
        }

        callback.onComplete(fullText.toString(), searchResults);
    }

    private void handleJsonResponse(ResponseBody responseBody, ChatbotCallback callback) {
        try {
            String jsonStr = responseBody.string();
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();

            String text = "";
            List<ModelChatMessage.SearchResult> searchResults = new ArrayList<>();

            if (json.has("response")) {
                text = json.get("response").getAsString();
            } else if (json.has("text")) {
                text = json.get("text").getAsString();
            } else if (json.has("message")) {
                text = json.get("message").getAsString();
            }

            if (json.has("searchResults")) {
                json.getAsJsonArray("searchResults").forEach(element -> {
                    JsonObject resultObj = element.getAsJsonObject();
                    ModelChatMessage.SearchResult result = new ModelChatMessage.SearchResult(
                            resultObj.has("id") ? resultObj.get("id").getAsString() : "",
                            resultObj.has("name") ? resultObj.get("name").getAsString() : "",
                            resultObj.has("storageLocation") ? resultObj.get("storageLocation").getAsString() : null
                    );
                    searchResults.add(result);
                });
            }

            callback.onStreamUpdate(text);
            callback.onComplete(text, searchResults);

        } catch (Exception e) {
            Log.e(TAG, "JSON response error", e);
            callback.onError("응답 처리 오류");
        }
    }

    public void cancel() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }
}
