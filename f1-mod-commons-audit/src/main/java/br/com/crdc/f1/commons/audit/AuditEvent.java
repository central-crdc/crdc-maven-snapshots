package br.com.crdc.f1.commons.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEvent(String eventId, String eventType, Actor actor, Entity entity,
        Map<String, Object> payload, Instant timestamp, String correlationId, String service, String version) {
    public static final String SCHEMA_VERSION = "1";
    public record Actor(String sub, String email, String ip) {}
    public record Entity(String type, String id, String schema) {}
    public static AuditEvent of(String eventType, Actor actor, Entity entity,
            Map<String, Object> payload, String correlationId, String service) {
        return new AuditEvent(UUID.randomUUID().toString(), eventType, actor, entity,
            payload, Instant.now(), correlationId, service, SCHEMA_VERSION);
    }
}
