package com.lemon.spring.aop.audit;

import com.lemon.spring.annotation.AutoAudit;
import com.lemon.spring.component.audit.AuditAware;
import com.lemon.spring.domain.internal.AbstractAudit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.util.Iterator;

@Configuration
@Aspect
public class AuditAwareAspect {
    @Inject
    private AuditAware auditAware;

    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository.save(..))")
    public void repositorySaveCallPointcut() {
    }

    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository.saveAll(..))")
    public void repositorySaveAllCallPointcut() {
    }

    @Pointcut("execution(* com.lemon.framework.orm.capture.AbstractCapture.persist(..)) || execution(* com.lemon.framework.orm.capture.AbstractCapture.save(..))")
    public void hibernateSessionSaveCall() {
    }

    @Pointcut("execution(* org.springframework.jdbc.core.JdbcTemplate.update(..)) || execution(* org.springframework.jdbc.core.JdbcTemplate.batchUpdate(..))")
    public void jdbcTemplateSaveOperation() {
    }

    @Pointcut("repositorySaveCallPointcut() || hibernateSessionSaveCall()")
    public void allPointcut() {
    }

    @Before("allPointcut()")
    public void auditForSave(JoinPoint joinPoint) throws Throwable {
        Object obj = joinPoint.getArgs()[0];
        if (isAuditAble(obj))
            auditAware.aware((AbstractAudit) obj, obj.getClass().getAnnotation(AutoAudit.class));
    }

    @SuppressWarnings("unchecked")
    @Before("repositorySaveAllCallPointcut()")
    public void auditForSaveAll(JoinPoint joinPoint) throws Throwable {
        Iterable<Object> objects = (Iterable<Object>) joinPoint.getArgs()[0];
        Iterator<Object> objectIterator = objects.iterator();
        if (objectIterator.hasNext()) {
            Object obj = objectIterator.next();
            if (isAuditAble(obj)) {
                auditAware.aware((AbstractAudit) obj, obj.getClass().getAnnotation(AutoAudit.class));
                while (objectIterator.hasNext()) {
                    Object obj2 = objectIterator.next();
                    auditAware.aware((AbstractAudit) obj2, obj2.getClass().getAnnotation(AutoAudit.class));
                }
            }
        }
    }

    private boolean isAuditAble(Object obj) {
        return obj instanceof AbstractAudit && obj.getClass().isAnnotationPresent(AutoAudit.class);
    }
}
