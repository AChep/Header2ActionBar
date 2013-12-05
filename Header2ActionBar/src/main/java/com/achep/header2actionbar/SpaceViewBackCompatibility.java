package com.achep.header2actionbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.Space;

/**
 * Class returns instance of the Space view
 * @see android.widget.Space
 *
 * Created by nixan on 05.12.13.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class SpaceViewBackCompatibility {

    public static View getSpace(Context context) {
        return new Space(context);
    }
}
