package org.irenical.drowsy.query.builder.mysql;

import java.util.Arrays;

import org.irenical.drowsy.query.builder.sql.BaseSelectBuilder;

public class MySqlSelectBuilder extends BaseSelectBuilder<MySqlSelectBuilder> {

	MySqlSelectBuilder() {
	}

	public static MySqlSelectBuilder create(String sql) {
		MySqlSelectBuilder result = new MySqlSelectBuilder();
		result.literal(sql);
		return result;
	}

	public static MySqlSelectBuilder select(Object... columns) {
		return create("select ").literals(Arrays.asList(columns), "", "", ",");
	}

	public MySqlSelectBuilder limit(Object... limit) {
		return literal(" limit ").literals(Arrays.asList(limit), null, null, ",");
	}

	public MySqlSelectBuilder offset(Object offset) {
		return literal(" offset ").literal(offset);
	}

}
