package br.com.crdc.f1.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Aspect
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditPublisher publisher;
    private final String serviceName;

    public AuditAspect(AuditPublisher publisher, String serviceName) {
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
    }

    @Around("@annotation(audited)")
    public Object around(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        Object result = pjp.proceed();
        try {
            AuditEvent event = AuditEvent.of(audited.action(), extractActor(),
                extractEntity(audited, result),
                audited.capturePayload() ? capturePayload(pjp) : Map.of(),
                MDC.get("traceId"), serviceName);
            publishAfterCommit(event);
        } catch (Exception ex) {
            log.warn("audit event emission failed for action={}: {}", audited.action(), ex.getMessage());
        }
        return result;
    }

    private void publishAfterCommit(AuditEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { publisher.publish(event); }
            });
        } else {
            publisher.publish(event);
        }
    }

    private AuditEvent.Actor extractActor() {
        return new AuditEvent.Actor(MDC.get("actor.sub"), MDC.get("actor.email"), MDC.get("actor.ip"));
    }

    private AuditEvent.Entity extractEntity(Audited audited, Object result) {
        String type = audited.entityType();
        if (type == null || type.isEmpty()) return null;
        String id = null;
        if (result != null) {
            try {
                var getId = result.getClass().getMethod("getId");
                Object value = getId.invoke(result);
                if (value != null) id = value.toString();
            } catch (ReflectiveOperationException ignored) {}
        }
        return new AuditEvent.Entity(type, id, null);
    }

    private Map<String, Object> capturePayload(ProceedingJoinPoint pjp) {
        Signature signature = pjp.getSignature();
        if (!(signature instanceof MethodSignature sig)) return Map.of();
        String[] paramNames = sig.getParameterNames();
        Object[] args = pjp.getArgs();
        if (paramNames == null || paramNames.length == 0) return Map.of();
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < paramNames.length; i++) map.put(paramNames[i], args[i]);
        return map;
    }
}
