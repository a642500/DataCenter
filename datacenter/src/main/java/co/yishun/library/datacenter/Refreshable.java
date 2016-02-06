package co.yishun.library.datacenter;

/**
 * Created by carlos on 2/6/16.
 */
public interface Refreshable {

    void setOnRefreshListener(OnRefreshListener listener);

    void setRefreshing(boolean refreshing);

    void setEnabled(boolean enabled);

    interface OnRefreshListener {
        void onRefresh();
    }
}
