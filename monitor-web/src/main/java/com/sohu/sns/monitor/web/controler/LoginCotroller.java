package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.services.SnsWebUserService;
import com.sohu.sns.sso.client.AuthFilter;
import com.sohu.sns.sso.client.SSO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

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
