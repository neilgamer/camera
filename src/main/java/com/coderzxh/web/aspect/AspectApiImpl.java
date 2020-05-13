package com.coderzxh.web.aspect;

import com.coderzxh.common.util.ThreadLocalUtil;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * 基本被装饰类,做一些公共处理
 */
public class AspectApiImpl implements AspectApi {

    @Override
    public Object doHandlerAspect(ProceedingJoinPoint pjp, Method method) throws Throwable {
//        Constant.isPass=false;
        ThreadLocalUtil.setIsPass(false);
        return null;
    }
}
