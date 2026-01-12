package com.kyad.traystorage.data.remote;

import com.kyad.traystorage.data.RemoteDataSource;
import retrofit2.Retrofit;

public class RemoteDataSourceFactory {
	public static RemoteDataSource getInstance() {
//		if (true) { // for test
//			return new DummyDataSourceHelper();
//		}
		final Retrofit retrofit = RemoteDataSourceHelper.createRetrofit(RemoteDataSource.API_BASE_URL);
		return retrofit.create(RemoteDataSource.class);
	}
}
