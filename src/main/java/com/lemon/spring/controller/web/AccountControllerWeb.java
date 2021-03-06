package com.lemon.spring.controller.web;

import com.lemon.framework.protocolservice.auth.AccountService;
import com.lemon.framework.security.auth.AuthorizationBridge;
import com.lemon.spring.config.Constants;
import com.lemon.spring.domain.internal.User;
import com.lemon.spring.interfaces.impl.AbstractViewController;
import com.lemon.spring.repository.AuthorityRepository;
import com.lemon.spring.repository.UserRepository;
import com.lemon.spring.security.AuthoritiesConstant;
import com.lemon.spring.security.SecurityUtils;
import com.lemon.spring.service.account.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.math.BigInteger;

import static com.lemon.spring.interfaces.ViewController.PRIVATE_VIEW;

@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "Duplicates"})
@Controller
@Profile(value = {Constants.PROFILE_STATEFUL, Constants.PROFILE_BOTH})
@RequestMapping(PRIVATE_VIEW)
public class AccountControllerWeb extends AbstractViewController<User, BigInteger> {
    public static final String BASE_PATH = "/account-controller";
    public static final String FULL_PATH = PRIVATE_VIEW + BASE_PATH;
    private static final String BASE_VIEW = "/account";
    private final Logger log = LogManager.getLogger(AccountControllerWeb.class);

    @Inject
    private AccountService accountService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuthorityRepository authorityRepository;

    @Inject
    private AuthorizationBridge authorizationBridge;

    @Inject
    private UserService userService;


    @GetMapping(BASE_PATH + "/home")
    @Override
    public String home() {
        if (authorizationBridge.hasAnyAuthority(AuthoritiesConstant.ROLES_FOR_ADMIN))
            return super.home();
        return view("/profile");
    }

    @GetMapping(value = BASE_PATH + "/save-entry")
    @Override
    public String saveEntry(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("authorities", authorityRepository.findAll());
        return super.saveEntry(model);
    }

    @PostMapping(value = BASE_PATH)
    @Override
    public String save(@ModelAttribute User user) {
        if (user.getId() == null)
            accountService.register(user);
        else return update(user);
        log.debug("Successfully Registered...");
        return super.save(user);
    }

    @PreAuthorize("hasPermission('USER','IS_ADMIN')")
    @GetMapping(value = BASE_PATH + "/update-entry/{id}")
    @Override
    public String updateEntry(Model model, @PathVariable BigInteger id) {
        User user = userService.userForUpdate(id);
        model.addAttribute("user", user);
        model.addAttribute("authorities", authorityRepository.findAll());
        return super.updateEntry(model, id);
    }


    @GetMapping(value = BASE_PATH + "/update-entry/me")
    public String updateEntry(Model model) {
        return updateEntry(model, SecurityUtils.currentUserId());
    }

    @PreAuthorize("#user.username==principal.username OR hasPermission(#user,'IS_ADMIN')")
    @PostMapping(value = BASE_PATH + "/update")
    @Override
    public String update(@ModelAttribute User user) {
        userService.updateUser(user);
        return super.update(user);
    }

    @PreAuthorize("@auth.isAdmin()")
    @GetMapping(BASE_PATH)
    @Override
    public String findAll(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return super.findAll(model);
    }

    @PreAuthorize("@auth.isAdmin()")
    @GetMapping(BASE_PATH + "/delete/{id}")
    @Override
    public String delete(@PathVariable BigInteger id) {
        userRepository.delete(new User(id));
        return home();
    }

    @Override
    public String baseView() {
        return BASE_VIEW;
    }

    @Override
    public String basePath() {
        return BASE_PATH;
    }


    @GetMapping(value = BASE_PATH + "/profile")
    public String profile() {
        return home();
    }

    @GetMapping(value = BASE_PATH + "/login")
    public String login(Model model, @RequestParam(name = "error", required = false) String error, @RequestParam(name = "logout", required = false) String logout) {
        return loginPage();
    }

    private String loginPage() {
        return view("/login");
    }
}
