package smartenergy.energyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Pair;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by sandro on 30.10.17.
 * Inspired by:
 * -> https://developer.android.com/training/basics/data-storage/databases.html#DbHelper
 * -> https://www.tutorialspoint.com/android/android_sqlite_database.htm
 * -> https://stackoverflow.com/questions/9156340/how-to-copy-a-row-and-insert-in-same-table-with-a-autoincrement-field-in-mysql
 */

public final class DBAggregatedHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "samples_aggregated.db";

    public DBAggregatedHelper(Context context) {
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

    public double getKmsForTransportInPeriod(DBRawHelper dbRawHelper, DBProcessedHelper dbProcessedHelper, MeanOfTransport transport, TimePeriod timePeriod, Timestamp endDate) {
        dbProcessedHelper.refreshCache(dbRawHelper, this);
        String fromTime = String.valueOf(TimePeriod.getTimestampOf(timePeriod).getTime() / 1000);
        long endTsInSeconds = System.currentTimeMillis() / 1000;
        if (endDate != null) endTsInSeconds = endDate.getTime() / 1000;
        String toTime = String.valueOf(endTsInSeconds);
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT " + AggregatedEntry.COLUMN_NAME_TIMESTAMP + ", SUM(" + rowFor(transport) + ") AS kms " +
                        "FROM " + AggregatedEntry.TABLE_NAME + " " +
                        "WHERE " + AggregatedEntry.COLUMN_NAME_TIMESTAMP + " >= ? " +
                        "AND " + AggregatedEntry.COLUMN_NAME_TIMESTAMP + " <= ? ",
                new String[]{
                        fromTime,
                        toTime
                });
        if (!cursor.moveToNext()) return -1.0;
        return cursor.getDouble(cursor.getColumnIndexOrThrow("kms"));
    }

    public ArrayList<Pair<Integer, Double>> getKmsPerPeriod(DBRawHelper dbRawHelper, DBProcessedHelper dbProcessedHelper, TimePeriod since, TimePeriod granularity, MeanOfTransport transport) {
        dbProcessedHelper.refreshCache(dbRawHelper, this);
        String timeSelector;
        if (granularity.equals(TimePeriod.DAILY))
            timeSelector = "%d";
        else if (granularity.equals(TimePeriod.MONTHLY))
            timeSelector = "%m";
        else throw new RuntimeException("Unsupported time period: " + granularity);
        String startTs = String.valueOf(TimePeriod.getTimestampOf(since).getTime() / 1000);
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT strftime('" + timeSelector + "',`" + AggregatedEntry.COLUMN_NAME_TIMESTAMP + "`, 'unixepoch') as `timeAgr`, " +
                        AggregatedEntry.COLUMN_NAME_TIMESTAMP + ", " +
                        "Sum(" + rowFor(transport) + ") AS `kms` " +
                        "FROM " + AggregatedEntry.TABLE_NAME + " " +
                        "WHERE " + AggregatedEntry.COLUMN_NAME_TIMESTAMP + " >= ? " +
                        "GROUP BY `timeAgr`",
                new String[]{String.valueOf(startTs)}
        );
        ArrayList<Pair<Integer, Double>> res = new ArrayList<>();
        if (!cursor.moveToFirst()) return res;
        do {
            res.add(new Pair<Integer, Double>(
                    cursor.getInt(cursor.getColumnIndexOrThrow(AggregatedEntry.COLUMN_NAME_TIMESTAMP)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("kms"))
            ));
        } while (cursor.moveToNext());
        return res;
    }

    public void addKms(Timestamp timestamp, MeanOfTransport transport, double kms) {
        int tsSeconds = (int) (TimePeriod.getTimestampOf(TimePeriod.DAILY, timestamp).getTime() / 1000);
        // Load existing entry if any
        Cursor cursor = getReadableDatabase().rawQuery(
                String.format("SELECT * FROM %s WHERE %s = ?",
                        AggregatedEntry.TABLE_NAME, AggregatedEntry.COLUMN_NAME_TIMESTAMP),
                new String[]{String.valueOf(tsSeconds)}
        );
        String concernedRow = rowFor(transport);
        if (cursor.moveToFirst()) {
            double newKms = kms + cursor.getDouble(cursor.getColumnIndexOrThrow(concernedRow));
            ContentValues contentValues = new ContentValues();
            contentValues.put(concernedRow, newKms);
            getWritableDatabase().update(
                    AggregatedEntry.TABLE_NAME,
                    contentValues,
                    AggregatedEntry.COLUMN_NAME_TIMESTAMP + "=?",
                    new String[]{String.valueOf(tsSeconds)}
            );
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AggregatedEntry.COLUMN_NAME_TIMESTAMP, tsSeconds);
            for (String col : AggregatedEntry.ALL_TRANSPORT_COLUMN_NAMES) {
                contentValues.put(col, col.equals(concernedRow) ? kms : 0);
            }
            getWritableDatabase().insert(
                    AggregatedEntry.TABLE_NAME,
                    null,
                    contentValues
            );
        }
        cursor.close();
    }

    public void deleteEntries() {
        getWritableDatabase().execSQL("DELETE FROM " + AggregatedEntry.TABLE_NAME);
    }

    private String rowFor(MeanOfTransport transport) {
        switch (transport) {
            case CAR:
                return AggregatedEntry.COLUMN_NAME_KM_CAR;
            case TRAMWAY:
                return AggregatedEntry.COLUMN_NAME_KM_TRAMWAY;
            case TRAIN:
                return AggregatedEntry.COLUMN_NAME_KM_TRAIN;
            case BUS:
                return AggregatedEntry.COLUMN_NAME_KM_BUS;
            case BOAT:
                return AggregatedEntry.COLUMN_NAME_KM_BOAT;
            case AIRPLANE:
                return AggregatedEntry.COLUMN_NAME_KM_AIRPLANE;
            case FOOT:
                return AggregatedEntry.COLUMN_NAME_KM_FOOT;
            case BICYCLE:
                return AggregatedEntry.COLUMN_NAME_KM_BICYCLE;
            default:
                throw new RuntimeException("Unsupported mean of transport: " + transport.toString());
        }
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + AggregatedEntry.TABLE_NAME + " (" +
                    AggregatedEntry._ID + "INTEGER PRIMARY KEY," +
                    AggregatedEntry.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    AggregatedEntry.COLUMN_NAME_KM_CAR + " REAL," +
                    AggregatedEntry.COLUMN_NAME_KM_TRAMWAY + " REAL," +
                    AggregatedEntry.COLUMN_NAME_KM_TRAIN + " REAL," +
                    AggregatedEntry.COLUMN_NAME_KM_BUS + " REAL," +
                    AggregatedEntry.COLUMN_NAME_KM_BOAT + " REAL," +
                    AggregatedEntry.COLUMN_NAME_KM_AIRPLANE + " REAL," +
                    AggregatedEntry.COLUMN_NAME_KM_FOOT + " REAL," +
                    AggregatedEntry.COLUMN_NAME_KM_BICYCLE + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AggregatedEntry.TABLE_NAME;

    /* Inner class that defines the table contents */
    private static class AggregatedEntry implements BaseColumns {
        public static final String TABLE_NAME = "aggregatedEntry";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_KM_CAR = "km_car";
        public static final String COLUMN_NAME_KM_TRAMWAY = "km_tramway";
        public static final String COLUMN_NAME_KM_TRAIN = "km_train";
        public static final String COLUMN_NAME_KM_BUS = "km_bus";
        public static final String COLUMN_NAME_KM_BOAT = "km_boat";
        public static final String COLUMN_NAME_KM_AIRPLANE = "km_airplane";
        public static final String COLUMN_NAME_KM_FOOT = "km_foot";
        public static final String COLUMN_NAME_KM_BICYCLE = "km_bicycle";
        public static String[] ALL_TRANSPORT_COLUMN_NAMES = {
                COLUMN_NAME_KM_CAR,
                COLUMN_NAME_KM_TRAMWAY,
                COLUMN_NAME_KM_TRAIN,
                COLUMN_NAME_KM_BUS,
                COLUMN_NAME_KM_BOAT,
                COLUMN_NAME_KM_AIRPLANE,
                COLUMN_NAME_KM_FOOT,
                COLUMN_NAME_KM_BICYCLE
        };
    }

}