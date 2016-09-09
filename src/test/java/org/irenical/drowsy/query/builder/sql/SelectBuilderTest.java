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
    assertBuilder(qb, "select * from some_table where some_column in(?, ?, ?)", Arrays.asList(3,5,7));
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
  
  @Test
  public void testSingleNotIn() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").not().in("some_value");
    assertBuilder(qb, "select * from some_table where some_field not in(?)", Arrays.asList("some_value"));
  }
  
  @Test
  public void testEmptyIn() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").in().literal("(").param("some_value").literal(")");
    assertBuilder(qb, "select * from some_table where some_field in(?)", Arrays.asList("some_value"));
  }
  
  @Test
  public void testIsNull() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").eq((Object)null);
    assertBuilder(qb, "select * from some_table where some_field is null", Arrays.asList());
  }
  
  @Test
  public void testLiteralEq() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").eq().param("some_value");
    assertBuilder(qb, "select * from some_table where some_field=?", Arrays.asList("some_value"));
  }
  
  @Test
  public void testImplicitIn() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").eq("some_value","some_other_value");
    assertBuilder(qb, "select * from some_table where some_field in(?, ?)", Arrays.asList("some_value","some_other_value"));
  }
  
  @Test
  public void testNotEq() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").notEq("some_value");
    assertBuilder(qb, "select * from some_table where some_field!=?", Arrays.asList("some_value"));
  }
  
  @Test
  public void testLiteralNotEq() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").notEq().param("some_value");
    assertBuilder(qb, "select * from some_table where some_field!=?", Arrays.asList("some_value"));
  }
  
  @Test
  public void testIsNotNull() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").notEq((Object)null);
    assertBuilder(qb, "select * from some_table where some_field is not null", Arrays.asList());
  }
  
  @Test
  public void testImplicitNotIn() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").notEq("some_value","some_other_value");
    assertBuilder(qb, "select * from some_table where some_field not in(?, ?)", Arrays.asList("some_value","some_other_value"));
  }
  
  @Test
  public void testGreater() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").greater("some_value");
    assertBuilder(qb, "select * from some_table where some_field>?", Arrays.asList("some_value"));
  }
  
  @Test
  public void testGreaterOrEqual() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").greaterOrEqual("some_value");
    assertBuilder(qb, "select * from some_table where some_field>=?", Arrays.asList("some_value"));
  }
  
  @Test
  public void testLesser() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").lesser("some_value");
    assertBuilder(qb, "select * from some_table where some_field<?", Arrays.asList("some_value"));
  }
  
  @Test
  public void testLesserOrEqual() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").lesserOrEqual("some_value");
    assertBuilder(qb, "select * from some_table where some_field<=?", Arrays.asList("some_value"));
  }
  
  @Test
  public void testLike() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").like("some_value");
    assertBuilder(qb, "select * from some_table where some_field like ?", Arrays.asList("some_value"));
  }

  @Test
  public void testAnd() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").eq("some_value").and("some_other_field").eq("some_other_value");
    assertBuilder(qb, "select * from some_table where some_field=? and some_other_field=?", Arrays.asList("some_value","some_other_value"));
  }
  
  @Test
  public void testOr() {
    SelectBuilder qb = SelectBuilder.select("*").from("some_table").where("some_field").eq("some_value").or("some_other_field").eq("some_other_value");
    assertBuilder(qb, "select * from some_table where some_field=? or some_other_field=?", Arrays.asList("some_value","some_other_value"));
  }
  
}
