package io.gridgo.connector.jetty.parser;

import static io.gridgo.connector.httpcommon.HttpContentType.APPLICATION_OCTET_STREAM;
import static io.gridgo.connector.httpcommon.HttpContentType.DEFAULT_TEXT;
import static io.gridgo.connector.httpcommon.HttpContentType.forValueOrDefault;
import static io.gridgo.connector.jetty.support.HttpEntityHelper.readMultiPart;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultHttpRequestParser extends AbstractHttpRequestParser {

    private static final BElement EMPTY = BValue.of("");

    private final String format;

    @Override
    protected BElement extractBody(HttpServletRequest request) throws Exception {
        if (NO_BODY_METHODS.contains(request.getMethod().toLowerCase().trim()))
            return EMPTY;

        var contentType = forValueOrDefault(request.getContentType(), DEFAULT_TEXT);

        if (contentType == APPLICATION_OCTET_STREAM && format != null)
            return BElement.ofBytes(request.getInputStream(), format);

        if (contentType.isMultipartFormat())
            return readMultiPart(request.getParts());

        if (contentType.isJsonFormat())
            return BElement.ofJson(request.getInputStream());

        if (contentType.isBinaryFormat())
            return BReference.of(request.getInputStream());

        var out = new StringWriter();
        request.getReader().transferTo(out);
        return BValue.of(out.toString());
    }
}
