package co.yishun.library.datacenter;

import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Created by carlos on 2/6/16.
 */
public class SwipeRefreshLayoutRefreshable implements Refreshable {
    private final SwipeRefreshLayout mSwipeRefreshLayout;

    public SwipeRefreshLayoutRefreshable(SwipeRefreshLayout mSwipeRefreshLayout) {
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
    }

    @Override
    public void setOnRefreshListener(final OnRefreshListener listener) {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listener.onRefresh();
            }
        });
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mSwipeRefreshLayout.setEnabled(enabled);
    }
}
