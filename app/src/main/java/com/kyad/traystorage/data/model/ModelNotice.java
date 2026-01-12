package com.kyad.traystorage.data.model;

import java.util.List;

public class ModelNotice extends ModelBase{
    public int id;
    public String title;
    public String reg_time;
    public int view_count;

    public static class ListModel extends ModelBase{
        public List<ModelNotice> list;
    }
}
