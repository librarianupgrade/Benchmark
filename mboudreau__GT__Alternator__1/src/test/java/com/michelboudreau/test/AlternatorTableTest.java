package com.michelboudreau.test;

import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodb.model.DeleteTableRequest;
import com.amazonaws.services.dynamodb.model.DeleteTableResult;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.DescribeTableResult;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.ListTablesRequest;
import com.amazonaws.services.dynamodb.model.ListTablesResult;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.amazonaws.services.dynamodb.model.UpdateTableRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext.xml" })
public class AlternatorTableTest extends AlternatorTest {

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		deleteAllTables();
	}

	@Test
	public void createTableWithStringHashKey() {
		String name = createTableName();
		TableDescription res = createTable(name, createKeySchema(createStringKeyElement()));
		Assert.assertNotNull(res);
		Assert.assertEquals(res.getTableName(), name);
	}

	@Test
	public void createTableWithNumberHashKey() {
		String name = createTableName();
		TableDescription res = createTable(name, createKeySchema(createNumberKeyElement()));
		Assert.assertNotNull(res);
		Assert.assertEquals(res.getTableName(), name);
	}

	@Test
	public void createTableWithStringHashKeyAndStringRangeKey() {
		String name = createTableName();
		TableDescription res = createTable(name, createKeySchema(createStringKeyElement(), createStringKeyElement()));
		Assert.assertNotNull(res);
		Assert.assertEquals(res.getTableName(), name);
	}

	@Test
	public void createTableWithStringHashKeyAndNumberRangeKey() {
		String name = createTableName();
		TableDescription res = createTable(name, createKeySchema(createStringKeyElement(), createNumberKeyElement()));
		Assert.assertNotNull(res);
		Assert.assertEquals(res.getTableName(), name);
	}

	@Test
	public void createTableWithNumberHashKeyAndStringRangeKey() {
		String name = createTableName();
		TableDescription res = createTable(name, createKeySchema(createNumberKeyElement(), createStringKeyElement()));
		Assert.assertNotNull(res);
		Assert.assertEquals(res.getTableName(), name);
	}

	@Test
	public void createTableWithNumberHashKeyAndNumberRangeKey() {
		String name = createTableName();
		TableDescription res = createTable(name, createKeySchema(createNumberKeyElement(), createNumberKeyElement()));
		Assert.assertNotNull(res);
		Assert.assertEquals(res.getTableName(), name);
	}

	@Test
	public void createTableWithoutHashKey() {
		try {
			createTable(createKeySchema(null, createNumberKeyElement()));
			Assert.assertTrue(false);// Should have thrown an exception
		} catch (AmazonServiceException ase) {
		}
	}

	@Test
	public void createTableWithoutName() {
		try {
			createTable(null, createKeySchema());
			Assert.assertTrue(false);// Should have thrown an exception
		} catch (AmazonServiceException ase) {
		}
	}

	@Test
	public void createTableWithoutThroughput() {
		try {
			createTable(createTableName(), createKeySchema(), null);
			Assert.assertTrue(false);// Should have thrown an exception
		} catch (AmazonServiceException ase) {
		}
	}

	@Test
	public void createTableWithSameHashAndRangeKey() {
		KeySchemaElement el = createStringKeyElement();
		try {
			createTable(createKeySchema(el, el));
			Assert.assertTrue(false);// Should have thrown an exception
		} catch (AmazonServiceException ase) {
		}
	}

	@Test
	public void describeTable() {
		String name = createTableName();
		createTable(name);
		DescribeTableResult res = getClient().describeTable(new DescribeTableRequest().withTableName(name));
		Assert.assertNotNull(res.getTable());
		Assert.assertEquals(res.getTable().getTableName(), name);
	}

	@Test
	public void describeTableWithoutTableName() {
		createTable();
		boolean wasError = false;
		try {
			DescribeTableResult res = getClient().describeTable(new DescribeTableRequest());
			wasError = res.getTable() == null;
		} catch (Exception e) {
			wasError = true;
		}
		Assert.assertTrue(wasError);
	}

	@Test
	public void listTables() {
		String name = createTableName();
		createTable(name);
		ListTablesResult res = getClient().listTables();
		Assert.assertTrue(res.getTableNames().contains(name));
	}

	@Test
	public void listTablesWithLimitOverTableCount() {
		String name = createTableName();
		createTable(name);
		ListTablesResult res = getClient().listTables(new ListTablesRequest().withLimit(5));
		Assert.assertTrue(res.getTableNames().contains(name));
		Assert.assertTrue(res.getTableNames().size() == 1);
	}

	@Test
	public void listTablesWithLimitUnderTableCount() {
		String name = createTableName();
		createTable();
		createTable(name);
		createTable();
		ListTablesResult res = getClient().listTables(new ListTablesRequest().withLimit(2));
		Assert.assertTrue(res.getTableNames().contains(name));
		Assert.assertTrue(res.getTableNames().size() == 2);
	}

	@Test
	public void listTablesWithExclusiveTableName() {
		String name = createTableName();
		createTable();
		createTable();
		createTable(name);
		ListTablesResult res = getClient().listTables(new ListTablesRequest().withExclusiveStartTableName(name));
		Assert.assertTrue(res.getTableNames().contains(name));
		Assert.assertTrue(res.getTableNames().size() == 1);
	}

	@Test
	public void listTablesWithLimitUnderTableCountAndExclusiveTableName() {
		String name = createTableName();
		createTable();
		createTable();
		createTable(name);
		createTable();
		createTable();
		ListTablesResult res = getClient()
				.listTables(new ListTablesRequest().withLimit(1).withExclusiveStartTableName(name));
		Assert.assertTrue(res.getTableNames().contains(name));
		Assert.assertTrue(res.getTableNames().size() == 1);
	}

	@Test
	public void listTablesWithLimitOverTableCountAndExclusiveTableName() {
		String name = createTableName();
		createTable();
		createTable(name);
		createTable();
		createTable();
		createTable();
		ListTablesResult res = getClient()
				.listTables(new ListTablesRequest().withLimit(10).withExclusiveStartTableName(name));
		Assert.assertTrue(res.getTableNames().contains(name));
		Assert.assertTrue(res.getTableNames().size() == 4);
	}

	@Test
	public void deleteTableTest() {
		String name = createTableName();
		createTable(name);
		getClient().deleteTable(new DeleteTableRequest(name));
		ListTablesResult res = getClient().listTables();
		Assert.assertFalse(res.getTableNames().contains(name));
		Assert.assertTrue(res.getTableNames().size() == 0);
	}

	@Test
	public void deleteTableWithoutName() {
		String name = createTableName();
		createTable(name);
		try {
			DeleteTableResult res = getClient().deleteTable(new DeleteTableRequest());
			Assert.assertTrue(false);// Should have thrown an exception
		} catch (AmazonServiceException ase) {
		}
		Assert.assertTrue(getClient().listTables().getTableNames().contains(name));
	}

	@Test
	public void updateTable() {
		String name = createTableName();
		createTable(name);
		ProvisionedThroughput throughput = new ProvisionedThroughput().withReadCapacityUnits(50L)
				.withWriteCapacityUnits(50L);
		UpdateTableRequest req = new UpdateTableRequest().withTableName(name).withProvisionedThroughput(throughput);
		Date date = new Date();
		TableDescription desc = getClient().updateTable(req).getTableDescription();
		Assert.assertNotNull(desc);
		Assert.assertEquals(name, desc.getTableName());
		Assert.assertEquals(Math.round(date.getTime() / 1000),
				Math.round(desc.getProvisionedThroughput().getLastDecreaseDateTime().getTime() / 1000));
		Assert.assertEquals(Math.round(date.getTime() / 1000),
				Math.round(desc.getProvisionedThroughput().getLastIncreaseDateTime().getTime() / 1000));
	}

	@Test
	public void updateTableWithoutName() {
		createTable();
		ProvisionedThroughput throughput = new ProvisionedThroughput().withReadCapacityUnits(50L)
				.withWriteCapacityUnits(50L);
		UpdateTableRequest req = new UpdateTableRequest().withProvisionedThroughput(throughput);
		try {
			getClient().updateTable(req).getTableDescription();
			Assert.assertTrue(false);// Should have thrown an exception
		} catch (AmazonServiceException ase) {
		}
	}

	@Test
	public void updateTableWithoutThroughput() {
		String name = createTableName();
		createTable(name);
		UpdateTableRequest req = new UpdateTableRequest().withTableName(name);
		try {
			getClient().updateTable(req).getTableDescription();
			Assert.assertTrue(false);// Should have thrown an exception
		} catch (AmazonServiceException ase) {
		}
	}

	@Test
	public void updateTableWithoutNameOrThroughput() {
		createTable();
		try {
			getClient().updateTable(new UpdateTableRequest()).getTableDescription();
			Assert.assertTrue(false);// Should have thrown an exception
		} catch (AmazonServiceException ase) {
		}
	}
}
