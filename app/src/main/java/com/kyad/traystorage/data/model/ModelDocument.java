package com.kyad.traystorage.data.model;

import com.kyad.traystorage.app.Constants;

import java.util.List;

public class ModelDocument extends ModelBase {
    public int id;
    public int user_id;
    public Integer category_id;
    public String title;
    public String content;
    public String images;
    public Integer label;
    public String tags;
    public String code;
    public String ocr_text;
    public String create_time;
    public String reg_time;
    public List<String> tag_list;
    public List<String> image_list;
    //modified
    public String tagValue;
    public boolean isWhite;

    public void updateValues() {
        if (tag_list != null) {
            tagValue = "";
            for (int i = 0; i < tag_list.size(); i++) {
                tagValue += "#" + tag_list.get(i);
                if (i < tag_list.size() - 1) tagValue += " ";
            }
        }
        isWhite = label == 0;
    }

    public static class ListModel extends ModelBase {
        public List<ModelDocument> document_list;
    }
    public static class DetailModel extends ModelBase {
        public ModelDocument document;
    }
}
