package com.bfsforum.apigateway.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

/** This filter is used to validate the JWT token in the cookie. */
@Component
public class JwtCookieFilter implements WebFilter {

    private final ReactiveJwtDecoder jwtDecoder;

    @Value("${security.cookie.name:token}")
    private String cookieName;

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    // Define the paths to exclude from cookie validation
    private final List<String> excludedPaths = Arrays.asList(
            "/auth/*",
            "/users/register",
            "/messages",
            "/files/upload",
            "/files/download"
    );
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    public JwtCookieFilter(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Check if the request path is in the excluded list
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod requestMethod = request.getMethod();
        boolean isExcluded = (requestMethod == HttpMethod.POST) &&
            excludedPaths.stream()
                .anyMatch(pattern -> pathPatternParser.parse(pattern).matches(exchange.getRequest().getPath()));
        if (isExcluded) {
            return chain.filter(exchange);
        }

        // If the path is NOT excluded, proceed with cookie validation
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(cookieName);
        if (cookie == null) {
            return chain.filter(exchange);
        }
        String token = cookie.getValue();

        return jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    String userId = jwt.getClaimAsString("userId");
                    String role = jwt.getClaimAsString("role");

                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header(HEADER_USER_ID, userId)
                            .header(HEADER_USER_ROLE, role)
                            .build();
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(ex -> {
                    System.err.println("JWT Decoding or Validation Failed: " + ex.getMessage());
                    return Mono.error(new BadCredentialsException("Invalid or expired JWT token", ex));
                });
    }
}

