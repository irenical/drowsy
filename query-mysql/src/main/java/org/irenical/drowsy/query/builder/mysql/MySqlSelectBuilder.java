package org.irenical.drowsy.query.builder.mysql;

import java.util.Arrays;

import org.irenical.drowsy.query.Query.TYPE;
import org.irenical.drowsy.query.builder.sql.ExpressionBuilder;

public class MySqlSelectBuilder extends ExpressionBuilder<MySqlSelectBuilder> {

	protected MySqlSelectBuilder() {
		super(TYPE.SELECT);
	}

	public static MySqlSelectBuilder create(String sql) {
		MySqlSelectBuilder result = new MySqlSelectBuilder();
		result.literal(sql);
		return result;
	}

	public static MySqlSelectBuilder select(Object... columns) {
		return create("select ").literals(Arrays.asList(columns), "", "", ",");
	}

	public MySqlSelectBuilder from(String table) {
		return literal(" from ").literal(table);
	}

	public MySqlSelectBuilder join(String table) {
		return literal(" join ").literal(table);
	}

	public MySqlSelectBuilder innerJoin(String table) {
		return literal(" inner join ").literal(table);
	}

	public MySqlSelectBuilder leftJoin(String table) {
		return literal(" left join ").literal(table);
	}

	public MySqlSelectBuilder rightJoin(String table) {
		return literal(" right join ").literal(table);
	}

	public MySqlSelectBuilder fullJoin(String table) {
		return literal(" full join ").literal(table);
	}

	public MySqlSelectBuilder on(String lvalue) {
		return literal(" on ").literal(lvalue);
	}

	public MySqlSelectBuilder where(String lvalue) {
		return literal(" where ").literal(lvalue);
	}

	public MySqlSelectBuilder limit(Object... limit) {
		return literal(" limit ").literals(Arrays.asList(limit), null, null, ",");
	}

	public MySqlSelectBuilder offset(Object offset) {
		return literal(" offset ").literal(offset);
	}

}
