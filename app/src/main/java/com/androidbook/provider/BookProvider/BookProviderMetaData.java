package com.androidbook.provider.BookProvider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Shadow on 2016/6/30.
 */
public class BookProviderMetaData {
    public static final String AUTHORITY = "com.androidbook.provider.BookProvider";

    public static final String DATABASE_NAME = "book.db";
    public static final int DATABASE_VERSION = 1;
    public static final String BOOK_TABLE_NAME = "books";

    private BookProviderMetaData() {}

    public static final class BookTableMetaData implements BaseColumns {
        private BookTableMetaData() {}
        public static final String TABLE_NAME = "books";

        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/books");

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.android.book";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.android.book";

        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        public static final String BOOK_NAME = "name";

        public static final String BOOK_ISBN = "isbn";

        public static final String BOOK_AUTHOR = "author";

        public static final String CREATED_DATA = "created";

        public static final String MODIFIED_DATA = "modified";
    }
}
