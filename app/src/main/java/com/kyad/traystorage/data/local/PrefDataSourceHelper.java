package com.kyad.traystorage.data.local;

import android.content.Context;

import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import com.kyad.traystorage.data.LocalDataSource;
import com.kyad.traystorage.data.model.ModelBase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PrefDataSourceHelper implements LocalDataSource {
    private static final String PREFS_MODEL = "PREFS_MODEL";

    private final PrefManager mPreference;

    PrefDataSourceHelper(Context context) {
        mPreference = new PrefManager(context);
    }

    @Override
    public <T extends ModelBase> void setModel(T model) {
        String prefName = PREFS_MODEL + model.getClass().getName();
        if (model == null) {
            mPreference.put(prefName, "");
            return;
        }

        mPreference.put(prefName, new Gson().toJson(model));
    }

    @Override
    public <T extends ModelBase> void removeModel(Class<T> type) {
        String prefName = PREFS_MODEL + type.getName();
        mPreference.put(prefName, "");
    }

    @Override
    public <T extends ModelBase> T getModel(Class<T> type) {
        String prefName = PREFS_MODEL + type.getName();
        return new Gson().fromJson(mPreference.getString(prefName, ""), type);
    }
}
