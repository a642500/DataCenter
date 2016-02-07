package co.yishun.library.datacenter;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by carlos on 2/5/16.
 */
public class DataCenterImpl<T extends Updatable> implements DataCenter<T> {
    public static final int START_PAGE = 0;
    public static final int RESET_PAGE = -1;
    private final AdapterDelegate mAdapterDelegate = new AdapterDelegate();
    private final List<T> mData;
    private final LoaderDelegate<T> mLoaderDelegate = new LoaderDelegate<T>();
    private OnEndListener mOnEndListener;
    private ExecutorService sExecutor = Executors.newFixedThreadPool(2);
    private Refreshable mRefreshable;
    private AtomicBoolean pendingReset = new AtomicBoolean(false);
    private AtomicInteger page = new AtomicInteger(START_PAGE);
    private DoubleAsyncTask<Integer, Void, Pair<Integer, List<T>>> mCurrentTask;
    private Lock pageLock = new ReentrantLock();

    DataCenterImpl() {
        mData = new ArrayList<>();
    }

    @Override
    public void setLoader(final DataLoader<T> dataLoader) {
        mLoaderDelegate.loader = dataLoader;
    }

    @Override
    public void setOnEndListener(OnEndListener listener) {
        mOnEndListener = listener;
    }

    private void onFail(int page) {
        if (mOnEndListener != null) {
            mOnEndListener.onFail(page);
        }
    }

    private void onSuccess(int page) {
        if (mOnEndListener != null) {
            mOnEndListener.onSuccess(page);
        }
    }

    private void setRefreshing(boolean refreshing) {
        if (mRefreshable != null) {
            mRefreshable.setRefreshing(refreshing);
        }
    }

    @Override
    public void setRefreshable(Refreshable refreshable) {
        mRefreshable = refreshable;
        if (refreshable != null)
            refreshable.setOnRefreshListener(new Refreshable.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    reset();
                    loadNext();
                }
            });
    }

    @Override
    public void reset() {
        page.set(0);
        pendingReset.set(true);
    }

    @Override
    public void loadNext() {
        if (mCurrentTask != null) {
            //TODO bug: unable to load more, if begin with loading more as well as loading the first page.
            return;
        }
        mCurrentTask = new DoubleAsyncTask<Integer, Void, Pair<Integer, List<T>>>() {
            @Override
            protected void onPostExecute(Pair<Integer, List<T>> requestAndResult) {
                int pageCopy = requestAndResult.first;
                if (pendingReset.get()) {
                    return;// handle by necessary post only, skip
                }
                if (requestAndResult.second == null) {
                    onFail(pageCopy);

                    mCurrentTask = null;
                    setRefreshing(false);
                    mRefreshable.setEnabled(true);
                } else {
                    pageLock.lock();

                    if (pageCopy == page.get()) {
                        // Difference means those code had been called once.
                        add(requestAndResult.second);
                        onSuccess(pageCopy);
                        page.incrementAndGet();
                    } else {
                        update(requestAndResult.second);
                    }
                    if (post) {
                        // last callback
                        mCurrentTask = null;
                        setRefreshing(false);
                        mRefreshable.setEnabled(true);
                    }
                }
            }

            @Override
            protected void onNecessaryPostExecute(Pair<Integer, List<T>> requestAndResult) {
                if (pendingReset.get()) {
                    if (requestAndResult.second == null) {
                        onFail(RESET_PAGE);
                    } else {
                        page.incrementAndGet();
                        mData.clear();
                        add(requestAndResult.second);
                        onSuccess(RESET_PAGE);
                    }
                    pendingReset.set(false);
                    setRefreshing(false);
                    mCurrentTask = null;
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            protected Pair<Integer, List<T>> doOptionalInBackground(Integer... params) {
                List<T> result = mLoaderDelegate.loadOptional(params[0]);
                Collections.sort(result);
                return Pair.create(params[0], result);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected Pair<Integer, List<T>> doNecessaryInBackground(Integer... params) {
                List<T> result = mLoaderDelegate.loadNecessary(params[0]);
                Collections.sort(result);
                return Pair.create(params[0], result);
            }
        };
        if (!pendingReset.get())
            mRefreshable.setEnabled(false);
        mCurrentTask.executeOnExecutor(sExecutor, page.get());
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


    public static class AdapterDelegate {
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

    public static class LoaderDelegate<T extends Updatable> {
        DataLoader<T> loader;

        public LoaderDelegate() {
        }

        public List<T> loadOptional(int page) {
            if (loader != null) {
                return loader.loadOptional(page);
            } else {
                throw new IllegalStateException("You should set DataLoader.");
            }

        }

        public List<T> loadNecessary(int page) {
            if (loader != null) {
                return loader.loadNecessary(page);
            } else {
                throw new IllegalStateException("You should set DataLoader.");
            }

        }
    }
}