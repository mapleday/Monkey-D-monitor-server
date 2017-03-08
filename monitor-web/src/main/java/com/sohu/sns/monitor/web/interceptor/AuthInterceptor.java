package com.sohu.sns.monitor.web.interceptor;


import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.sso.client.SSO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;


/**
 * @author zhouhe
 * @since 上午12:13:08
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private static final JsonMapper jsonMapper = JsonMapper.nonEmptyMapper();
    private UrlPathHelper helper = new UrlPathHelper();
    private PathMatcher pathMatcher = new AntPathMatcher();
    private List<String> whiteLists;

    @Override
    public void afterCompletion(HttpServletRequest req,
                                HttpServletResponse resp, Object obj, Exception e)
            throws Exception {
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse resp,
                           Object obj, ModelAndView mav) throws Exception {
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp,
                             Object obj) throws Exception {
        // 登录拦截
//        String uri = helper.getLookupPathForRequest(req);
//        for (String res : whiteLists) {
//            if (pathMatcher.match(res, uri)) {
//                return true;
//            }
//        }
        // 登录拦截
        Principal p = req.getUserPrincipal();

        if (p == null) {
            resp.sendRedirect(SSO.getProtocol() + "://" + SSO.getSsoServer() + "/auth");
            return false;
        }
        HttpSession session = req.getSession();
        Boolean loginState=(Boolean) session.getAttribute("loginState");
        if (loginState==null){
            session.setAttribute("loginState",true);
            loginState=true;
        }

        if (session != null&&loginState!=true) {
//            Operator operator = (Operator) session.getAttribute("operator");
//            if (operator == null) {
//                operator = operatorServiceImpl.get(p.getName());
//                if (operator == null) {
                    session.setAttribute("loginState",null);
                    resp.sendRedirect(SSO.getProtocol() + "://" + SSO.getSsoServer() + "/auth");
                    return false;
//                }
//                log.info(jsonMapper.toJson(operator));
//                session.setAttribute("operator", operator);
//            }


        }
        return true;
    }

    public List<String> getWhiteLists() {
        return whiteLists;
    }

    public void setWhiteLists(List<String> whiteLists) {
        this.whiteLists = whiteLists;
    }
}
