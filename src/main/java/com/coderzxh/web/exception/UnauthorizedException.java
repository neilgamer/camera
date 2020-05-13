package com.coderzxh.web.exception;

import org.apache.shiro.authc.AuthenticationException;

import java.io.Serializable;

/**
 * 身份认证异常
 */
public class UnauthorizedException extends AuthenticationException implements Serializable {


    private static final long serialVersionUID = 103425800003826538L;

    public UnauthorizedException(String msg) {
        super(msg);
    }

    public UnauthorizedException() {
        super();
    }
}
