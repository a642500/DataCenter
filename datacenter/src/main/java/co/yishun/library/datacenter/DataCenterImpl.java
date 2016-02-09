package co.yishun.library.datacenter;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by carlos on 2/5/16.
 */
public class DataCenterImpl<I extends LoadIndexProvider.LoadIndex<T>, T extends Updatable> implements DataCenter<I, T> {
    private final AdapterDelegate mAdapterDelegate = new AdapterDelegate();
    private final List<T> mData;
    private final LoaderDelegate<I, T> mLoaderDelegate = new LoaderDelegate<>();
    private final RefreshableDelegate mRefreshableDelegate = new RefreshableDelegate();
    private final LoadMoreDelegate mLoadMoreDelegate = new LoadMoreDelegate();
    private OnEndListener<I, T> mOnEndListener;
    private ExecutorService sExecutor = Executors.newFixedThreadPool(2);
    private AtomicBoolean pendingReset = new AtomicBoolean(false);
    private I mIndex;
    private I mResetIndex;
    private DoubleAsyncTask<I, Void, Pair<I, List<T>>> mCurrentTask;
    private Lock pageLock = new ReentrantLock();

    DataCenterImpl() {
        mData = new ArrayList<>();
    }

    @Override
    public void setLoadIndexProvider(LoadIndexProvider<I, T> provider) {
        mIndex = provider.initInstance();
        mResetIndex = provider.resetIndex();
    }

    @Override
    public void setLoader(DataLoader<I, T> dataLoader) {
        mLoaderDelegate.loader = dataLoader;
    }

    @Override
    public void setOnEndListener(OnEndListener<I, T> listener) {
        mOnEndListener = listener;
    }

    private void onFail(I index) {
        if (mOnEndListener != null) {
            mOnEndListener.onFail(index);
        }
    }

    private void onSuccess(I index) {
        if (mOnEndListener != null) {
            mOnEndListener.onSuccess(index);
        }
    }

