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

  /**
   * Adds <i>set column=?</i> to the query and appends the value to the
   * parameter list. Use for columnA=some input value. For something like
   * columnA=columnB use {@link #setLiteral(String, String) setLiteral} instead
   * 
   * @param column
   *          - the column name
   * @param value
   *          - the variable value
   * @return
   */
  public UpdateBuilder setParam(String column, Object value) {
    if (!firstSet) {
      literal(", ");
    } else {
      literal(" set ");
      firstSet = false;
    }
    return literal(column).literal("=").param(value);
  }

  /**
   * Adds <i>set column=literal</i> to the query. Useful for columnA=columnB
   * like cases
   * 
   * @param column
   *          - the column name
   * @param literal
   *          - the literal value
   * @return the builder
   */
  public UpdateBuilder setLiteral(String column, String literal) {
    if (!firstSet) {
      literal(", ");
    } else {
      literal(" set ");
      firstSet = false;
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
