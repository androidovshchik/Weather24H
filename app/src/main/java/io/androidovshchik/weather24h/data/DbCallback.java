package io.androidovshchik.weather24h.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class DbCallback extends SupportSQLiteOpenHelper.Callback {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "weather.sqlite";

    private static final String DATABASE_PATH_SUFFIX = "/databases/";

    public DbCallback() {
        super(DATABASE_VERSION);
    }

    @Override
    public void onCreate(SupportSQLiteDatabase db) {}

    @Override
    public void onUpgrade(SupportSQLiteDatabase db, int oldVersion, int newVersion) { }

    public void openDatabase(Context context) {
        File file = context.getDatabasePath(DATABASE_NAME);
        if (!file.exists()) {
            try {
                copyDatabaseFromAssets(context);
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }

    private void copyDatabaseFromAssets(Context context) throws IOException {
        InputStream input = context.getAssets().open(DATABASE_NAME);
        String outFilename = getDatabasePath(context);
        File file = new File(context.getApplicationInfo().dataDir + DATABASE_PATH_SUFFIX);
        boolean made;
        if (!file.exists()) {
            made = file.mkdir();
            if (!made) {
                return;
            }
        }
        OutputStream output = new FileOutputStream(outFilename);
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } finally {
            output.flush();
            output.close();
            input.close();
        }
    }

    private String getDatabasePath(Context context) {
        return context.getApplicationInfo().dataDir + DATABASE_PATH_SUFFIX + DATABASE_NAME;
    }
}