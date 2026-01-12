package helper;

import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewHelper {
	public static <T> void linkAdapterAndObserable(RecyclerView.Adapter adapter, ObservableList<T> list) {
		// item update를 다르게 하고 싶을때 이것을 재정의하면 됨.
		list.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<T>>() {
			@Override
			public void onChanged(ObservableList<T> sender) {
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onItemRangeChanged(ObservableList<T> sender, int positionStart, int itemCount) {
				adapter.notifyItemChanged(positionStart, itemCount);
			}

			@Override
			public void onItemRangeInserted(ObservableList<T> sender, int positionStart, int itemCount) {
				if (adapter.getItemCount() == itemCount) {
					adapter.notifyDataSetChanged();
				} else {
					adapter.notifyItemRangeInserted(positionStart, itemCount);
				}
			}

			@Override
			public void onItemRangeMoved(ObservableList<T> sender, int fromPosition, int toPosition, int itemCount) {
				adapter.notifyItemMoved(toPosition, itemCount);
			}

			@Override
			public void onItemRangeRemoved(ObservableList<T> sender, int positionStart, int itemCount) {
				if (list.size() == 0) {
					adapter.notifyDataSetChanged();
				} else {
					adapter.notifyItemRangeRemoved(positionStart, itemCount);
				}
			}
		});
	}
}
