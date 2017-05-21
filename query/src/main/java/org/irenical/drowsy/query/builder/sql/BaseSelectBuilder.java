package org.irenical.drowsy.query.builder.sql;

import java.util.Arrays;

import org.irenical.drowsy.query.Query.TYPE;

public class BaseSelectBuilder<BUILDER_CLASS extends ExpressionBuilder<BUILDER_CLASS>>
		extends ExpressionBuilder<BUILDER_CLASS> {

	private boolean firstColumn = true;

	protected BaseSelectBuilder() {
		super(TYPE.SELECT);
	}

	public BUILDER_CLASS identifier(Object... identifiers) {
		String prefix = firstColumn ? "\"" : ", \"";
		if (identifiers.length > 0) {
			firstColumn = false;
		}
		return literals(Arrays.asList(identifiers), prefix, "\"", "\", \"");
	}

	public BUILDER_CLASS column(Object... columns) {
		String prefix = firstColumn ? null : ", ";
		if (columns.length > 0) {
			firstColumn = false;
		}
		return literals(Arrays.asList(columns), prefix, null, ", ");
	}

	public BUILDER_CLASS as(String alias) {
		return literal(" as ").literal(alias);
	}

	public BUILDER_CLASS from(String table) {
		return literal(" from ").literal(table);
	}

	public BUILDER_CLASS join(String table) {
		return literal(" join ").literal(table);
	}

	public BUILDER_CLASS innerJoin(String table) {
		return literal(" inner join ").literal(table);
	}

	public BUILDER_CLASS leftJoin(String table) {
		return literal(" left join ").literal(table);
	}

	public BUILDER_CLASS rightJoin(String table) {
		return literal(" right join ").literal(table);
	}

	public BUILDER_CLASS fullJoin(String table) {
		return literal(" full join ").literal(table);
	}

	public BUILDER_CLASS on(String lvalue) {
		return literal(" on ").literal(lvalue);
	}

	public BUILDER_CLASS where(String lvalue) {
		return literal(" where ").literal(lvalue);
	}

}
