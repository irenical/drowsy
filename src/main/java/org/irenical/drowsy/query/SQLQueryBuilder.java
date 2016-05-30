package org.irenical.drowsy.query;

import java.util.Arrays;

import org.irenical.drowsy.query.Query.TYPE;

public class SQLQueryBuilder extends BaseQueryBuilder<SQLQueryBuilder> {

  private final TYPE type;

  private SQLQueryBuilder(TYPE type, String prefix) {
    this.type = type;
    if (prefix != null) {
      literal(prefix);
    }
  }

  /**
   * Creates an custom query of given type
   * 
   * @param type
   *          - the query type
   * @return Returns a new instance of a query builder
   */
  public static SQLQueryBuilder custom(TYPE type) {
    SQLQueryBuilder result = new SQLQueryBuilder(type, null);
    return result;
  }

  /**
   * Creates a new select query builder. The query will be prefixed with the
   * <i>SELECT</i> keyword. To create other types of select queries, like
   * PostgreSQL <i>with</i> queries, use other(TYPE.SELECT) instead.
   * 
   * @param commaSeparatedLiterals
   *          - zero or more literals, a convenient way to declare the SELECT's
   *          fields. Ex: select("id", "name") will create an expression
   *          starting with SELECT id, name... <br>
   *          You can also write a whole simple select query with the last
   *          argument. Ex: select("id", "name from my_table where id>100").
   *          <br>
   *          No arguments will create an incomplete select query, containing
   *          only the string <i>SELECT</i> followed by a trailing whitespace
   * @return Returns a new instance of a select query builder
   */
  public static SQLQueryBuilder select(Object... commaSeparatedLiterals) {
    SQLQueryBuilder result = new SQLQueryBuilder(TYPE.SELECT, "SELECT ");
    if (commaSeparatedLiterals != null && commaSeparatedLiterals.length > 0) {
      result.literals(Arrays.asList(commaSeparatedLiterals), null, null, ",");
    }
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
