package pro.akosarev.sandbox.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.SpringVersion;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class KeycloakController {

    @GetMapping("/account")
    public String accountKeycloak(HttpServletRequest request, HttpServletResponse response) throws IOException{

        response.sendRedirect("http://localhost:8282/realms/ecatalog_realm/account/");
        return null;
    }

    @GetMapping("/exit")
    public String logoutKeycloak(HttpServletRequest request, HttpServletResponse response) throws IOException {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {

            session.invalidate();

            String keycloakLogoutUrl = "http://localhost:8282/realms/ecatalog_realm/protocol/openid-connect/logout";
            response.sendRedirect(keycloakLogoutUrl);
        } else {
            response.sendRedirect("http://localhost:8080/index.html");
        }

        return null;
    }
}
