package io.gridgo.bean.serialization.json.dsl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dslplatform.json.DslJson;

import io.gridgo.bean.BElement;
import io.gridgo.bean.serialization.BSerializationPlugin;
import io.gridgo.bean.serialization.BSerializer;
import io.gridgo.bean.serialization.text.JsonSerializer;
import io.gridgo.utils.exception.RuntimeIOException;

@BSerializationPlugin("dsljson")
public class DslJsonSerializer implements BSerializer {

	private final JsonSerializer fallback = new JsonSerializer();
	private final DslJson<Object> dslJson = new DslJson<Object>();

	@Override
	public void serialize(BElement element, OutputStream out) {
		if (element.isReference()) {
			try {

				dslJson.serialize((Object) element.asReference().getReference(), out);
			} catch (IOException e) {
				throw new RuntimeIOException(e);
			}
		} else {
			fallback.serialize(element, out);
		}
	}

	@Override
	public BElement deserialize(InputStream in) {
		return fallback.deserialize(in);
	}
}
