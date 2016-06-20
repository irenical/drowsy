package org.irenical.drowsy.query.builder.sql;

import org.irenical.drowsy.query.Query.TYPE;

public class SQLQueryBuilder extends BaseQueryBuilder<SQLQueryBuilder> {

  private SQLQueryBuilder(TYPE type) {
    super(type);
  }

  public static SQLQueryBuilder select(String sql) {
    SQLQueryBuilder result = new SQLQueryBuilder(TYPE.SELECT);
    result.literal(sql);
    return result;
  }

  public static SQLQueryBuilder insert(String sql) {
    SQLQueryBuilder result = new SQLQueryBuilder(TYPE.INSERT);
    result.literal(sql);
    return result;
  }

  public static SQLQueryBuilder update(String sql) {
    SQLQueryBuilder result = new SQLQueryBuilder(TYPE.UPDATE);
    result.literal(sql);
    return result;
  }

  public static SQLQueryBuilder delete(String sql) {
    SQLQueryBuilder result = new SQLQueryBuilder(TYPE.DELETE);
    result.literal(sql);
    return result;
  }

  public static SQLQueryBuilder call(String sql) {
    SQLQueryBuilder result = new SQLQueryBuilder(TYPE.CALL);
    result.literal(sql);
    return result;
  }

  /**
   * Appends given object to the query, prefixed with a whitespace
   * 
   * @param sql
   *          - the object representing a piece o SQL
   * @return
   */
  public SQLQueryBuilder append(Object sql) {
    return literal(" ").literal(sql);
  }

}
