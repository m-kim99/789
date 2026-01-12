package widgets.recyclerview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class DragItemCallback<T extends RecyclerView.ViewHolder> extends ItemTouchHelper.Callback {

    private final ItemTouchHelperContract mAdapter;
    private final boolean verticalMove;

    public DragItemCallback(boolean verticalMove, ItemTouchHelperContract adapter) {
        this.verticalMove = verticalMove;
        this.mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }


    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = verticalMove ? ItemTouchHelper.UP | ItemTouchHelper.DOWN : ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            mAdapter.onRowSelected((T) viewHolder);
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        mAdapter.onRowClear((T) viewHolder);
    }

    public interface ItemTouchHelperContract<T extends RecyclerView.ViewHolder> {

        void onRowMoved(int fromPosition, int toPosition);

        void onRowSelected(T myViewHolder);

        void onRowClear(T myViewHolder);
    }
}
