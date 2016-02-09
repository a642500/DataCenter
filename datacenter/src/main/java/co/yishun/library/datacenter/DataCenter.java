package co.yishun.library.datacenter;

import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by Carlos on 1/28/16.
 */
public interface DataCenter<I extends LoadIndexProvider.LoadIndex<T>, T extends Updatable> {

    List<T> data();

    void setObservableAdapter(RecyclerView.Adapter adapter);

    void setLoader(DataLoader<I, T> dataLoader);

    void setOnEndListener(OnEndListener<I, T> listener);

    void setRefreshable(Refreshable refreshable);

    void setLoadMore(LoadMore loadMore);

    void setLoadIndexProvider(LoadIndexProvider<I, T> provider);

    void reset();

    void loadNext();

    void release();

    interface DataLoader<I extends LoadIndexProvider.LoadIndex<T>, T extends Updatable> {
        List<T> loadOptional(I index);

        List<T> loadNecessary(I index);
    }

    interface OnEndListener<I extends LoadIndexProvider.LoadIndex<T>, T extends Updatable> {
        void onFail(I index);

        void onSuccess(I index);
    }
}
