package base;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import lombok.Getter;
import lombok.Setter;

public class BaseViewModel<T> extends ViewModel {
	/**
	 * Collects all subscriptions to unsubscribe later
	 */
	@NonNull
	private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

	@Getter
	@Setter
	private T navigator;

	@Override
	protected void onCleared() {
		mCompositeDisposable.clear();
		super.onCleared();
	}

	/************************************************************
	 *  Helpers
	 ************************************************************/
	public void addDisposable(Disposable disposable) {
		mCompositeDisposable.add(disposable);
	}
}
