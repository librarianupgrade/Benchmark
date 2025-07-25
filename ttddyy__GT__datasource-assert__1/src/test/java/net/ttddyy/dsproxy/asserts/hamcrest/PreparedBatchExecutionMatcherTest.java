package net.ttddyy.dsproxy.asserts.hamcrest;

import net.ttddyy.dsproxy.asserts.PreparedBatchExecution;
import net.ttddyy.dsproxy.asserts.PreparedBatchExecutionEntry;
import org.hamcrest.Matcher;
import org.junit.Test;

import static net.ttddyy.dsproxy.asserts.ParameterKeyValueUtils.createSetParam;
import static net.ttddyy.dsproxy.asserts.hamcrest.DataSourceProxyMatchers.batch;
import static net.ttddyy.dsproxy.asserts.hamcrest.DataSourceProxyMatchers.batchSize;
import static net.ttddyy.dsproxy.asserts.hamcrest.DataSourceProxyMatchers.param;
import static net.ttddyy.dsproxy.asserts.hamcrest.DataSourceProxyMatchers.paramAsInteger;
import static net.ttddyy.dsproxy.asserts.hamcrest.DataSourceProxyMatchers.paramAsString;
import static net.ttddyy.dsproxy.asserts.hamcrest.DataSourceProxyMatchers.paramIndexes;
import static net.ttddyy.dsproxy.asserts.hamcrest.DataSourceProxyMatchers.paramsByIndex;
import static net.ttddyy.dsproxy.asserts.hamcrest.DataSourceProxyMatchers.query;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * @author Tadaya Tsuyukubo
 * @since 1.0
 */
public class PreparedBatchExecutionMatcherTest {

	@Test
	public void testQuery() {
		PreparedBatchExecution pbe = new PreparedBatchExecution();
		pbe.setQuery("foo");

		assertThat(pbe, query(is("foo")));
		assertThat(pbe, query(startsWith("fo")));
		assertThat(pbe.getQuery(), is("foo"));
	}

	@Test
	public void testBatchSize() {
		PreparedBatchExecutionEntry entry1 = new PreparedBatchExecutionEntry();
		entry1.getAllParameters().add(createSetParam(10, "FOO"));
		entry1.getAllParameters().add(createSetParam(11, "BAR"));

		PreparedBatchExecutionEntry entry2 = new PreparedBatchExecutionEntry();
		entry2.getAllParameters().add(createSetParam(20, "FOO"));
		entry2.getAllParameters().add(createSetParam(21, "BAR"));

		PreparedBatchExecution pbe = new PreparedBatchExecution();
		pbe.addBatchExecutionEntry(entry1);
		pbe.addBatchExecutionEntry(entry2);

		assertThat(pbe, batchSize(2));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testBatch() {

		PreparedBatchExecutionEntry entry = new PreparedBatchExecutionEntry();
		entry.getAllParameters().add(createSetParam(1, "FOO"));
		entry.getAllParameters().add(createSetParam(2, "BAR"));
		entry.getAllParameters().add(createSetParam(10, 100));

		PreparedBatchExecution pbe = new PreparedBatchExecution();
		pbe.addBatchExecutionEntry(entry);

		assertThat(pbe, batch(0, paramsByIndex(hasEntry(2, (Object) "BAR"))));
		assertThat(pbe, batch(0, paramsByIndex(hasEntry(10, (Object) 100))));
		assertThat(pbe, batch(0, paramIndexes(hasItem(1))));
		assertThat(pbe, batch(0, paramIndexes(hasItems(1, 2))));

		assertThat(pbe, batch(0, param(1, is((Object) "FOO"))));
		assertThat(pbe, batch(0, param(1, (Matcher) startsWith("FOO"))));
		assertThat(pbe, batch(0, param(10, is((Object) 100))));

		assertThat(pbe, batch(0, param(1, String.class, is("FOO"))));
		assertThat(pbe, batch(0, param(1, String.class, startsWith("FOO"))));
		assertThat(pbe, batch(0, param(10, Integer.class, is(100))));

	}

	@Test
	@SuppressWarnings("unchecked")
	public void testBatchByIndex() {

		PreparedBatchExecutionEntry entry = new PreparedBatchExecutionEntry();
		entry.getAllParameters().add(createSetParam(1, "FOO"));
		entry.getAllParameters().add(createSetParam(2, "BAR"));
		entry.getAllParameters().add(createSetParam(3, 100));

		PreparedBatchExecution pbe = new PreparedBatchExecution();
		pbe.addBatchExecutionEntry(entry);

		assertThat(pbe, batch(0, paramsByIndex(hasEntry(2, (Object) "BAR"))));
		assertThat(pbe, batch(0, paramsByIndex(hasEntry(3, (Object) 100))));
		assertThat(pbe, batch(0, paramIndexes(hasItem(1))));
		assertThat(pbe, batch(0, paramIndexes(hasItems(1, 2))));
		assertThat(pbe, batch(0, paramIndexes(1)));
		assertThat(pbe, batch(0, paramIndexes(1, 2)));

		assertThat(pbe, batch(0, param(1, is((Object) "FOO"))));
		assertThat(pbe, batch(0, param(1, (Matcher) startsWith("FOO"))));
		assertThat(pbe, batch(0, param(3, is((Object) 100))));

		assertThat(pbe, batch(0, param(1, String.class, is("FOO"))));
		assertThat(pbe, batch(0, param(1, String.class, startsWith("FOO"))));
		assertThat(pbe, batch(0, param(3, Integer.class, is(100))));

		assertThat(pbe, batch(0, paramAsString(1, is("FOO"))));
		assertThat(pbe, batch(0, paramAsInteger(3, is(100))));

	}
}
