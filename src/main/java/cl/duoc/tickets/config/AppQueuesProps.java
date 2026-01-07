package cl.duoc.tickets.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.queues")
public class AppQueuesProps {
    private String ok;
    private String error;
}
