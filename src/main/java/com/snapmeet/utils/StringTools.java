package com.snapmeet.utils;

import com.snapmeet.constants.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    //生成会议号
    public static final String getMeetingNoOrMeetingId(){
        return StringTools.getRandomNumber(Constants.LENGTH_10);
    }

    //复制列表
    public static <T,S> List<T> copyList(List<S> sList, Class<T> classz){
        if (sList == null || sList.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> list = new ArrayList<>(sList.size());
        for (S s : sList) {
            if (s == null) continue;
            list.add(copy(s, classz));
        }
        return list;
    }

    //复制对象
    public static <T,S> T copy(S s,Class<T> clazz){
        if (s == null) return null;
        T t = null;
        try {
            t = clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(s, t);
        } catch (Exception e) {
            throw new RuntimeException("对象属性复制失败: 无法实例化 " + clazz.getName(), e);
        }
        return t;
    }
}
