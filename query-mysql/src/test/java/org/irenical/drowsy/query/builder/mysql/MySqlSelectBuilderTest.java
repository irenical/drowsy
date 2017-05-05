package org.irenical.drowsy.query.builder.mysql;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.irenical.drowsy.query.Query;
import org.irenical.drowsy.query.builder.QueryBuilder;
import org.junit.Assert;
import org.junit.Test;

public class MySqlSelectBuilderTest {

	public void assertBuilder(QueryBuilder<?> builder, String expectedQuery, List<Object> expectedParams) {
		Query q = builder.build();
		Assert.assertEquals(expectedQuery, q.getQuery());
		Assert.assertEquals(expectedParams, q.getParameters());
	}

	@Test
	public void testLiteralQuery() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.create("select * from some_table");
		assertBuilder(qb, "select * from some_table", new LinkedList<>());
	}

	@Test
	public void testSingleParameterQuery() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.create("select * from some_table where some_column").eq(3);
		assertBuilder(qb, "select * from some_table where some_column=?", Collections.singletonList(3));
	}

	@Test
	public void testMultipleParameterQuery() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.create("select * from some_table where some_column").in(3, 5, 7);
		assertBuilder(qb, "select * from some_table where some_column in(?, ?, ?)", Arrays.asList(3, 5, 7));
	}

	@Test
	public void testSimpleQuery() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table");
		assertBuilder(qb, "select * from some_table", new LinkedList<>());
	}

	@Test
	public void testJoin() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").join("other_table")
				.on("some_table.other_id=other_table.id");
		assertBuilder(qb, "select * from some_table join other_table on some_table.other_id=other_table.id",
				new LinkedList<>());
	}

	@Test
	public void testInnerJoin() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").innerJoin("other_table")
				.on("some_table.other_id=other_table.id");
		assertBuilder(qb, "select * from some_table inner join other_table on some_table.other_id=other_table.id",
				new LinkedList<>());
	}

	@Test
	public void testLeftJoin() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").leftJoin("other_table")
				.on("some_table.other_id=other_table.id");
		assertBuilder(qb, "select * from some_table left join other_table on some_table.other_id=other_table.id",
				new LinkedList<>());
	}

	@Test
	public void testRightJoin() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").rightJoin("other_table")
				.on("some_table.other_id=other_table.id");
		assertBuilder(qb, "select * from some_table right join other_table on some_table.other_id=other_table.id",
				new LinkedList<>());
	}

	@Test
	public void testFullJoin() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").fullJoin("other_table")
				.on("some_table.other_id=other_table.id");
		assertBuilder(qb, "select * from some_table full join other_table on some_table.other_id=other_table.id",
				new LinkedList<>());
	}

	@Test
	public void testWhere() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq("some_value");
		assertBuilder(qb, "select * from some_table where some_field=?", Arrays.asList("some_value"));
	}

	@Test
	public void testSingleNotIn() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").not()
				.in("some_value");
		assertBuilder(qb, "select * from some_table where some_field not in(?)", Arrays.asList("some_value"));
	}

	@Test
	public void testEmptyIn() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").in().literal("(")
				.param("some_value").literal(")");
		assertBuilder(qb, "select * from some_table where some_field in(?)", Arrays.asList("some_value"));
	}

	@Test
	public void testIsNull() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq((Object) null);
		assertBuilder(qb, "select * from some_table where some_field is null", Arrays.asList());
	}

	@Test
	public void testLiteralEq() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq()
				.param("some_value");
		assertBuilder(qb, "select * from some_table where some_field=?", Arrays.asList("some_value"));
	}

	@Test
	public void testImplicitIn() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq("some_value",
				"some_other_value");
		assertBuilder(qb, "select * from some_table where some_field in(?, ?)",
				Arrays.asList("some_value", "some_other_value"));
	}

	@Test
	public void testNotEq() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field")
				.notEq("some_value");
		assertBuilder(qb, "select * from some_table where some_field!=?", Arrays.asList("some_value"));
	}

	@Test
	public void testLiteralNotEq() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").notEq()
				.param("some_value");
		assertBuilder(qb, "select * from some_table where some_field!=?", Arrays.asList("some_value"));
	}

	@Test
	public void testIsNotNull() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field")
				.notEq((Object) null);
		assertBuilder(qb, "select * from some_table where some_field is not null", Arrays.asList());
	}

	@Test
	public void testImplicitNotIn() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field")
				.notEq("some_value", "some_other_value");
		assertBuilder(qb, "select * from some_table where some_field not in(?, ?)",
				Arrays.asList("some_value", "some_other_value"));
	}

	@Test
	public void testGreater() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field")
				.greater("some_value");
		assertBuilder(qb, "select * from some_table where some_field>?", Arrays.asList("some_value"));
	}

	@Test
	public void testGreaterOrEqual() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field")
				.greaterOrEqual("some_value");
		assertBuilder(qb, "select * from some_table where some_field>=?", Arrays.asList("some_value"));
	}

	@Test
	public void testLesser() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field")
				.lesser("some_value");
		assertBuilder(qb, "select * from some_table where some_field<?", Arrays.asList("some_value"));
	}

	@Test
	public void testLesserOrEqual() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field")
				.lesserOrEqual("some_value");
		assertBuilder(qb, "select * from some_table where some_field<=?", Arrays.asList("some_value"));
	}

	@Test
	public void testLike() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field")
				.like("some_value");
		assertBuilder(qb, "select * from some_table where some_field like ?", Arrays.asList("some_value"));
	}

	@Test
	public void testAnd() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq("some_value")
				.and("some_other_field").eq("some_other_value");
		assertBuilder(qb, "select * from some_table where some_field=? and some_other_field=?",
				Arrays.asList("some_value", "some_other_value"));
	}

	@Test
	public void testOr() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq("some_value")
				.or("some_other_field").eq("some_other_value");
		assertBuilder(qb, "select * from some_table where some_field=? or some_other_field=?",
				Arrays.asList("some_value", "some_other_value"));
	}

	@Test
	public void testInCollection() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field")
				.in(Arrays.asList("some_value", "some_other_value"));
		assertBuilder(qb, "select * from some_table where some_field in(?, ?)",
				Arrays.asList("some_value", "some_other_value"));
	}

	@Test
	public void testNotInCollection() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").not()
				.in(Arrays.asList("some_value", "some_other_value"));
		assertBuilder(qb, "select * from some_table where some_field not in(?, ?)",
				Arrays.asList("some_value", "some_other_value"));
	}
	
	@Test
	public void testLimit() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq("some_value").limit(100);
		assertBuilder(qb, "select * from some_table where some_field=? limit 100", Arrays.asList("some_value"));
	}
	
	@Test
	public void testMySQLOffset() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq("some_value").limit(10,100);
		assertBuilder(qb, "select * from some_table where some_field=? limit 10,100", Arrays.asList("some_value"));
	}
	
	@Test
	public void testPostgreSQLOffset() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq("some_value").offset(10);
		assertBuilder(qb, "select * from some_table where some_field=? offset 10", Arrays.asList("some_value"));
	}
	
	@Test
	public void testHybridOffset() {
		MySqlSelectBuilder qb = MySqlSelectBuilder.select("*").from("some_table").where("some_field").eq("some_value").limit(100).offset(10);
		assertBuilder(qb, "select * from some_table where some_field=? limit 100 offset 10", Arrays.asList("some_value"));
	}

}
