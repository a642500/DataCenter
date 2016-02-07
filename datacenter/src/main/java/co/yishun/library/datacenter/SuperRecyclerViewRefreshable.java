package co.yishun.library.datacenter;

import android.support.v4.widget.SwipeRefreshLayout;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

/**
 * Created by carlos on 2/6/16.
 */
public class SuperRecyclerViewRefreshable implements Refreshable {
    private final SuperRecyclerView mSuperRecyclerView;

    public SuperRecyclerViewRefreshable(SuperRecyclerView mSuperRecyclerView) {
        this.mSuperRecyclerView = mSuperRecyclerView;
    }

    @Override
    public void setOnRefreshListener(final OnRefreshListener listener) {
        mSuperRecyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listener.onRefresh();
            }
        });
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        SwipeRefreshLayout swipeRefreshLayout = mSuperRecyclerView.getSwipeToRefresh();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        SwipeRefreshLayout swipeRefreshLayout = mSuperRecyclerView.getSwipeToRefresh();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(enabled);
        }
    }
}
