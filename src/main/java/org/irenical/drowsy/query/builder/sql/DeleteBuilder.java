package org.irenical.drowsy.query.builder.sql;

import org.irenical.drowsy.query.Query.TYPE;

public class DeleteBuilder extends ExpressionBuilder<DeleteBuilder> {

  protected DeleteBuilder() {
    super(TYPE.DELETE);
  }

  public static DeleteBuilder create(String sql) {
    DeleteBuilder result = new DeleteBuilder();
    result.literal(sql);
    return result;
  }

  public static DeleteBuilder from(String tableName) {
    return create("delete from " + tableName);
  }
  
  public DeleteBuilder where(String lvalue) {
    return literal(" where ").literal(lvalue);
  }

}
