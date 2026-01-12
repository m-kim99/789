package com.kyad.traystorage.data.model;

import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;

import java.util.List;

public class ModelAgreement extends ModelBase {
    public Integer id;
    public String title;
    public String content;
    public Integer status;

    public ObservableBoolean isChecked = new ObservableBoolean(false);

    public static class ListModel extends ModelBase{
        public List<ModelAgreement> list;
    }
}
