package co.yishun.library.datacenter.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String GITHUB_URL = "https://github.com/a642500/DataCenter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            case R.id.githubMenu:
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "No application can handle this request."
                            + " Please install a web browser", Toast.LENGTH_LONG).show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onSuperRecyclerViewSample(View view) {
        Intent intent = new Intent(this, SuperRecyclerViewSampleActivity.class);
        startActivity(intent);
    }

    public void onRecyclerView(View view) {
        Intent intent = new Intent(this, RecyclerViewSampleActivity.class);
        startActivity(intent);
    }
}
