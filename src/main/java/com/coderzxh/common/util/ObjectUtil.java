package com.coderzxh.common.util;

import com.coderzxh.common.base.BusinessException;
import com.coderzxh.common.base.PublicResultConstant;

public class ObjectUtil {

    public static Object handleParamNull(Object o) throws BusinessException {
        if(o == null){
            throw new BusinessException(PublicResultConstant.PARAM_NULL);
        }else{
            return o;
        }
    }
}
