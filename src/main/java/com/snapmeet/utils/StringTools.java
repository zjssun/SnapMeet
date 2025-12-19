package com.snapmeet.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {

    //是否为空
    public static final boolean isEmpty(CharSequence cs){
        if (cs == null || cs.length() == 0) {
            return true;
        }

        // 再严谨判断是否全是空格/换行/制表符
        // 只要有一个字符不是空白，它就不是空
        for (int i = 0; i < cs.length(); i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        // 运行到这里说明长度 > 0 但全是空白字符
        return true;
    }


    //生成数字随机数
    public static final String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }

    //生成字符串随机数
    public static final String getRandomString(Integer count){
        return RandomStringUtils.random(count,true,true);
    }

    //MD5加密
    public static String encodeByMD5(String originString){
        return StringTools.isEmpty(originString) ? null : DigestUtils.md5Hex(originString);
    }
}
