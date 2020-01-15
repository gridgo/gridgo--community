package io.gridgo.connector.file.support.engines;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
import io.gridgo.connector.file.support.exceptions.UnsupportedFormatException;
import io.gridgo.connector.support.FormattedMarshallable;

public interface FileConsumerEngine extends FormattedMarshallable {

    public default BElement deserialize(byte[] responseBody, int length) {
        if (responseBody == null)
            return null;
        var sliced = slice(responseBody, length);
        var format = getFormat();
        if (format == null || format.equals("json"))
            return BElement.ofBytes(sliced, "json");
        if (format.equals("xml"))
            return BElement.ofBytes(sliced, "xml");
        if (format.equals("string"))
            return BValue.of(sliced);
        if (format.equals("raw"))
            return BElement.ofBytes(sliced);
        throw new UnsupportedFormatException(format);
    }

    private byte[] slice(byte[] responseBody, int length) {
        var result = new byte[length];
        System.arraycopy(responseBody, 0, result, 0, length);
        return result;
    }

    public void readAndPublish();
}
