package helper;

import android.app.Activity;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

// Have to adjust resize on activity
public class KeyboardUtil {

	public final static int MIN_KEYBOARD_HEIGHT = 170;

	private KeyboardVisibleListener mListener = null;
	private View mRootView = null;
	private boolean mKeyboardOpend = false;
	private int screenHeight = 0;

	public KeyboardUtil(View rootView) {
		this.mRootView = rootView;
	}

	public KeyboardUtil(View rootView, int screenHeight) {
		this.mRootView = rootView;
		this.screenHeight = screenHeight;
	}

	public void setKeyboardVisibleListner(KeyboardVisibleListener listner) {
		mListener = listner;

		if (mRootView == null) {
			return;
		}

		mRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

			Rect r = new Rect();

			mRootView.getWindowVisibleDisplayFrame(r);
			if (screenHeight == 0) {
				screenHeight = mRootView.getHeight();
			}

			int heightDiff = screenHeight - (r.bottom);
			int keyboardDiff = MIN_KEYBOARD_HEIGHT;

			if (heightDiff > keyboardDiff && !mKeyboardOpend) { // 99% of the time the height diff will be due to a keyboard.
				if (mListener != null) {
					mListener.onShow(heightDiff + r.top);
				}
				mKeyboardOpend = true;
			} else if (mKeyboardOpend == true) {
				mKeyboardOpend = false;

				if (mListener != null) {
					mListener.onHide();
				}
			}
		});
	}

	public interface KeyboardVisibleListener {
		void onShow(int height);

		void onHide();
	}

	public static void hide(Activity activity) {
		View focusView = activity.getCurrentFocus();
		if (focusView != null) {
			IBinder binder = focusView.getWindowToken();
			InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(binder, 0);
		}
	}
}
