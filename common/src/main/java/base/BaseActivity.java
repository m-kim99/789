package base;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import helper.KeyboardUtil;

public abstract class BaseActivity extends AppCompatActivity {
	private Dialog mLoadingDialog;

	protected Dialog loadingDialog() {
		return null;
	}

    @Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLoadingDialog = loadingDialog();
		EventBus.getDefault().register(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
			target35Ui();
		}
	}

	private void target35Ui() {
		final View decorView = getWindow().getDecorView();

		ViewCompat.setOnApplyWindowInsetsListener(decorView, new OnApplyWindowInsetsListener() {
			@Override
			public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
				Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

				// DecorView에 직접 패딩 적용
				v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

				WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
					getWindow(), decorView);
				if (controller != null) {
					controller.setAppearanceLightStatusBars(true);
					controller.setAppearanceLightNavigationBars(true);
				}

				return insets;
			}
		});
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
	}

	/************************************************************
	 *  Event Bus
	 ************************************************************/
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onLoadingEvent(BaseEvent.LoadingEvent e) {
		if (e.isLoading) {
			if (mLoadingDialog != null) mLoadingDialog.show();
		} else {
			if (mLoadingDialog != null) mLoadingDialog.hide();
		}
	}

	/************************************************************
	 *  Helpers & Methods
	 ************************************************************/

	public void setupKeyboard(View view) {
		// Set up touch listener for non-text box views to hide keyboard.
		if (!(view instanceof EditText)) {
			if(view.getTag()==null) {
				view.setOnTouchListener(new View.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						KeyboardUtil.hide(BaseActivity.this);
						return false;
					}
				});
			}
		}

		//If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupKeyboard(innerView);
			}
		}
	}
}
