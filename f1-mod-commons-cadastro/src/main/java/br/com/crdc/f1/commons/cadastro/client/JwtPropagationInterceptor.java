package br.com.crdc.f1.commons.cadastro.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * {@link ClientHttpRequestInterceptor} that forwards the caller's {@code Authorization}
 * header from the current inbound HTTP request into the outbound call to the Cadastro.
 */
public class JwtPropagationInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtPropagationInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String authHeader = currentAuthorizationHeader();
        if (authHeader != null && !authHeader.isBlank()) {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, authHeader);
            log.debug("Cadastro call: Authorization header propagated (length={})",
                    authHeader.length());
        } else {
            log.debug("Cadastro call: no Authorization header found in the inbound request");
        }
        return execution.execute(request, body);
    }

    String currentAuthorizationHeader() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            return null;
        }
        HttpServletRequest inbound = servletAttrs.getRequest();
        return inbound.getHeader(HttpHeaders.AUTHORIZATION);
    }
}
