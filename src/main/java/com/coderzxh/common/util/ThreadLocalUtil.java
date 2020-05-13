package com.coderzxh.common.util;

/**
 * @author zxh
 * @since on 2018/5/8.
 */
public class ThreadLocalUtil {

    private static ThreadLocal<Boolean> isPassThreadLocal = new PassThreadLocal();

    private static class PassThreadLocal extends ThreadLocal<Boolean>{
        @Override
        protected Boolean initialValue() {
            return false;
        }
    }

    public static Boolean getIsPass(){
        return isPassThreadLocal.get();
    }

    public static void setIsPass(Boolean boo){
        isPassThreadLocal.set(boo);
    }

    public static void removeIsPass(){
        isPassThreadLocal.remove();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 0; i++) {
            System.out.println(i);
        }
    }
}
