package com.shuyu.lbsmap;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
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

    boolean mNeedRequest = false; //初始化时是否需要请求数据

    //城市是否改变
    boolean mIsChangeCity;

    //是否移动
    boolean mIsMove;

    int mTotalCount;

    Handler mHandler = new Handler();

    NetRunnable mNetRunnable;

    List<ClusterBaiduItem> mClusterBaiduItems = new ArrayList<>();

    ClusterBaiduItem mClickItem;

    List<LBSModel> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initListeners();

        showLoadingDialog();

        FingerRequestLogic(false, false);
    }

    @Override
    protected void loading() {
        super.loading();
        FingerRequestLogic(false, false);
    }

    protected void initListeners() {

        //移动，方式，经纬度变化
        mClusterManager.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            //记录上一个状态
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
                //如果首次会出现切换城市的话
                if (mNeedRequest) {
                    mNeedRequest = false;
                    showLoadingDialog();
                    netDataLogic(true);
                    mFrontMapStatus = null;
                } else {
                    if (ScaleAndMoveLogic(mFrontMapStatus, mapStatus)) {//处理移动与放大
                        mFrontMapStatus = null;
                    }
                }
                mCurrentMapStatus = mapStatus;
            }
        });

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //传入点击的marker
                return mMarkerManager.onMarkerClick(marker);
            }
        });

        //单个的点击
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterBaiduItem>() {
            @Override
            public boolean onClusterItemClick(ClusterBaiduItem item) {
                mClickItem = item;
                Toast.makeText(MainActivity.this, item.getLBAModel().getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //聚合的点击
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterBaiduItem>() {
            @Override
            public boolean onClusterClick(Cluster<ClusterBaiduItem> cluster) {
                Toast.makeText(MainActivity.this, "聚合列表：" + cluster.getSize(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }


    /**
     * 地图的手势逻辑
     */
    private boolean ScaleAndMoveLogic(MapStatus frontMapStatus, MapStatus mapStatus) {

        mSearchModel.setGps(mapStatus.bound.getCenter().longitude + "," + mapStatus.bound.getCenter().latitude);

        mSearchModel.setLevel(mapStatus.zoom);

        if (frontMapStatus == null)
            return false;

        //屏幕实际 X/米
        double areaLength1 = DistanceUtil.getDistance(mapStatus.bound.northeast, mapStatus.bound.southwest);

        //半径
        int radius = (int) areaLength1 / 2;//将半径坐标存入到筛选器

        mSearchModel.setRadius(radius);

        //是否放大缩小
        if (frontMapStatus.zoom == mapStatus.zoom) {
            if (frontMapStatus.bound == null)
                return false;
            //移动距离
            double moveLenght = DistanceUtil.getDistance(frontMapStatus.bound.getCenter(), mapStatus.bound.getCenter());
            //如果大于等于半径
            if (moveLenght >= radius / 2) {
                FingerRequestLogic(true, true);
                return true;
            }

            //如果经纬度发生变化了
            if (mChangeStatus != null && (mapStatus.target.latitude) != (int) (mChangeStatus.target.latitude)
                    && (int) (mapStatus.target.longitude) != (int) (mChangeStatus.target.longitude) && mIsChangeCity) {
                FingerRequestLogic(true, true);
                mIsChangeCity = false;
                return true;
            }

            return false;
        } else {//处理放大缩小
            FingerRequestLogic(true, true);
            return true;
        }
    }

    /**
     * 开始因为status变化而产生的请求
     */
    private void FingerRequestLogic(final boolean hideLoadBtn, final boolean isClearStatus) {

        if (hideLoadBtn) {
            hideLoading();
        }

        if (mHandler != null && mNetRunnable != null)
            mHandler.removeCallbacks(mNetRunnable);

        mIsMove = true;

        mNetRunnable = new NetRunnable(isClearStatus);

        mHandler.postDelayed(mNetRunnable, 1500);

    }

    /**
     * 请求数据
     */
    private void netDataLogic(boolean isClearStatus) {
        clearStatus(isClearStatus);
        requestData(mIndex);
    }

    private void requestData(int pageIndex) {
        DemoApplication.getApplication().getJobManager().clear();
        mUUID = UUID.randomUUID().toString();
        DemoApplication.getApplication().getJobManager().addJob(new RequestLogicJob(mSearchModel, pageIndex, mUUID));

    }

    /**
     * 显示地图ICON
     */
    private void showMapData(List<LBSModel> dataList, List<ClusterBaiduItem> clusterList, int lastSize, int totalCount) {
        mTotalCount = totalCount;

        mDataList = dataList;

        mClusterBaiduItems = clusterList;

        int page = (int) Math.ceil(((float) totalCount / DemoApplication.PAGE_SIZE)) - 1;

        mMaxPageSize = (page >= 0) ? page : 0;

        if (mTotalCount == 0 && (mDataList == null || mDataList.size() == 0)) {
            mIndex = 0;
        }

        mClusterManager.clearItems();

        mClusterManager.addItems(mClusterBaiduItems);

        mBaiduMap.clear();

        //定义点聚合管理类ClusterManager
        if (mBaiduMap != null && mCurrentMapStatus != null)
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mCurrentMapStatus));

        if (mClusterManager != null)
            mClusterManager.cluster();

        DownLoadIcons(mClusterBaiduItems);

        showLoading();

        dismissLoadingDialog();

    }

    public static void DownLoadIcons(List<ClusterBaiduItem> clusterBaiduItems) {
        if (clusterBaiduItems == null || clusterBaiduItems.size() == 0)
            return;
        List<IconModel> logoUrl = new ArrayList<>();
        for (int i = 0; i < clusterBaiduItems.size(); i++) {
            ClusterBaiduItem clusterBaiduItem = clusterBaiduItems.get(i);
            if (!TextUtils.isEmpty(clusterBaiduItem.getIcon_url()) && !new File(clusterBaiduItem.getUrlLocalMarkerIconPath()).exists()) {
                IconModel iconModel = new IconModel();
                iconModel.setUrl(clusterBaiduItem.getIcon_url());
                iconModel.setId(clusterBaiduItem.getLBAModel().getGeotable_id());
                logoUrl.add(iconModel);
            }
        }
        if (logoUrl.size() > 0) {
            IConJob iConJob = new IConJob(logoUrl);
            DemoApplication.getApplication().getJobManager().addJob(iConJob);
        }
    }


    private void ChangeIconLogic(IconEvent e) {
        for (ClusterBaiduItem clusterBaiduItem : mClusterBaiduItems) {
            LBSModel lbsModel = clusterBaiduItem.getLBAModel();
            //此处根据id设置图片
            if (lbsModel.getGeotable_id() == e.geteId()) {
                BitmapDescriptor bitmapDescriptor;
                if (!TextUtils.isEmpty(clusterBaiduItem.getUrlLocalMarkerIconPath()) && new File(clusterBaiduItem.getUrlLocalMarkerIconPath()).exists()) {
                    bitmapDescriptor = clusterBaiduItem.getUrlMarkerIconBitmapDescriptor();
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
                mClusterManager.cluster();
                return;
            }

        }
    }


    /**
     * 清除请求状态
     */
    protected void clearStatus(boolean isClearPageIndex) {
        mBaiduMap.clear();
        mClusterManager.clearItems();
        mClusterBaiduItems.clear();
        if (isClearPageIndex)
            mIndex = 0;
        mIsMove = false;
    }


    public void onEventMainThread(RequestDataEvent e) {

        if (e.getEventType() == RequestDataEvent.SUCCESS) {
            if (mUUID.equals(e.getUUID())) {
                showMapData(e.getDataList(), e.getClusterBaiduItems(), e.getLastSize(), e.getTotalSize());
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
     * 下载完图片或者领取了红包
     */
    public void onEventMainThread(IconEvent e) {
        ChangeIconLogic(e);
    }

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
