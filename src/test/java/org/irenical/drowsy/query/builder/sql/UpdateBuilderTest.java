package org.irenical.drowsy.query.builder.sql;

import java.util.Arrays;
import java.util.Collections;

import org.irenical.drowsy.query.builder.QueryBuilder;
import org.junit.Test;

public class UpdateBuilderTest extends BaseBuilder {

  @Test
  public void testCustomUpdate() {
    QueryBuilder<?> qb = UpdateBuilder.create("update some_table").setLiteral("some_column", "some_value");
    assertBuilder(qb, "update some_table set some_column=some_value", Collections.emptyList());
  }

  @Test
  public void testSimpleUpdate() {
    QueryBuilder<?> qb = UpdateBuilder.update("some_table").setLiteral("some_column", "some_value");
    assertBuilder(qb, "update some_table set some_column=some_value", Collections.emptyList());
  }

  @Test
  public void testExpressionUpdate() {
    QueryBuilder<?> qb = UpdateBuilder.update("some_table").setParam("some_column", "some_value");
    assertBuilder(qb, "update some_table set some_column=?", Arrays.asList("some_value"));
  }

  @Test
  public void testMultipleParamUpdate() {
    QueryBuilder<?> qb = UpdateBuilder.update("some_table").setParam("some_column", "some_value")
        .setParam("some_other_column", "some_other_value");
    assertBuilder(qb, "update some_table set some_column=?, some_other_column=?",
        Arrays.asList("some_value", "some_other_value"));
  }

  @Test
  public void testMultipleLiteralUpdate() {
    QueryBuilder<?> qb = UpdateBuilder.update("some_table").setLiteral("some_column", "some_value")
        .setParam("some_other_column", "some_other_value");
    assertBuilder(qb, "update some_table set some_column=some_value, some_other_column=?",
        Arrays.asList("some_other_value"));
  }

  @Test
  public void testMultipleMixedUpdate() {
    QueryBuilder<?> qb = UpdateBuilder.update("some_table").setLiteral("some_column", "some_value")
        .setLiteral("some_other_column", "some_other_value");
    assertBuilder(qb, "update some_table set some_column=some_value, some_other_column=some_other_value",
        Collections.emptyList());
  }

  @Test
  public void testConditionalUpdate() {
    QueryBuilder<?> qb = UpdateBuilder.update("some_table").setParam("some_column", "some_value")
        .setParam("some_other_column", "some_other_value").where("id").eq(1337);
    assertBuilder(qb, "update some_table set some_column=?, some_other_column=? where id=?",
        Arrays.asList("some_value", "some_other_value", 1337));
  }
  
  @Test
  public void testFromLiteralUpdate() {
    QueryBuilder<?> qb = UpdateBuilder.update("some_table").setParam("some_column", "some_value")
        .setParam("some_other_column", "some_other_value").from("somewhere").where("id").eq(1337);
    assertBuilder(qb, "update some_table set some_column=?, some_other_column=? from somewhere where id=?",
        Arrays.asList("some_value", "some_other_value", 1337));
  }
  
}
