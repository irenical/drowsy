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

  public static UpdateBuilder table(String tableName) {
    return create("update " + tableName);
  }

  public UpdateBuilder setParam(String lvalue, Object param) {
    if(!firstSet){
      literal(",");
      firstSet=false;
    }
    return literal(lvalue).literal("=").param(param);
  }

  public UpdateBuilder setExpression(String lvalue, Object rvalue) {
    if(!firstSet){
      literal(",");
      firstSet=false;
    }
    return literal(lvalue).literal("=").literal(rvalue);
  }

  public UpdateBuilder set(String lvalue) {
    return literal(" set ").literal(lvalue);
  }

  public UpdateBuilder from(String lvalue) {
    return literal(" from ").literal(lvalue);
  }

  public UpdateBuilder where(String lvalue) {
    return literal(" where ").literal(lvalue);
  }

}
