package com.getbase.android.db.loaders;

import com.google.common.base.Function;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteLoader<T> extends AbstractLoader<T> {

  private final Function<Cursor, T> transform;
  private final SQLiteDatabaseProvider dbProvider;
  private final Function<SQLiteDatabase, Cursor> dbQuerier;

  SQLiteLoader(Context context, SQLiteDatabaseProvider dbProvider, Function<SQLiteDatabase, Cursor> querier, Function<Cursor, T> transform) {
    super(context);
    this.transform = transform;
    this.dbProvider = dbProvider;
    this.dbQuerier = querier;
  }

  @Override
  public T loadInBackground() {
    final Cursor cursor = dbQuerier.apply(dbProvider.provide());
    return transform.apply(cursor);
  }
}
