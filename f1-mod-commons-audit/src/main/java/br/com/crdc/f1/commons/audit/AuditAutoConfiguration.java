package br.com.crdc.f1.commons.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.firehose.FirehoseAsyncClient;

@AutoConfiguration
@ConditionalOnProperty(prefix = "crdc.audit", name = "enabled", havingValue = "true")
public class AuditAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    public ObjectMapper auditObjectMapper() { return new ObjectMapper(); }

    @Bean @ConditionalOnMissingBean @ConditionalOnBean(FirehoseAsyncClient.class)
    @ConditionalOnProperty(prefix = "crdc.audit.firehose", name = "stream-name")
    public FirehoseAuditPublisher firehoseAuditPublisher(FirehoseAsyncClient firehose,
            ObjectMapper auditObjectMapper, @Value("${crdc.audit.firehose.stream-name}") String streamName) {
        return new FirehoseAuditPublisher(firehose, auditObjectMapper, streamName);
    }

    @Bean @ConditionalOnMissingBean(AuditPublisher.class)
    public AuditPublisher noOpAuditPublisher() { return new NoOpAuditPublisher(); }

    @Bean @ConditionalOnMissingBean
    public AuditAspect auditAspect(AuditPublisher auditPublisher,
            @Value("${spring.application.name:unknown}") String serviceName) {
        return new AuditAspect(auditPublisher, serviceName);
    }
}
