package com.kl.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.jwt")
@Component
@Data
public class JwtProperties {

    /**
     * JWT signing secret.
     * Keep this out of source control in real environments.
     */
    private String secret;

    /**
     * Token expiration time in milliseconds.
     * Example: 604800000 = 7 days
     */
    private long expirationMillis;

    /**
     * HTTP header used to carry the token.
     */
    private String header;
}
