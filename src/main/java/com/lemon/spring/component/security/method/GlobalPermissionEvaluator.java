package com.lemon.spring.component.security.method;

import com.lemon.spring.domain.internal.Authority;
import com.lemon.spring.security.AuthoritiesConstant;
import com.lemon.spring.security.SecurityUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

@SuppressWarnings("WeakerAccess")
public class GlobalPermissionEvaluator implements PermissionEvaluator {
    public static final String READ = "READ";
    public static final String WRITE = "WRITE";
    public static final String IS_ADMIN = "IS_ADMIN";

    /**
     * permission is the permission type of like hasPermission(,'READ')
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object permission) {
        switch (permission.toString()) {
            case READ:
                return true;
            case WRITE:
                return target instanceof Authority;
            case IS_ADMIN:
                return SecurityUtils.hasAnyAuthority(AuthoritiesConstant.ROLES_FOR_ADMIN);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
