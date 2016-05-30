package org.irenical.drowsy.query;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseQueryBuilder<BUILDER_CLASS extends QueryBuilder<BUILDER_CLASS>>
    implements QueryBuilder<BUILDER_CLASS> {

  private static final char VALUE = '?';

  private final List<Object> parameters = new LinkedList<Object>();

  private final StringBuilder sb = new StringBuilder();

  public BaseQueryBuilder() {
  }

  @Override
  public Query build() {
    BaseQuery result = new BaseQuery();
    result.setParameters(new LinkedList<>(parameters));
    result.setQuery(sb.toString());
    return result;
  }

  public BUILDER_CLASS not() {
    return literal(" not");
  }

  public BUILDER_CLASS in(Object... that) {
    if (that == null || that.length == 0) {
      return literal(" in");
    } else {
      return eq(that);
    }
  }

  public BUILDER_CLASS eq(Object... that) {
    if (that != null) {
      if (that.length == 1) {
        if (that[0] == null) {
          return literal(" is null");
        } else {
          return literal("=").param(that[0]);
        }
      } else {
        return params(Arrays.asList(that), " in(", ")", ",");
      }
    } else {
      return literal("=");
    }
  }

  public BUILDER_CLASS notEq(Object... that) {
    if (that != null) {
      if (that.length == 1) {
        if (that[0] == null) {
          return literal(" is not null");
        } else {
          return literal("!=").param(that[0]);
        }
      } else {
        literal(" not");
        return in(that);
      }
    } else {
      return literal("!=");
    }
  }

  private BUILDER_CLASS binaryOperation(String op, Object that) {
    if (that == null) {
      return literal(op);
    } else {
      return literal(op).param(that);
    }
  }

  public BUILDER_CLASS greater(Object that) {
    return binaryOperation(">", that);
  }

  public BUILDER_CLASS lesserOrEqual(Object that) {
    return binaryOperation("<=", that);
  }

  public BUILDER_CLASS lesser(Object that) {
    return binaryOperation("<", that);
  }

  public BUILDER_CLASS greaterOrEqual(Object that) {
    return binaryOperation(">=", that);
  }

  public BUILDER_CLASS like(Object that) {
    return binaryOperation(" like ", that);
  }

  @Override
  public BUILDER_CLASS literal(Object sql) {
    return smartLiteral(sql);
  }

  @SuppressWarnings("unchecked")
  private BUILDER_CLASS smartLiteral(Object sql) {
    sb.append(sql);
    return (BUILDER_CLASS) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public BUILDER_CLASS literals(Iterable<Object> sql, String prefix, String suffix,
      String separator) {
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
        smartLiteral(s);
        first = false;
      }
      if (!first && suffix != null) {
        sb.append(suffix);
      }
    }
    return (BUILDER_CLASS) this;
  }

  @Override
  public BUILDER_CLASS param(Object value) {
    return smartValue(value);
  }

  @SuppressWarnings("unchecked")
  private BUILDER_CLASS smartValue(Object value) {
    sb.append(VALUE);
    parameters.add(value);
    return (BUILDER_CLASS) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public BUILDER_CLASS params(Iterable<Object> values, String prefix, String suffix,
      String separator) {
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
        smartValue(value);
        first = false;
      }
      if (!first && suffix != null) {
        sb.append(suffix);
      }
    }
    return (BUILDER_CLASS) this;
  }

}
