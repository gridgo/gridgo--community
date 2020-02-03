package io.gridgo.connector.jdbc;

import com.zaxxer.hikari.HikariDataSource;

import org.jdbi.v3.core.ConnectionFactory;
import org.joo.promise4j.PromiseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.impl.SimpleRegistry;

public class JdbcConnectorTest {

    private static Registry registry;
    private static ConnectorContext context;
    private static Connector connector;
    private static Producer producer;
    private static HikariDataSource pool;

    @BeforeClass
    public static void initialize() {
        pool = new HikariDataSource();
        pool.setJdbcUrl("jdbc:mysql://localhost:3306/test");
        pool.setUsername("root");
        registry = new SimpleRegistry().register("hikari", (ConnectionFactory) pool::getConnection);
        context = new DefaultConnectorContextBuilder().setRegistry(registry).build();
        connector = new DefaultConnectorFactory()
                .createConnector("jdbc:mysql://localhost:3306/test?user=root&pool=hikari&useSSL=false", context);
        connector.start();
        producer = connector.getProducer().orElseThrow();
    }

    @AfterClass
    public static void cleanup() {
        pool.close();
    }

    @Test
    public void testBatch() throws PromiseException, InterruptedException {
        var batches = BObject.of(JdbcConstants.IS_BATCH, true)
                .setAny(JdbcConstants.BATCH_DATA, BArray.ofSequence(
                        BObject.of("id", 1),
                        BObject.of("id", 2),
                        BObject.of("id", 3)
                ));
        var msg = producer.callAny("drop table if exists test_users")
                .then(r -> producer.callAny("create table test_users(id int)"))
                .then(r -> producer.callAny(
                        batches,
                        "insert into test_users values(:id)"))
                .then(r -> producer.callAny("select count(*) as total from test_users")).get();
        Assert.assertEquals(3, (int) msg.body().asArray().getObject(0).getInteger("total", 0));
    }

    @Test
    public void testSelectWithAlias() throws PromiseException, InterruptedException {
        var msg = producer.callAny("drop table if exists test_users")
                .then(r -> producer.callAny("create table test_users(id int)"))
                .then(r -> producer.callAny("insert into test_users values(1)"))
                .then(r -> producer.callAny("select id as user_id from test_users where id = 1")).get();
        Assert.assertEquals(1, (int) msg.body().asArray().getObject(0).getInteger("user_id", 0));
    }

    @Test
    public void testSelect() {
        TestUtil testUtil = new TestUtil("testSelect");
        var latch = new CountDownLatch(1);
        try {
            dropTable(testUtil, latch);
            latch.await();
            latch = new CountDownLatch(1);
            createTable(testUtil, latch);
            latch.await();
            latch = new CountDownLatch(1);
            insert(testUtil, latch);
            latch.await();
            latch = new CountDownLatch(1);
            select(testUtil, latch);
            latch.await();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    private void select(TestUtil testUtil, CountDownLatch latch) {
        var ok = producer.call(testUtil.createSelectRequest());
        var sqlValues = testUtil.getSqlValues();
        ok.done(msg -> {
            try {
                var list = msg.getPayload().getBody().asArray();
                for (BElement bElement : list) {
                    var result = bElement.asObject();
                    Assert.assertEquals(sqlValues.get("integertest"), result.getInteger("integertest"));
                    Assert.assertEquals(sqlValues.get("stringtest"), result.getString("stringtest"));
                    Assert.assertEquals(sqlValues.get("bigdecimaltest"),
                            result.get("bigdecimaltest").asValue().getDataAs(BigDecimal.class));
                    Assert.assertEquals(sqlValues.get("booleantest"), result.getBoolean("booleantest"));
                    Assert.assertEquals(sqlValues.get("datetest"), result.get("datetest").asReference().getReference());
                    Assert.assertEquals(sqlValues.get("timetest"), result.get("timetest").asReference().getReference());
                    Assert.assertEquals(sqlValues.get("timestamptest"),
                            result.get("timestamptest").asReference().getReference());
                    latch.countDown();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Assert.fail();
            }
        });
    }

    private void insert(TestUtil testUtil, CountDownLatch latch) {
        var ok = producer.call(testUtil.createInsertRequest());
        ok.done(msg -> {
            var list = msg.getPayload().getBody().asValue().getInteger();
            Assert.assertEquals(Integer.valueOf(1), list);
            latch.countDown();
        });
        ok.fail(ex -> {
            ex.printStackTrace();
            Assert.fail();
        });
    }

    private void dropTable(TestUtil testUtil, CountDownLatch latch) {
        Message message = testUtil.createDropTableMessage();
        producer.call(message).done(msg -> latch.countDown()).fail(ex -> {
            ex.printStackTrace();
            Assert.fail();
        });
    }

    private void createTable(TestUtil testUtil, CountDownLatch latch) {
        Message message = testUtil.createCreateTableMessage();
        producer.call(message).done(msg -> latch.countDown()).fail(ex -> {
            ex.printStackTrace();
            Assert.fail();
        });
    }
}
