package org.irenical.drowsy.query.builder.sql;

import java.util.Arrays;

import org.irenical.drowsy.query.builder.QueryBuilder;
import org.junit.Test;

public class DeleteBuilderTest extends BaseBuilder {

  @Test
  public void testCustomDelete() {
    QueryBuilder<?> qb = DeleteBuilder.create("delete from some_table where some_column").eq("some_value");
    assertBuilder(qb, "delete from some_table where some_column=?", Arrays.asList("some_value"));
  }
  
  @Test
  public void testBuiltDelete() {
    QueryBuilder<?> qb = DeleteBuilder.delete("some_table").where("some_column").eq("some_value");
    assertBuilder(qb, "delete from some_table where some_column=?", Arrays.asList("some_value"));
  }

}
