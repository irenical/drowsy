package org.irenical.drowsy.query.builder.sql;

import java.util.Arrays;

import org.irenical.drowsy.query.builder.QueryBuilder;
import org.junit.Test;

public class InsertBuilderTest extends BaseBuilder {
  
  @Test
  public void testCustomInsert() {
    QueryBuilder<?> qb = InsertBuilder.create("insert into some_table(some_column) values(").param("some_value").literal(")");
    assertBuilder(qb, "insert into some_table(some_column) values(?)",Arrays.asList("some_value"));
  }
  
  @Test
  public void testInsertInto() {
    String query = "insert into some_table(some_column) values(?)";
    QueryBuilder<?> qb = InsertBuilder.into("some_table").columns("some_column").values("some_value");
    assertBuilder(qb, query, Arrays.asList("some_value"));
  }
  
  @Test
  public void testDefault() {
    String query = "insert into some_table(some_column) default values";
    QueryBuilder<?> qb = InsertBuilder.into("some_table").columns("some_column").defaultValues();
    assertBuilder(qb, query, Arrays.asList());
  }
  
  @Test
  public void testSubselect() {
    String query = "insert into some_table(some_column) (select some_other_column from some_other_table where id=?)";
    QueryBuilder<?> sel = SelectBuilder.select("some_other_column").from("some_other_table").where("id").eq(1);
    QueryBuilder<?> qb = InsertBuilder.into("some_table").columns("some_column").subquery(sel.build());
    assertBuilder(qb, query, Arrays.asList(1));
  }
  
}
