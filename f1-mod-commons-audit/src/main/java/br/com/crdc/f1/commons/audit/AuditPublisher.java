package br.com.crdc.f1.commons.audit;
public interface AuditPublisher {
    void publish(AuditEvent event);
}
