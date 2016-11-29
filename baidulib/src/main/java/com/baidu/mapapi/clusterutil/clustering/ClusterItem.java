/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */

package com.baidu.mapapi.clusterutil.clustering;


import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.model.LatLng;

/**
 * ClusterItem represents a marker on the map.
 * add myself 增加了自定义接口
 */
public interface ClusterItem {

    /**
     * The position of this marker. This must always return the same value.
     */
    LatLng getPosition();

    BitmapDescriptor getBitmapDescriptor();

    BitmapDescriptor getUrlClusterIconBitmapDescriptor();

    /**
     * 网络的单个marker的实例
     */
    BitmapDescriptor getUrlMarkerIconBitmapDescriptor(boolean select);

    /**
     * 网络的单个marker的icon路径
     */
    String getUrlLocalMarkerIconPath();

    /**
     * 网络的单个聚合的icon路径
     */
    String getUrlClusterIconPath();

}