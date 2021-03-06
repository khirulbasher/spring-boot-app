package com.lemon.spring.component.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lemon on 10/1/18.
 */

@SuppressWarnings({"unused", "DefaultFileTemplate"})
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private final Logger log = LogManager.getLogger(CustomLogoutSuccessHandler.class);

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        log.debug("Log-Out Success:--->");
    }
}
