package com.example.limits.config;

import com.example.limits.config.enums.MissBehavior;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "limits.strategy")
@Getter
@Setter
public class LimitsProperties {
    private MissBehavior missBehavior = MissBehavior.USE_DEFAULT;
}
