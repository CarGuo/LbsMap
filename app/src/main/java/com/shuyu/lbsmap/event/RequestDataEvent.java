package com.shuyu.lbsmap.event;

import com.shuyu.lbsmap.ClusterBaiduItem;
import com.shuyu.lbsmap.model.LBSModel;

import java.util.List;
import java.util.Map;

public class RequestDataEvent {

    public static final int SUCCESS = 1;
    public static final int FAIL = 0;
    public static final int DEFAULT = 22;
    public static final int NULL = 26;

    private List<ClusterBaiduItem> mClusterBaiduItems;

    private List<LBSModel> mDataList;

    private Map<String, String> mParamsMap;

    private String mUUID;

    private int mEventType;

    private int mLastSize;

    private int mTotalSize;

    public RequestDataEvent() {

    }

    public List<ClusterBaiduItem> getClusterBaiduItems() {
        return mClusterBaiduItems;
    }

    public void setClusterBaiduItems(List<ClusterBaiduItem> clusterBaiduItems) {
        this.mClusterBaiduItems = clusterBaiduItems;
    }

    public List<LBSModel> getDataList() {
        return mDataList;
    }

    public void setDataList(List<LBSModel> dataList) {
        this.mDataList = dataList;
    }

    public Map<String, String> getParamsMap() {
        return mParamsMap;
    }

    public void setParamsMap(Map<String, String> paramsMap) {
        this.mParamsMap = paramsMap;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String UUID) {
        this.mUUID = UUID;
    }

    public int getEventType() {
        return mEventType;
    }

    public void setEventType(int eventType) {
        this.mEventType = eventType;
    }

    public int getLastSize() {
        return mLastSize;
    }

    public void setLastSize(int lastSize) {
        this.mLastSize = lastSize;
    }

    public int getTotalSize() {
        return mTotalSize;
    }

    public void setTotalSize(int totalSize) {
        this.mTotalSize = totalSize;
    }
}