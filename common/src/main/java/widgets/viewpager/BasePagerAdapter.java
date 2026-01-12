package widgets.viewpager;

import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class BasePagerAdapter extends FragmentPagerAdapter {
	private static final String TAG = BasePagerAdapter.class.getSimpleName();
	private final List<Fragment> fragments = new ArrayList<>();

	OnAddedFragmentListener onAddedFragmentListener;

	public BasePagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	public Fragment getItem(int i) {
		Fragment fragment = this.fragments.get(i);
		if (onAddedFragmentListener != null)
			onAddedFragmentListener.onAddedFragment(i);
		return fragment;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
	}

	@Override
	public void finishUpdate(ViewGroup container) {
		super.finishUpdate(container);
	}

	public void addFragment(Fragment fragment, String str) {
		this.fragments.add(fragment);
	}

	public void setFragments(List<Fragment> list) {
		Log.i(TAG, "");
		if (list != null) {
			this.fragments.clear();
			this.fragments.addAll(list);
			notifyDataSetChanged();
		}
	}

	public int getItemPosition(Fragment fragment) {
		return this.fragments.contains(fragment) ? -1 : -2;
	}

	public int getCount() {
		return this.fragments.size();
	}

	public void setAdapterListener(OnAddedFragmentListener listener) {
		onAddedFragmentListener = listener;
	}

	public interface OnAddedFragmentListener {
		void onAddedFragment(int position);
	}
}