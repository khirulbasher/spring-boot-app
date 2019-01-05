package com.lemon.spring.controller.web;

import com.lemon.spring.config.Constants;
import com.lemon.spring.domain.User;
import com.lemon.spring.interfaces.ViewController;
import com.lemon.spring.service.account.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;

@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "Duplicates"})
@Controller
@Profile(value = {Constants.PROFILE_STATEFUL,Constants.PROFILE_BOTH})
@RequestMapping("/web")
public class AccountControllerWeb implements ViewController<User> {
    private final Logger log= LogManager.getLogger(AccountControllerWeb.class);


    @Inject
    private AccountService accountService;

    public static final String BASE_PATH="/account-controller";

    @GetMapping(value = BASE_PATH+"/register")
    public String register(Model model) {
        model.addAttribute("user",new User());
        model.addAttribute("authorities",accountService.authorities());
        return "account/register";
    }

    @PostMapping(value = BASE_PATH)
    public String save(@ModelAttribute User user) {
        accountService.register(user);
        log.debug("Successfully Registered...");
        return "account/login";
    }

    @Override
    public String update(User entity) {
        return null;
    }

    @Override
    public String findOne(BigInteger id) {
        return null;
    }

    @Override
    public String findAll() {
        return null;
    }

    @Override
    public String delete(BigInteger id) {
        return null;
    }

    @GetMapping(value = BASE_PATH+"/profile")
    public String profile(HttpServletResponse httpServletResponse) {
        return "account/profile";
    }

    @GetMapping(value = BASE_PATH+"/login")
    public String login(Model model, @RequestParam(name = "error",required = false) String error,@RequestParam(name = "logout",required = false) String logout) {
        return "account/login";
    }
}
