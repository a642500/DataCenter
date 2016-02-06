package co.yishun.library.datacenter;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by Carlos on 1/28/16.
 */
public interface DataCenter<T extends Updatable> {

    List<T> data();

    void setObservableAdapter(RecyclerView.Adapter adapter);

    void setLoader(DataLoader<T> dataLoader);

    void setOnFailListener(OnEndListener listener);

    void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout);

    void reset();

    void loadNext();

    interface DataLoader<T> {
        List<T> loadOptional(int page);

        List<T> loadNecessary(int page);
    }

    interface OnEndListener {
        void onFail(int page);

        void onSuccess(int page);
    }
}
