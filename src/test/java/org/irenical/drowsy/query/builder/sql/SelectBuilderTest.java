package org.irenical.drowsy.query.builder.sql;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.Test;

public class SelectBuilderTest extends BaseBuilder {
  
  @Test
  public void testLiteralQuery() {
    SelectBuilder qb = SelectBuilder.create("select * from some_table");
    assertBuilder(qb, "select * from some_table", new LinkedList<>());
  }
  
  @Test
  public void testSingleParameterQuery() {
    SelectBuilder qb = SelectBuilder.create("select * from some_table where some_column").eq(3);
    assertBuilder(qb, "select * from some_table where some_column=?", Collections.singletonList(3));
  }

  @Test
  public void testMultipleParameterQuery() {
    SelectBuilder qb = SelectBuilder.create("select * from some_table where some_column").in(3,
        5, 7);
    assertBuilder(qb, "select * from some_table where some_column in(?,?,?)", Arrays.asList(3,5,7));
  }
  
  @Test
  public void testSimpleQuery() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table");
    assertBuilder(qb, "select * from some_table", new LinkedList<>());
  }
  
  @Test
  public void testJoin() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").join("other_table").on("some_table.other_id=other_table.id");
    assertBuilder(qb, "select * from some_table join other_table on some_table.other_id=other_table.id", new LinkedList<>());
  }
  
  @Test
  public void testInnerJoin() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").innerJoin("other_table").on("some_table.other_id=other_table.id");
    assertBuilder(qb, "select * from some_table inner join other_table on some_table.other_id=other_table.id", new LinkedList<>());
  }
  
  @Test
  public void testLeftJoin() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").leftJoin("other_table").on("some_table.other_id=other_table.id");
    assertBuilder(qb, "select * from some_table left join other_table on some_table.other_id=other_table.id", new LinkedList<>());
  }
  
  @Test
  public void testRightJoin() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").rightJoin("other_table").on("some_table.other_id=other_table.id");
    assertBuilder(qb, "select * from some_table right join other_table on some_table.other_id=other_table.id", new LinkedList<>());
  }
  
  @Test
  public void testFullJoin() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").fullJoin("other_table").on("some_table.other_id=other_table.id");
    assertBuilder(qb, "select * from some_table full join other_table on some_table.other_id=other_table.id", new LinkedList<>());
  }
  
  @Test
  public void testWhere() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").eq("some_value");
    assertBuilder(qb, "select * from some_table where some_field=?", Arrays.asList("some_value"));
  }
  
}
