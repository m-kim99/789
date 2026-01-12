package com.kyad.traystorage.data.model;

import java.util.List;

public class ModelPopupInfo extends ModelBase {
    public Integer id;
    public String title;
    public Integer content_type;
    public String content;
    public String content_image;
    public Integer close_method;
    public Integer move_type;
    public String move_path;

    public class ListModel extends ModelBase {
        public List<ModelPopupInfo> popup_list;
    }
}
