package co.yishun.library.datacenter.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.yishun.library.datacenter.DataCenter;
import co.yishun.library.datacenter.DataCenterAdapter;
import co.yishun.library.datacenter.PageIntegerLoadIndex;
import co.yishun.library.datacenter.PageIntegerLoadIndexProvider;
import co.yishun.library.datacenter.SuperRecyclerViewLoadMore;
import co.yishun.library.datacenter.SuperRecyclerViewRefreshable;
import co.yishun.library.datacenter.Updatable;

/**
 * Created by carlos on 2/5/16.
 */
public class SuperRecyclerViewSampleActivity extends AppCompatActivity implements
        DataCenter.OnEndListener<PageIntegerLoadIndex<SuperRecyclerViewSampleActivity.SampleStringData>
                , SuperRecyclerViewSampleActivity.SampleStringData>,
        DataCenter.DataLoader<PageIntegerLoadIndex<SuperRecyclerViewSampleActivity.SampleStringData>
                , SuperRecyclerViewSampleActivity.SampleStringData> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_recycler_view);

        SuperRecyclerView superRecyclerView = (SuperRecyclerView) findViewById(R.id.superRecyclerView);
        superRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final DataCenterAdapter<PageIntegerLoadIndex<SuperRecyclerViewSampleActivity.SampleStringData>,
                SampleStringData, SampleViewHolder> adapter = new SampleDataCenterAdapter(this);
        adapter.setLoader(this);
        adapter.setOnEndListener(this);
        adapter.setRefreshable(new SuperRecyclerViewRefreshable(superRecyclerView));
        adapter.setLoadMore(new SuperRecyclerViewLoadMore(superRecyclerView));
        adapter.setLoadIndexProvider(new PageIntegerLoadIndexProvider<SampleStringData>());
        superRecyclerView.setAdapter(adapter);

        adapter.loadNext();

    }

    @Override
    public List<SampleStringData> loadOptional(PageIntegerLoadIndex<SampleStringData> index) {
        try {
            Thread.sleep(1000 * 1);
        } catch (InterruptedException ignore) {
        }
        List<SampleStringData> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            result.add(new SampleStringData(true, 100, i + index.getPage() * 10));
        }
        return result;
    }

    @Override
    public List<SampleStringData> loadNecessary(PageIntegerLoadIndex<SampleStringData> index) {
        try {
            Thread.sleep(1000 * 7);
        } catch (InterruptedException ignore) {
        }
        List<SampleStringData> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            result.add(new SampleStringData(false, 1000, i + index.getPage() * 10));
        }
        return result;
    }

    @Override
    public void onFail(PageIntegerLoadIndex<SampleStringData> index) {
        Toast.makeText(this, "fail!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(PageIntegerLoadIndex<SampleStringData> index) {

    }

    public final static class SampleDataCenterAdapter extends
            DataCenterAdapter<PageIntegerLoadIndex<SuperRecyclerViewSampleActivity.SampleStringData>,
                    SampleStringData, SampleViewHolder> {
        public SampleDataCenterAdapter(FragmentActivity activity) {
            super(activity);
        }

        @Override
        public void onBindViewHolder(SampleViewHolder holder, int position, SampleStringData data) {
            holder.setData(data);
        }


        @Override
        public SampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_item_list, parent, false);
            return new SampleViewHolder(view);
        }
    }

    public final static class SampleStringData implements Updatable {
        private final boolean cached;
        private final int time;
        private final int index;

        public SampleStringData(boolean cached, int time, int index) {
            this.time = time;
            this.index = index;
            this.cached = cached;
        }

        public String value() {
            return index + (cached ? " cached" : "");
        }

        @Override
        public boolean updateThan(Updatable updatable) {
            return time > ((SampleStringData) updatable).time;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SampleStringData && this.index == ((SampleStringData) o).index;
        }

        @Override
        public int compareTo(@NonNull Object another) {
            return index - ((SampleStringData) another).index;
        }
    }

    public final static class SampleViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public SampleViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }

        public void setData(SampleStringData data) {
            textView.setText(data.value());
        }
    }
}