    @Override
    public void setRefreshable(Refreshable refreshable) {
        mRefreshableDelegate.mRefreshable = refreshable;
        mRefreshableDelegate.setOnRefreshListener(new Refreshable.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reset();
                loadNext();
            }
        });

    }

    @Override
    public void setLoadMore(LoadMore loadMore) {
        mLoadMoreDelegate.mLoadMore = loadMore;
        mLoadMoreDelegate.setOnMoreListener(new LoadMore.OnMoreListener() {
            @Override
            public void onMore() {
                mLoadMoreDelegate.setLoading(true);
                loadNext();
            }
        });
    }

    @Override
    public void reset() {
        mIndex.reset();
        pendingReset.set(true);
    }

    @Override
    public void loadNext() {
        if (mCurrentTask != null) {
            //TODO bug: unable to load more, if begin with loading more as well as loading the first page.
            return;
        }
        mCurrentTask = new DoubleAsyncTask<I, Void, Pair<I, List<T>>>() {
            @Override
            protected void onPostExecute(Pair<I, List<T>> requestAndResult) {
                I indexCopy = requestAndResult.first;//TODO copy index
                if (pendingReset.get()) {
                    return;// handle by necessary post only, skip
                }
                if (requestAndResult.second == null) {
                    onFail(indexCopy);

                    mCurrentTask = null;
                    mRefreshableDelegate.setRefreshing(false);
                    mLoadMoreDelegate.setLoading(false);
                    mRefreshableDelegate.setEnabled(true);
                } else {
                    pageLock.lock();

                    if (indexCopy.equals(mIndex)) {
                        // Difference means those code had been called once.
                        add(requestAndResult.second);
                        onSuccess(indexCopy);
                        mIndex.increment(requestAndResult.second);
                    } else {
                        update(requestAndResult.second);
                    }
                    if (post) {
                        // last callback
                        mCurrentTask = null;
                        mLoadMoreDelegate.setLoading(false);
                        mRefreshableDelegate.setEnabled(true);
                    }
                }
            }

            @Override
            protected void onNecessaryPostExecute(Pair<I, List<T>> requestAndResult) {
                if (pendingReset.get()) {
                    if (requestAndResult.second == null) {
                        onFail(mResetIndex);
                    } else {
                        mIndex.increment(requestAndResult.second);
                        mData.clear();
                        mData.addAll(requestAndResult.second);
                        mAdapterDelegate.notifyDataSetChanged();
                        onSuccess(mResetIndex);
                    }
                    pendingReset.set(false);
                    mRefreshableDelegate.setRefreshing(false);
                    mLoadMoreDelegate.setEnabled(true);
                    mCurrentTask = null;
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            protected Pair<I, List<T>> doOptionalInBackground(I... params) {
                List<T> result = mLoaderDelegate.loadOptional(params[0]);
                if (result != null) {
                    Collections.sort(result);
                }
                return Pair.create(params[0], result);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected Pair<I, List<T>> doNecessaryInBackground(I... params) {
                List<T> result = mLoaderDelegate.loadNecessary(params[0]);
                if (result != null) {
                    Collections.sort(result);
                }
                return Pair.create(params[0], result);
            }
        };
        if (pendingReset.get()) {
            mLoadMoreDelegate.setEnabled(false);
        } else {
            mRefreshableDelegate.setEnabled(false);
        }
        //noinspection unchecked
        mCurrentTask.executeOnExecutor(sExecutor, (I) mIndex.getCopy());
    }

    @Override
    public void release() {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        sExecutor.shutdownNow();
    }

    @Override
    public List<T> data() {
        return mData;
    }

    @Override
    public void setObservableAdapter(RecyclerView.Adapter adapter) {
        mAdapterDelegate.mAdapter = adapter;
    }

    private void add(List<T> newData) {
        int size = mData.size();
        mData.addAll(newData);
        mAdapterDelegate.notifyItemRangeChanged(size, newData.size());
    }

    private void update(List<T> newData) {
        for (T t : newData) {
            int index = mData.indexOf(t);
            if (index >= 0) {
                T old = mData.get(index);
                if (t.updateThan(old)) {
                    mData.remove(old);
                    mData.add(index, t);
                    mAdapterDelegate.notifyItemChanged(index);
                }
                // discard
            } else {
                mData.add(t);
                mAdapterDelegate.notifyItemInserted(index);
            }
        }
    }


    private static class AdapterDelegate {
        private RecyclerView.Adapter mAdapter;

        public AdapterDelegate() {
        }

        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            if (mAdapter != null) {
                mAdapter.notifyItemRangeRemoved(positionStart, itemCount);
            }
        }

        public void notifyDataSetChanged() {
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }

        public void notifyItemChanged(int position) {
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(position);
            }
        }

        public void notifyItemChanged(int position, Object payload) {
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(position, payload);
            }
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            if (mAdapter != null) {
                mAdapter.notifyItemRangeChanged(positionStart, itemCount);
            }
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount, Object payload) {
            if (mAdapter != null) {
                mAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
            }
        }

        public void notifyItemInserted(int position) {
            if (mAdapter != null) {
                mAdapter.notifyItemInserted(position);
            }
        }

        public void notifyItemMoved(int fromPosition, int toPosition) {
            if (mAdapter != null) {
                mAdapter.notifyItemMoved(fromPosition, toPosition);
            }
        }

        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            if (mAdapter != null) {
                mAdapter.notifyItemRangeInserted(positionStart, itemCount);
            }
        }

        public void notifyItemRemoved(int position) {
            if (mAdapter != null) {
                mAdapter.notifyItemRemoved(position);
            }
        }
    }

    private static class LoaderDelegate<I extends LoadIndexProvider.LoadIndex<T>, T extends Updatable> {
        DataLoader<I, T> loader;

        public LoaderDelegate() {
        }

        public List<T> loadOptional(I index) {
            if (loader != null) {
                return loader.loadOptional(index);
            } else {
                throw new IllegalStateException("You should set DataLoader.");
            }

        }

        public List<T> loadNecessary(I index) {
            if (loader != null) {
                return loader.loadNecessary(index);
            } else {
                throw new IllegalStateException("You should set DataLoader.");
            }

        }
    }

    private static class RefreshableDelegate {
        private Refreshable mRefreshable;

        public RefreshableDelegate() {
        }

        public void setOnRefreshListener(Refreshable.OnRefreshListener listener) {
            if (mRefreshable != null)
                mRefreshable.setOnRefreshListener(listener);
        }

        public void setRefreshing(boolean refreshing) {
            if (mRefreshable != null)
                mRefreshable.setRefreshing(refreshing);
        }

        public void setEnabled(boolean enabled) {
            if (mRefreshable != null)
                mRefreshable.setEnabled(enabled);
        }
    }

    private static class LoadMoreDelegate {
        private LoadMore mLoadMore;

        public LoadMoreDelegate() {
        }

        public void setOnMoreListener(LoadMore.OnMoreListener listener) {
            if (mLoadMore != null)
                mLoadMore.setOnMoreListener(listener);
        }

        public void setEnabled(boolean enabled) {
            if (mLoadMore != null)
                mLoadMore.setEnabled(enabled);
        }

        public void setLoading(boolean refreshing) {
            if (mLoadMore != null)
                mLoadMore.setLoading(refreshing);
        }
    }
}