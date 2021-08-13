package com.smartdoc.function;

import org.beetl.core.*;

/**
 * Created by Qiao on 2021/8/13
 */
public class NotEqualFunction implements Function {
    @Override
    public Object call(Object[] paras, Context ctx) {
        if (paras == null || paras.length < 2) {
            return true;
        }
        return !paras[0].equals(paras[1]);
    }
}
