package io.gridgo.bean.serialization.xml.test;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;

public class TestBElement {

    @Test
    public void testXmlFromFile() throws IOException {
        try (InputStream in = TestBElement.class.getClassLoader().getResourceAsStream("test.xml");
                Reader reader = new InputStreamReader(in)) {

            String xml = new BufferedReader(reader).lines().collect(Collectors.joining("\n"));

            BObject obj = BElement.ofBytes(xml.getBytes("utf-8"), "xml");
            assertObject(obj);

            byte[] bytes = obj.toBytes();
            BObject fromRaw = BElement.ofBytes(bytes);
            assertObject(fromRaw);
        }
    }

    private void assertObject(BObject obj) {
        Assert.assertEquals("hello", obj.getString("str"));
        Assert.assertEquals(false, obj.getBoolean("bool"));
        Assert.assertEquals(1.11, obj.getDouble("double"), 0.0001);
        Assert.assertEquals((byte) 1, (byte) obj.getByte("byte"));
        Assert.assertEquals(2, (int) obj.getObject("obj").getInteger("int"));
        Assert.assertEquals('a', (char) obj.getChar("char"));
        Assert.assertEquals(1, (int) obj.getInteger("int"));
        Assert.assertEquals(1L, (long) obj.getLong("long"));
        Assert.assertEquals(Arrays.asList(1, 2, 3), obj.getArray("arr").toList());
    }

    @Test
    public void testXml2() throws UnsupportedEncodingException {
        var obj = BObject.ofEmpty() //
                .set("int", BValue.of(1)) //
                .setAny("long", 1L) //
                .setAny("char", 'a') //
                .setAny("str", "hello") //
                .setAny("double", 1.11) //
                .setAny("byte", (byte) 1) //
                .setAny("arr", new int[] { 1, 2, 3 }) //
                .set("obj", BObject.ofEmpty().setAny("int", 2));
        var xml = "<object><string name=\"str\" value=\"hello\"/><array name=\"arr\"><integer value=\"1\"/><integer value=\"2\"/><integer value=\"3\"/></array><double name=\"double\" value=\"1.11\"/><byte name=\"byte\" value=\"1\"/><object name=\"obj\"><integer name=\"int\" value=\"2\"/></object><char name=\"char\" value=\"a\"/><integer name=\"int\" value=\"1\"/><long name=\"long\" value=\"1\"/></object>";

        Assert.assertEquals(xml, obj.toXml());
        obj = BElement.ofBytes(xml.getBytes("utf-8"), "xml");
        Assert.assertEquals(Integer.valueOf(1), obj.getInteger("int", -1));
        Assert.assertEquals("hello", obj.getString("str", null));
        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, //
                obj.getArray("arr", BArray.ofEmpty()).stream() //
                        .map(e -> e.asValue().getData()) //
                        .toArray(size -> new Integer[size]));
        Assert.assertEquals(Long.valueOf(1L), obj.getLong("long", -1));
        Assert.assertEquals(Character.valueOf('a'), obj.getChar("char", '\0'));
        Assert.assertEquals(1.11, obj.getDouble("double", -1), 0);
        Assert.assertEquals(1.11, obj.getFloat("double", -1), 0.001);
        Assert.assertEquals(Byte.valueOf((byte) 1), obj.getByte("byte", -1));
        Assert.assertEquals(Integer.valueOf(2), obj.getObject("obj", null).getInteger("int"));
    }

    @Test
    public void testXml() throws UnsupportedEncodingException {
        var val = BValue.of(new byte[] { 1, 2, 4, 8, 16, 32, 64 });
        val = BElement.ofBytes(val.toXml().getBytes("utf-8"), "xml").asValue();
        Assert.assertArrayEquals(new byte[] { 1, 2, 4, 8, 16, 32, 64 }, (byte[]) val.getData());
    }
}
