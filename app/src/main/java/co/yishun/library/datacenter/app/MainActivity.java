package co.yishun.library.datacenter.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import co.yishun.library.datacenter.DataCenter;
import co.yishun.library.datacenter.DataCenters;
import co.yishun.library.datacenter.SwipeRefreshLayoutRefreshable;
import co.yishun.library.datacenter.Updatable;

public class MainActivity extends AppCompatActivity implements DataCenter.OnEndListener, DataCenter.DataLoader<MainActivity.SampleStringData> {
    private DataCenter<SampleStringData> mDataCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mDataCenter.reset();
                mDataCenter.loadNext();
            }
        });

        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.recyclerView));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDataCenter = DataCenters.getDataCenter();

        RecyclerView.Adapter<SampleViewHolder> adapter = new SampleAdapter(this, mDataCenter);
        recyclerView.setAdapter(adapter);

        mDataCenter.setObservableAdapter(adapter);
        mDataCenter.setLoader(this);
        mDataCenter.setOnEndListener(this);
        mDataCenter.setRefreshable(new SwipeRefreshLayoutRefreshable(swipeRefreshLayout));
        mDataCenter.loadNext();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuEntryTest:
                Intent intent = new Intent(this, TestActivity.class);
                startActivity(intent);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public List<SampleStringData> loadOptional(int page) {
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException ignore) {
        }
        List<SampleStringData> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            result.add(new SampleStringData(String.valueOf((page + 1) * 10 + i), 1000 + i, page + i));
        }
        return result;
    }

    @Override
    public List<SampleStringData> loadNecessary(int page) {
        try {
            Thread.sleep(1000 * 1);
        } catch (InterruptedException ignore) {
        }
        List<SampleStringData> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            result.add(new SampleStringData(String.valueOf((page + 1) * 100 + i), 2000 + i, page + i));
        }
        return result;
    }

    @Override
    public void onFail(int page) {
        Toast.makeText(this, "fail!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(int page) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataCenter.release();
    }

    public final static class SampleAdapter extends RecyclerView.Adapter<SampleViewHolder> {
        private final Context mContext;
        private final DataCenter<SampleStringData> mDataCenter;

        public SampleAdapter(Context mContext, DataCenter<SampleStringData> mDataCenter) {
            this.mContext = mContext;
            this.mDataCenter = mDataCenter;
        }

        @Override
        public SampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new SampleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SampleViewHolder holder, int position) {
            holder.setData(mDataCenter.data().get(position));
        }

        @Override
        public int getItemCount() {
            return mDataCenter.data().size();
        }
    }

    public final static class SampleStringData implements Updatable {
        private final String mValue;
        private final int time;
        private final int index;

        public SampleStringData(String mValue, int time, int index) {
            this.mValue = mValue;
            this.time = time;
            this.index = index;
        }

        public String value() {
            return mValue;
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
            return Integer.valueOf(mValue) - Integer.valueOf(((SampleStringData) another).mValue);
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
