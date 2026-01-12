package base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

public abstract class BaseBindingFragment<T extends ViewDataBinding> extends BaseFragment {
	public T binding;

	public abstract int getLayout();
	public abstract void init();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = DataBindingUtil.inflate(inflater, getLayout(), container, false);

		root = binding.getRoot();

		init();

		return binding.getRoot();
	}
}
