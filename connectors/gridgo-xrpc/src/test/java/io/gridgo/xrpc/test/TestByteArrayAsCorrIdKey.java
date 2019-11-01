package io.gridgo.xrpc.test;

import static io.gridgo.xrpc.decorator.corrid.CorrIdDecoratorHelper.generateCorrId;
import static io.gridgo.xrpc.decorator.corrid.CorrIdDecoratorHelper.wrapCorrId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import io.gridgo.utils.wrapper.ByteArray;

public class TestByteArrayAsCorrIdKey {

    @Test
    public void testByteArrayHashMap() {
        var map = new HashMap<ByteArray, Integer>();
        var list = new ArrayList<ByteArray>();

        int size = 100_000;
        for (int i = 0; i < size; i++) {
            var corrId = generateCorrId();
            map.put(corrId, i);
            list.add(corrId);
        }

        assertEquals(size, map.size());
        assertEquals(size, list.size());

        var i = 0;
        for (var corrId : list) {
            Integer mappedValue = map.get(wrapCorrId(corrId.getSource()));
            assertNotNull(mappedValue);
            assertEquals(i, mappedValue.intValue());
            i++;
        }
    }

    @Test
    public void testSpecificKeys() {
        byte[] key1 = new byte[] { 36, -85, 6, 13, -4, 114, 17, -23, -81, -121, -84, -34, 72, 0, 17, 34 };
        byte[] key2 = new byte[] { 36, -74, 2, -115, -4, 114, 17, -23, -81, -121, -84, -34, 72, 0, 17, 34 };

        var map = new HashMap<ByteArray, Integer>();
        map.put(wrapCorrId(key1), 1);
        map.put(wrapCorrId(key2), 2);

        assertEquals(1, map.get(wrapCorrId(key1)).intValue());
        assertEquals(2, map.get(wrapCorrId(key2)).intValue());
    }
}
