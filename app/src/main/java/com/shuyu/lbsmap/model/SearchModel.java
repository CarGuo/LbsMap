package com.shuyu.lbsmap.model;


/**
 * Created by shuyu on 2016/11/27.
 */

public class SearchModel {

    //搜索经纬度
    private String mGps;

    //搜索类型
    private int mSearchType ;

    //表名
    private int mTableId;

    //半径
    private int mRadius;

    //层级
    private float mLevel = 13;


    public String getGps() {
        return mGps;
    }

    public void setGps(String gps) {
        this.mGps = gps;
    }

    public int getSearchType() {
        return mSearchType;
    }

    public void setSearchType(int searchType) {
        this.mSearchType = searchType;
    }

    public int getTableId() {
        return mTableId;
    }

    public void setTableId(int tableId) {
        this.mTableId = tableId;
    }

    public float getLevel() {
        return mLevel;
    }

    public void setLevel(float level) {
        this.mLevel = level;
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
        this.mRadius = radius;
    }
}
