package io.gridgo.bean.serialization.xml.test;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.bean.factory.BFactory;
import io.gridgo.bean.serialization.xml.BXmlReader;

public class TextXmlSerializer {

    @Test
    public void testXmlSerializer() {
        var obj = BObject.ofEmpty() //
                .setAny("bool", false) //
                .set("int", BValue.of(1)) //
                .setAny("long", 1L) //
                .setAny("char", 'a') //
                .setAny("str", "hello") //
                .setAny("double", 1.11) //
                .setAny("byte", (byte) 1) //
                .setAny("raw", new byte[] { 1, 2, 3, 4, 5, 6 }) //
                .setAny("arr", new int[] { 1, 2, 3 }) //
                .set("obj", BObject.ofEmpty().setAny("int", 2)) //
        ;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        obj.writeBytes(out, "xml");
        byte[] bytes = out.toByteArray();

        BElement unpackedEle = BElement.ofBytes(new ByteArrayInputStream(bytes), "xml");
        assertNotNull(unpackedEle);
        assertTrue(unpackedEle.isObject());
        assertEquals(obj, unpackedEle);

        unpackedEle = new BXmlReader(BFactory.DEFAULT).parse(new String(bytes));
        assertNotNull(unpackedEle);
        assertTrue(unpackedEle.isObject());
        assertEquals(obj, unpackedEle);
    }

    @Test
    public void testXmlReaderNull() {
        var reader = new BXmlReader(BFactory.DEFAULT);
        Assert.assertNull(reader.parse((String) null));
        Assert.assertNull(reader.parse((InputStream) null));
    }
}
