package io.gridgo.connector.redis.test;

import static io.gridgo.connector.redis.test.support.TestRedisCommon.checkLongResult;
import static io.gridgo.connector.redis.test.support.TestRedisCommon.checkStringResult;
import static io.gridgo.connector.redis.test.support.TestRedisConsts.BITOPAND;
import static io.gridgo.connector.redis.test.support.TestRedisConsts.BITOPNOT;
import static io.gridgo.connector.redis.test.support.TestRedisConsts.BITOPOR;
import static io.gridgo.connector.redis.test.support.TestRedisConsts.BITOPXOR;
import static io.gridgo.redis.command.RedisCommands.APPEND;
import static io.gridgo.redis.command.RedisCommands.BITCOUNT;
import static io.gridgo.redis.command.RedisCommands.BITFIELD;
import static io.gridgo.redis.command.RedisCommands.BITOP;
import static io.gridgo.redis.command.RedisCommands.BITPOS;
import static io.gridgo.redis.command.RedisCommands.BLPOP;
import static io.gridgo.redis.command.RedisCommands.BRPOP;
import static io.gridgo.redis.command.RedisCommands.DECR;
import static io.gridgo.redis.command.RedisCommands.DECRBY;
import static io.gridgo.redis.command.RedisCommands.DEL;
import static io.gridgo.redis.command.RedisCommands.DUMP;
import static io.gridgo.redis.command.RedisCommands.ECHO;
import static io.gridgo.redis.command.RedisCommands.EXISTS;
import static io.gridgo.redis.command.RedisCommands.EXPIRE;
import static io.gridgo.redis.command.RedisCommands.EXPIREAT;
import static io.gridgo.redis.command.RedisCommands.GEOADD;
import static io.gridgo.redis.command.RedisCommands.GET;
import static io.gridgo.redis.command.RedisCommands.GETBIT;
import static io.gridgo.redis.command.RedisCommands.GETRANGE;
import static io.gridgo.redis.command.RedisCommands.HDEL;
import static io.gridgo.redis.command.RedisCommands.HEXISTS;
import static io.gridgo.redis.command.RedisCommands.HGET;
import static io.gridgo.redis.command.RedisCommands.HGETALL;
import static io.gridgo.redis.command.RedisCommands.HINCRBY;
import static io.gridgo.redis.command.RedisCommands.HINCRBYFLOAT;
import static io.gridgo.redis.command.RedisCommands.HKEYS;
import static io.gridgo.redis.command.RedisCommands.HLEN;
import static io.gridgo.redis.command.RedisCommands.HMGET;
import static io.gridgo.redis.command.RedisCommands.HMSET;
import static io.gridgo.redis.command.RedisCommands.HSET;
import static io.gridgo.redis.command.RedisCommands.HSTRLEN;
import static io.gridgo.redis.command.RedisCommands.HVALS;
import static io.gridgo.redis.command.RedisCommands.INCR;
import static io.gridgo.redis.command.RedisCommands.INCRBY;
import static io.gridgo.redis.command.RedisCommands.INCRBYFLOAT;
import static io.gridgo.redis.command.RedisCommands.KEYS;
import static io.gridgo.redis.command.RedisCommands.LINDEX;
import static io.gridgo.redis.command.RedisCommands.LINSERT;
import static io.gridgo.redis.command.RedisCommands.LLEN;
import static io.gridgo.redis.command.RedisCommands.LPOP;
import static io.gridgo.redis.command.RedisCommands.LPUSH;
import static io.gridgo.redis.command.RedisCommands.LPUSHX;
import static io.gridgo.redis.command.RedisCommands.LRANGE;
import static io.gridgo.redis.command.RedisCommands.LREM;
import static io.gridgo.redis.command.RedisCommands.LSET;
import static io.gridgo.redis.command.RedisCommands.LTRIM;
import static io.gridgo.redis.command.RedisCommands.MGET;
import static io.gridgo.redis.command.RedisCommands.MSET;
import static io.gridgo.redis.command.RedisCommands.MSETNX;
import static io.gridgo.redis.command.RedisCommands.PERSIST;
import static io.gridgo.redis.command.RedisCommands.PEXPIRE;
import static io.gridgo.redis.command.RedisCommands.PSETEX;
import static io.gridgo.redis.command.RedisCommands.PTTL;
import static io.gridgo.redis.command.RedisCommands.RANDOMKEY;
import static io.gridgo.redis.command.RedisCommands.RENAME;
import static io.gridgo.redis.command.RedisCommands.RENAMENX;
import static io.gridgo.redis.command.RedisCommands.RPOP;
import static io.gridgo.redis.command.RedisCommands.RPOPLPUSH;
import static io.gridgo.redis.command.RedisCommands.RPUSH;
import static io.gridgo.redis.command.RedisCommands.RPUSHX;
import static io.gridgo.redis.command.RedisCommands.SADD;
import static io.gridgo.redis.command.RedisCommands.SCAN;
import static io.gridgo.redis.command.RedisCommands.SCARD;
import static io.gridgo.redis.command.RedisCommands.SDIFF;
import static io.gridgo.redis.command.RedisCommands.SDIFFSTORE;
import static io.gridgo.redis.command.RedisCommands.SET;
import static io.gridgo.redis.command.RedisCommands.SETBIT;
import static io.gridgo.redis.command.RedisCommands.SETEX;
import static io.gridgo.redis.command.RedisCommands.SETNX;
import static io.gridgo.redis.command.RedisCommands.SETRANGE;
import static io.gridgo.redis.command.RedisCommands.SINTER;
import static io.gridgo.redis.command.RedisCommands.SINTERSTORE;
import static io.gridgo.redis.command.RedisCommands.SISMEMBER;
import static io.gridgo.redis.command.RedisCommands.SMEMBERS;
import static io.gridgo.redis.command.RedisCommands.SMOVE;
import static io.gridgo.redis.command.RedisCommands.SPOP;
import static io.gridgo.redis.command.RedisCommands.SRANDMEMBER;
import static io.gridgo.redis.command.RedisCommands.STRLEN;
import static io.gridgo.redis.command.RedisCommands.SUNION;
import static io.gridgo.redis.command.RedisCommands.SUNIONSTORE;
import static io.gridgo.redis.command.RedisCommands.TOUCH;
import static io.gridgo.redis.command.RedisCommands.TTL;
import static io.gridgo.redis.command.RedisCommands.TYPE;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.joo.promise4j.Promise;
import org.junit.Assert;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.Message;

