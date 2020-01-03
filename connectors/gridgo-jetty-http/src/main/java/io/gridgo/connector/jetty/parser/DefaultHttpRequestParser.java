package io.gridgo.connector.jetty.parser;

import static io.gridgo.connector.httpcommon.HttpContentType.APPLICATION_OCTET_STREAM;
import static io.gridgo.connector.httpcommon.HttpContentType.DEFAULT_TEXT;
import static io.gridgo.connector.httpcommon.HttpContentType.forValueOrDefault;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import io.gridgo.connector.jetty.support.HttpEntityHelper;
import io.gridgo.utils.wrapper.ByteBufferOutputStream;
import lombok.Builder;
import lombok.NonNull;

public class DefaultHttpRequestParser extends AbstractHttpRequestParser {

    private static final int DEFAULT_STRING_BUFFER_SIZE = 65536;

    private static final BElement EMPTY = BValue.of("");

    @NonNull
    private final ThreadLocal<ByteBuffer> bufferThreadLocal;

    private final String format;

    @NonNull
    private final Charset charset;

    @Builder
    private DefaultHttpRequestParser(String format, Charset charset, Integer stringBufferSize) {
        this.format = format;
        this.charset = charset == null ? Charset.forName("UTF-8") : charset;

        final int bufferSize = stringBufferSize == null ? DEFAULT_STRING_BUFFER_SIZE : stringBufferSize;
        this.bufferThreadLocal = ThreadLocal.withInitial(() -> ByteBuffer.allocate(bufferSize));
    }

    @Override
    protected BElement extractBody(HttpServletRequest request) throws Exception {
        if (NO_BODY_METHODS.contains(request.getMethod().toLowerCase().trim()))
            return EMPTY;

        var contentType = forValueOrDefault(request.getContentType(), DEFAULT_TEXT);

        if (contentType == APPLICATION_OCTET_STREAM && format != null)
            return BElement.ofBytes(request.getInputStream(), format);

        if (contentType.isMultipartFormat())
            return HttpEntityHelper.readMultipart(request.getParts());

        if (contentType.isJsonFormat())
            return BElement.ofJson(request.getInputStream());

        if (contentType.isBinaryFormat())
            return BReference.of(request.getInputStream());

        var bb = bufferThreadLocal.get().clear();
        try (var out = new ByteBufferOutputStream(bb)) {
            request.getInputStream().transferTo(out);
            return BValue.of(new String(bb.array(), 0, bb.position(), charset));
        }
    }
}
