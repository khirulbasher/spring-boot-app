package com.lemon.spring.component.audit.impl;

import com.lemon.framework.security.auth.AuthorizationBridge;
import com.lemon.spring.annotation.AutoAudit;
import com.lemon.spring.component.audit.AuditAware;
import com.lemon.spring.domain.AbstractAudit;
import com.lemon.spring.domain.User;
import com.lemon.spring.repository.UserRepository;
import com.lemon.spring.security.AuthoritiesConstant;
import com.lemon.spring.security.SecurityUtils;
import com.lemon.spring.web.page.PageImpl;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;

@Component
public class AuditAwareImpl implements AuditAware {
    @Inject
    private UserRepository userRepository;

    @Inject
    private AuthorizationBridge<BigInteger> authorizationBridge;


    @Override
    public void aware(AbstractAudit audit, AutoAudit autoAudit) {
        if(!audit.isUpdate())awareCreate(audit,autoAudit);
        else awareUpdate(audit,autoAudit);
    }

    @Override
    public void awareCreate(AbstractAudit audit,AutoAudit autoAudit) {
        audit.setCreateBy(new User(findCreatorId()));
        audit.setCreateDate(LocalDate.now());
        switch (autoAudit.autoActive()) {
            case ALWAYS_ACTIVE:
            case ACTIVE_ON_CREATE:
                audit.setActive(true);
                break;
            case ALWAYS_INACTIVE:
            case INACTIVE_ON_CREATE:
                audit.setActive(false);
                break;
            case ACTIVE_IF_HAS_ANY_ROLE:
            case ACTIVE_IF_HAS_ANY_ROLE_ON_CREATE:
                audit.setActive(authorizationBridge.hasAnyAuthority(autoAudit.activeInactiveRole()));
                break;
            case ACTIVE_IF_HAS_ROLE:
            case ACTIVE_IF_HAS_ROLE_ON_CREATE:
                audit.setActive(authorizationBridge.hasAuthority(autoAudit.activeInactiveRole()));
                break;
            case ACTIVE_IF_HAS_NO_ANY_ROLE:
            case ACTIVE_IF_HAS_NO_ANY_ROLE_ON_CREATE:
                audit.setActive(authorizationBridge.hasNoAnyAuthority(autoAudit.activeInactiveRole()));
                break;
            case ACTIVE_IF_HAS_NO_ROLE:
            case ACTIVE_IF_HAS_NO_ROLE_ON_CREATE:
                audit.setActive(authorizationBridge.hasNoAuthority(autoAudit.activeInactiveRole()));
                break;
            case INACTIVE_IF_HAS_ANY_ROLE:
            case INACTIVE_IF_HAS_ANY_ROLE_ON_CREATE:
                audit.setActive(!authorizationBridge.hasAnyAuthority(autoAudit.activeInactiveRole()));
                break;
            case INACTIVE_IF_HAS_ROLE:
            case INACTIVE_IF_HAS_ROLE_ON_CREATE:
                audit.setActive(!authorizationBridge.hasAuthority(autoAudit.activeInactiveRole()));
                break;
            case INACTIVE_IF_HAS_NO_ANY_ROLE:
            case INACTIVE_IF_HAS_NO_ANY_ROLE_ON_CREATE:
                audit.setActive(!authorizationBridge.hasNoAnyAuthority(autoAudit.activeInactiveRole()));
                break;
            case INACTIVE_IF_HAS_NO_ROLE:
            case INACTIVE_IF_HAS_NO_ROLE_ON_CREATE:
                audit.setActive(!authorizationBridge.hasNoAuthority(autoAudit.activeInactiveRole()));
                break;
        }
    }

    @Override
    public void awareUpdate(AbstractAudit audit,AutoAudit autoAudit) {
        audit.setUpdateBy(new User(findCreatorId()));
        audit.setUpdateDate(LocalDate.now());
        switch (autoAudit.autoActive()) {
            case ALWAYS_ACTIVE:
            case ACTIVE_ON_UPDATE:
                audit.setActive(true);
                break;
            case ALWAYS_INACTIVE:
            case INACTIVE_ON_UPDATE:
                audit.setActive(false);
                break;
            case ACTIVE_IF_HAS_ANY_ROLE:
            case ACTIVE_IF_HAS_ANY_ROLE_ON_UPDATE:
                audit.setActive(authorizationBridge.hasAnyAuthority(autoAudit.activeInactiveRole()));
                break;
            case ACTIVE_IF_HAS_ROLE:
            case ACTIVE_IF_HAS_ROLE_ON_UPDATE:
                audit.setActive(authorizationBridge.hasAuthority(autoAudit.activeInactiveRole()));
                break;
            case ACTIVE_IF_HAS_NO_ANY_ROLE:
            case ACTIVE_IF_HAS_NO_ANY_ROLE_ON_UPDATE:
                audit.setActive(authorizationBridge.hasNoAnyAuthority(autoAudit.activeInactiveRole()));
                break;
            case ACTIVE_IF_HAS_NO_ROLE:
            case ACTIVE_IF_HAS_NO_ROLE_ON_UPDATE:
                audit.setActive(authorizationBridge.hasNoAuthority(autoAudit.activeInactiveRole()));
                break;
            case INACTIVE_IF_HAS_ANY_ROLE:
            case INACTIVE_IF_HAS_ANY_ROLE_ON_UPDATE:
                audit.setActive(!authorizationBridge.hasAnyAuthority(autoAudit.activeInactiveRole()));
                break;
            case INACTIVE_IF_HAS_ROLE:
            case INACTIVE_IF_HAS_ROLE_ON_UPDATE:
                audit.setActive(!authorizationBridge.hasAuthority(autoAudit.activeInactiveRole()));
                break;
            case INACTIVE_IF_HAS_NO_ANY_ROLE:
            case INACTIVE_IF_HAS_NO_ANY_ROLE_ON_UPDATE:
                audit.setActive(!authorizationBridge.hasNoAnyAuthority(autoAudit.activeInactiveRole()));
                break;
            case INACTIVE_IF_HAS_NO_ROLE:
            case INACTIVE_IF_HAS_NO_ROLE_ON_UPDATE:
                audit.setActive(!authorizationBridge.hasNoAuthority(autoAudit.activeInactiveRole()));
                break;
        }
    }

    private BigInteger findCreatorId() {
        try {
            return SecurityUtils.currentUserId();
        } catch (Throwable e) {
        }
        try {
            return userRepository.findOneByUsername("system").getId();
        } catch (Throwable e) {
        }
        try {
            return userRepository.findAllByAuthority(new PageImpl(1), AuthoritiesConstant.ROLE_ADMIN).get(0).getId();
        } catch (Throwable e) {
        }
        return userRepository.findAll(new PageImpl(1)).getContent().get(0).getId();

    }
}
