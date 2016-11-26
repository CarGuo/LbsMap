package com.shuyu.lbsmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.text.TextUtils;

import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.clustering.view.DefaultClusterRenderer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.shuyu.lbsmap.DemoApplication.SK;

public class ClusterBaiduItem implements ClusterItem {

    private final LatLng mPosition;

    private String itemAddress;
    private String localClusterPath;
    private String localSinglePath;
    private String unLocalSinalePath;
    private String icon_url;
    private String icon_un_url;
    private int logoUrlType;

    private int bitmapId = -1;
    private boolean isLocation = false;


    public String getItemAddress() {
        return itemAddress;
    }

    public void setItemAddress(String itemAddress) {
        this.itemAddress = itemAddress;
    }

    public ClusterBaiduItem(LatLng latLng) {
        mPosition = latLng;
    }

    public void setBitmapId(int bitmapId) {
        this.bitmapId = bitmapId;
    }

    public boolean isLocation() {
        return isLocation;
    }

    public void setIsLocation(boolean isLocation) {
        this.isLocation = isLocation;
    }

    @Override
    public String getLocalSinglePath() {
        return localSinglePath;
    }

    public void setLocalSinglePath(String localSinglePath) {
        this.localSinglePath = localSinglePath;
    }

    @Override
    public String getLocalClusterPath() {
        return localClusterPath;
    }

    public void setLocalClusterPath(String localClusterPath) {
        this.localClusterPath = localClusterPath;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }


    @Override
    public BitmapDescriptor getBitmapDescriptor() {
        if (bitmapId != -1) {
            return BitmapDescriptorFactory
                    .fromResource(bitmapId);
        }

        return BitmapDescriptorFactory
                .fromResource(R.drawable.baidu_map_loaction);
    }

    @Override
    public BitmapDescriptor getLocalClusterBitmapDescriptor() {
        if (!TextUtils.isEmpty(localClusterPath)) {
            Bitmap var1 = BitmapFactory.decodeFile(localClusterPath);
            if (var1 == null) {
                if (!DefaultClusterRenderer.LOADING_LOGO)
                    deleteFile(localClusterPath);
                return null;
            } else {
                var1.recycle();
            }
            return BitmapDescriptorFactory
                    .fromPath(localClusterPath);

        }

        return null;
    }

    @Override
    public BitmapDescriptor getLocalSingleBitmapDescriptor() {
        if (!TextUtils.isEmpty(localSinglePath)) {
            Bitmap var1 = BitmapFactory.decodeFile(localSinglePath);
            if (var1 == null) {
                if (!DefaultClusterRenderer.LOADING_LOGO)
                    deleteFile(localSinglePath);
                return null;
            } else {
                var1.recycle();
            }
            return BitmapDescriptorFactory
                    .fromPath(localSinglePath);
        }

        return null;
    }

    private void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getIcon_un_url() {
        return icon_un_url;
    }

    public void setIcon_un_url(String icon_un_url) {
        this.icon_un_url = icon_un_url;
    }

    public String getUnLocalSinalePath() {
        return unLocalSinalePath;
    }

    public void setUnLocalSinalePath(String unLocalSinalePath) {
        this.unLocalSinalePath = unLocalSinalePath;
    }

    public int getLogoUrlType() {
        return logoUrlType;
    }

    public void setLogoUrlType(int logoUrlType) {
        this.logoUrlType = logoUrlType;
    }

    /********************************
     * 以下为公共方法
     * ***********************************************************************
     **/

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

    /**
     * 计算sn跟参数对出现顺序有关，所以用LinkedHashMap保存<key,value>，此方法适用于get请求，
     * 如果是为发送post请求的url生成签名，请保证参数对按照key的字母顺序依次放入Map.
     * 以get请求为例：http://api.map.baidu.com/geocoder/v2/?address=百度大厦&output=json&ak=yourak，
     * paramsMap中先放入address，再放output，然后放ak，放入顺序必须跟get请求中对应参数的出现顺序保持一致。
     */
    public static String SnSign(String head, Map map) {
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

    public static String UrltoQueryString(Map<?, ?> data) {
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


}