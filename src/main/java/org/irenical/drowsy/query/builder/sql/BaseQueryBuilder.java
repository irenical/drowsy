package org.irenical.drowsy.query.builder.sql;

import java.util.LinkedList;
import java.util.List;

import org.irenical.drowsy.query.BaseQuery;
import org.irenical.drowsy.query.Query;
import org.irenical.drowsy.query.Query.TYPE;
import org.irenical.drowsy.query.builder.QueryBuilder;

public abstract class BaseQueryBuilder<BUILDER_CLASS extends QueryBuilder<BUILDER_CLASS>>
    implements QueryBuilder<BUILDER_CLASS> {

  private static final char VALUE = '?';

  private final List<Object> parameters = new LinkedList<Object>();

  private final StringBuilder sb = new StringBuilder();

  private final TYPE type;

  public BaseQueryBuilder(TYPE type) {
    this.type = type;
  }

  @Override
  public Query build() {
    BaseQuery result = new BaseQuery();
    result.setParameters(new LinkedList<>(parameters));
    result.setQuery(sb.toString());
    result.setType(type);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public BUILDER_CLASS literal(Object sql) {
    sb.append(sql);
    return (BUILDER_CLASS) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public BUILDER_CLASS literals(Iterable<?> sql, String prefix, String suffix, String separator) {
    if (sql != null) {
      boolean first = true;
      for (Object s : sql) {
        if (first) {
          if (prefix != null) {
            sb.append(prefix);
          }
        } else if (separator != null) {
          sb.append(separator);
        }
        sb.append(s);
        first = false;
      }
      if (!first && suffix != null) {
        sb.append(suffix);
      }
    }
    return (BUILDER_CLASS) this;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public BUILDER_CLASS param(Object value) {
    sb.append(VALUE);
    parameters.add(value);
    return (BUILDER_CLASS) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public BUILDER_CLASS params(Iterable<?> values, String prefix, String suffix, String separator) {
    if (values != null) {
      boolean first = true;
      for (Object value : values) {
        if (first) {
          if (prefix != null) {
            sb.append(prefix);
          }
        } else if (separator != null) {
          sb.append(separator);
        }
        param(value);
        first = false;
      }
      if (!first && suffix != null) {
        sb.append(suffix);
      }
    }
    return (BUILDER_CLASS) this;
  }
  
}
