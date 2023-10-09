package com.bin.david.form.component;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.format.selected.ISelectFormat;
import com.bin.david.form.data.table.TableData;
import com.bin.david.form.matrix.MatrixHelper;

/**
 * Created by huang on 2018/1/12.
 * 选中操作
 * 这个类用于处理选中操作
 * 暂时只做比较简单点击效果
 */

public class SelectionOperation implements MatrixHelper.OnInterceptListener {
    /**
     * 选中区域
     */
    private static final int INVALID = -1; //无效坐标
    public static final int SELECT_MODE_ITEM=0;
    public static final int SELECT_MODE_ROW=1;
    private int selectMode = SELECT_MODE_ITEM;//选中模式

    private Rect selectionRect;
    private ISelectFormat selectFormat;
    private int selectRow = INVALID;
    private int selectColumn = INVALID;
    private boolean isShow;

    OnSelectListener onSelectListener;

    void reset(){
        isShow = false;
        setSelectionRect(INVALID,INVALID,null);
    }

    SelectionOperation() {
        this.selectionRect = new Rect();
    }

    void setSelectionRect(int selectColumn,int selectRow, Rect rect){
        this.selectRow = selectRow;
        this.selectColumn = selectColumn;
        if(rect != null){
            selectionRect.set(rect);
        }else{
            selectionRect.set(0,0,0,0);
        }
        isShow = selectColumn!=INVALID && selectRow !=INVALID && rect!=null;
        if(onSelectListener!=null){
            onSelectListener.onSelectChange(isShow,selectRow,selectColumn);
        }
    }

    boolean isSelectedPoint( int selectColumn,int selectRow){
       return  selectRow == this.selectRow  && (selectMode == SELECT_MODE_ROW || selectColumn == this.selectColumn);
    }

    void checkSelectedPoint(int selectColumn,int selectRow, Rect rect){

         if(isSelectedPoint(selectColumn,selectRow)){
             selectionRect.set(rect);
             isShow = true;
         }
    }


    public void draw(Canvas canvas, Rect showRect, TableConfig config){

        if(selectFormat !=null && isShow){
          selectFormat.draw(canvas,selectionRect,showRect,config);
        }
    }

    public ISelectFormat getSelectFormat() {
        return selectFormat;
    }

    void setSelectFormat(ISelectFormat selectFormat) {
        this.selectFormat = selectFormat;
    }

    @Override
    public boolean isIntercept(MotionEvent e1, float distanceX, float distanceY) {
        return false;
    }


    public int getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(int selectMode) {
        this.selectMode = selectMode;
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
    }

    public interface OnSelectListener{
        void onSelectChange(boolean isSelect,int selectRow,int selectColumn);
    }
}
