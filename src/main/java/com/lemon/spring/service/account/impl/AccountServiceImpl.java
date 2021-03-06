package com.lemon.spring.service.account.impl;

import com.lemon.framework.data.JWToken;
import com.lemon.framework.data.LogoutInfo;
import com.lemon.framework.data.UserInfo;
import com.lemon.framework.orm.capture.hbm.HbmCapture;
import com.lemon.framework.properties.spring.ApplicationProperties;
import com.lemon.framework.protocolservice.auth.AccountService;
import com.lemon.framework.springsecurity.jwt.JwtAuthManager;
import com.lemon.framework.springsecurity.session.SessionAuthManager;
import com.lemon.framework.statistics.generator.random.impl.RandomString;
import com.lemon.spring.domain.internal.Authority;
import com.lemon.spring.domain.internal.PasswordRecover;
import com.lemon.spring.domain.internal.User;
import com.lemon.spring.repository.PasswordRecoverRepository;
import com.lemon.spring.repository.UserRepository;
import com.lemon.spring.security.SecurityUtils;
import com.lemon.spring.service.ApplicationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"unchecked", "JpaQlInspection", "SpringJavaAutowiredFieldsWarningInspection"})
@Service
public class AccountServiceImpl implements AccountService<BigInteger> {

    private final Logger log = LogManager.getLogger(AccountServiceImpl.class);

    @Inject
    private RandomString randomString;

    @Inject
    private HbmCapture hbmCapture;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JwtAuthManager jwtAuthManager;

    @Autowired(required = false)
    private SessionAuthManager sessionAuthManager;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private UserRepository userRepository;

    @Inject
    private JavaMailSender mailSender;

    @Inject
    private ApplicationProperties properties;

    @Inject
    private PasswordRecoverRepository passwordRecoverRepository;

    @Inject
    private ApplicationService applicationService;

    @Override
    public String currentUsername() {
        return SecurityUtils.currentUserLogin();
    }

    @Override
    public BigInteger currentUserId() {
        return SecurityUtils.currentUserId();
    }


    @Override
    public void register(Object userObj) {
        User user = (User) userObj;
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Authority> authorities = new HashSet<>();
        user.getAuthorities().forEach(v -> {
            if (hbmCapture.findOne("SELECT auth FROM Authority auth WHERE auth.name='" + v.getName() + "'") == null) {
                hbmCapture.save(v);
            }
            authorities.add(v);
        });
        user.setAuthorities(authorities);
        hbmCapture.save(user);
    }

    @Override
    public void passwordRecover(String userEmail) {
        if (!properties.settings.security.account.password.recoverable) {
            log.debug("Password Recovery Mode is Deactivated");
            return;
        }
        User user = userRepository.findOneByEmail(userEmail);
        if (user != null) {
            String code = randomString.generate();

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(userEmail);
            mailMessage.setSubject(properties.settings.general.name + " Account Recovery");
            mailMessage.setText("Hey " + user.getFullName() + "\nYour Account Password Recovery Code is:\n\t[ " + code + " ]\n\nBest Regards\nThe " + properties.settings.general.name + " Team\nThank You");
            mailSender.send(mailMessage);

            PasswordRecover recover = passwordRecoverRepository.findByUser(user);
            if (recover == null) {
                recover = new PasswordRecover();
                recover.setUser(user);
            } else {
                recover.setActive(true);
            }
            recover.setLastAccessDate(LocalDate.now());
            recover.setCreateTime(LocalDateTime.now());
            recover.setRecoveryCode(code);
            recover.setWrongAccessCount(0L);

            passwordRecoverRepository.save(recover);
        }
    }

    @Override
    public boolean passwordRecover(String userEmail, String password, String recoveryCode) {
        if (!properties.settings.security.account.password.recoverable) {
            log.debug("Password Recovery Mode is Deactivated");
            return false;
        }
        boolean result = false;
        User user = userRepository.findOneByEmail(userEmail);
        if (user != null) {
            PasswordRecover recover = passwordRecoverRepository.findByUser(user);
            if (recover != null) {
                if (recoverable(recover, recoveryCode)) {
                    int diff = LocalDateTime.now().getMinute() - recover.getCreateTime().getMinute();
                    if (diff <= properties.settings.security.account.password.recoveryTimeoutMinutes) {
                        user.setPassword(passwordEncoder.encode(password));
                        userRepository.save(user);
                        result = true;
                    }
                }
                recover.increaseAccessCodeCount();
                recover.setActive(false);
                passwordRecoverRepository.save(recover);
            }

        }
        return result;
    }

    private boolean recoverable(PasswordRecover recover, String recoveryCode) {
        return recover.isActive() &&
                recover.getWrongAccessCount() <= properties.settings.security.account.password.recoveryLimitationForWrongCode &&
                applicationService.isToday(recover.getCreateTime()) &&
                recover.getRecoveryCode().equals(recoveryCode);
    }

    @Override
    public Set<String> authorities() {
        return new HashSet<>(jdbcTemplate.query("SELECT AUTHORITY_NAME FROM AUTHORITY", (rs, rowNum) -> rs.getString(1)));
    }

    @Override
    public JWToken authenticate(UserInfo userInfo) {
        /*If The App is Stateful-Only*/
        if (jwtAuthManager == null) {
            sessionAuthManager.authenticate(userInfo);
            return new JWToken(""); /*Ensure The caller side that the login is success, that's why we need to send non-null object*/
        }
        try {
            /*If The app stateless or both. Cause A stateless App doesn't create session-id, and if it is stateless and stateful then it create session automatically and create token also*/
            return jwtAuthManager.authenticate(userInfo).token();
        } catch (NullPointerException e) {
            log.error("May-Be your Application is Not Enabled For Token-Based Authentication", e);
            throw new SecurityException("May-Be your Application is Not Enabled For Token-Based Authentication");
        }
    }

    @Override
    public void logout(LogoutInfo logoutInfo) {
        SecurityContextHolder.clearContext();
        if (logoutInfo == null) return;
        logoutInfo.setUserId(SecurityUtils.currentUserId());

        if (jwtAuthManager != null)
            jwtAuthManager.logout(logoutInfo);
        sessionAuthManager.logout(logoutInfo);
    }


}
