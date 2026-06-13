package br.com.crdc.f1.commons.audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
public class NoOpAuditPublisher implements AuditPublisher {
    private static final Logger log = LoggerFactory.getLogger(NoOpAuditPublisher.class);
    @Override
    public void publish(AuditEvent event) {
        Objects.requireNonNull(event, "event");
        if (log.isDebugEnabled()) log.debug("audit event discarded (NoOp publisher): type={} entity={}/{}",
            event.eventType(), event.entity() != null ? event.entity().type() : "-", event.entity() != null ? event.entity().id() : "-");
    }
}
