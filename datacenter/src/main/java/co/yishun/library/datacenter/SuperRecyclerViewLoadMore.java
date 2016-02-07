package co.yishun.library.datacenter;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

/**
 * Created by carlos on 2/7/16.
 */
public class SuperRecyclerViewLoadMore implements LoadMore {
    private final SuperRecyclerView mSuperRecyclerView;
    private com.malinskiy.superrecyclerview.OnMoreListener mListener;

    public SuperRecyclerViewLoadMore(SuperRecyclerView mSuperRecyclerView) {
        this.mSuperRecyclerView = mSuperRecyclerView;
    }

    @Override
    public void setOnMoreListener(final OnMoreListener listener) {
        mListener = new com.malinskiy.superrecyclerview.OnMoreListener() {
            @Override
            public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
                listener.onMore();
            }
        };
        mSuperRecyclerView.setOnMoreListener(mListener);
    }

    @Override
    public void setLoading(boolean refreshing) {
        // do nothing, because SuperRecyclerView has done this if notifying the adapter.
        // mSuperRecyclerView.setLoadingMore(refreshing);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled)
            mSuperRecyclerView.setOnMoreListener(mListener);
        else
            mSuperRecyclerView.setOnMoreListener(null);
    }
}
