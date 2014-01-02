package com.getbase.android.db.provider;

import com.google.common.base.Joiner;

import android.content.ContentProviderOperation;
import android.net.Uri;
import android.os.RemoteException;

import java.util.Collection;

public class Delete extends ProviderAction<Integer> implements ConvertibleToOperation {

  private final Selection selection = new Selection();

  Delete(Uri uri) {
    super(uri);
  }

  public Delete where(String selection, Object... selectionArgs) {
    this.selection.append(selection, selectionArgs);
    return this;
  }

  public <T> Delete whereIn(String column, Collection<T> collection) {
    this.selection.append(column + " IN (" + Joiner.on(",").join(collection) + ")");
    return this;
  }

  @Override
  public Integer perform(CrudHandler crudHandler) throws RemoteException {
    return crudHandler.delete(getUri(), selection.getSelection(), selection.getSelectionArgs());
  }

  @Override
  public ContentProviderOperation toContentProviderOperation() {
    return ContentProviderOperation.newDelete(getUri())
        .withSelection(selection.getSelection(), selection.getSelectionArgs())
        .build();
  }
}
