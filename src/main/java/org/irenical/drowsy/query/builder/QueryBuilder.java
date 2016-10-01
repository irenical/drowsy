package org.irenical.drowsy.query.builder;

import org.irenical.drowsy.query.Query;

public interface QueryBuilder<BUILDER_CLASS extends QueryBuilder<BUILDER_CLASS>> {

  /**
   * Returns a built query. The returned Query is a snapshot of the current
   * QueryBuilder state. That is to say that further changes to QueryBuilder
   * should not affect it.
   * 
   * @return Query - the query built so far
   */
  public Query build();

  /**
   * Append literal value
   * 
   * @param sql
   *          - the SQL query fragment to be appended
   * @return the builder
   */
  public abstract BUILDER_CLASS literal(Object sql);

  public abstract BUILDER_CLASS literals(Iterable<?> sql, String prefix, String suffix,
      String separator);
  
  /**
   * Append a parameter. A ? will be appended to the query
   * 
   * @param param
   *          - the object representing the value
   * @return the builder
   */
  public abstract BUILDER_CLASS param(Object param);
  
  /**
   * Append multiple values. A ? will be appended to the query for each value
   * Useful for IN expressions
   * 
   * @param params
   *          - the objects representing the parameters
   * @param prefix
   *          - a literal prepended to the parameters
   * @param suffix
   *          - a literal postpended to the parameters
   * @param separator
   *          - a literal separating each parameter
   * @return the builder
   */
  public abstract BUILDER_CLASS params(Iterable<?> params, String prefix, String suffix,
      String separator);

}