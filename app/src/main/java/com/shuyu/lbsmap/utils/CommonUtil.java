package com.shuyu.lbsmap.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.shuyu.lbsmap.DemoApplication;
import com.shuyu.lbsmap.R;
import com.shuyu.lbsmap.model.SearchModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.shuyu.lbsmap.DemoApplication.SK;


/**
 * Created by shuyu on 2016/11/26.
 */

public class CommonUtil {

    public static Point bitmapSize;

    public static Point getBitmapSize() {

        if (bitmapSize == null) {
            Bitmap var2 = BitmapFactory.decodeResource(DemoApplication.getApplication().getResources(), R.drawable.defualt_map_size_icon);
            bitmapSize = new Point(var2.getWidth(), var2.getHeight());
            var2.recycle();
        }

        return bitmapSize;
    }

    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

    }


    /**
     * 组装一个map用于url显示
     */
    public static Map<String, String> inputMapForUrl(SearchModel searchModel, int index, int pageSize) {
        Map<String, String> map = new LinkedHashMap<String, String>();

        //ak
        map.put("ak", DemoApplication.AK());

        //表名
        map.put("geotable_id", "" + searchModel.getTableId());

        //第几页
        map.put("page_index", "" + index);

        //每页数量
        map.put("page_size", "" + pageSize);

        //经纬度
        map.put("location", "" + searchModel.getGps());

        //半径
        map.put("radius", "" + searchModel.getRadius());

        //还可以有关键字q，标签tag，排序sort，过滤等

        return map;
    }

    /**
     * 签名，一般服务端做的，因为请求数据一般也是服务端请求后交给客户端的
     */
    public static String SNLogic(String requestHeader, Map map) {
        try {
            String contentUrl = toSNString(map);

            String wholeStr = new String(requestHeader + contentUrl + SK());

            //utf8编码
            String tempStr = URLEncoder.encode(wholeStr, "UTF-8");

            String sn = MD5(tempStr);

            return sn;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }


    private static String toSNString(Map<?, ?> data)
            throws UnsupportedEncodingException {
        StringBuffer queryString = new StringBuffer();
        for (Map.Entry<?, ?> pair : data.entrySet()) {
            queryString.append(pair.getKey() + "=");
            if (pair.getKey().equals("tags")) {
                String ss[] = pair.getValue().toString().split(",");
                //针对Tag中的/
                if (ss.length > 1) {
                    for (String s : ss) {
                        String ss2[] = s.split("/");
                        if (ss2.length > 1) {
                            for (String s2 : ss2) {
                                queryString.append(URLEncoder.encode(s2, "UTF-8") + "/");
                            }
                            queryString.deleteCharAt(queryString.length() - 1);
                            queryString.append(",");
                        } else {
                            queryString.append(URLEncoder.encode(s, "UTF-8") + ",");
                        }
                    }
                    queryString.deleteCharAt(queryString.length() - 1);
                    queryString.append("&");
                } else {
                    String ss2[] = pair.getValue().toString().split("/");
                    if (ss2.length > 1) {
                        for (String s2 : ss2) {
                            queryString.append(URLEncoder.encode(s2, "UTF-8") + "/");
                        }
                        queryString.deleteCharAt(queryString.length() - 1);
                        queryString.append("&");
                    } else {
                        queryString.append(URLEncoder.encode((String) pair.getValue(),
                                "UTF-8") + "&");
                    }
                }
            } else if (pair.getKey().equals("q")) {
                String ss[] = pair.getValue().toString().split(",");
                //针对Tag中的/
                if (ss.length > 1) {
                    for (String s : ss) {
                        queryString.append(URLEncoder.encode(s, "UTF-8") + ",");
                    }
                    queryString.deleteCharAt(queryString.length() - 1);
                    queryString.append("&");
                } else {
                    queryString.append(URLEncoder.encode((String) pair.getValue(),
                            "UTF-8") + "&");
                }

            } else if (pair.getKey().equals("filter")) {
                String ss[] = pair.getValue().toString().split("\\|");
                //针对Tag中的/
                if (ss.length > 1) {
                    queryString.append(ss[0]).append(URLEncoder.encode("|", "UTF-8")).append(ss[1]);
                    queryString.append("&");
                } else {
                    queryString.append(pair.getValue() + "&");
                }
            } else {
                queryString.append(pair.getValue() + "&");
            }

        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }

    /**
     * MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
     */
    private static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }


    /**
     * 将map转为url
     */
    public static String MapToUrl(Map<?, ?> data) {
        StringBuffer queryString = new StringBuffer();
        for (Map.Entry<?, ?> pair : data.entrySet()) {
            queryString.append(pair.getKey() + "=");
            queryString.append(pair.getValue() + "&");
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }


    /**
     * MD5加密
     *
     * @param s
     * @return String
     */
    public static String MD5L(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] strTemp = s.getBytes();
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

}
