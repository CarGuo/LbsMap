package com.shuyu.lbsmap;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.utils.DistanceUtil;
import com.shuyu.lbsmap.event.RequestDataEvent;
import com.shuyu.lbsmap.event.IconEvent;
import com.shuyu.lbsmap.job.IConJob;
import com.shuyu.lbsmap.job.RequestLogicJob;
import com.shuyu.lbsmap.model.IconModel;
import com.shuyu.lbsmap.model.LBSModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends BaseActivity {

    boolean mHadRequest = false;

    //城市是否改变
    boolean mIsChangeCity;

    //是否移动
    boolean mIsMove;

    //所有的数量
    int mTotalCount;

    ClusterBaiduItem mPreClickItem;

    Handler mHandler = new Handler();

    //网络请求的线程
    NetRunnable mNetRunnable;

    //图标的item
    List<ClusterBaiduItem> mClusterBaiduItems = new ArrayList<>();

    //lbs数据列表
    List<LBSModel> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initListeners();

        showLoadingDialog();
        //初始化请求数据
        RequestNewDataLogic(false, false);
    }

    @Override
    protected void loading() {
        super.loading();
        //点击loading请求数据
        RequestNewDataLogic(false, false);
    }

    protected void initListeners() {

        //地图状态发生变化，主要是移动、放大、经纬度发生变化
        mClusterManager.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            //记住变化前的上一个状态
            private MapStatus mFrontMapStatus;

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                if (mFrontMapStatus == null) {
                    mFrontMapStatus = mapStatus;
                }
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                //此处需要注意，如果是进入的时候重新定位了地址，或者进入后在改变地图状态，可能也会进入这里
                if (mHadRequest) {
                    if (StatusChangeLogic(mFrontMapStatus, mapStatus)) {//处理移动与放大
                        mFrontMapStatus = null;
                    }
                }
                mCurrentMapStatus = mapStatus;
            }
        });

        //将百度的图标点击转为marker的点击
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //通过这里转到mClusterManager
                return mMarkerManager.onMarkerClick(marker);
            }
        });

        //单个的点击
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterBaiduItem>() {
            @Override
            public boolean onClusterItemClick(ClusterBaiduItem item) {
                Toast.makeText(MainActivity.this, item.getLBAModel().getTitle(), Toast.LENGTH_SHORT).show();
                IconClick(item);
                return true;
            }
        });

        //聚合的点击
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterBaiduItem>() {
            @Override
            public boolean onClusterClick(Cluster<ClusterBaiduItem> cluster) {
                Toast.makeText(MainActivity.this, "聚合图标：" + cluster.getSize(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }


    /**
     * 地图因为操作而发生了状态改变
     */
    private boolean StatusChangeLogic(MapStatus frontMapStatus, MapStatus mapStatus) {
        //重新确定搜索半径的中心图标
        mSearchModel.setGps(mapStatus.bound.getCenter().longitude + "," + mapStatus.bound.getCenter().latitude);
        //重新确定层级
        mSearchModel.setLevel(mapStatus.zoom);

        if (frontMapStatus == null)
            return false;

        //得到屏幕的距离大小
        double areaLength1 = DistanceUtil.getDistance(mapStatus.bound.northeast, mapStatus.bound.southwest);

        //计算屏幕的大小半径
        int radius = (int) areaLength1 / 2;

        //重新确定搜索的半径
        mSearchModel.setRadius(radius);


        if (frontMapStatus.zoom == mapStatus.zoom) {
            if (frontMapStatus.bound == null)
                return false;
            //如果是移动了，得到距离
            double moveLenght = DistanceUtil.getDistance(frontMapStatus.bound.getCenter(), mapStatus.bound.getCenter());
            //如果移动距离大于屏幕的检索半径，请求数据
            if (moveLenght >= radius) {
                RequestNewDataLogic(true, true);
                return true;
            }

            //如果经纬度发生变化了，一般都是切换的城市之类的
            if (mChangeStatus != null && (mapStatus.target.latitude) != (int) (mChangeStatus.target.latitude)
                    && (int) (mapStatus.target.longitude) != (int) (mChangeStatus.target.longitude) && mIsChangeCity) {
                RequestNewDataLogic(true, true);
                mIsChangeCity = false;
                return true;
            }

            return false;
        } else {
            //如果是缩放的话，地图层级发生改变，重新请求数据
            RequestNewDataLogic(true, true);
            return true;
        }
    }

    /**
     * 请求新数据的逻辑
     */
    private void RequestNewDataLogic(final boolean hideFreshBtn, final boolean clearMap) {

        //移动或者缩放之类的产生新的数据请求的时候，需要隐藏掉按键，避免冲突
        if (hideFreshBtn) {
            hideLoading();
        }

        //取消掉原本的请求
        if (mHandler != null && mNetRunnable != null)
            mHandler.removeCallbacks(mNetRunnable);

        //标志位已经移动了
        mIsMove = true;

        mNetRunnable = new NetRunnable(clearMap);
        //等待确定请求逻辑
        mHandler.postDelayed(mNetRunnable, 1300);

    }

    /**
     * 请求数据
     */
    private void netDataLogic(boolean isClearStatus) {
        mHadRequest = true;
        //是否清除掉地图上已经有的
        clearStatus(isClearStatus);
        //请求当前数据是第几页
        requestData(mIndex);
    }

    /**
     * 发起真正的请求
     */
    private void requestData(int pageIndex) {
        DemoApplication.getApplication().getJobManager().clear();
        //这个UUID，用于判断当前回来的数据是否为最新请求的数据
        mUUID = UUID.randomUUID().toString();
        //将请求的JOB发布，请求数据，组装数据并返回。
        DemoApplication.getApplication().getJobManager().addJob(new RequestLogicJob(mSearchModel, pageIndex, mUUID));

    }

    /**
     * 显示地图ICON marker
     */
    private void showMapData(List<LBSModel> dataList, List<ClusterBaiduItem> clusterList, int totalCount) {
        //总数
        mTotalCount = totalCount;
        //lbs数据列表
        mDataList = dataList;
        //组装好的百度item
        mClusterBaiduItems = clusterList;
        //计算有多少页，这里不保险，因为有时候一页不一定是你需要的数量
        int page = (int) Math.ceil(((float) totalCount / DemoApplication.PAGE_SIZE)) - 1;
        //最大页数
        mMaxPageSize = (page >= 0) ? page : 0;

        if (mTotalCount == 0 && (mDataList == null || mDataList.size() == 0)) {
            mIndex = 0;
        }
        //清除聚合管理器数据
        mClusterManager.clearItems();
        //重新加入聚合管理器数据
        mClusterManager.addItems(mClusterBaiduItems);

        mBaiduMap.clear();

        //更新状态
        if (mBaiduMap != null && mCurrentMapStatus != null)
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mCurrentMapStatus));
        //更新页面
        if (mClusterManager != null)
            mClusterManager.cluster();

        //下载网络icon
        DownLoadIcons(mClusterBaiduItems);

        //显示刷新按键
        showLoading();

        dismissLoadingDialog();

    }

    /**
     * 下载lbs数据中对应的icon作为marker
     */
    private void DownLoadIcons(List<ClusterBaiduItem> clusterBaiduItems) {
        if (clusterBaiduItems == null || clusterBaiduItems.size() == 0)
            return;
        List<IconModel> logoUrl = new ArrayList<>();
        for (int i = 0; i < clusterBaiduItems.size(); i++) {
            ClusterBaiduItem clusterBaiduItem = clusterBaiduItems.get(i);
            //将所有没有下载的market拿出来
            if (!TextUtils.isEmpty(clusterBaiduItem.getMarkerUrl()) && !new File(clusterBaiduItem.getUrlLocalMarkerIconPath()).exists()) {
                IconModel iconModel = new IconModel();
                iconModel.setUrl(clusterBaiduItem.getMarkerUrl());
                iconModel.setId(clusterBaiduItem.getLBAModel().getUid());
                logoUrl.add(iconModel);
            }
        }
        //执行下载图标的job
        if (logoUrl.size() > 0) {
            IConJob iConJob = new IConJob(MainActivity.this, logoUrl);
            DemoApplication.getApplication().getJobManager().addJob(iConJob);
        }
    }


    /**
     * 下载的图标发生了改变
     */
    private void ChangeIconLogic(IconEvent e) {
        for (ClusterBaiduItem clusterBaiduItem : mClusterBaiduItems) {
            LBSModel lbsModel = clusterBaiduItem.getLBAModel();
            //此处根据id设置对应的图片
            if (lbsModel.getUid() == e.geteId()) {
                BitmapDescriptor bitmapDescriptor;
                if (!TextUtils.isEmpty(clusterBaiduItem.getUrlLocalMarkerIconPath()) &&
                        new File(clusterBaiduItem.getUrlLocalMarkerIconPath()).exists()) {
                    bitmapDescriptor = clusterBaiduItem.getUrlMarkerIconBitmapDescriptor(false);
                    if (bitmapDescriptor == null) {
                        bitmapDescriptor = clusterBaiduItem.getBitmapDescriptor();
                    }
                } else {
                    bitmapDescriptor = clusterBaiduItem.getBitmapDescriptor();
                }
                //从聚合管理器里面拿到marker，动态改变它
                Marker marker = mClusterManager.getDefaultClusterRenderer().getMarker(clusterBaiduItem);
                if (marker != null) {
                    marker.setIcon(bitmapDescriptor);
                }
                //刷新
                mClusterManager.cluster();
                return;
            }

        }
    }


    /**
     * 点击逻辑
     */
    private void IconClick(ClusterBaiduItem clusterBaiduItem) {
        if (mPreClickItem != null) {
            BitmapDescriptor bitmapDescriptor;
            if (!TextUtils.isEmpty(mPreClickItem.getUrlLocalMarkerIconPath()) &&
                    new File(mPreClickItem.getUrlLocalMarkerIconPath()).exists()) {
                bitmapDescriptor = mPreClickItem.getUrlMarkerIconBitmapDescriptor(false);
                if (bitmapDescriptor == null) {
                    bitmapDescriptor = mPreClickItem.getBitmapDescriptor();
                }
            } else {
                bitmapDescriptor = mPreClickItem.getBitmapDescriptor();
            }
            //从聚合管理器里面拿到marker，动态改变它
            Marker marker = mClusterManager.getDefaultClusterRenderer().getMarker(mPreClickItem);
            if (marker != null) {
                marker.setIcon(bitmapDescriptor);
            }
        }

        if (clusterBaiduItem != null) {
            BitmapDescriptor bitmapDescriptor;
            if (!TextUtils.isEmpty(clusterBaiduItem.getUrlLocalMarkerIconPath()) &&
                    new File(clusterBaiduItem.getUrlLocalMarkerIconPath()).exists()) {
                bitmapDescriptor = clusterBaiduItem.getUrlMarkerIconBitmapDescriptor(true);
                if (bitmapDescriptor == null) {
                    bitmapDescriptor = clusterBaiduItem.getBitmapDescriptor();
                }
            } else {
                bitmapDescriptor = clusterBaiduItem.getBitmapDescriptor();
            }
            //从聚合管理器里面拿到marker，动态改变它
            Marker marker = mClusterManager.getDefaultClusterRenderer().getMarker(clusterBaiduItem);
            if (marker != null) {
                marker.setIcon(bitmapDescriptor);
            }
            //刷新
            mClusterManager.cluster();

        }
        mPreClickItem = clusterBaiduItem;
    }


    /**
     * 清除各种状态
     */
    protected void clearStatus(boolean isClearPageIndex) {
        mBaiduMap.clear();
        mClusterManager.clearItems();
        mClusterBaiduItems.clear();
        if (isClearPageIndex)
            mIndex = 0;
        mIsMove = false;
    }


    /**
     * 请求数据对应的返回
     */
    public void onEventMainThread(RequestDataEvent e) {

        if (e.getEventType() == RequestDataEvent.SUCCESS) {
            //成功，是最新的请求
            if (mUUID.equals(e.getUUID())) {
                showMapData(e.getDataList(), e.getClusterBaiduItems(), e.getTotalSize());
            }
        } else if (e.getEventType() == RequestDataEvent.DEFAULT) {

        } else {
            //没有数据
            if (mUUID.equals(e.getUUID())) {
                if (e.getEventType() == RequestDataEvent.FAIL)
                    Toast.makeText(this, "加载数据失败。", Toast.LENGTH_SHORT).show();
                showLoading();
            }
        }

    }

    /**
     * 下载完图片完成
     */
    public void onEventMainThread(IconEvent e) {
        ChangeIconLogic(e);
    }


    /**
     * 网络请求执行逻辑
     */
    private class NetRunnable implements Runnable {

        private boolean isClearStatus;

        public NetRunnable() {
            super();
        }

        public NetRunnable(boolean isClearStatus) {
            super();
            this.isClearStatus = isClearStatus;
        }

        @Override
        public void run() {
            netDataLogic(isClearStatus);
        }
    }

}
