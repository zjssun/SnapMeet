package com.snapmeet.enums;

import com.snapmeet.utils.StringTools;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

public enum FileTypeEnum {
    IMAGE(0,new String[]{".jpeg",".jpg",".png",".gif",".bmp",".webp"},".jpg","图片"),
    VIDEO(1,new String[]{".mp4",".avi",".mkv",".mov"},".mp4","视频");

    @Getter
    private Integer type;
    @Getter
    private String[] suffixArray;
    @Getter
    private String suffix;
    @Getter
    private String desc;

    FileTypeEnum(Integer type,String[] suffixArray,String suffix,String desc){
        this.type=type;
        this.suffixArray=suffixArray;
        this.suffix=suffix;
        this.desc=desc;
    }

    public static FileTypeEnum getByType(Integer type){
        if(null==type){
            return null;
        }
        for(FileTypeEnum typeEnum : FileTypeEnum.values()){
            if(typeEnum.getType().equals(type)){
                return  typeEnum;
            }
        }
        return null;
    }

    public static FileTypeEnum getBySuffix(String suffix){
        if(StringTools.isEmpty(suffix)){
            return null;
        }
        for(FileTypeEnum typeEnum : FileTypeEnum.values()){
            if(ArrayUtils.contains(typeEnum.getSuffixArray(),suffix)){
                return typeEnum;
            }
        }
        return null;
    }
}
