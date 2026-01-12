package widgets.recyclerview;

import androidx.recyclerview.widget.RecyclerView;

public interface DragStartListener {
    void requestDrag(RecyclerView.ViewHolder viewHolder);
}
