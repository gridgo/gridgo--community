package io.gridgo.xrpc.decorator.corrid;

import static io.gridgo.utils.wrapper.ByteArray.newInstanceWithJavaSafeHashCodeCalculator;

import io.gridgo.bean.BElement;
import io.gridgo.bean.exceptions.InvalidTypeException;
import io.gridgo.utils.UuidUtils;
import io.gridgo.utils.wrapper.ByteArray;
import lombok.NonNull;

public class CorrIdDecoratorHelper {

    public static final ByteArray wrapCorrId(@NonNull byte[] value) {
        return newInstanceWithJavaSafeHashCodeCalculator(value);
    }

    public static final ByteArray generateCorrId() {
        return wrapCorrId(UuidUtils.timebasedUUIDAsBytes());
    }

    public static final ByteArray wrapCorrId(@NonNull BElement element) {
        if (!element.isValue())
            throw new InvalidTypeException("support only BValue, got: " + element.getType());
        return wrapCorrId(element.asValue().getRaw());
    }
}
