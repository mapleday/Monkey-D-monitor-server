package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.sso.client.SSO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by yw on 2017/2/20.
 */
@Controller
public class LoginCotroller {
    @RequestMapping(value = "/logout")
    public void doLogout(HttpServletRequest req, HttpServletResponse resp,Model model) throws IOException {
        resp.sendRedirect(SSO.getProtocol() + "://" + SSO.getSsoServer() + "/logout");
    }
}
