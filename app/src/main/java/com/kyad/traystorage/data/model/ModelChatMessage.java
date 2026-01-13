package com.kyad.traystorage.data.model;

import java.util.Date;
import java.util.List;

public class ModelChatMessage extends ModelBase {
    public String id;
    public String role; // "user" or "assistant"
    public String content;
    public Date timestamp;
    public List<SearchResult> searchResults;

    public ModelChatMessage() {
    }

    public ModelChatMessage(String id, String role, String content, Date timestamp) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public static class SearchResult {
        public String id;
        public String name;
        public String storageLocation;

        public SearchResult() {
        }

        public SearchResult(String id, String name, String storageLocation) {
            this.id = id;
            this.name = name;
            this.storageLocation = storageLocation;
        }
    }

    public static class ListModel {
        public List<ModelChatMessage> messages;
    }
}
