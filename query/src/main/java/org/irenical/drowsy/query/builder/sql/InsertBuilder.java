package org.irenical.drowsy.query.builder.sql;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.irenical.drowsy.query.BaseQuery;
import org.irenical.drowsy.query.Query;
import org.irenical.drowsy.query.Query.TYPE;

public class InsertBuilder extends BaseQueryBuilder<InsertBuilder> {

	protected InsertBuilder() {
		super(TYPE.INSERT);
	}

	@Override
	public Query build() {
		BaseQuery bq = (BaseQuery) super.build();
		bq.setReturnGeneratedKeys(true);
		return bq;
	}

	public static InsertBuilder create(String sql) {
		InsertBuilder result = new InsertBuilder();
		result.literal(sql);
		return result;
	}

	public static InsertBuilder into(String tableName) {
		return create("insert into " + tableName);
	}

	public InsertBuilder columns(Object... values) {
		if (values != null && values.length > 0) {
			literals(Arrays.asList(values), "(", ")", ",");
		}
		return this;
	}

	public InsertBuilder defaultValues() {
		return literal(" default values");
	}

	public InsertBuilder values(Object... values) {
		if (values != null && values.length > 0) {
			params(Arrays.asList(values), " values(", ")", ", ");
		}
		return this;
	}

	public <ROW> InsertBuilder values(Iterable<ROW> rows, Function<ROW, Iterable<Object>> mapper) {
		List<Iterable<Object>> input = new LinkedList<>();
		rows.forEach(row -> input.add(mapper.apply(row)));
		if (!input.isEmpty()) {
			literal(" values ");
			boolean first = true;
			for(Iterable<Object> rowValues : input){
				if(!first){
					literal(", ");
				}
				params(rowValues, "(", ")", ", ");
				first = false;
			}
		}
		return this;
	}

}
