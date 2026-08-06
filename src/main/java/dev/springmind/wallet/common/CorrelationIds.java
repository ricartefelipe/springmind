package dev.springmind.wallet.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class CorrelationIds {

    private CorrelationIds() {}

    public static String current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            Object attribute = request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE);
            if (attribute instanceof String correlationId && !correlationId.isBlank()) {
                return correlationId;
            }
            String header = request.getHeader(CorrelationIdFilter.HEADER_NAME);
            if (header != null && !header.isBlank()) {
                return header;
            }
        }
        return UUID.randomUUID().toString();
    }
}
