package com.bin.david.form.data.format;

import com.bin.david.form.data.column.Column;

/**
 * Created by huang on 2017/10/30.
 */

public abstract class IFormat2 implements IFormat  {

    public abstract String format(Column column,int row);

    @Override
    public String format(Object o) {
        return "- -";
    }
}
