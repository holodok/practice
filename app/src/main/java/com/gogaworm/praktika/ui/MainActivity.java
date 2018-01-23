package com.gogaworm.praktika.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.MonthInfo;
import com.gogaworm.praktika.data.MonthInfoLoader;
import com.gogaworm.praktika.dataexportimport.ExportImportHelper;
import com.gogaworm.praktika.db.DataReaderDbHelper;
import com.gogaworm.praktika.db.MonthInfoDataSource;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<MonthInfo>>, ExportImportHelper.ExportListener {
    private ListView listView;

    private MonthInfoDataSource monthInfoDataSource;
    private ExportImportHelper exportImportHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(android.R.id.list);
        registerForContextMenu(listView);
        TextView emptyView = (TextView) findViewById(android.R.id.empty);
        listView.setEmptyView(emptyView);

        monthInfoDataSource = new MonthInfoDataSource(this);
        exportImportHelper = ExportImportHelper.getInstance();

        getSupportLoaderManager().initLoader(100, null, this).forceLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        monthInfoDataSource.open();
        exportImportHelper.addExportListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        exportImportHelper.removeExportListener(this);
        monthInfoDataSource.close();
    }

    @Override
    public Loader<List<MonthInfo>> onCreateLoader(int id, Bundle args) {
        return new MonthInfoLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<MonthInfo>> loader, List<MonthInfo> data) {
        listView.setAdapter(new MonthInfoAdapter(this, data));
    }

    @Override
    public void onLoaderReset(Loader<List<MonthInfo>> loader) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            getSupportLoaderManager().getLoader(100).forceLoad();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(MainActivity.this, NewMonthActivity.class);
                Calendar instance = Calendar.getInstance();
                intent.putExtra("monthInfo", new MonthInfo(0, instance.get(Calendar.YEAR), instance.get(Calendar.MONTH)));
                startActivityForResult(intent, 100);
                break;
            case R.id.action_export:
                exportData();
                break;
            case R.id.action_import:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_context_main, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.action_delete:
                MonthInfo monthInfo = (MonthInfo) listView.getAdapter().getItem(info.position);
                new DeleteRecordAsyncTask().execute(monthInfo);
                return true;
            case R.id.action_edit:
                //todo
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void startMonthDayList(MonthInfo monthInfo) {
        Intent intent = new Intent(this, MonthDayListActivity.class);
        intent.putExtra("monthInfo", monthInfo);
        startActivity(intent);
    }

    private void exportData() {
        exportImportHelper.showSelectFileNameDialog(getString(R.string.app_name), this);
    }

    @Override
    public void onFileNameSelected() {
        exportImportHelper.exportDatabase(this, getString(R.string.app_name), DataReaderDbHelper.DATABASE_NAME);
    }

    private static abstract class DialogTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
        Context context;

        DialogTask(Context context) {
            this.context = context;
        }

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setIndeterminate(true);
            progressDialog.show();

        }

        @Override
        protected final void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                internalPostExecute(result);
            }
        }

        abstract void internalPostExecute(Result result);
    }

    private class MonthInfoAdapter extends ArrayAdapter<MonthInfo> {

        MonthInfoAdapter(Context context, List<MonthInfo> data) {
            super(context, android.R.layout.simple_list_item_1, android.R.id.text1, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            final MonthInfo item = getItem(position);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(item.getTitle(getContext()));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startMonthDayList(item);
                    notifyDataSetInvalidated();
                }
            });
            return view;
        }
    }

    private class DeleteRecordAsyncTask extends AsyncTask<MonthInfo, Void, Void> {
        @Override
        protected Void doInBackground(MonthInfo... monthInfos) {
            monthInfoDataSource.deleteMonthInfo(monthInfos[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getSupportLoaderManager().getLoader(100).forceLoad();
        }
    }
}
