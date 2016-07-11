package com.androidbook.provider.BookProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Shadow on 2016/7/11.
 */
public class BookProvider extends ContentProvider {
    private static final String TAG = "BookProvider";

    private static HashMap<String, String> sBookProjectionMap;
    static
    {
        sBookProjectionMap = new HashMap<String, String>();
        sBookProjectionMap.put(BookProviderMetaData.BookTableMetaData._ID,
                BookProviderMetaData.BookTableMetaData._ID);

        sBookProjectionMap.put(BookProviderMetaData.BookTableMetaData.BOOK_NAME,
                BookProviderMetaData.BookTableMetaData.BOOK_NAME);
        sBookProjectionMap.put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN,
                BookProviderMetaData.BookTableMetaData.BOOK_ISBN);
        sBookProjectionMap.put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR,
                BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR);

        sBookProjectionMap.put(BookProviderMetaData.BookTableMetaData.CREATED_DATA,
                BookProviderMetaData.BookTableMetaData.CREATED_DATA);
        sBookProjectionMap.put(BookProviderMetaData.BookTableMetaData.MODIFIED_DATA,
                BookProviderMetaData.BookTableMetaData.MODIFIED_DATA);

    }
    private static final UriMatcher sUriMatcher;
    private static final int INCOMING_BOOK_COLLECTION_URI_INDICATOR = 1;
    private static final int INCOMING_SINGLE_BOOK_URI_INDICATOR = 2;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(BookProviderMetaData.AUTHORITY, "books", INCOMING_BOOK_COLLECTION_URI_INDICATOR);
        sUriMatcher.addURI(BookProviderMetaData.AUTHORITY, "books/#", INCOMING_SINGLE_BOOK_URI_INDICATOR);
    }
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context,
                    BookProviderMetaData.DATABASE_NAME,
                    null,
                    BookProviderMetaData.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "inner oncreate called");
            db.execSQL("CREATE TABLE " + BookProviderMetaData.BookTableMetaData.TABLE_NAME + " ("
                    + BookProviderMetaData.BookTableMetaData._ID + " INTEGER PRIMARY KEY,"
                    + BookProviderMetaData.BookTableMetaData.BOOK_NAME + " TEXT,"
                    + BookProviderMetaData.BookTableMetaData.BOOK_ISBN + " TEXT,"
                    + BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR + " TEXT,"
                    + BookProviderMetaData.BookTableMetaData.CREATED_DATA + "INTEGER,"
                    + BookProviderMetaData.BookTableMetaData.MODIFIED_DATA + "INTEGER"
                    + ");");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "inner onupgrade called");
            Log.w(TAG, "Upgrading database from version "
                    + oldVersion + " to "
                    + newVersion + ",which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " +
                    BookProviderMetaData.BookTableMetaData.TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "main onCreate called");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                qb.setTables(BookProviderMetaData.BookTableMetaData.TABLE_NAME);
                qb.setProjectionMap(sBookProjectionMap);
                break;
            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                qb.setTables(BookProviderMetaData.BookTableMetaData.TABLE_NAME);
                qb.setProjectionMap(sBookProjectionMap);
                qb.appendWhere(BookProviderMetaData.BookTableMetaData._ID + "="
                    + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknow URI " + uri);
        }
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = BookProviderMetaData.BookTableMetaData.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs,null, null, orderBy);

        int i = c.getCount();

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                return BookProviderMetaData.BookTableMetaData.CONTENT_TYPE;
            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                return BookProviderMetaData.BookTableMetaData.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri)
                != INCOMING_BOOK_COLLECTION_URI_INDICATOR) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        if (values.containsKey(BookProviderMetaData.BookTableMetaData.CREATED_DATA) == false) {
            values.put(BookProviderMetaData.BookTableMetaData.CREATED_DATA, now);
        }
        if (values.containsKey(BookProviderMetaData.BookTableMetaData.MODIFIED_DATA) == false) {
            values.put(BookProviderMetaData.BookTableMetaData.MODIFIED_DATA, now);
        }

        if (values.containsKey(BookProviderMetaData.BookTableMetaData.BOOK_NAME) == false) {
            throw new android.database.SQLException(
                    "Failed to insert row because Book Name is needed " + uri);
        }

        if(values.containsKey(BookProviderMetaData.BookTableMetaData.BOOK_ISBN) == false) {
            values.put(BookProviderMetaData.BookTableMetaData.BOOK_ISBN, "Unknow ISBN");
        }
        if(values.containsKey(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR) == false) {
            values.put(BookProviderMetaData.BookTableMetaData.BOOK_AUTHOR, "Unknow Author");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                BookProviderMetaData.BookTableMetaData.BOOK_NAME, values);
        if (rowId > 0) {
            Uri insertedBookUri =
                    ContentUris.withAppendedId(
                            BookProviderMetaData.BookTableMetaData.CONTENT_URI, rowId
                    );
            getContext()
                    .getContentResolver()
                    .notifyChange(insertedBookUri, null);

            return insertedBookUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                count = db.delete(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                        where, whereArgs);
                break;
            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                String rowId = uri.getPathSegments().get(1);
                count = db.delete(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                        BookProviderMetaData.BookTableMetaData._ID + "=" + rowId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                        whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknow URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                count = db.update(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                        values, where, whereArgs);
                break;
            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                String rowId = uri.getPathSegments().get(1);
                count = db.update(BookProviderMetaData.BookTableMetaData.TABLE_NAME,
                        values,
                        BookProviderMetaData.BookTableMetaData._ID + "=" + rowId
                                + (!TextUtils.isEmpty(where)? " AND (" + where +')' : ""),
                        whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknow Uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