public abstract class RedisTestCases {

    private static final String CMD = "cmd";

    private static final DefaultConnectorFactory CONNECTOR_FACTORY = new DefaultConnectorFactory();;

    private Connector connector;

    private Producer producer;

    protected void setup() {
        this.connector = CONNECTOR_FACTORY.createConnector(this.getEndpoint());
        this.connector.start();

        this.producer = connector.getProducer().orElseThrow();
    }

    protected void tearDown() {
        this.connector.stop();
    }

    private BObject buildCommand(String command) {
        return BObject.of(CMD, command);
    }

    public abstract String getEndpoint();

    /*
     * Test `bitcount` command https://redis.io/commands/bitcount
     */
    public void testBitcountCommand() throws InterruptedException {
        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", "foobar")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(BITCOUNT), BArray.ofSequence("mykey", "0", "0"))))//
                .pipeDone(result -> checkLongResult(result, 4))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(BITCOUNT), BArray.ofSequence("mykey", 1, 1))))//
                .pipeDone(result -> checkLongResult(result, 6)) //
                .done(msg -> latch.countDown()) //
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/bitop
     */

    public void testBitopCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("key1", "foobar"))) //
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(BITOP),
                        BArray.ofSequence(BITOPAND, "resultAnd", BArray.ofSequence("key1", "key2")))))// case AND
                .pipeDone(result -> checkLongResult(result, 6))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "resultAnd"))) //
                .pipeDone(result -> checkStringResult(result, "`bc`ab"))

                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("key1", "foobar"))))//
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(BITOP),
                        BArray.ofSequence(BITOPOR, "resultOr", BArray.ofSequence("key1", "key2")))))// case OR
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "resultOr")))//
                .pipeDone(result -> checkStringResult(result, "goofev"))

                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("key1", "foobar"))))//
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(BITOP),
                        BArray.ofSequence(BITOPXOR, "resultXor", BArray.ofSequence("key1", "key2")))))// case XOR
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "resultXor")))//
                .pipeDone(result -> checkStringResult(result, "\\a\\r\\x0c\\x06\\x04\\x14"))

                .pipeDone(result -> producer
                        .call(Message.ofAny(BObject.of(CMD, "set"), BArray.ofSequence("key1", "foobar"))))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(BObject.of(CMD, "set"), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(BITOP), BArray.ofSequence(BITOPNOT, "dest", "key1"))))// case
                // NOT
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "dest")))//
                .pipeDone(result -> checkStringResult(result, "\\x99\\x90\\x90\\x9d\\x9e\\x8d"))

                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        Assert.assertNull(exRef.get());

    }

    /*
     * https://redis.io/commands/bitpos
     */
    public void testBitposCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", "foo"))) //
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(BITPOS), BArray.ofSequence("mykey", false))))//
                .pipeDone(result -> checkLongResult(result, 0))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(BITPOS), BArray.ofSequence("mykey", true))))//
                .pipeDone(result -> checkLongResult(result, 1))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(BITPOS), BArray.ofSequence("mykey", true, 1))))//
                .pipeDone(result -> checkLongResult(result, 9))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(BITPOS), BArray.ofSequence("mykey", false, 1))))//
                .pipeDone(result -> checkLongResult(result, 8))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(BITPOS), BArray.ofSequence("mykey", true, 1, 2))))//
                .pipeDone(result -> checkLongResult(result, 9))//
                .done(result -> latch.countDown()).fail(ex -> {
                    ex.printStackTrace();
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/bitfield
     */

    public void testBitFieldCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", "100")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(BITFIELD),
                        BArray.ofSequence("mykey", "INCRBY", "i5", "100", "1", "GET", "u4", "0"))))//
                .pipeDone(result -> {
                    var responses = result.body().asArray();
                    if (1L == responses.get(0).asValue().getLong() && 0L == responses.get(1).asValue().getLong()) {
                        return Promise.of(result);
                    }
                    return Promise.ofCause(new RuntimeException());
                })//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }
    /*
     * https://redis.io/commands/setbit
     */

    public void testSetBitCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(DEL), "mykey"))//
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SETBIT), BArray.ofSequence("mykey", 7, 1))))//
                .pipeDone(result -> checkLongResult(result, 0))//
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SETBIT), BArray.ofSequence("mykey", 7, 0))))//
                .pipeDone(result -> checkLongResult(result, 1))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "mykey")))//
                .pipeDone(result -> checkStringResult(result, "\u0000"))//
                .done(result -> latch.countDown()).fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/getbit
     */

    public void testGBitCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(DEL), "mykey"))//
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SETBIT), BArray.ofSequence("mykey", 7, 1))))//
                .pipeDone(result -> checkLongResult(result, 0))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GETBIT), BArray.ofSequence("mykey", 0))))//
                .pipeDone(result -> checkLongResult(result, 0))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GETBIT), BArray.ofSequence("mykey", 7))))//
                .pipeDone(result -> checkLongResult(result, 1))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GETBIT), BArray.ofSequence("mykey", 100))))//
                .pipeDone(result -> checkLongResult(result, 0))//
                .done(result -> latch.countDown()).fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/decr
     */
    public void testDecrementCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", "10")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(DECR), "mykey")))//
                .pipeDone(result -> checkLongResult(result, 9))//
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", "234293482390480948029348230948"))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(DECR), "mykey")))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    if (ex.getMessage().equals(
                            "io.lettuce.core.RedisCommandExecutionException: ERR value is not an integer or out of range")) {
                        exRef.set(null);
                        latch.countDown();
                        return;
                    }
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /**
     * https://redis.io/commands/decrby
     */
    public void testDecrbyCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", "10")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(DECRBY), BArray.ofSequence("mykey", 3))))//
                .pipeDone(result -> checkLongResult(result, 7))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/mset
     */
    public void testMsetCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(MSET), BArray.ofSequence("key1", "Hello", "key2", "World")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "key1")))//
                .pipeDone(result -> checkStringResult(result, "Hello"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "key2")))//
                .pipeDone(result -> checkStringResult(result, "World"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/getset
     */
    public void testGetsetCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(MSET), BArray.ofSequence("key1", "Hello", "key2", "World")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "key1")))//
                .pipeDone(result -> checkStringResult(result, "Hello"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "key2")))//
                .pipeDone(result -> checkStringResult(result, "World"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /**
     * https://redis.io/commands/mget
     */
    public void testMGetCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("key1", "Hello")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("key2", "World"))))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(MGET), BArray.ofSequence("key1", "key2", "nonexistkey"))))//
                .pipeDone(result -> {
                    System.out.println(result.getPayload().getHeaders());
                    System.out.println(result);
                    var responses = result.body().asArray();
                    System.out.println("responses" + responses);
                    if ("Hello".equals(responses.get(0).asArray().get(1).toString())
                            && "World".equals(responses.get(1).asArray().get(1).toString())) {
                        return Promise.of(result);
                    }
                    return Promise.ofCause(new RuntimeException());

                })//
                .done(result -> {
                    System.out.println("res: " + result);
                    latch.countDown();
                })//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/getrange
     */
    public void testGetRangeCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", "This is a string")))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(GETRANGE), BArray.ofSequence("mykey", 0, 3))))//
                .pipeDone(result -> checkStringResult(result, "This"))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(GETRANGE), BArray.ofSequence("mykey", -3, -1))))//
                .pipeDone(result -> checkStringResult(result, "ing"))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(GETRANGE), BArray.ofSequence("mykey", 0, -1))))//
                .pipeDone(result -> checkStringResult(result, "This is a string"))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(GETRANGE), BArray.ofSequence("mykey", 10, 100))))//
                .pipeDone(result -> checkStringResult(result, "string")).done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());

    }

    public void testStrLenCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", "Hello world")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(STRLEN), "mykey")))//
                .pipeDone(result -> checkLongResult(result, 11))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(STRLEN), "nonexisting")))//
                .pipeDone(result -> checkLongResult(result, 0))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testIncrCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(DEL), "mykey"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", 10))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(INCR), "mykey")))//
                .pipeDone(result -> checkLongResult(result, 11))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "mykey")))//
                .pipeDone(result -> checkStringResult(result, "11"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSetRangeCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("key1", "Hello World")))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SETRANGE), BArray.ofSequence("key1", 6, "Redis"))))//
                .pipeDone(result -> checkLongResult(result, 11))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "key1")))//
                .pipeDone(result -> checkStringResult(result, "Hello Redis"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSetNxCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(DEL), "mykey"))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SETNX), BArray.ofSequence("mykey", "Hello"))))//
                .pipeDone(result -> checkLongResult(result, 1))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SETNX), BArray.ofSequence("mykey", "World"))))//
                .pipeDone(result -> checkLongResult(result, 0))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "mykey")))//
                .pipeDone(result -> checkStringResult(result, "Hello"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());

    }

    public void testSetExCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        int hold = 10;
        producer.call(Message.ofAny(buildCommand(DEL), "mykey"))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SETEX), BArray.ofSequence("mykey", hold, "Hello"))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(TTL), "mykey")))//
                .pipeDone(result -> {
                    if (result.body().asValue().getLong() > 0) {
                        return Promise.of(result);
                    }
                    return Promise.ofCause(new TimeoutException());
                })//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "mykey")))//
                .pipeDone(result -> checkStringResult(result, "Hello"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testIncrByCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(DEL), "mykey"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", 10))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(INCRBY), BArray.ofSequence("mykey", 10))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "mykey")))//
                .pipeDone(result -> checkLongResult(result, 20))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testMsetNxCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(MSETNX), BArray.ofSequence("key1", "Hello", "key2", "there")))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(MSETNX), BArray.ofSequence("key2", "there", "key3", "world"))))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(MGET), BArray.ofSequence("key1", "key2", "key3"))))//
                .pipeDone(result -> {
                    System.out.println("----DEBUG----");
                    System.out.println(result);
                    return Promise.ofCause(new RuntimeException());
                })//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testPSetxECommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(PSETEX), BArray.ofSequence("mykey", 1000, "Hello")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "mykey")))//
                .pipeDone(result -> checkStringResult(result, "Hello"))//
                .pipeDone(result -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Promise.of(result);
                })//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), "mykey")))//
                .pipeDone(result -> {
                    if (!result.body().asValue().isNull()) {
                        return Promise.ofCause(new RuntimeException());
                    }
                    return Promise.of(result);
                })//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testIncrByFloatCommand() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), "mykey"))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("mykey", "10.5"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(INCRBYFLOAT), BArray.ofSequence("mykey", 0.1f))))
                .done(result -> latch.countDown()).fail(failedCause -> {
                    exRef.set(failedCause);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testEcho() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(ECHO), "hello world")).done(result -> {
            var body = result.body();
            if (!body.isValue() || !StringUtils.equals("hello world", body.asValue().convertToString().getString())) {
                exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
            }
            latch.countDown();
        }).fail(e -> {
            exRef.set(e);
            latch.countDown();
        });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testDelete() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("del", 10.5f)))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(DEL), "del"))).done(result -> {
                    var body = result.body();
                    if (1 != body.asValue().getLong()) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    /**
     * List command Sets url: https://redis.io/commands#set
     *
     * @throws InterruptedException
     */

    public void testSadd() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sadd", "Hello")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sadd", "World"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SMEMBERS), "sadd"))).done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testScard() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("scard", "Hello")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("scard", "World"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SMEMBERS), "scard")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SCARD), "scard"))).done(result -> {
                    var body = result.body();
                    if (body.asValue().getLong() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSdiff() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff1", "a")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff1", "b"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff1", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff2", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff2", "d"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff2", "e"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SDIFF), BArray.ofSequence("diff1", "diff2"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSdiffStore() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff1", "a")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff1", "b"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff1", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff2", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff2", "d"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("diff2", "e"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SDIFFSTORE), BArray.ofSequence("diff", "diff1", "diff2"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSinter() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter1", "a")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter1", "b"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter1", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter2", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter2", "d"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter2", "e"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SINTER), BArray.ofSequence("sinter1", "sinter2"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 1) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.toJson()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSinterStore() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter1", "a")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter1", "b"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter1", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter2", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter2", "d"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sinter2", "e"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SINTERSTORE), BArray.ofSequence("sinter1", "sinter2"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 1) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSismember() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sismember", "one"))).pipeDone(
                result -> producer.call(Message.ofAny(buildCommand(SISMEMBER), BArray.ofSequence("sismember", "one"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 1) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSmembers() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("smembers", "Hello")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("smembers", "World"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SMEMBERS), "smembers"))).done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSmove() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("smove1", "one")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("smove1", "two"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("smove2", "three"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SMOVE), BArray.ofSequence("smove1", "smove2", "two"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 1) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSpop() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("spop", "one")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("spop", "two"))))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("spop", "three"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(SPOP), BArray.ofSequence("spop"))))
                .done(result -> {
                    var body = result.body();
                    if (StringUtils.isEmpty(body.asValue().getString())) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSrandMember() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("srandmember", "one", "two", "three")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SRANDMEMBER), BArray.ofSequence("srandmember", "2"))))
                .done(result -> {

                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSrem() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("srem", "one", "two", "three"))).pipeDone(
                result -> producer.call(Message.ofAny(buildCommand(SRANDMEMBER), BArray.ofSequence("srem", "one"))))
                .done(result -> {
//                    var body = result.body();
//                    if(body.asArray().size() != 1) {
//                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
//                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSunion() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sunion1", "one")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sunion2", "two"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sunion3", "three"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SUNION), BArray.ofSequence("sunion1", "sunion2", "sunion3"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 3) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testSunionStore() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sunion1", "one")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sunion2", "two"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SADD), BArray.ofSequence("sunion3", "three"))))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(SUNIONSTORE), BArray.ofSequence("sunion1", "sunion2", "sunion3"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testScan() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SCAN), 0)).done(result -> {
            var body = result.body();
            if (body.asObject().getArray("keys") == null) {
                exRef.set(new RuntimeException("Body mismatch"));
            }
            latch.countDown();
        }).fail(e -> {
            exRef.set(e);
            latch.countDown();
        });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /**
     * GEO redis url: https://redis.io/commands#geo
     */
    public void testGeoAdd() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), "Sicily")).pipeDone(result -> producer.call(Message
                .ofAny(buildCommand(GEOADD), BArray.ofSequence("Sicily", "-73.9454966", "40.747533", "Palermo"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getLong() != 1L) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());

    }

    /**
     * Key command url: https://redis.io/commands#generic
     */
    public void testDel() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(APPEND), BArray.ofSequence("del", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(DEL), "del"))).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("result must equal to 1"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testDump() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(APPEND), BArray.ofSequence("dump", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(DUMP), "dump"))).done(result -> {
                    var body = result.body().asValue().getString();
                    if (body.length() == 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testExists() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("set", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(EXISTS), "set"))).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testExpire() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("expire", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(EXPIRE), BArray.ofSequence("expire", 10))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(TTL), "expire"));
                }).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 10) {
                        exRef.set(new RuntimeException("Result must equal to 10"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testExpireat() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        long currentTimestamp = Instant.now().toEpochMilli();
        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("expireat", "Hello")))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(EXPIREAT), BArray.ofSequence("expireat", currentTimestamp + 1000))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(TTL), "expireat"));
                }).done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body > currentTimestamp) {
                        exRef.set(new RuntimeException("Result must greater than " + currentTimestamp));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testKeys() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        Map<String, String> map = new LinkedHashMap<>();
        map.put("KEY1", "1");
        map.put("KEY2", "2");
        map.put("KEY3", "3");

        producer.call(Message.ofAny(buildCommand(MSET), BArray.ofSequence(map)))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(KEYS), "KEY*"))).done(result -> {
                    var body = result.body().asArray();
                    Predicate<String> predicate = value -> map.containsKey(value);
                    body.stream().forEach(bElement -> {
                        if (!predicate.test(bElement.asValue().getString())) {
                            exRef.set(new RuntimeException("Body miss match"));
                        }
                    });
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testPersits() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("persits", "Hello")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(EXPIRE), BArray.ofSequence("persits", 10))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(PERSIST), "persits"));
                }).done(result -> {
                    var body = result.body().asValue();
                    if (!body.getBoolean()) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testPExpire() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("pexpireat", "Hello")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(PEXPIRE), BArray.ofSequence("pexpireat", 5000))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(PTTL), "pexpireat"));
                }).done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body <= 0) {
                        exRef.set(new RuntimeException("Body mismtach"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testPExpireat() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        long currentTimestamp = Instant.now().toEpochMilli();
        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("pexpireat", "Hello")))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(EXPIREAT), BArray.ofSequence("pexpireat", currentTimestamp + 5000))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(PTTL), "pexpireat"));
                }).done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body < currentTimestamp) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testPTTL() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("pexpireat", "Hello")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(EXPIRE), BArray.ofSequence("pexpireat", 1))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(PTTL), "pexpireat"));
                }).done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body < 0 || body > 1000) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testRandomkey() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RANDOMKEY), null)).done(result -> {
            var body = result.body().asValue().getString();
            if (StringUtils.isEmpty(body)) {
                exRef.set(new RuntimeException("Body mismatch"));
            }
            latch.countDown();
        }).fail(e -> {
            exRef.set(e);
            latch.countDown();
        });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testRename() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("rename", "Hello")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RENAME), BArray.ofSequence("rename", "rename1"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(GET), BArray.ofSequence("rename1"))))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (StringUtils.isEmpty(body)) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testRenamenx() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("renamenx1", "Hello")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SET), BArray.ofSequence("renamenx2", "WORLD"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RENAMENX), BArray.ofSequence("renamenx1", "renamenx2"))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    return producer.call(Message.ofAny(buildCommand(GET), BArray.ofSequence("renamenx2")));
                }).done(result -> {
                    var body = result.body().asValue().getString();
                    if (StringUtils.isEmpty(body) || !StringUtils.equals("WORLD", body)) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testTouch() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("touch1", "Hello")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("touch2", "WORLD"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(TOUCH), BArray.ofSequence("touch1", "touch2"))))
                .done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 2) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testType() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("type", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(TYPE), "type"))).done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "string")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testUnlink() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(SET), BArray.ofSequence("unlink1", "Hello")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(SET), BArray.ofSequence("unlink2", "WORLD"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(TOUCH), BArray.ofSequence("unlink1", "unlink2"))))
                .done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 2) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /**
     * Test Hash url: https://redis.io/commands#hash
     */
    public void testHash() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("hash", "field1", "foo")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(HDEL), BArray.ofSequence("hash", "field1"))))
                .done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHashExist() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("hashexist", "field1", "foo"))).pipeDone(
                result -> producer.call(Message.ofAny(buildCommand(HEXISTS), BArray.ofSequence("hashexist", "field1"))))
                .done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHashGet() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("hashget", "field1", "foo"))).pipeDone(
                result -> producer.call(Message.ofAny(buildCommand(HGET), BArray.ofSequence("hashget", "field1"))))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "foo")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHashGetAll() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("hashgetall", "field1", "foo")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("hashgetall", "field2", "bar"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("hashgetall", "field3", "sekiro"))))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(HGETALL), BArray.ofSequence("hashgetall"))))
                .done(result -> {
                    var body = result.body().asObject();
                    if (body.size() <= 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHINCRBY() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HINCRBY", "field1", "5")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HINCRBY), BArray.ofSequence("HINCRBY", "field1", "1"))))
                .done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 6) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHINCRBYFLOAT() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HINCRBYFLOAT", "field1", "5")))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(HINCRBYFLOAT), BArray.ofSequence("HINCRBYFLOAT", "field1", 0.1))))
                .done(result -> {
                    var body = result.body().asValue().getDouble();
                    if (body <= 5) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHKEYS() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HKEYS", "field1", "foo")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HKEYS", "field2", "bar"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HKEYS", "field3", "sekiro"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(HKEYS), BArray.ofSequence("HKEYS"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() <= 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHLEN() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HLEN", "field1", "foo")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HLEN", "field2", "bar"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HLEN", "field3", "sekiro"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(HLEN), BArray.ofSequence("HLEN"))))
                .done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 3) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHMGET() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HMGET", "field1", "foo")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HMGET", "field2", "bar"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HMGET", "field3", "sekiro"))))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(HMGET), BArray.ofSequence("HMGET", "field1", "field2", "field3"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() <= 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHMSET() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HMSET), BArray.ofSequence("HMSET", "field1", "foo", "field2", "bar")))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals("OK", body)) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHSET() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HSET", "field1", "foo")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(HGET), BArray.ofSequence("HSET", "field1"))))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals("foo", body)) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHSETNX() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HSETNX", "field1", "foo")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HGET), BArray.ofSequence("HSETNX", "field1", "bar"))))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "foo")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHSTRLEN() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HMSET),
                BArray.ofSequence("HSTRLEN", "field1", "HelloWorld", "field2", "bar")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSTRLEN), BArray.ofSequence("HSTRLEN", "field1"))))
                .done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 10) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testHVALS() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HMGET", "field1", "foo")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HMGET", "field2", "bar"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(HSET), BArray.ofSequence("HMGET", "field3", "sekiro"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(HVALS), BArray.ofSequence("HMGET"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() <= 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    /**
     * List command url: https://redis.io/commands#list
     */
    public void testRPUSH() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("RPUSH")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RPUSH), BArray.ofSequence("RPUSH", "Hello", "World"))))
                .done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body != 2) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testRPUSHX() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("RPUSHX")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RPUSHX), BArray.ofSequence("RPUSHX", "Hello", "World"))))
                .done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body != 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLTRIM() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LTRIM")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RPUSH),
                        BArray.ofSequence("LTRIM", "1", "2", "3", "4", "5", "6", "7"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LTRIM), BArray.ofSequence("LTRIM", "0", "3"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LRANGE), BArray.ofSequence("LTRIM", "0", "-1"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() != 4) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testRPOP() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("RPOP")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RPUSH), BArray.ofSequence("RPOP", "one", "two", "three"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RPOP), "RPOP"))).done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "three")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testRPOPLPUSH() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("RPOPLPUSH1")))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(RPUSH), BArray.ofSequence("RPOPLPUSH1", "one", "two", "three"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RPOPLPUSH), BArray.ofSequence("RPOPLPUSH1", "RPOPLPUSH2"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LRANGE), BArray.ofSequence("RPOPLPUSH2", "0", "-1"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() == 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLRANGE() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LRANGE")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RPUSH),
                        BArray.ofSequence("LRANGE", "1", "2", "3", "4", "5", "6", "7"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LRANGE), BArray.ofSequence("LRANGE", "0", "-1"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() != 7) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLREM() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LREM")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RPUSH),
                        BArray.ofSequence("LREM", "1", "2", "3", "4", "1", "6", "1"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LREM), BArray.ofSequence("LREM", "-2", "1"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LRANGE), BArray.ofSequence("LREM", "0", "-1"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() != 5) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLSET() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LSET")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RPUSH), BArray.ofSequence("LSET", "one", "two", "three"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LSET), BArray.ofSequence("LSET", "-1", "four"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RPOP), BArray.ofSequence("LSET"))))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "four")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLPOP() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LPOP")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RPUSH), BArray.ofSequence("LPOP", "one", "two", "three"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(LPOP), BArray.ofSequence("LPOP"))))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "one")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLPUSH() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LPUSH")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LPUSH), BArray.ofSequence("LPUSH", "one", "two", "three"))))
                .done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body != 3) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLPUSHX() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LPUSHX")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LPUSHX), BArray.ofSequence("LPUSHX", "one", "two", "three"))))
                .done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body != 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLINDEX() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LINDEX")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LPUSH), BArray.ofSequence("LINDEX", "one", "two", "three"))))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(LINDEX), BArray.ofSequence("LINDEX", "1"))))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "two")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLINSERT() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LINSERT")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LPUSH), BArray.ofSequence("LINSERT", "one", "three"))))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(LINSERT), BArray.ofSequence("LINSERT", "BEFORE", "three", "two"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(LINDEX), BArray.ofSequence("LINSERT", 1))))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "two")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testLLEN() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("LLEN")))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LPUSH), BArray.ofSequence("LLEN", "one", "three"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(LLEN), BArray.ofSequence("LLEN"))))
                .done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body != 2) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testBLPOP() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("two")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(RPUSH), BArray.ofSequence("two", "2", "3"))))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(BLPOP), BArray.ofSequence(1, "one", "two"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() != 2) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testBRPOP() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("two")))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(RPUSH), BArray.ofSequence("two", "2", "3"))))
                .pipeDone(
                        result -> producer.call(Message.ofAny(buildCommand(BRPOP), BArray.ofSequence(1, "one", "two"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() != 2) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }

    public void testBRPOPLPUSH() throws InterruptedException {

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(DEL), BArray.ofSequence("BRPOPLPUSH1")))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(RPUSH), BArray.ofSequence("BRPOPLPUSH1", "one", "two", "three"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RPOPLPUSH), BArray.ofSequence("BRPOPLPUSH1", "BRPOPLPUSH2"))))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(LRANGE), BArray.ofSequence("BRPOPLPUSH2", "0", "-1"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() == 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        Assert.assertNull(exRef.get());
    }
}