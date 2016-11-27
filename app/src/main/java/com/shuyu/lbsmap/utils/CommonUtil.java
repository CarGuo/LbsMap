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


    private Bitmap resolveSize(String imgPath) {

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // Get bitmap info, but notice that bitmap is null now
        BitmapFactory.decodeFile(imgPath, newOpts);
        Point point = CommonUtil.getBitmapSize();
        int pixelW = (int) point.x;
        int pixelH = (int) point.y;

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > pixelW) {//如果宽度大的话根据宽度固定大小缩放
            be = (newOpts.outWidth / pixelW);
        } else if (h > pixelH) {//如果高度高的话根据宽度固定大小缩放
            be = (newOpts.outHeight / pixelH);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了
        return BitmapFactory.decodeFile(imgPath, newOpts);
    }


    public static Map<String, String> resolveUrlMap( SearchModel searchModel, int index, int pageSize) {
        Map<String, String> urlMap = new LinkedHashMap<String, String>();

        //ak
        urlMap.put("ak", DemoApplication.AK());

        //表名
        urlMap.put("geotable_id", "" + searchModel.getTableId());

        //第几页
        urlMap.put("page_index", "" + index);

        //每页数量
        urlMap.put("page_size", "" + pageSize);

        urlMap.put("location", "" + searchModel.getGps());

        //半径
        urlMap.put("radius", "" + searchModel.getRadius());

        //关键字q，标签tag，排序sort，过滤等

        return urlMap;
    }


    /**
     * 计算sn跟参数对出现顺序有关，所以用LinkedHashMap保存<key,value>，此方法适用于get请求，
     * 如果是为发送post请求的url生成签名，请保证参数对按照key的字母顺序依次放入Map.
     * 以get请求为例：http://api.map.baidu.com/geocoder/v2/?address=百度大厦&output=json&ak=yourak，
     * paramsMap中先放入address，再放output，然后放ak，放入顺序必须跟get请求中对应参数的出现顺序保持一致。
     */
    public static String SignSN(String head, Map map) {
        try {
            String paramsStr = toQueryString(map);

            String wholeStr = new String(head + paramsStr + SK());

            // 对上面wholeStr再作utf8编码
            String tempStr = URLEncoder.encode(wholeStr, "UTF-8");

            String sn = MD5(tempStr);

            //Debuger.printfError(sn);
            return sn;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 针对location参数的改进的utf8中文转换
     */
    public static String toQueryString(Map<?, ?> data)
            throws UnsupportedEncodingException {
        StringBuffer queryString = new StringBuffer();
        for (Map.Entry<?, ?> pair : data.entrySet()) {
            queryString.append(pair.getKey() + "=");
            if (pair.getKey().equals("region") || pair.getKey().equals("tags")) {
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
