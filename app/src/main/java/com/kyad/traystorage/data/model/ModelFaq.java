package com.kyad.traystorage.data.model;

import androidx.databinding.ObservableBoolean;

import java.util.List;

public class ModelFaq extends ModelBase{
    public String title;
    public String content;

    public ObservableBoolean isExpanded = new ObservableBoolean(false);

    public static class ListModel extends ModelBase{
        public List<ModelFaq> list;
    }
}
