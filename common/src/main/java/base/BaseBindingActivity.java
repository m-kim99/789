package base;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

public abstract class BaseBindingActivity<T extends ViewDataBinding> extends BaseActivity {
	public T binding;

	public abstract int getLayout();
	public abstract void init();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.binding = DataBindingUtil.setContentView(this, getLayout());

		init();
	}

	public void onApiError(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
