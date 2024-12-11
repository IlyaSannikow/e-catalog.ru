package pro.akosarev.sandbox.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class RedirectController {
    @GetMapping("admin")
    public String getManagerPage() {
        return "admin";
    }

    @GetMapping("registration")
    public String getRegistrationPage() {
        return "registration";
    }

    @GetMapping("profile")
    public String getProfilePage() {
        return "profile";
    }

}
