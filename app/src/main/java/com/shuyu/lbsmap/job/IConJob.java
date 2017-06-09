package com.shuyu.lbsmap.job;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.baidu.mapapi.clusterutil.clustering.view.DefaultClusterRenderer;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.shuyu.lbsmap.DemoApplication;
import com.shuyu.lbsmap.event.IconEvent;
import com.shuyu.lbsmap.model.IconModel;
import com.shuyu.lbsmap.utils.CommonUtil;
import com.shuyu.lbsmap.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.finalteam.okhttpfinal.FileDownloadCallback;
import cn.finalteam.okhttpfinal.HttpRequest;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static com.shuyu.lbsmap.utils.CommonUtil.dip2px;
import static com.shuyu.lbsmap.utils.CommonUtil.getBitmapSize;
import static com.shuyu.lbsmap.utils.FileUtils.BIG_END;
import static com.shuyu.lbsmap.utils.FileUtils.BIG_SIZE;
import static com.shuyu.lbsmap.utils.FileUtils.getLogoNamePath;

public class IConJob extends Job {

    private List<IconModel> logoUrlList = new ArrayList<>();
    private Handler handler;
    private int size = 0;
    private int bigSize = 0;

    protected IConJob() {
        super(new Params(1000));
    }

    public IConJob(Context context, List<IconModel> logoUrlList) {
        super(new Params(1000));
        this.logoUrlList = logoUrlList;
        handler = new Handler();
        bigSize = dip2px(context, BIG_SIZE);

    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        /*handler.post(new Runnable() {
            @Override
            public void run() {
                if (logoUrlList != null && logoUrlList.size() > 0) {
                    DefaultClusterRenderer.LOADING_LOGO = true;
                    downloadIcon(logoUrlList.get(size));
                }
            }
        });*/

        DefaultClusterRenderer.LOADING_LOGO = true;
        Observable.fromIterable(logoUrlList)
                .filter(new Predicate<IconModel>() {
                    @Override
                    public boolean test(IconModel iconModel) throws Exception {
                        File normalFile = new File(getLogoNamePath(iconModel.getUrl()));
                        File bigFile = new File(getLogoNamePath(iconModel.getUrl()) + BIG_END);
                        boolean flag = !(normalFile.exists() && bigFile.exists());
                        if (!flag) {
                            //通知更新
                            IconEvent iconEvent = new IconEvent(IconEvent.EventType.success);
                            iconEvent.seteId(iconModel.getId());
                            DemoApplication.getApplication().getEventBus().post(iconEvent);
                        }
                        return flag;
                    }
                })
                .flatMap(new Function<IconModel, ObservableSource<IconModel>>() {
                    @Override
                    public ObservableSource<IconModel>  apply(IconModel iconModel) throws Exception {
                        return downloadFile(iconModel);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<IconModel>() {
                    @Override
                    public void onComplete() {
                        DefaultClusterRenderer.LOADING_LOGO = false;
                        IconEvent iconEvent = new IconEvent(IconEvent.EventType.success);
                        iconEvent.seteId(logoUrlList.get(logoUrlList.size() - 1).getId());
                        DemoApplication.getApplication().getEventBus().post(iconEvent);
                        Log.e("******onComplete*****", "onComplete : " );
                    }

                    @Override
                    public void onError(Throwable e) {
                        DefaultClusterRenderer.LOADING_LOGO = false;
                        IconEvent iconEvent = new IconEvent(IconEvent.EventType.success);
                        iconEvent.seteId(logoUrlList.get(logoUrlList.size() - 1).getId());
                        DemoApplication.getApplication().getEventBus().post(iconEvent);
                        Log.e("***********", "Throwable : " + e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e("******onSubscribe*****", "onSubscribe : " );
                    }

                    @Override
                    public void onNext(IconModel iconModel) {
                        final String name = getLogoNamePath(iconModel.getUrl()) + "tmp";
                        Point point = getBitmapSize();
                        changeIconToSuccess(name, name.replace("tmp", ""), point.x, point.y);
                        //通知更新
                        IconEvent iconEvent = new IconEvent(IconEvent.EventType.success);
                        iconEvent.seteId(iconModel.getId());
                        DemoApplication.getApplication().getEventBus().post(iconEvent);
                        Log.e("***********", "onNext : " + name);
                    }
                });
    }

    private Observable<IconModel> downloadFile(final IconModel iconModel) {
        final String name = CommonUtil.MD5L(iconModel.getUrl()) + "_" + "tmp";
        return Rx2AndroidNetworking.download(iconModel.getUrl(), FileUtils.getAppPath(), name)
                .build()
                .getDownloadObservable()
                .flatMap(new Function<String, ObservableSource<IconModel>>() {
                    @Override
                    public ObservableSource<IconModel> apply(String user) throws Exception {
                        return Observable.fromArray(iconModel);
                    }
                });
    }

    @Override
    protected void onCancel() {

    }


    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }


    /**
     * 下载图标
     */
    /*private void downloadIcon(final IconModel iconModel) {
        //先保存为临时的，成功了在改名字
        final String name = getLogoNamePath(iconModel.getUrl()) + "tmp";
        File saveFile = new File(name);
        File normalFile = new File(getLogoNamePath(iconModel.getUrl()));
        File bigFile = new File(getLogoNamePath(iconModel.getUrl()) + BIG_END);
        if (normalFile.exists() && bigFile.exists()) {
            //通知更新
            IconEvent iconEvent = new IconEvent(IconEvent.EventType.success);
            iconEvent.seteId(iconModel.getId());
            DemoApplication.getApplication().getEventBus().post(iconEvent);
            //继续看后面还有没有需要下载的
            DownLoadNext();
            return;
        }
        HttpRequest.download(iconModel.getUrl(), saveFile, new FileDownloadCallback() {
            @Override
            public void onDone() {
                super.onDone();
                Point point = getBitmapSize();
                changeIconToSuccess(name, name.replace("tmp", ""), point.x, point.y);
                //通知更新
                IconEvent iconEvent = new IconEvent(IconEvent.EventType.success);
                iconEvent.seteId(iconModel.getId());
                DemoApplication.getApplication().getEventBus().post(iconEvent);
                //继续看后面还有没有需要下载的
                DownLoadNext();
            }

            @Override
            public void onFailure() {
                super.onFailure();
                //失败了删除文件
                File file = new File(name);
                if (file.exists()) {
                    file.delete();
                }
                DownLoadNext();
            }
        });
    }*/


    /*private void DownLoadNext() {
        size += 1;
        if (size > (logoUrlList.size() - 1)) {
            DefaultClusterRenderer.LOADING_LOGO = false;
            IconEvent iconEvent = new IconEvent(IconEvent.EventType.success);
            iconEvent.seteId(logoUrlList.get(logoUrlList.size() - 1).getId());
            DemoApplication.getApplication().getEventBus().post(iconEvent);
        } else {
            String url = logoUrlList.get(size).getUrl();
            final String name = getLogoNamePath(url);
            if (!new File(name).exists()) {
                downloadIcon(logoUrlList.get(size));
            } else {
                DownLoadNext();
            }
        }
    }*/

    private void changeIconToSuccess(String fromFile, String toFile, int width, int height) {
        try {
            //不成图片有何用
            Bitmap bitmap = BitmapFactory.decodeFile(fromFile);
            if (bitmap == null) {
                File file = new File(fromFile);
                if (file.exists()) {
                    file.delete();
                }
                return;
            }
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            // 缩放图片的尺寸
            float scaleWidth = (float) width / bitmapWidth;
            float scaleHeight = (float) height / bitmapHeight;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 产生缩放后的Bitmap对象
            Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            // save file
            File saveFile = new File(toFile);
            FileOutputStream out = new FileOutputStream(saveFile);
            if (resizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            } else {
                out.close();
            }

            scaleWidth = (float) (width + bigSize) / bitmapWidth;
            scaleHeight = (float) (height + bigSize) / bitmapHeight;
            matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap bigBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            saveFile = new File(toFile + BIG_END);
            out = new FileOutputStream(saveFile);
            if (bigBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            } else {
                out.close();
            }
            if (!bitmap.isRecycled()) {
                bitmap.recycle();//记得释放资源，否则会内存溢出
            }

            if (!resizeBitmap.isRecycled()) {
                resizeBitmap.recycle();
            }
            if (!bigBitmap.isRecycled()) {
                bigBitmap.recycle();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}