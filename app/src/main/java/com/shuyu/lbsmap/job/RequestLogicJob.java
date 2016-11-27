package com.shuyu.lbsmap.job;


import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.shuyu.lbsmap.ClusterBaiduItem;
import com.shuyu.lbsmap.DemoApplication;
import com.shuyu.lbsmap.event.RequestDataEvent;
import com.shuyu.lbsmap.model.LBSModel;
import com.shuyu.lbsmap.model.SearchModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.finalteam.okhttpfinal.HttpRequest;
import cn.finalteam.okhttpfinal.JsonHttpRequestCallback;

import static com.shuyu.lbsmap.utils.CommonUtil.SignSN;
import static com.shuyu.lbsmap.utils.CommonUtil.MapToUrl;
import static com.shuyu.lbsmap.utils.CommonUtil.resolveUrlMap;

public class RequestLogicJob extends Job {

    private final static String TAG = "RequestLogicJob";

    private List<ClusterBaiduItem> mClusterBaiduItems = new ArrayList<>();

    private String mUUID;

    private Map<String, String> paramsMap;

    private SearchModel mSearchModel;

    private Handler mHandler = new Handler();

    private int mPageIndex = 0;

    protected RequestLogicJob() {
        super(new Params(1000));
    }

    public RequestLogicJob(SearchModel searchModel, int pageIndex,  String uuid) {
        super(new Params(1000));
        this.mSearchModel = searchModel;
        this.mPageIndex = pageIndex;
        this.mUUID = uuid;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        requestCloudData();
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    private void requestCloudData() {

        paramsMap = resolveUrlMap(mSearchModel, mPageIndex, 50);

        //头部链接
        String urlHead = "/geosearch/v3/nearby?";

        //sn签名
        String sn = SignSN(urlHead, paramsMap);

        final String url = "http://api.map.baidu.com" + urlHead + MapToUrl(paramsMap) + "&sn=" + sn;

        Log.i(TAG, "url " + url.replace("|", "%7C"));

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                HttpRequest.get(url.replace("|", "%7C"), new getDataListener());
            }
        });
    }


    private class getDataListener extends JsonHttpRequestCallback {

        @Override
        protected void onSuccess(final JSONObject jsonObject) {
            super.onSuccess(jsonObject);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONArray dataJSON = jsonObject.getJSONArray("contents");
                    List<LBSModel> list = toListModel(jsonObject);
                    successResult((dataJSON == null || dataJSON.size() == 0) ? 0 : jsonObject.getInteger("total"), list);
                }
            }).start();
        }

        @Override
        public void onFailure(int errorCode, String msg) {
            super.onFailure(errorCode, msg);
            FailToEvent();
        }
    }


    private List<LBSModel> toListModel(JSONObject response) {
        int status = response.getInteger("status");
        if (status != 0) {
            FailToEvent();
            return null;
        }
        JSONArray dataJSON = response.getJSONArray("contents");
        List<LBSModel> data = new ArrayList<>();
        if (dataJSON != null && !TextUtils.isEmpty(dataJSON.toString())) {
            List<LBSModel> list = JSON.parseArray(dataJSON.toString(), LBSModel.class);
            data.addAll(list);
        }
        return data;

    }

    private void successResult(int totalCount, List<LBSModel> lbsModels) {
        if (lbsModels != null && lbsModels.size() != 0) {
            this.mClusterBaiduItems.clear();
            //生成百度items
            bindBaiduItemList(lbsModels);
            RequestDataEvent requestDataEvent = new RequestDataEvent();
            requestDataEvent.setEventType(RequestDataEvent.SUCCESS);
            requestDataEvent.setClusterBaiduItems(mClusterBaiduItems);
            requestDataEvent.setDataList(lbsModels);
            requestDataEvent.setLastSize(lbsModels.size());
            requestDataEvent.setUUID(mUUID);
            requestDataEvent.setTotalSize(totalCount);
            requestDataEvent.setParamsMap(paramsMap);
            DemoApplication.getApplication().getEventBus().post(requestDataEvent);

        } else {

            RequestDataEvent requestDataEvent = new RequestDataEvent();
            requestDataEvent.setEventType(RequestDataEvent.NULL);
            requestDataEvent.setUUID(mUUID);
            DemoApplication.getApplication().getEventBus().post(requestDataEvent);

        }
    }


    private void bindBaiduItemList(List<LBSModel> list) {
        List<ClusterBaiduItem> items = new ArrayList<>();
        BaiduItemLogic(items, list);
        mClusterBaiduItems.addAll(items);
    }


    private static void BaiduItemLogic(List<ClusterBaiduItem> items, List<LBSModel> list) {

        for (LBSModel lbsModel : list) {
            LatLng ll = new LatLng(lbsModel.getLocation()[1], lbsModel.getLocation()[0]);
            ClusterBaiduItem baiduItem = new ClusterBaiduItem(ll);
            baiduItem.setItemAddress(lbsModel.getAddress());
            baiduItem.setLBAModel(lbsModel);
            items.add(baiduItem);
        }
    }


    private void FailToEvent() {
        RequestDataEvent requestDataEvent = new RequestDataEvent();
        requestDataEvent.setEventType(RequestDataEvent.FAIL);
        requestDataEvent.setUUID(mUUID);
        DemoApplication.getApplication().getEventBus().post(requestDataEvent);
    }

}