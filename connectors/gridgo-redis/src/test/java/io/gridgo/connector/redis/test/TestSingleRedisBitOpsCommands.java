package io.gridgo.connector.redis.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import lombok.AccessLevel;
import lombok.Getter;

public class TestSingleRedisBitOpsCommands extends RedisTestCases {

    @Getter(AccessLevel.PUBLIC)
    private final String endpoint = "redis:single://[localhost:6379]";

    @Before
    public void setup() {
        super.setup();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
//    @Ignore
    @Override
    public void testBitcountCommand() throws InterruptedException {
        super.testBitcountCommand();
    }

    @Test
    @Ignore
    @Override
    public void testBitopCommand() throws InterruptedException {
        super.testBitopCommand();
    }

    @Test
    @Ignore
    @Override
    public void testBitposCommand() throws InterruptedException {
        super.testBitposCommand();
    }

    @Test
    @Ignore
    @Override
    public void testBitFieldCommand() throws InterruptedException {
        super.testBitFieldCommand();
    }

}
