package org.irenical.drowsy.query.builder.sql;

import org.irenical.drowsy.query.Query;
import org.irenical.drowsy.query.builder.sql.SelectBuilder;
import org.junit.Assert;
import org.junit.Test;

public class QueryTest {

  @Test
  public void testLiteralQuery() {
    SelectBuilder qb = SelectBuilder.create("select * from some_table");
    Query q = qb.build();
    Assert.assertEquals("select * from some_table", q.getQuery());
    Assert.assertTrue(q.getParameters().isEmpty());
  }

  @Test
  public void testSingleParameterQuery() {
    SelectBuilder qb = SelectBuilder.create("select * from some_table where some_column").eq(3);
    Query q = qb.build();
    Assert.assertEquals("select * from some_table where some_column=?", q.getQuery());
    Assert.assertTrue(q.getParameters().size() == 1);
    Assert.assertTrue(q.getParameters().get(0) instanceof Integer);
    Assert.assertTrue(((Integer) q.getParameters().get(0)).equals(3));
  }

  @Test
  public void testMultipleParameterQuery() {
    SelectBuilder qb = SelectBuilder.create("select * from some_table where some_column").in(3,
        5, 7);
    Query q = qb.build();
    Assert.assertEquals("select * from some_table where some_column in(?,?,?)", q.getQuery());
    Assert.assertEquals(3, q.getParameters().size());
    Assert.assertTrue(q.getParameters().get(0) instanceof Integer);
    Assert.assertTrue(q.getParameters().get(1) instanceof Integer);
    Assert.assertTrue(q.getParameters().get(2) instanceof Integer);
    Assert.assertTrue(((Integer) q.getParameters().get(0)).equals(3));
    Assert.assertTrue(((Integer) q.getParameters().get(1)).equals(5));
    Assert.assertTrue(((Integer) q.getParameters().get(2)).equals(7));
  }
  
  @Test
  public void testCustomInsert() {
    Query q = InsertBuilder.create("insert into some_table(some_column) values(").param("some_value").literal(")").build();
    Assert.assertEquals("insert into some_table(some_column) values(?)", q.getQuery());
    Assert.assertEquals(q.getParameters().size(), 1);
    Assert.assertEquals("some_value",q.getParameters().get(0));
  }
  
  @Test
  public void testInsertInto() {
    String query = "insert into some_table(some_column) values(?)";
    Query q = InsertBuilder.into("some_table").columns("some_column").values("some_value").build();
    Assert.assertEquals(query, q.getQuery());
    Assert.assertEquals(q.getParameters().size(), 1);
    Assert.assertEquals("some_value",q.getParameters().get(0));
  }

}
