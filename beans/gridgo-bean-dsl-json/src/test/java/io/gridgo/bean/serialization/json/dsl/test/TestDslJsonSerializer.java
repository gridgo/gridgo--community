package io.gridgo.bean.serialization.json.dsl.test;

import org.junit.Test;

import io.gridgo.bean.BReference;
import io.gridgo.bean.test.support.Bar;

public class TestDslJsonSerializer {

	@Test
	public void testPrint() {
		var bref = BReference.of(new Bar());
		String str = new String(bref.toBytes("dsljson"));
		System.out.println(str);
	}
}
