package com.siggytech.utils.communication.presentation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {

    LinearLayoutManager layoutManager;
    boolean isKeyboardDismissedByScroll;

    public PaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount - lastVisibleItemPosition) <= totalItemCount
                    && firstVisibleItemPosition == 0) {
                loadMoreItems();
            }
        }
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                if (!isKeyboardDismissedByScroll) {
                    hideKeyboard();
                    isKeyboardDismissedByScroll = !isKeyboardDismissedByScroll;
                }
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
                isKeyboardDismissedByScroll = false;
                break;
        }
    }


    protected abstract void loadMoreItems();
    public abstract long getTotalPageCount();
    public abstract boolean isLastPage();
    public abstract boolean isLoading();
    public abstract void hideKeyboard();

}
