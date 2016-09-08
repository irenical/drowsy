package org.irenical.drowsy.query.builder.sql;

import org.irenical.drowsy.query.Query.TYPE;

public class UpdateBuilder extends ExpressionBuilder<UpdateBuilder> {
  
  private boolean firstSet = true;

  protected UpdateBuilder() {
    super(TYPE.UPDATE);
  }

  public static UpdateBuilder create(String sql) {
    UpdateBuilder result = new UpdateBuilder();
    result.literal(sql);
    return result;
  }

  public static UpdateBuilder update(String tableName) {
    return create("update " + tableName);
  }

  public UpdateBuilder setParam(String column, Object value) {
    if(!firstSet){
      literal(", ");
    } else {
      literal(" set ");
      firstSet=false;
    }
    return literal(column).literal("=").param(value);
  }
  
  public UpdateBuilder setLiteral(String column, String literal) {
    if(!firstSet){
      literal(", ");
    } else {
      literal(" set ");
      firstSet=false;
    }
    return literal(column).literal("=").literal(literal);
  }

  public UpdateBuilder from(String lvalue) {
    return literal(" from ").literal(lvalue);
  }

  public UpdateBuilder where(String lvalue) {
    return literal(" where ").literal(lvalue);
  }

}
