package com.getbase.android.db.loaders;

import android.database.sqlite.SQLiteDatabase;

public interface SQLiteDatabaseProvider {

  SQLiteDatabase provide();
}
