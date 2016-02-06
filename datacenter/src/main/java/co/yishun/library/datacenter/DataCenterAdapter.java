package co.yishun.library.datacenter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by carlos on 2/5/16.
 */
public abstract class DataCenterAdapter<T extends Updatable, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private DataCenter<T> mDataCenter;
    private Context mContext;

    public DataCenterAdapter(FragmentActivity activity) {
        mContext = activity.getApplicationContext();
        mDataCenter = DataCenters.getDataCenter();
        mDataCenter.setObservableAdapter(this);
    }

    public void setRefreshable(Refreshable refreshable) {
        mDataCenter.setRefreshable(refreshable);
    }

    public void release() {
        mDataCenter.release();
    }

    public Context getContext() {
        return mContext;
    }

    public void setOnEndListener(DataCenter.OnEndListener listener) {
        mDataCenter.setOnEndListener(listener);
    }

    public void setLoader(DataCenter.DataLoader<T> dataLoader) {
        mDataCenter.setLoader(dataLoader);
    }

    public void reset() {
        mDataCenter.reset();
    }

    public void loadNext() {
        mDataCenter.loadNext();
    }

    public List<T> data() {
        return mDataCenter.data();
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        onBindViewHolder(holder, position, mDataCenter.data().get(position));
    }

    public abstract void onBindViewHolder(VH holder, int position, T data);


    @Override
    public int getItemCount() {
        return mDataCenter.data().size();
    }
}
