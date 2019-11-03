package io.gridgo.bean.serialization.xml;


import java.io.InputStream;
import java.io.OutputStream;

import io.gridgo.bean.BElement;
import io.gridgo.bean.factory.BFactory;
import io.gridgo.bean.serialization.AbstractBSerializer;
import io.gridgo.bean.serialization.BSerializationPlugin;
import lombok.NonNull;

@BSerializationPlugin(XmlSerializer.NAME)
public class XmlSerializer extends AbstractBSerializer {

    public static final String NAME = "xml";

    private BXmlReader xmlParser = null;

    @Override
    public void setFactory(@NonNull BFactory factory) {
        super.setFactory(factory);
        xmlParser = new BXmlReader(factory);
    }

    @Override
    public void serialize(@NonNull BElement element, @NonNull OutputStream out) {
        BXmlWriter.write(out, element);
    }

    @Override
    public BElement deserialize(@NonNull InputStream in) {
        return xmlParser.parse(in);
    }
}
