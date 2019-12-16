package io.gridgo.connector.jetty.support;

import static io.gridgo.connector.httpcommon.HttpCommonConstants.BODY;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.CONTENT_TYPE;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.NAME;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.SUBMITTED_FILE_NAME;
import static io.gridgo.connector.httpcommon.HttpContentType.isBinaryType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

import javax.servlet.http.Part;

import org.apache.http.HttpEntity;
import org.eclipse.jetty.http.MultiPartFormInputStream;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;

public class HttpEntityHelper {

    public static BArray readMultiPart(Collection<Part> parts) throws IOException {
        var results = BArray.ofEmpty();
        for (var part : parts) {
            var contentType = part.getContentType();
            results.add(contentType != null && isBinaryType(contentType) //
                    ? BObject.ofEmpty() //
                            .setAny(NAME, part.getName()) //
                            .setAny(CONTENT_TYPE, contentType) //
                            .setAny(SUBMITTED_FILE_NAME, part.getSubmittedFileName()) //
                            .setAny(BODY, BReference.of(part.getInputStream())) //
                    : BObject.ofEmpty() //
                            .setAny(NAME, part.getName()) //
                            .setAny(CONTENT_TYPE, contentType)//
                            .setAny(BODY, BElement.ofJson(part.getInputStream())));
        }
        return results;
    }

    public static final BArray readMultiPart(HttpEntity entity) throws IOException {
        return readMultiPart(entity.getContent(), entity.getContentType().getValue());
    }

    public static final BArray readMultiPart(InputStream input, String contentTypeWithBoundary) throws IOException {
        return readMultiPart(new MultiPartFormInputStream(input, contentTypeWithBoundary, null, null).getParts());
    }

    public static String readString(InputStream input) throws IOException {
        return readString(input, Charset.forName("UTF-8"));
    }

    public static String readString(InputStream input, Charset charset) throws IOException {
        try (var output = new ByteArrayOutputStream(input.available())) {
            input.transferTo(output);
            return output.toString(charset);
        }
    }
}
