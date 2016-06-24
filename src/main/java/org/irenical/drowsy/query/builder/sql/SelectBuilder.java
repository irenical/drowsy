package org.irenical.drowsy.query.builder.sql;

import java.util.Arrays;

import org.irenical.drowsy.query.Query.TYPE;

public class SelectBuilder extends ExpressionBuilder<SelectBuilder> {

  protected SelectBuilder() {
    super(TYPE.SELECT);
  }

  public static SelectBuilder create(String sql) {
    SelectBuilder result = new SelectBuilder();
    result.literal(sql);
    return result;
  }

  public static SelectBuilder columns(Object... columns) {
    return create("select ").literals(Arrays.asList(columns), "", "", ",");
  }

  public SelectBuilder from(String table) {
    return literal(" from ").literal(table);
  }

  public SelectBuilder join(String table) {
    return literal(" join ").literal(table);
  }

  public SelectBuilder innerJoin(String table) {
    return literal(" inner join ").literal(table);
  }

  public SelectBuilder leftJoin(String table) {
    return literal(" left join ").literal(table);
  }

  public SelectBuilder rightJoin(String table) {
    return literal(" right join ").literal(table);
  }

  public SelectBuilder fullJoin(String table) {
    return literal(" full join ").literal(table);
  }

  public SelectBuilder on(String lvalue) {
    return literal(" on ").literal(lvalue);
  }

  public SelectBuilder where(String lvalue) {
    return literal(" where ").literal(lvalue);
  }

}
