/*
 * Copyright (C) 2013 AChep@xda <artemchep@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.achep.header2actionbar;

import android.app.ActionBar;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

/**
 * Created by AChep@xda <artemchep@gmail.com>
 */
public class FadingActionBarHelper {

    private static final String TAG = "FadingActionBarHelper";

    private int mAlpha = 255;
    private Drawable mDrawable;
    private boolean isAlphaLocked;

    private final ActionBar mActionBar;

    public FadingActionBarHelper(final ActionBar actionBar) {
        mActionBar = actionBar;
    }

    public FadingActionBarHelper(final ActionBar actionBar, final Drawable drawable) {
        mActionBar = actionBar;
        setActionBarBackgroundDrawable(drawable);
    }

    public void setActionBarBackgroundDrawable(Drawable drawable) {
        setActionBarBackgroundDrawable(drawable, true);
    }

    public void setActionBarBackgroundDrawable(Drawable drawable, boolean mutate) {
        mDrawable = mutate ? drawable.mutate() : drawable;
        mActionBar.setBackgroundDrawable(mDrawable);

        if (mAlpha == 255) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                mAlpha = mDrawable.getAlpha();
        } else {
            setActionBarAlpha(mAlpha);
        }
    }

    /**
     * An {@link android.app.ActionBar} background drawable.
     *
     * @see #setActionBarBackgroundDrawable(android.graphics.drawable.Drawable)
     * @see #setActionBarAlpha(int)
     */
    public Drawable getActionBarBackgroundDrawable() {
        return mDrawable;
    }

    /**
     * Please use this method for global changes only!
     * Otherwise, please, use {@link android.graphics.drawable.Drawable#setAlpha(int)}
     * to {@link #getActionBarBackgroundDrawable()} directly.
     *
     * @param alpha a value from 0 to 255
     * @see #getActionBarBackgroundDrawable()
     * @see #getActionBarAlpha()
     */
    public void setActionBarAlpha(int alpha) {
        if (mDrawable == null) {
            Log.w(TAG, "Set action bar background before setting alpha!");
            return;
        }
        if (!isAlphaLocked) mDrawable.setAlpha(alpha);
        mAlpha = alpha;
    }

    public int getActionBarAlpha() {
        return mAlpha;
    }

    public void setActionBarAlphaLocked(boolean isLocked) {
        isAlphaLocked = isLocked;
    }
}
