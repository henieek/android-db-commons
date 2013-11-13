package com.getbase.android.db.provider;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.android.content.ContentValuesEntry.entry;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ProviderActionsTest {

  private static final Uri TEST_URI = Uri.parse("content://authority/people");

  @Mock
  private ContentResolver contentResolverMock;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldPassNullsEverywhere() throws Exception {
    ProviderAction.query(TEST_URI)
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq((String) null), eq((String[]) null), eq((String) null));
  }

  @Test
  public void shouldUseProjectionWhenQuery() throws Exception {
    ProviderAction.query(TEST_URI)
        .projection("COL1")
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq(new String[] { "COL1" }), eq((String) null), eq((String[]) null), eq((String) null));
  }

  @Test
  public void shouldAppendProjection() throws Exception {
    ProviderAction.query(TEST_URI)
        .projection("COL1")
        .projection("COL2")
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq(new String[] { "COL1", "COL2" }), eq((String) null), eq((String[]) null), eq((String) null));
  }

  @Test
  public void shouldConcatenateSelectionProperlyWhenQuerying() throws Exception {
    ProviderAction.query(TEST_URI)
        .where("COL1 = ?", "arg")
        .where("COL2 = ?", "arg2")
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq("COL1 = ? AND COL2 = ?"), eq(new String[] { "arg", "arg2" }), eq((String) null));
  }

  @Test
  public void shouldUseOrderBy() throws Exception {
    ProviderAction.query(TEST_URI)
        .orderBy("COL1 DESC")
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq((String) null), eq((String[]) null), eq("COL1 DESC"));
  }

  @Test
  public void shouldPerformProperInsert() throws Exception {
    ContentValues values = new ContentValues();
    values.put("asdf", "value");
    ProviderAction.insert(TEST_URI)
        .values(values)
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), eq(values));
  }

  @Test
  public void shouldPerformInsertWithSingleValue() throws Exception {
    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.insert(TEST_URI)
        .value("col1", "val1")
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), contentValuesArgument.capture());
    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"));
  }

  @Test
  public void shouldNotModifyPassedContentValues() throws Exception {
    ContentValues genericValues = new ContentValues();

    ProviderAction.insert(TEST_URI)
        .values(genericValues)
        .value("key", "value")
        .perform(contentResolverMock);

    reset(contentResolverMock);

    ProviderAction.insert(TEST_URI)
        .values(genericValues)
        .value("another_key", "another_value")
        .perform(contentResolverMock);

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    verify(contentResolverMock).insert(eq(TEST_URI), contentValuesArgument.capture());
    final ContentValues valuesReceived = contentValuesArgument.getValue();
    assertThat(valuesReceived).contains(entry("another_key", "another_value"));
    Assertions.assertThat(valuesReceived.containsKey("key")).isFalse();
  }

  @Test
  public void shouldPerformInsertWithConcatenatedContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");

    ContentValues secondValues = new ContentValues();
    secondValues.put("col2", "val2");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.insert(TEST_URI)
        .values(firstValues)
        .values(secondValues)
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), contentValuesArgument.capture());

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", "val2"));
  }

  @Test
  public void shouldPerformInsertWithContentValuesOverriddenBySingleValue() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    values.put("col2", "val2");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.insert(TEST_URI)
        .values(values)
        .value("col2", null)
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), contentValuesArgument.capture());

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", null));
  }

  @Test
  public void shouldPerformInsertWithContentValuesOverriddenByOtherContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");
    firstValues.put("col2", "val2");

    ContentValues secondValues = new ContentValues();
    secondValues.putNull("col2");
    secondValues.put("col3", "val3");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.insert(TEST_URI)
        .values(firstValues)
        .values(secondValues)
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), contentValuesArgument.capture());

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col3", "val3"), entry("col2", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectInsertWithSingleValueOfUnsupportedType() throws Exception {
    ProviderAction.insert(TEST_URI).value("col1", new Object());
  }

  @Test
  public void shouldPerformUpdateWithValues() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    ProviderAction.update(TEST_URI)
        .values(values)
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), eq(values), eq((String) null), eq((String[]) null));
  }

  @Test
  public void shouldPerformUpdateWithSingleValue() throws Exception {
    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.update(TEST_URI)
        .value("col1", "val1")
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), contentValuesArgument.capture(), eq((String) null), eq((String[]) null));
    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"));
  }

  @Test
  public void shouldPerformUpdateWithConcatenatedContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");

    ContentValues secondValues = new ContentValues();
    secondValues.put("col2", "val2");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.update(TEST_URI)
        .values(firstValues)
        .values(secondValues)
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), contentValuesArgument.capture(), eq((String) null), eq((String[]) null));

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", "val2"));
  }

  @Test
  public void shouldPerformUpdateWithContentValuesOverriddenBySingleValue() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    values.put("col2", "val2");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.update(TEST_URI)
        .values(values)
        .value("col2", null)
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), contentValuesArgument.capture(), eq((String) null), eq((String[]) null));

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", null));
  }

  @Test
  public void shouldPerformUpdateWithContentValuesOverriddenByOtherContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");
    firstValues.put("col2", "val2");

    ContentValues secondValues = new ContentValues();
    secondValues.putNull("col2");
    secondValues.put("col3", "val3");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.update(TEST_URI)
        .values(firstValues)
        .values(secondValues)
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), contentValuesArgument.capture(), eq((String) null), eq((String[]) null));

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col3", "val3"), entry("col2", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectUpdateWithSingleValueOfUnsupportedType() throws Exception {
    ProviderAction.update(TEST_URI).value("col1", new Object());
  }

  @Test
  public void shouldPerformUpdateWithSelectionAndSelectionArgs() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    ProviderAction.update(TEST_URI)
        .values(values)
        .where("col2 = ?", "blah")
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), eq(values), eq("col2 = ?"), eq(new String[] { "blah" }));
  }

  @Test
  public void shouldPerformDeleteOnUri() throws Exception {
    ProviderAction.delete(TEST_URI).perform(contentResolverMock);
    verify(contentResolverMock).delete(eq(TEST_URI), eq((String) null), eq((String[]) null));
  }

  @Test
  public void shouldCareAboutSelectionAndSelectionArgsWhenDeleting() throws Exception {
    ProviderAction.delete(TEST_URI)
        .where("col1 = ?", "val1")
        .perform(contentResolverMock);
    verify(contentResolverMock).delete(eq(TEST_URI), eq("col1 = ?"), eq(new String[] { "val1" }));
  }

  @Test
  public void shouldBeAbleToUseNonStringObjectsInSelectionArgs() throws Exception {
    ProviderAction.query(TEST_URI)
        .where("col1 > ?", 18)
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq("col1 > ?"), eq(new String[] { "18" }), eq((String) null));
  }

  @Test
  public void shouldBeAbleToCreateASelectionWithWhereIn() throws Exception {
    final List<Long> inSet = Lists.newArrayList(1L, 2L, 3L);
    ProviderAction.query(TEST_URI)
        .whereIn("col1", inSet)
        .perform(contentResolverMock);
    final String expectedSelection = "col1 IN (" + Joiner.on(",").join(inSet) + ")";
    verify(contentResolverMock).query(eq(TEST_URI),
        eq((String[]) null),
        eq(expectedSelection),
        eq((String[])null),
        eq((String) null));
  }

  @Test
  public void shouldAlwaysPassNonNullContentValuesOnInsert() throws Exception {
    ProviderAction.insert(TEST_URI)
        .perform(contentResolverMock);

    verify(contentResolverMock).insert(eq(TEST_URI), isNotNull(ContentValues.class));
  }

  @Test
  public void shouldAlwaysPassNonNullContentValuesOnUpdate() throws Exception {
    ProviderAction.update(TEST_URI)
        .perform(contentResolverMock);

    verify(contentResolverMock).update(eq(TEST_URI), isNotNull(ContentValues.class), isNull(String.class), isNull(String[].class));
  }
}
