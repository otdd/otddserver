package io.otdd.otddserver.util;

import java.util.Date;

public class PaddingUtil {
    public static void main_bak(String args[]){
        System.out.println(PaddingUtil.paddingLong(new Date().getTime()));
    }

    //https://www.elastic.co/blog/apache-lucene-numeric-filters
    public static String paddingLong(long l){
        String s = ""+l;
        if(l<0){
            return s;
        }
        StringBuilder sb = new StringBuilder();
        if(s.length()<15){
            for(int i=0;i<15-s.length();i++){
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
