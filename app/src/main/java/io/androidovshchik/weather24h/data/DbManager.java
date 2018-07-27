package io.androidovshchik.weather24h.data;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Factory;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.squareup.sqlbrite3.BriteDatabase;
import com.squareup.sqlbrite3.SqlBrite;

import io.androidovshchik.weather24h.BuildConfig;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;

public class DbManager {

    public BriteDatabase db;

    public DbManager(Context context) {
        DbCallback dbCallback = new DbCallback();
        dbCallback.openDatabase(context);
        Configuration configuration = Configuration.builder(context)
            .name(DbCallback.DATABASE_NAME)
            .callback(dbCallback)
            .build();
        Factory factory = new FrameworkSQLiteOpenHelperFactory();
        SupportSQLiteOpenHelper openHelper = factory.create(configuration);
        db = new SqlBrite.Builder()
            .logger(message -> Log.v(getClass().getSimpleName(), message))
            .build()
            .wrapDatabaseHelper(openHelper, Schedulers.io());
        db.setLoggingEnabled(BuildConfig.DEBUG);
    }

    @SuppressWarnings("all")
    public Observable<Cursor> onSelectTable(String sql) {
        return Observable.create((ObservableEmitter<Cursor> emitter) -> {
            if (emitter.isDisposed()) {
                return;
            }
            Cursor cursor = null;
            BriteDatabase.Transaction transaction = db.newTransaction();
            try {
                cursor = db.query(sql);
                transaction.markSuccessful();
            } finally {
                transaction.end();
            }
            if (cursor != null) {
                emitter.onNext(cursor);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }
}
