package com.shuyu.lbsmap.job;


import android.os.Handler;
import android.text.TextUtils;

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

import static com.shuyu.lbsmap.utils.CommonUtil.SNLogic;
import static com.shuyu.lbsmap.utils.CommonUtil.MapToUrl;
import static com.shuyu.lbsmap.utils.CommonUtil.inputMapForUrl;
import static com.shuyu.lbsmap.utils.FileUtils.getLogoNamePath;

public class RequestLogicJob extends Job {

    private String mUUID;

    private Map<String, String> urlMap;

    private SearchModel mSearchModel;

    private Handler mHandler = new Handler();

    private int mPageIndex = 0;

    protected RequestLogicJob() {
        super(new Params(1000));
    }

    public RequestLogicJob(SearchModel searchModel, int pageIndex, String uuid) {
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

        urlMap = inputMapForUrl(mSearchModel, mPageIndex, DemoApplication.PAGE_SIZE);

        //头部链接
        String requestHeader = "/geosearch/v3/nearby?";

        //sn签名
        String sn = SNLogic(requestHeader, urlMap);

        //生成请求URL
        final String url = "http://api.map.baidu.com" + requestHeader + MapToUrl(urlMap) + "&sn=" + sn;

        //发起请求
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                HttpRequest.get(url, new getDataListener());
            }
        });
    }

    /**
     * 拿到了数据
     */
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

    /**
     * 返回数据
     */
    private void successResult(int totalCount, List<LBSModel> lbsModels) {
        if (lbsModels != null && lbsModels.size() != 0) {

            //生成百度items
            List<ClusterBaiduItem> items = BaiduItemLogic(lbsModels);

            RequestDataEvent requestDataEvent = new RequestDataEvent();
            requestDataEvent.setEventType(RequestDataEvent.SUCCESS);
            requestDataEvent.setClusterBaiduItems(items);
            requestDataEvent.setDataList(lbsModels);
            requestDataEvent.setLastSize(lbsModels.size());
            requestDataEvent.setUUID(mUUID);
            requestDataEvent.setTotalSize(totalCount);
            requestDataEvent.setParamsMap(urlMap);
            DemoApplication.getApplication().getEventBus().post(requestDataEvent);

        } else {
            RequestDataEvent requestDataEvent = new RequestDataEvent();
            requestDataEvent.setEventType(RequestDataEvent.NULL);
            requestDataEvent.setUUID(mUUID);
            DemoApplication.getApplication().getEventBus().post(requestDataEvent);

        }
    }

    /**
     * 组装百度需要的item
     */
    private List<ClusterBaiduItem> BaiduItemLogic(List<LBSModel> list) {
        List<ClusterBaiduItem> items = new ArrayList<>();
        for (LBSModel lbsModel : list) {
            LatLng ll = new LatLng(lbsModel.getLocation()[1], lbsModel.getLocation()[0]);
            ClusterBaiduItem baiduItem = new ClusterBaiduItem(ll);
            baiduItem.setMarkerAddress(lbsModel.getAddress());
            baiduItem.setLBAModel(lbsModel);
            baiduItem.setMarkerUrl(lbsModel.getIcons());
            //如果是图片字段会变为几个尺寸的model
            if (!TextUtils.isEmpty(lbsModel.getIcons())) {
                baiduItem.setUrlMarkerPath(getLogoNamePath(lbsModel.getIcons()));
            }
            items.add(baiduItem);
        }
        return items;
    }

    /**
     * 请求失败
     */
    private void FailToEvent() {
        RequestDataEvent requestDataEvent = new RequestDataEvent();
        requestDataEvent.setEventType(RequestDataEvent.FAIL);
        requestDataEvent.setUUID(mUUID);
        DemoApplication.getApplication().getEventBus().post(requestDataEvent);
    }

}