package co.yishun.library.datacenter;

/**
 * Created by carlos on 2/7/16.
 */
public interface LoadMore {
    void setOnMoreListener(OnMoreListener listener);


    /**
     * Call to show/hide the loading more progress view. If you handle it by yourself, you can
     * create an stub implement with doing nothing.
     */
    void setLoading(boolean refreshing);

    /**
     * Disable the load more feature and its callback. You must implement it to keep safe in case
     * that loading more occurs when refreshing.
     */
    void setEnabled(boolean enabled);

    interface OnMoreListener {
        void onMore();
    }
}
