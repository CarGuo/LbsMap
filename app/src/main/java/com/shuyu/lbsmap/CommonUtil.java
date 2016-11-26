package com.shuyu.lbsmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;


/**
 * Created by shuyu on 2016/11/26.
 */

public class CommonUtil {

    public static Point bitmapSize;

    public static Point getBitmapSize() {

        if (bitmapSize == null) {
            Bitmap var2 = BitmapFactory.decodeResource(DemoApplication.get().getResources(), R.drawable.icon_redbag);
            bitmapSize = new Point(var2.getWidth(), var2.getHeight());
            var2.recycle();
        }

        return bitmapSize;

    }
}
