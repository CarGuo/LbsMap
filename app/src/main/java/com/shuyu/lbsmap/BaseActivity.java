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

    ClusterManager<ClusterBaiduItem> mClusterManager;

    MarkerManager mMarkerManager;

    //当前图片状态
    Bitmap mCLBitmap;

    //当前地图状态
    MapStatus mCurrentMapStatus;

    //当前地图变化状态
    MapStatus mChangeStatus;

    LatLng mCurrentLatLng;

    String mUUID = "";//请求tag标志，可以设置OKHTTP来抛弃请求

    LoadingDialog mLoadingDialog;

    SearchModel mSearchModel;

    boolean mIsLoading;

    int mMaxPageSize;

    int mIndex;

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

        if (mCLBitmap != null && !mCLBitmap.isRecycled()) {
            mCLBitmap.recycle();
        }

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

        mBaiduMap.setMyLocationEnabled(true);

        //为什么从这里读？这里读出来的size统一
        Bitmap bitmap = CommonUtil.getImageFromAssetsFile(DemoApplication.getApplication(), "current_location.png");

        float scale = 0.72f;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); //长和宽放大缩小的比例
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

        double llat = 22.276012;

        double llng = 113.583087;

        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(0)
                .direction(0).latitude(llat)
                .longitude(llng).build();

        mBaiduMap.setMyLocationData(locData);

        mCurrentLatLng = new LatLng(llng, llat);

        mCurrentMapStatus = new MapStatus.Builder().target(new LatLng(llat, llng)).zoom(14).build();
        // 定义点聚合管理类ClusterManager
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mCurrentMapStatus));


        mSearchModel = new SearchModel();
        mSearchModel.setGps(llng + "," + llat);
        //// TODO: 2016/11/27 设置公里数
        mSearchModel.setRadius(15000);
        mSearchModel.setLevel(14);
        mSearchModel.setTableId(DemoApplication.TABLE_ID());


    }


    protected void loading() {
        mIsLoading = true;
        hideLoading();
    }


    protected void hideLoading() {
        refresh.setVisibility(View.GONE);
        mIsLoading = false;
    }

    protected void showLoaing() {
        refresh.setVisibility(View.VISIBLE);
        mIsLoading = false;

    }


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
