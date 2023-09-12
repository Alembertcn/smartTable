package com.bin.david.form.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.matrix.MatrixHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class FullHeightSmartTable<T> extends SmartTable<T> {
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
                    scrollView.setOnScrollChangeListener(new OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                            matrixHelper.setTranslateY(scrollView.getScrollY());
                            getLocalVisibleRect(visibleRect);
                            invalidate(visibleRect);
                        }
                    });
                }
                break;
            }
            temParent = temParent.getParent();
        }
    }

    @Override
    protected void initShowRect() {
        super.initShowRect();
        getLocalVisibleRect(showRect);
        showRect.set(
                getPaddingLeft(), showRect.top,
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
                defaultHeight = Math.max(defaultHeight, maxHeight);
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

                }
            }
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        performJump();
    }

    public void performJump(){
        boolean ifJump = shouldJump.get();
        if(ifJump){
            shouldJump.set(false);
        }else{
            return;
        }

        int[] pointLocation = getPointLocation(currentRow,1);
        Rect scaleRect = matrixHelper.getZoomProviderRect(showRect, tableRect,
                tableData.getTableInfo());
        if(pointLocation!=null && scrollView!=null){
            int top =scaleRect==null ? 0 : scaleRect.top;
            int y = pointLocation[1] - top;
            matrixHelper.setTranslateY(y);
            scrollView.setScrollY(y);
            postInvalidate();
        }
    }

    AtomicBoolean shouldJump=new AtomicBoolean(false);

    class MatrixHelper2 extends MatrixHelper {
        /**
         * 手势帮助类构造方法
         *
         * @param context 用于获取GestureDetector，scroller ViewConfiguration
         */
        public MatrixHelper2(Context context) {
            super(context);
            mGestureDetector = new GestureDetector(getContext(), new OnTableGestureListener2());
        }

        @Override
        public void onDisallowInterceptEvent(View view, MotionEvent event) {
            super.onDisallowInterceptEvent(view, event);
            ViewParent parent = view.getParent();
            if (zoomRect == null || originalRect == null) {
                parent.requestDisallowInterceptTouchEvent(false);
                return;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float disX = event.getX() - mDownX;
                float disY = event.getY() - mDownY;
                if (Math.abs(disX) > Math.abs(disY)) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
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
                Log.d("testScroll", "OnTableGestureListener2 distanceX:" + distanceX + " distanceY:" + distanceY);
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
        Log.d("testSmartTable", "setCurrentRow " + currentRow);
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
