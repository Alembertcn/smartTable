package com.bin.david.form.component;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.TableInfo;
import com.bin.david.form.matrix.MatrixHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class FullHeightSmartTable<T> extends SmartTable<T> {
    private OnScrollChangeListener onVerticalScrollChangeListener;
    public FullHeightSmartTable(Context context) {
        this(context, null);
    }

    public FullHeightSmartTable(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FullHeightSmartTable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    int currentHeight = 0;
    protected MatrixHelper createMatrixHelper() {
        return new MatrixHelper2(getContext());
    }

    private Rect visibleRect = new Rect();

    private ScrollView scrollView;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (scrollView == null) {
            initScrollView();
        }
        getLocalVisibleRect(showRect);
        invalidate(showRect);
    }

    private void initScrollView() {
        ViewParent temParent = getParent();
        while (temParent != null) {
            if (temParent instanceof ScrollView) {
                scrollView = (ScrollView) temParent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    scrollView.setOnScrollChangeListener(new SyncOnScrollChangeListener());
                }
                break;
            }
            temParent = temParent.getParent();
        }
    }

    public void setOnVerticalScrollChangeListener(OnScrollChangeListener onVerticalScrollChangeListener) {
        this.onVerticalScrollChangeListener = onVerticalScrollChangeListener;
    }

    class SyncOnScrollChangeListener implements  OnScrollChangeListener{
        @Override
        public void onScrollChange(View view, int i, int i1, int i2, int i3) {
            matrixHelper.setTranslateY(scrollView.getScrollY());
            getLocalVisibleRect(visibleRect);
            invalidate(visibleRect);
            if(onVerticalScrollChangeListener!=null){
                onVerticalScrollChangeListener.onScrollChange(view, i, i1, i2, i3);
            }
        }
    }

    @Override
    protected void initShowRect() {
        super.initShowRect();
        getLocalVisibleRect(showRect);
        showRect.set(
                showRect.left+getPaddingLeft(), showRect.top,
                getWidth() - getPaddingRight(),
                showRect.bottom
        );
    }

    @Override
    protected void requestReMeasure() {
            // 设置定位表示
            if(currentRow>0) {
                shouldJump.set(true);
            }

            if (!isExactly && getMeasuredHeight() != 0 && tableData != null) {
            if (tableData.getTableInfo().getTableRect() != null) {
                int defaultHeight = tableData.getTableInfo().getTableRect().height()
                        + getPaddingTop();
                int defaultWidth = tableData.getTableInfo().getTableRect().width();
                int[] realSize = new int[2];
                getLocationInWindow(realSize);
                DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
                int screenWidth = dm.widthPixels;
                int screenHeight = dm.heightPixels;
                int maxWidth = screenWidth - realSize[0];
                int maxHeight = screenHeight - realSize[1];
//                defaultHeight = Math.max(defaultHeight, maxHeight);
                defaultWidth = Math.min(defaultWidth, maxWidth);
                //Log.e("SmartTable","old defaultHeight"+this.defaultHeight+"defaultWidth"+this.defaultWidth);
                if (this.defaultHeight != defaultHeight
                        || this.defaultWidth != defaultWidth) {
                    this.defaultHeight = defaultHeight;
                    this.defaultWidth = defaultWidth;
                    // Log.e("SmartTable","new defaultHeight"+defaultHeight+"defaultWidth"+defaultWidth);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            requestLayout();
                        }
                    });
                }else {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            performJump();
                        }
                    },20);
                }
            }
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        performJump();
    }

    private void performJump(){
        if(shouldJump.compareAndSet(true,false)){
            int[] pointLocation = getPointLocation(currentRow,1);
            int[] pointLocation2 = getPointLocation(currentRow-1,1);
            int itemHeight =0;
            if(pointLocation==null || pointLocation2 == null){
                itemHeight = 0;
            }else{
                itemHeight = pointLocation[1] - pointLocation2[1];
            }
            Rect scaleRect = matrixHelper.getZoomProviderRect(showRect, tableRect,
                    tableData.getTableInfo());
            if(pointLocation!=null && scrollView!=null){
                int top = scaleRect==null ? 0 : scaleRect.top;
                int scrollViewHeight = scrollView.getHeight();
                int y = pointLocation[1] - top - ((toCenter?(scrollViewHeight/2 - itemHeight -40):0));
                y = Math.min(y, getMeasuredHeight()- scrollViewHeight);//这里判断的有待商榷
                y = Math.max(0, y);
                matrixHelper.setTranslateY(y);
                scrollView.setScrollY(y);
                postInvalidate();
            }
            currentRow = -1;//防止每次更新都自动跳转
        }

    }

    AtomicBoolean shouldJump=new AtomicBoolean(false);

    class MatrixHelper2 extends MatrixHelper {
        @Override
        protected void initGesture(Context context) {
            super.initGesture(context);
            mGestureDetector = new GestureDetector(context, new OnTableGestureListener2());
        }

        /**
         * 手势帮助类构造方法
         *
         * @param context 用于获取GestureDetector，scroller ViewConfiguration
         */
        public MatrixHelper2(Context context) {
            super(context);
        }

        protected boolean toRectTop() {
            return true;
        }

        protected boolean toRectBottom() {
            return true;
        }

        class OnTableGestureListener2 extends OnTableGestureListener {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return super.onScroll(e1, e2, distanceX, 0f);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
              return super.onFling(e1, e2, velocityX, 0);
            }
        }
    }

    int currentRow;

    public void setCurrentRow(int currentRow){
       setCurrentRow(currentRow,true);
    }

    boolean toCenter;
    public void setCurrentRow(int currentRow,boolean toCenter){
        Log.d("testSmartTable", "setCurrentRow " + currentRow);
        this.toCenter = toCenter;
        this.currentRow = currentRow;
    }

    @Override
    public void postInvalidate() {
        super.postInvalidate();
        Log.d("testSmartTable", "postInvalidate");
    }

    @Override
    public void invalidate() {
        super.invalidate();
        Log.d("testSmartTable", "invalidate");
    }

    @Override
    public void invalidate(Rect dirty) {
        super.invalidate(dirty);
        Log.d("testSmartTable", "invalidate "+dirty);
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        super.invalidate(l, t, r, b);
        Log.d("testSmartTable", "invalidate "+l+" "+t+" "+r+" "+b);
    }
}
