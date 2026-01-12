package com.kyad.traystorage.data.model;

import androidx.databinding.ObservableBoolean;

import java.util.List;

public class ModelAsk extends ModelBase{
    public String title;
    public String content;
    public String reply;
    public int status;
    public String reg_time;

    public ObservableBoolean isExpanded = new ObservableBoolean(false);
    public ObservableBoolean replyVisible = new ObservableBoolean(false);

    public static class ListModel extends ModelBase{
        public List<ModelAsk> list;
    }
}
