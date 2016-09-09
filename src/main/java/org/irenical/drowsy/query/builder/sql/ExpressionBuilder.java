package org.irenical.drowsy.query.builder.sql;

import java.util.Arrays;

import org.irenical.drowsy.query.Query.TYPE;

public class ExpressionBuilder<BUILDER_CLASS extends BaseQueryBuilder<BUILDER_CLASS>>
    extends BaseQueryBuilder<BUILDER_CLASS> {

  protected ExpressionBuilder(TYPE type) {
    super(type);
  }

  public BUILDER_CLASS not() {
    return (BUILDER_CLASS) literal(" not");
  }

  public BUILDER_CLASS in(Object... that) {
    if (that == null || that.length == 0) {
      return literal(" in");
    } else {
      return params(Arrays.asList(that), " in(", ")", ", ");
    }
  }

  public BUILDER_CLASS eq(Object... that) {
    if (that == null || that.length == 0) {
      return literal("=");
    }
    if (that.length == 1) {
      if (that[0] == null) {
        return literal(" is null");
      } else {
        return literal("=").param(that[0]);
      }
    } else {
      return in(that);
    }
  }

  public BUILDER_CLASS notEq(Object... that) {
    if (that == null || that.length == 0) {
      return literal("!=");
    }
    if (that.length == 1) {
      if (that[0] == null) {
        return literal(" is not null");
      } else {
        return literal("!=").param(that[0]);
      }
    } else {
      not();
      return in(that);
    }
  }

  private BUILDER_CLASS binaryOperation(String op, Object that) {
    return literal(op).param(that);
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

  public BUILDER_CLASS and(String lvalue) {
    return literal(" and ").literal(lvalue);
  }

  public BUILDER_CLASS or(String lvalue) {
    return literal(" or ").literal(lvalue);
  }

}
