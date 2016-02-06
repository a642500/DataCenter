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

import java.util.ArrayList;
import java.util.List;

import co.yishun.library.datacenter.DataCenter;
import co.yishun.library.datacenter.DataCenterAdapter;
import co.yishun.library.datacenter.Updatable;

/**
 * Created by carlos on 2/5/16.
 */
public class TestActivity extends AppCompatActivity implements DataCenter.OnEndListener, DataCenter.DataLoader<TestActivity.SampleStringData> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.recyclerView));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DataCenterAdapter<SampleStringData, SampleViewHolder> adapter = new SampleDataCenterAdapter(this);
        adapter.setLoader(this);
        adapter.setOnFailListener(this);

        recyclerView.setAdapter(adapter);
        adapter.loadNext();

    }

    @Override
    public List<SampleStringData> loadOptional(int page) {
        List<SampleStringData> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            result.add(new SampleStringData(String.valueOf(page * 10 + i)));
        }
        return result;
    }

    @Override
    public List<SampleStringData> loadNecessary(int page) {
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException ignore) {
        }
        List<SampleStringData> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            result.add(new SampleStringData(String.valueOf(page * 10 + i)));
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

    public final static class SampleDataCenterAdapter extends DataCenterAdapter<SampleStringData, SampleViewHolder> {
        public SampleDataCenterAdapter(FragmentActivity activity) {
            super(activity);
        }

        @Override
        public void onBindViewHolder(SampleViewHolder holder, int position, SampleStringData data) {
            holder.setData(data);
        }


        @Override
        public SampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new SampleViewHolder(view);
        }
    }

    public final static class SampleStringData implements Updatable {
        private final String mValue;

        public SampleStringData(String mValue) {
            this.mValue = mValue;
        }

        public String value() {
            return mValue;
        }

        @Override
        public boolean updateThan(Updatable updatable) {
            return false;
        }

        @Override
        public int compareTo(@NonNull Object another) {
            return 0;
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
