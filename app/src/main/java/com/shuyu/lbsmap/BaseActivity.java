package com.shuyu.lbsmap;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.clusterutil.MarkerManager;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.shuyu.lbsmap.model.SearchModel;
import com.shuyu.lbsmap.utils.CommonUtil;
import com.shuyu.lbsmap.utils.LocationLevelUtils;
import com.shuyu.lbsmap.view.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shuyu on 2016/11/26.
 */

public class BaseActivity extends AppCompatActivity implements BaiduMap.OnMapLoadedCallback {


    @BindView(R.id.baidu_map)
    MapView mBaiduMapView;
    @BindView(R.id.refresh)
    Button refresh;

    //主要的操作对象是它
    BaiduMap mBaiduMap;

    //聚合管理器
    ClusterManager<ClusterBaiduItem> mClusterManager;

    //图标管理器
    MarkerManager mMarkerManager;

    //当前位置的图片
    Bitmap mCLBitmap;

    //当前地图状态
    MapStatus mCurrentMapStatus;

    //当前地图变化状态
    MapStatus mChangeStatus;

    //请求tag标志，用来判断是否最新请求，对应其实可以设置OKHTTP来抛弃请求
    String mUUID = "";

    //loading框
    LoadingDialog mLoadingDialog;

    //集合搜索的model，更具搜索model组装数据
    SearchModel mSearchModel;

    //是否loading中
    boolean mIsLoading;

    //最大页
    int mMaxPageSize;

    //当前请求页面
    int mIndex;

    //初始化默认搜索范围
    int mDefaultRadius = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initViews();

        initDefaultLocation();

        if (!DemoApplication.getApplication().getEventBus().isRegistered(this)) {
            DemoApplication.getApplication().getEventBus().register(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DemoApplication.getApplication().getEventBus().isRegistered(this)) {
            DemoApplication.getApplication().getEventBus().unregister(this);
        }

        //销毁对应的bitmap
        if (mCLBitmap != null && !mCLBitmap.isRecycled()) {
            mCLBitmap.recycle();
        }
        //销毁地图
        mBaiduMapView.onDestroy();

        DemoApplication.getApplication().getJobManager().clear();
        DemoApplication.getApplication().getJobManager().stop();

        dismissLoadingDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBaiduMapView != null)
            mBaiduMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBaiduMapView != null)
            mBaiduMapView.onPause();
    }


    @Override
    public void onMapLoaded() {
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mCurrentMapStatus));
    }

    @OnClick(R.id.refresh)
    public void onClick() {
        showLoadingDialog();
        PageIndex();
        loading();
    }

    private void initViews() {
        mBaiduMap = mBaiduMapView.getMap();
        // 比例尺控件
        mBaiduMapView.showScaleControl(true);
        // 缩放控件
        mBaiduMapView.showZoomControls(false);
        // 百度地图LoGo -> 这个好像不大好，哈哈哈
        mBaiduMapView.removeViewAt(1);
        //不倾斜
        mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(false);
        //不旋转
        mBaiduMap.getUiSettings().setRotateGesturesEnabled(false);
        //设置缩放层级
        mBaiduMap.setMaxAndMinZoomLevel(19, 12);
        //图标管理器
        mMarkerManager = new MarkerManager(mBaiduMap);
        //聚合与渲染管理器
        mClusterManager = new ClusterManager<>(this, mBaiduMap, mMarkerManager);

        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);

        mBaiduMap.setOnMapLoadedCallback(this);

        mBaiduMap.setMyLocationEnabled(true);

        //为什么从这里读？这里读出来的size统一
        Bitmap bitmap = CommonUtil.getImageFromAssetsFile(DemoApplication.getApplication(), "current_location.png");

        //调整当前位置图片的显示
        float scale = 0.80f;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        mCLBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        bitmap.recycle();

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(mCLBitmap);
        MyLocationConfiguration myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, false, bitmapDescriptor);
        mBaiduMap.setMyLocationConfigeration(myLocationConfiguration);

    }

    /**
     * 初始化地图默认位置
     */
    private void initDefaultLocation() {

        //珠海经纬度
        double llat = 22.276012;

        double llng = 113.583087;

        //显示上方位置的builder
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(0)
                .direction(0).latitude(llat)
                .longitude(llng).build();

        //显示上方位置-珠海
        mBaiduMap.setMyLocationData(locData);

        //显示等级-转换：初始化为mDefaultRadius半径的层级用于显示
        float level = LocationLevelUtils.returnCurZoom(mDefaultRadius);

        //当前地图状态
        mCurrentMapStatus = new MapStatus.Builder().target(new LatLng(llat, llng)).zoom(level).build();

        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mCurrentMapStatus));

        //初始化数据搜索model
        mSearchModel = new SearchModel();
        mSearchModel.setGps(llng + "," + llat);
        mSearchModel.setRadius(mDefaultRadius);
        mSearchModel.setLevel(level);
        mSearchModel.setTableId(DemoApplication.TABLE_ID());


    }

    //刷新
    protected void loading() {
        mIsLoading = true;
        hideLoading();
    }

    //隐藏
    protected void hideLoading() {
        refresh.setVisibility(View.GONE);
        mIsLoading = false;
    }

    //显示
    protected void showLoading() {
        refresh.setVisibility(View.VISIBLE);
        mIsLoading = false;

    }

    //计算刷新的index
    protected void PageIndex() {
        if (mIndex >= mMaxPageSize) {
            mIndex = 0;
        } else {
            mIndex++;
        }
    }

    protected void showLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            return;
        }
        if (isFinishing())
            return;
        mLoadingDialog = new LoadingDialog(this);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.show();
    }

    protected void dismissLoadingDialog() {
        if (isFinishing())
            return;
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }
}
