package com.kyad.traystorage.data.local;

import android.content.Context;

import com.kyad.traystorage.data.LocalDataSource;

public class LocalDataSourceFactory {
	public static LocalDataSource getInstance(Context context) {
		PrefDataSourceHelper prefDataSource = new PrefDataSourceHelper(context);
		return prefDataSource;
	}
}

