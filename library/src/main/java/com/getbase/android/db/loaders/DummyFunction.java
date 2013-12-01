package com.getbase.android.db.loaders;

import com.google.common.base.Function;

public class DummyFunction<T> implements Function<T, T> {

  private static final DummyFunction INSTANCE = new DummyFunction();

  public static <T> DummyFunction<T> instance() {
    return INSTANCE;
  }

  private DummyFunction() {
  }

  @Override
  public T apply(T t) {
    return t;
  }
}
