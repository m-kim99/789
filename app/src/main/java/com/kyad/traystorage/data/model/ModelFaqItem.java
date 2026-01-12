package com.kyad.traystorage.data.model;

import androidx.databinding.ObservableBoolean;

import java.util.List;

public class ModelFaqItem extends ModelBase{
    public int id;
    public String name;

    public ObservableBoolean isSelected = new ObservableBoolean(false);
    public static class ListModel extends ModelBase{
        public List<ModelFaqItem> list;
    }
}
