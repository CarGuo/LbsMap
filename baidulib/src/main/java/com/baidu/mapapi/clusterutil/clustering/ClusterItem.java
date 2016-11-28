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

    BitmapDescriptor getUrlMarkerIconBitmapDescriptor();

    String getUrlLocalMarkerIconPath();

    String getUrlClusterIconPath();

}