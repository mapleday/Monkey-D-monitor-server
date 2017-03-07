package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.services.SnsWebUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Created by yw on 2017/2/20.
 */
@Controller
public class LoginCotroller {

    @Autowired
    SnsWebUserService snsWebUserService;
//    private Integer flag=null;

    @RequestMapping(value = "/login")
    public String doLogin(HttpServletRequest request, Model model)
    {
//        if (null==flag){
//        StringBuffer url=request.getRequestURL();
//        String URL=url.delete(url.length()-request.getRequestURI().length(),url.length()).toString();
//        String result[]=URL.split("/");
//        SelectPersonDutyService.lock=result[2].split(":")[0];
//        System.out.println(result[2].split(":")[0]);
//        flag=1;
//        }

        System.out.println("----捕获！---");
        String userName=request.getParameter("userName");
        String password=request.getParameter("password");
        if (userName==null)
            return "login";
        UsernamePasswordToken token=new UsernamePasswordToken(userName,password,false);
        Subject subject= SecurityUtils.getSubject();
        if (subject!=null&&subject.isAuthenticated()){
            boolean isAuthorized=Boolean.valueOf( subject.getSession().getAttribute("isAuthorized").toString());
            if (isAuthorized){
                System.out.println("---已获得认证！---");
                return  "redirect:/index";
            }
        }
        String msg;
        try{
            subject.login(token);
            //通过认证
            if (subject.isAuthenticated()) {
                Set<String> roles = snsWebUserService.getRole(userName);
                if (!roles.isEmpty()) {
//                subject.getSession().setAttribute("isAuthorized", true);
                    System.out.println("已授权+token  "+token.getPrincipal()+"roles:"+roles);
                    return "redirect:/index";
                } else {//没有授权
                    msg = "您没有得到相应的授权！";
                    model.addAttribute("message", msg);
                    System.out.println(msg+"yyy");
//                subject.getSession().setAttribute("isAuthorized", false);
                    return "login";
                }}

            //0 未授权 1 账号问题 2 密码错误  3 账号密码错误
        }catch (IncorrectCredentialsException e) {
            msg = "登录密码错误. Password for account " + token.getPrincipal() + " was incorrect";
            model.addAttribute("message", msg);
        } catch (ExcessiveAttemptsException e) {
            msg = "登录失败次数过多";
            model.addAttribute("message", msg);
        } catch (LockedAccountException e) {
            msg = "帐号已被锁定. The account for username " + token.getPrincipal() + " was locked.";
            model.addAttribute("message", msg);
        } catch (DisabledAccountException e) {
            msg = "帐号已被禁用. The account for username " + token.getPrincipal() + " was disabled.";
            model.addAttribute("message", msg);
        } catch (ExpiredCredentialsException e) {
            msg = "帐号已过期. the account for username " + token.getPrincipal() + "  was expired.";
            model.addAttribute("message", msg);
        } catch (UnknownAccountException e) {
            msg = "帐号不存在. There is no user with username of " + token.getPrincipal();
            model.addAttribute("message", msg);
        } catch (UnauthorizedException e) {
            msg = "您没有得到相应的授权！" + e.getMessage();
            model.addAttribute("message", msg);
        }
        return "login";
    }

    @RequestMapping(value = "/logout")
    public String doLogout(HttpServletRequest request,Model model){
        model.addAttribute("oAuth",new Object());
        Subject subject=SecurityUtils.getSubject();
        System.out.println("清楚认证信息！");
        subject.logout();
        System.out.println("已注销！"+subject.isAuthenticated());
        return "redirect:login";
    }
}
