package com.lemon.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import static com.lemon.spring.config.Constants.*;

@SuppressWarnings("SpellCheckingInspection")
@SpringBootApplication
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    public static ApplicationContext applicationContext;
    @Inject
    private Environment env;

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication application = new SpringApplication(Application.class);
        SimpleCommandLinePropertySource propertySource = new SimpleCommandLinePropertySource(args);
        addDefaultProfile(application, propertySource);
        applicationContext = application.run(args);
        Environment env = applicationContext.getEnvironment();
        log.info("Access URLs:\n----------------------------------------------------------\n\t" +
                        "Local: \t\thttp://127.0.0.1:{}\n\t" +
                        "External: \thttp://{}:{}\n----------------------------------------------------------",
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"));

    }

    private static void addDefaultProfile(SpringApplication application, SimpleCommandLinePropertySource propertySource) {
        /*if (!propertySource.containsProperty("spring.profiles.active") &&
                !System.getenv().containsKey("SPRING_PROFILES_ACTIVE")) {

            //application.setAdditionalProfiles(PROFILE_DEVELOPMENT);
        }*/
    }

    @PostConstruct
    public void construct() {
        if (env.getActiveProfiles().length == 0) {
            log.warn("No Spring profile configured, running with default configuration");
        } else {
            log.info("Running with Spring profile(s) : {}", Arrays.toString(env.getActiveProfiles()));
            Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
            if (activeProfiles.contains(PROFILE_DEVELOPMENT) && activeProfiles.contains(PROFILE_PRODUCTION)) {
                log.error("You have misconfigured your application! " +
                        "It should not run with both the 'dev' and 'prod' profiles at the same time.");
            }
            if (activeProfiles.contains(PROFILE_PRODUCTION) && activeProfiles.contains(PROFILE_FAST)) {
                log.error("You have misconfigured your application! " +
                        "It should not run with both the 'prod' and 'fast' profiles at the same time.");
            }
            if (activeProfiles.contains(PROFILE_DEVELOPMENT) && activeProfiles.contains(PROFILE_CLOUD)) {
                log.error("You have misconfigured your application! " +
                        "It should not run with both the 'dev' and 'cloud' profiles at the same time.");
            }
        }

    }
}
