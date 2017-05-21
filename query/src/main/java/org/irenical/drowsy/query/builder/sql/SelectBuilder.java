package org.irenical.drowsy.query.builder.sql;

public class SelectBuilder extends BaseSelectBuilder<SelectBuilder> {
	
	SelectBuilder() {
	}

	public static SelectBuilder create(String sql) {
		SelectBuilder result = new SelectBuilder();
		result.literal(sql);
		return result;
	}

	public static SelectBuilder select(Object... columns) {
		return create("select ").expression(columns);
	}

}
