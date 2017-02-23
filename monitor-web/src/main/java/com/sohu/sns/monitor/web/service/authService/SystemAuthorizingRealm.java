package com.sohu.sns.monitor.web.service.authService;

import com.sohu.sns.common.model.User;
import com.sohu.sns.monitor.common.module.SnsWebUser;
import com.sohu.sns.monitor.common.services.SnsWebUserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Created by yw on 2017/2/19.
 * 系统安全认证
 */

@Service
public class SystemAuthorizingRealm extends AuthorizingRealm{
    @Autowired SnsWebUserService snsWebUserService;

    /**
     * 用户认证
     *
     * @param authenticationToken 含登录名密码的信息
     * @return 认证信息
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        if(authenticationToken==null){
            System.out.println("parameter token is null");
            throw new AuthenticationException("parameter token is null");
        }
        UsernamePasswordToken token=(UsernamePasswordToken)authenticationToken;
        String username=token.getUsername();
        String password=String.valueOf(token.getPassword());
        System.out.println(password+"yyyttt");
        List<SnsWebUser> users= snsWebUserService.getSnsWebUser(token.getUsername());
        if (!users.isEmpty()){
            if (!password.equals(users.get(0).getPassword()))
                throw new IncorrectCredentialsException();
            return  new SimpleAuthenticationInfo(username,password.toCharArray(),getName());
        }
        throw new UnknownAccountException();
    }

    /**
     * 授权验证
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        if(principalCollection==null)
            throw new AuthorizationException("parameter principalCollection is null!");
        String username=(String)getAvailablePrincipal(principalCollection);
        SimpleAuthorizationInfo simpleAuthorizationInfo=new SimpleAuthorizationInfo();
        Set<String> roles=snsWebUserService.getRole(username);
        simpleAuthorizationInfo.setRoles(roles);
        simpleAuthorizationInfo.setStringPermissions(roles);
        System.out.println(username+"yyy");
        return  simpleAuthorizationInfo;
    }
}
