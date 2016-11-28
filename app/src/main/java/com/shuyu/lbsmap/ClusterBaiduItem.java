package com.shuyu.lbsmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.clustering.view.DefaultClusterRenderer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;
import com.shuyu.lbsmap.model.LBSModel;

import java.io.File;

public class ClusterBaiduItem implements ClusterItem {


    private int bitmapId = -1;

    private boolean isLocation = false;

    private LatLng mPosition;

    private LBSModel mLBAModel;

    private String itemAddress;
    private String localClusterPath;
    private String localSinglePath;
    private String unLocalSinalePath;
    private String icon_url;
    private String icon_un_url;

    @Override
    public String getUrlLocalMarkerIconPath() {
        return localSinglePath;
    }

    public void setLocalSinglePath(String localSinglePath) {
        this.localSinglePath = localSinglePath;
    }

    @Override
    public String getUrlClusterIconPath() {
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
                .fromResource(R.drawable.default_map_icon);
    }

    @Override
    public BitmapDescriptor getUrlClusterIconBitmapDescriptor() {
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
    public BitmapDescriptor getUrlMarkerIconBitmapDescriptor() {
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

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public LBSModel getLBAModel() {
        return mLBAModel;
    }

    public void setLBAModel(LBSModel lbsModel) {
        this.mLBAModel = lbsModel;
    }
}