package com.coderzxh.web.shiro;

import com.coderzxh.common.util.ComUtil;
import com.coderzxh.common.util.JWTUtil;
import com.coderzxh.common.util.ThreadLocalUtil;
import com.coderzxh.persistence.entity.Permission;
import com.coderzxh.persistence.entity.User;
import com.coderzxh.persistence.entity.UserRole;
import com.coderzxh.service.SpringContextBeanService;
import com.coderzxh.service.base.IPermissionService;
import com.coderzxh.service.base.IRoleService;
import com.coderzxh.service.base.IUserRoleService;
import com.coderzxh.service.base.IUserService;
import com.coderzxh.web.exception.UnauthorizedException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class MyRealm extends AuthorizingRealm {
    private IUserService userService;
    private IUserRoleService userRoleService;
    private IPermissionService permissionService;
    private IRoleService roleService;
    /**
     * 大坑！，必须重写此方法，不然Shiro会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (userRoleService == null) {
            this.userRoleService = SpringContextBeanService.getBean(IUserRoleService.class);
        }
        if (permissionService == null) {
            this.permissionService = SpringContextBeanService.getBean(IPermissionService.class);
        }
        if (roleService == null) {
            this.roleService = SpringContextBeanService.getBean(IRoleService.class);
        }

        int userNo = JWTUtil.getUserCode(principals.toString());
        User user = userService.selectById(userNo);
        UserRole userRole = userRoleService.selectByUserId(user.getId());

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        /*
        Role role = roleService.selectOne(new EntityWrapper<Role>().eq("role_code", userToRole.getRoleCode()));
        //添加控制角色级别的权限
        Set<String> roleNameSet = new HashSet<>();
        roleNameSet.add(role.getRoleName());
        simpleAuthorizationInfo.addRoles(roleNameSet);
        */
        //控制菜单级别按钮  类中用@RequiresPermissions("user:list") 对应数据库中code字段来控制controller
        ArrayList<String> pers = new ArrayList<>();
        List<Permission> permissionList = permissionService.findPermissionByRoleCode(userRole.getRoleCode());
        for (Permission per : permissionList) {
             if (!ComUtil.isEmpty(per.getCode())) {
                  pers.add(String.valueOf(per.getCode()));
              }
        }
        Set<String> permission = new HashSet<>(pers);
        simpleAuthorizationInfo.addStringPermissions(permission);
        return simpleAuthorizationInfo;
    }

    /**
     * 默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws UnauthorizedException {
        if (userService == null) {
            this.userService = SpringContextBeanService.getBean(IUserService.class);
        }
        String token = (String) auth.getCredentials();
        Boolean isPass = false;
        try {
            isPass = ThreadLocalUtil.getIsPass();
        }finally {
            //最后一次使用后删除
            ThreadLocalUtil.removeIsPass();
        }
        if(isPass){
            return new SimpleAuthenticationInfo(token, token, this.getName());
        }
        // 解密获得username，用于和数据库进行对比
        int userNo = JWTUtil.getUserCode(token);
        if (userNo == 0) {
            throw new UnauthorizedException("token invalid");
        }
        User userBean = userService.selectById(userNo);
        if (userBean == null) {
            throw new UnauthorizedException("User didn't existed!");
        }
        if (! JWTUtil.verify(token, userNo, userBean.getPassword())) {
            throw new UnauthorizedException("Username or password error");
        }
        return new SimpleAuthenticationInfo(token, token, this.getName());
    }
}
