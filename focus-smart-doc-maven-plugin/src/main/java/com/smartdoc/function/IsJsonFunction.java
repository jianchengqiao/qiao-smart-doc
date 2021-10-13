package com.smartdoc.function;

import org.beetl.core.*;

/**
 * Created by Qiao on 2021/8/13
 */
public class IsJsonFunction implements Function {
    @Override
    public Object call(Object[] paras, Context ctx) {
        if (paras == null || paras.length < 1 || paras[0] == null) {
            return false;
        }
        return paras[0].toString().startsWith("{") || paras[0].toString().startsWith("[");
    }
}
