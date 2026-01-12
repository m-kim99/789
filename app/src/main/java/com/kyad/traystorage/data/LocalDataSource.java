package com.kyad.traystorage.data;

import com.kyad.traystorage.data.model.ModelBase;

import java.util.List;

public interface LocalDataSource {
	<T extends ModelBase> void setModel(T model);
	<T extends ModelBase> T getModel(Class<T> type);
	<T extends ModelBase> void removeModel(Class<T> type);
}
