package sjtukc3c.smallcar.Utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Author: wenhao.zhu[weehowe.z@gmail.com]
 * Created on 1:27 AM 12/28/16.
 */

public class CustomViewPager extends ViewPager {
    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //当事件发生在textview上不会响应页面滑动
    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return v instanceof TextView || super.canScroll(v, checkV, dx, x, y);
    }

}
