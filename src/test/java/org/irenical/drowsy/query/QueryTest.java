package org.irenical.drowsy.query;

import org.junit.Assert;
import org.junit.Test;

public class QueryTest {

  @Test
  public void testLiteralQuery() {
    SQLQueryBuilder qb = SQLQueryBuilder.select("* from some_table");
    Query q = qb.build();
    Assert.assertEquals("SELECT * from some_table", q.getQuery());
    Assert.assertTrue(q.getParameters().isEmpty());
  }

  @Test
  public void testSingleParameterQuery() {
    SQLQueryBuilder qb = SQLQueryBuilder.select("* from some_table where some_column").eq(3);
    Query q = qb.build();
    Assert.assertEquals("SELECT * from some_table where some_column=?", q.getQuery());
    Assert.assertTrue(q.getParameters().size() == 1);
    Assert.assertTrue(q.getParameters().get(0) instanceof Integer);
    Assert.assertTrue(((Integer) q.getParameters().get(0)).equals(3));
  }

  @Test
  public void testMultipleParameterQuery() {
    SQLQueryBuilder qb = SQLQueryBuilder.select("* from some_table where some_column").in(3, 5, 7);
    Query q = qb.build();
    Assert.assertEquals("SELECT * from some_table where some_column in(?,?,?)", q.getQuery());
    Assert.assertEquals(3, q.getParameters().size());
    Assert.assertTrue(q.getParameters().get(0) instanceof Integer);
    Assert.assertTrue(q.getParameters().get(1) instanceof Integer);
    Assert.assertTrue(q.getParameters().get(2) instanceof Integer);
    Assert.assertTrue(((Integer) q.getParameters().get(0)).equals(3));
    Assert.assertTrue(((Integer) q.getParameters().get(1)).equals(5));
    Assert.assertTrue(((Integer) q.getParameters().get(2)).equals(7));
  }

}
