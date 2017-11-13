package smartenergy.energyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.sql.Timestamp;
import java.util.Objects;


public final class DBProcessedHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "samples_processed.db";

    public DBProcessedHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // Returns samples between endDate-timePeriod and endDate
    public Sample[] queryDB(DBRawHelper dbRawHelper, DBAggregatedHelper dbAggregatedHelper, TimePeriod timePeriod, Timestamp endDate, String sortOrder) {
        refreshCache(dbRawHelper, dbAggregatedHelper);
        return queryLocalDB(timePeriod, endDate, sortOrder);
    }

    public Sample[] queryDB(DBRawHelper dbRawHelper, DBAggregatedHelper dbAggregatedHelper, TimePeriod timePeriod, String sortOrder) {
        refreshCache(dbRawHelper, dbAggregatedHelper);
        return queryLocalDB(timePeriod, null, sortOrder);
    }

    public void refreshCache(DBRawHelper dbRawHelper, DBAggregatedHelper dbAggregatedHelper) {
        Log.d("DBProcessedHelper", "Refreshing database, this can take a while...");
        // Find out latest (highest) cached timestamp
        Cursor cursor = getReadableDatabase().rawQuery(
                String.format("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1", ProcessedEntry.COLUMN_NAME_TIMESTAMP, ProcessedEntry.TABLE_NAME, ProcessedEntry.COLUMN_NAME_TIMESTAMP),
                null
        );
        int tsInSecsOfNewestCached = 0;
        if (cursor.moveToFirst()) {
            tsInSecsOfNewestCached = cursor.getInt(cursor.getColumnIndexOrThrow(ProcessedEntry.COLUMN_NAME_TIMESTAMP));
        }

        // Fill in missing entries from raw db
        Sample[] newSamples = dbRawHelper.queryDB(tsInSecsOfNewestCached, "ASC");
        for (Sample s : newSamples) insertSample(dbAggregatedHelper, s);
        Log.d("DBProcessedHelper", "Inserted " + newSamples.length + " samples from raw db");
    }

    public void deleteProcessedAndAggregatedEntries(DBAggregatedHelper dbAggregatedHelper) {
        getWritableDatabase().execSQL("DELETE FROM " + ProcessedEntry.TABLE_NAME);
        dbAggregatedHelper.deleteEntries();
    }

    private Sample[] queryLocalDB(TimePeriod timePeriod, Timestamp endDate, String sortOrder) {
        return queryLocalDB(timePeriod, endDate, sortOrder, null);
    }

    private Sample[] queryLocalDB(TimePeriod timePeriod, Timestamp endDate, String sortOrder, Integer limit) {
        long startTsInSeconds = TimePeriod.getTimestampOf(timePeriod, endDate).getTime() / 1000;
        long endTsInSeconds = System.currentTimeMillis() / 1000;
        if (endDate != null) endTsInSeconds = endDate.getTime() / 1000;

        Cursor cursor = getReadableDatabase().rawQuery(
                String.format("SELECT * FROM %s WHERE %s > ? AND %s < ? ORDER BY %s %s %s",
                        ProcessedEntry.TABLE_NAME,
                        ProcessedEntry.COLUMN_NAME_TIMESTAMP,
                        ProcessedEntry.COLUMN_NAME_TIMESTAMP,
                        ProcessedEntry.COLUMN_NAME_TIMESTAMP,
                        sortOrder,
                        limit == null ? "" : "LIMIT " + limit),
                new String[]{String.valueOf(startTsInSeconds), String.valueOf(endTsInSeconds)}
        );
        Sample[] cachedSamples = new Sample[cursor.getCount()];
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                MeanOfTransport transport = null;
                String meanOfTransport = cursor.getString(cursor.getColumnIndexOrThrow(ProcessedEntry.COLUMN_NAME_MEAN_OF_TRANSPORT));
                for (MeanOfTransport v : MeanOfTransport.values()) {
                    if (Objects.equals(v.toString(), meanOfTransport)) {
                        transport = v;
                        break;
                    }
                }
                if (transport == null) {
                    Log.e("DBProcessedHelper", "Unknown transport: " + meanOfTransport);
                    transport = MeanOfTransport.STILL;
                }
                // Create Sample object and insert it
                cachedSamples[i] = new Sample(
                        new Timestamp(1000L * cursor.getInt(cursor.getColumnIndexOrThrow(ProcessedEntry.COLUMN_NAME_TIMESTAMP))),
                        transport,
                        cursor.getDouble(cursor.getColumnIndexOrThrow(ProcessedEntry.COLUMN_NAME_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(ProcessedEntry.COLUMN_NAME_LONGITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(ProcessedEntry.COLUMN_NAME_SPEED))
                );
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cachedSamples;
    }

    private Sample getLatestSample() {
        Sample[] samples = queryLocalDB(TimePeriod.YEARLY, null, "DESC", 1);
        if (samples.length == 0) return null;
        return samples[0];
    }

    // This gets called internally when the processed DB gets refreshed
    private void insertSample(DBAggregatedHelper dbAggregatedHelper, Sample sample) {
        // Update AggregatedDB with the new distance
        Sample latestSample = getLatestSample();
        if (latestSample != null && !sample.mostLikelyMeanOfTransport.equals(MeanOfTransport.STILL)) {
            double kms = Analyzer.distFrom(latestSample.getLatitude(), latestSample.getLongitude(), sample.getLatitude(), sample.getLongitude());
            dbAggregatedHelper.addKms(sample.timestamp, sample.mostLikelyMeanOfTransport, kms);
        }

        // Insert into Processed DB
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProcessedEntry.COLUMN_NAME_TIMESTAMP, sample.timestamp.getTime() / 1000);
        values.put(ProcessedEntry.COLUMN_NAME_MEAN_OF_TRANSPORT, sample.mostLikelyMeanOfTransport.toString());
        values.put(ProcessedEntry.COLUMN_NAME_LONGITUDE, sample.getLongitude());
        values.put(ProcessedEntry.COLUMN_NAME_LATITUDE, sample.getLatitude());
        db.insert(ProcessedEntry.TABLE_NAME, null, values);
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProcessedEntry.TABLE_NAME + " (" +
                    ProcessedEntry._ID + "INTEGER PRIMARY KEY," +
                    ProcessedEntry.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    ProcessedEntry.COLUMN_NAME_MEAN_OF_TRANSPORT + " TEXT," +
                    ProcessedEntry.COLUMN_NAME_LONGITUDE + " REAL," +
                    ProcessedEntry.COLUMN_NAME_LATITUDE + " REAL," +
                    ProcessedEntry.COLUMN_NAME_SPEED + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProcessedEntry.TABLE_NAME;

    /* Inner class that defines the table contents */
    private static class ProcessedEntry implements BaseColumns {
        public static final String TABLE_NAME = "activityEntry";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_MEAN_OF_TRANSPORT = "mean_of_transport";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_SPEED = "speed";
    }

}
