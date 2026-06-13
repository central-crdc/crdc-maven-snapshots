package br.com.crdc.f1.commons.audit;
import java.lang.annotation.*;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String action();
    String entityType() default "";
    boolean capturePayload() default false;
}
