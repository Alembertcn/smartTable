package com.bin.david.form.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.matrix.MatrixHelper;

public class FullHeightSmartTable<T> extends SmartTable<T> {
    public FullHeightSmartTable(Context context) {
        this(context,null);
    }

    public FullHeightSmartTable(Context context, AttributeSet attrs) {
        this(context, attrs,0);
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
        if(scrollView==null){
            initScrollView();
        }
        getLocalVisibleRect(showRect);
        invalidate(showRect);
    }

    private void initScrollView() {
        ViewParent temParent = getParent();
        while (temParent !=null){
            if(temParent instanceof ScrollView){
                scrollView = (ScrollView) temParent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    scrollView.setOnScrollChangeListener(new OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                            matrixHelper.translateY = scrollView.getScrollY();
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isNotifying.get() &&
                getTableData()!=null &&
                getTableData().getTableInfo().getTableRect() != null) {

//            val scaleRect = matrixHelper.getZoomProviderRect(
//                showRect, tableRect,
//                tableData.tableInfo
//            )
//            canvas?.let {
//                it.drawText("- 暂无更多数据 -",10f,scaleRect.top+contentHeight.toFloat()-100,paint)
//            }

            int contentHeight = getTableData().getTableInfo().getTableRect().bottom;
            if (contentHeight > 0 && currentHeight != contentHeight) {
                currentHeight = contentHeight;
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = currentHeight;
                setLayoutParams(layoutParams);
                requestLayout();
            }
        }
    }

    class MatrixHelper2 extends MatrixHelper{
        /**
         * 手势帮助类构造方法
         *
         * @param context 用于获取GestureDetector，scroller ViewConfiguration
         */
        public MatrixHelper2(Context context) {
            super(context);
            mGestureDetector = new GestureDetector(getContext(),new  OnTableGestureListener2());
        }

        @Override
        public void onDisallowInterceptEvent(View view, MotionEvent event) {
            super.onDisallowInterceptEvent(view, event);
            ViewParent parent = view.getParent();
            if (zoomRect == null || originalRect == null) {
                parent.requestDisallowInterceptTouchEvent(false);
                return;
            }
            if(event.getAction() == MotionEvent.ACTION_MOVE){
                float disX = event.getX() - mDownX;
                float disY = event.getY() - mDownY;
                if (Math.abs(disX) > Math.abs(disY)) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
        }

        protected boolean toRectTop(){
            return true;
        }

       protected boolean toRectBottom(){
           return true;
        }

        class OnTableGestureListener2 extends OnTableGestureListener {
           @Override
           public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
               Log.d("testScroll","OnTableGestureListener2 distanceX:"+distanceX+" distanceY:"+distanceY);
               return super.onScroll(e1, e2, distanceX, 0f);
           }

           @Override
           public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
               return super.onFling(e1, e2, velocityX, 0);
           }
        }
    }
}
