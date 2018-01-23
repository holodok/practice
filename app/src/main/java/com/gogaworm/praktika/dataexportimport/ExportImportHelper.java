package com.gogaworm.praktika.dataexportimport;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.util.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;

/**
 * Created by Ilona
 * on 19.11.2017.
 */
public class ExportImportHelper {
    private static ExportImportHelper instance;

    private ExportImportHelper() {
    }

    public static ExportImportHelper getInstance() {
        if (instance == null) {
            instance = new ExportImportHelper();
        }
        return instance;
    }

    private String selectedFileName;
    private boolean selectFileNameDialogShowing;

    private final List<ExportListener> exportListeners = new ArrayList<>();

    public void showSelectFileNameDialog(final String appName, Context context) {
        View dialogView = View.inflate(context, R.layout.dialog_input_file_name, null);
        final TextView errorMessageView = (TextView) dialogView.findViewById(R.id.errorMessage);
        final EditText fileNameView = (EditText) dialogView.findViewById(R.id.fileName);
        fileNameView.setText(new SimpleDateFormat("'backup_'yyyyMMddHHmmss", Locale.US).format(new Date()));

        new AlertDialog.Builder(context)
                .setTitle(R.string.title_export)
                .setView(R.layout.dialog_input_file_name)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        final String fileName = fileNameView.getText().toString();
                        if (isEmpty(fileName)) {
                            errorMessageView.setVisibility(View.VISIBLE);
                            return;
                        }

                        new AsyncTask<String, Void, Boolean>() {

                            @Override
                            protected Boolean doInBackground(String... strings) {
                                return FileUtils.hasFileName(appName, strings[0]);
                            }

                            @Override
                            protected void onPostExecute(Boolean result) {
                                super.onPostExecute(result);
                                if (result) {
                                    dialog.dismiss();
                                    selectFileNameDialogShowing = true;
                                    notifyFileNameSelected(fileName);
                                } else {
                                    errorMessageView.setVisibility(View.VISIBLE);
                                }
                            }
                        }.execute(fileName);
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        selectFileNameDialogShowing = false;
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        selectFileNameDialogShowing = false;
                    }
                })
                .show();

        selectFileNameDialogShowing = true;
    }

    public void exportDatabase(final Context context, final String appName, final String dbName) {
        if (isEmpty(selectedFileName)) {
            return;
        }

        new AsyncTask<Void, Void, Boolean>() {
            String errorMessage;

            @Override
            protected Boolean doInBackground(Void... voids) {
                String exportPath = ""; //todo: appname + selectedfileName
                try {
                    new SqliteToJson(context, dbName, FileUtils.getApplicationDir(appName))
                            .exportAllTables(selectedFileName + ".json");
                    return true;
                } catch (Exception ex) { //todo: remember exception to tell user
                    errorMessage = ex.getLocalizedMessage();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                notifyDataExported(result, errorMessage);
            }
        }.execute();
    }

    public void importDatabase(String appName, String dbName) {

    }

    public void addExportListener(ExportListener exportListener) {
        exportListeners.add(exportListener);
    }

    public void removeExportListener(ExportListener exportListener) {
        exportListeners.remove(exportListener);
    }

    private void notifyFileNameSelected(String fileName) {
        if (exportListeners.isEmpty()) {
            selectedFileName = fileName;
        } else {
            for (ExportListener exportListener : exportListeners) {
                exportListener.onFileNameSelected();
            }
        }
    }

    private void notifyDataExported(boolean result, String error) {
        if (exportListeners.isEmpty()) {
            //store state
        } else {
            for (ExportListener listener : exportListeners) {
                if (result) {
                    listener.onDataExported(selectedFileName);
                } else {
                    listener.onDataExportFailed(error);
                }
            }
        }
    }

    public interface ExportListener {
        void onFileNameSelected();
        void onDataExported(String fileName);
        void onDataExportFailed(String error);
    }
}
