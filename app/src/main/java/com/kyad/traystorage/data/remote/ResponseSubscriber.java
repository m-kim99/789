package com.kyad.traystorage.data.remote;

import com.kyad.traystorage.data.ApiResponse;

import org.greenrobot.eventbus.EventBus;

import base.BaseEvent;
import io.reactivex.subscribers.ResourceSubscriber;

public class ResponseSubscriber<T> extends ResourceSubscriber<ApiResponse<T>> {
	private ApiResponse<T> mResponse;

	@Override
	protected void onStart() {
		super.onStart();
		EventBus.getDefault().post(new BaseEvent.LoadingEvent(true));
	}

	@Override
	public void onNext(ApiResponse<T> t) {
		mResponse = t;
	}

	@Override
	public void onComplete() {
		EventBus.getDefault().post(new BaseEvent.LoadingEvent(false));
	}

	@Override
	public void onError(Throwable e) {
		EventBus.getDefault().post(new BaseEvent.LoadingEvent(false));
	}

	public ApiResponse<T> getResponse() {
		return mResponse;
	}
}
