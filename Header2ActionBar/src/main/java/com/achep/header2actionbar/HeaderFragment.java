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

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Little header fragment.
 * <p>
 * Created by AChep@xda <artemchep@gmail.com>
 * </p>
 */
public class HeaderFragment extends Fragment {

    private static final String TAG = "HeaderFragment";

    private View mHeader;
    private int mHeaderHeight;
    private int mCurrentHeaderHeight;
    private int mCurrentHeaderTranslateY;

    private OnHeaderScrollChangeListener mOnHeaderScrollChangeListener;

    public interface OnHeaderScrollChangeListener {
        public void onHeaderScrollChanged(float progress, int height, int scroll);
    }

    public void setOnHeaderScrollChangeListener(OnHeaderScrollChangeListener listener) {
        mOnHeaderScrollChangeListener = listener;
    }

    private View getSpaceView(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? SpaceViewBackCompatibility.getSpace(activity) : new LinearLayout(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Activity activity = getActivity();

        mHeader = inflater.inflate(getHeaderResource(), container, false);
        mHeaderHeight = mHeader.getLayoutParams().height;
        mCurrentHeaderHeight = mHeaderHeight;
        mCurrentHeaderTranslateY = 0;
        onPrepareHeaderView(mHeader);

        // Perform fake header view.
        final View fakeHeader = getSpaceView(activity);
        fakeHeader.setLayoutParams(new ListView.LayoutParams(
                0, mHeaderHeight));

        View content = inflater.inflate(getContentResource(), container, false);
        assert content != null;
        if (content instanceof ListView) {
            final ListView listView = (ListView) content;

            onPrepareContentListView(listView);
            listView.addHeaderView(fakeHeader);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) { /* unused */ }

                @Override
                public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                    final View child = absListView.getChildAt(0);
                    if (child == fakeHeader) {
                        updateHeaderScroll(child.getTop());
                    } else {
                        updateHeaderScroll(-mHeaderHeight);
                    }
                }
            });
        } else {
            onPrepareContentView(content);

            // Add fake header view
            final LinearLayout ll = new LinearLayout(activity);
            ll.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(fakeHeader);
            ll.addView(content);

            final NotifyingScrollView scrollView = new NotifyingScrollView(activity);
            scrollView.addView(ll);
            scrollView.setOnScrollChangedListener(new NotifyingScrollView.OnScrollChangedListener() {
                @Override
                public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                    updateHeaderScroll(-t);
                }
            });
            content = scrollView;
        }

        // Initial notify
        notifyOnHeaderScrollChangeListener(0, mHeaderHeight, 0);

        final FrameLayout root = new FrameLayout(activity);
        root.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(mHeader);
        return root;
    }

    private void updateHeaderScroll(int scrollTo) {
        scrollTo = scrollTo > 0 ? 0 : scrollTo < -mHeaderHeight ? -mHeaderHeight : scrollTo;

        final boolean allowChangeHeight = isHeaderHeightFloating();
        final int height = mHeaderHeight + scrollTo / 2;
        final int transY = allowChangeHeight ? scrollTo / 2 : scrollTo;

        if (height != mCurrentHeaderHeight && allowChangeHeight) {
            final ViewGroup.LayoutParams lp = mHeader.getLayoutParams();
            lp.height = height;
            mHeader.setLayoutParams(lp);
            mCurrentHeaderHeight = height;
        }
        if (transY != mCurrentHeaderTranslateY) {
            ObjectAnimator.ofInt(mHeader, "translationY", transY);
//            mHeader.setTranslationY(transY);
            mCurrentHeaderTranslateY = transY;

            notifyOnHeaderScrollChangeListener( (float) -scrollTo / mHeaderHeight,
                    mHeaderHeight, -scrollTo);
        }
    }

    private void notifyOnHeaderScrollChangeListener(float progress, int height, int scroll) {
        if (mOnHeaderScrollChangeListener != null) {
            // Notify upper fragment to update ActionBar's alpha or whatever.
            mOnHeaderScrollChangeListener.onHeaderScrollChanged(progress, height, scroll);
        }
    }

    /**
     * If true, header's height might be changed on scroll.
     * <p>Note: It takes a lot of calculations to measure the header all the time.</p>
     */
    public boolean isHeaderHeightFloating() {
        return false;
    }

    /**
     * Int reference to header's resource.
     *
     * @see #onPrepareHeaderView(android.view.View)
     * @see #getContentResource()
     */
    public int getHeaderResource() {
        return 0;
    }

    /**
     * This is the place for setting up the header.
     *
     * @param view inflated header view.
     * @see #getHeaderResource()
     */
    public void onPrepareHeaderView(View view) { /* for my child */ }

    /**
     * Int reference to content's resource.
     * <p>
     * <b>Attention</b>: Parent view must be {@link android.widget.ListView ListView}
     * or something else which will work inside of {@link android.widget.ScrollView ScrollView}.
     * Otherwise it <b>WON'T</b> work.
     * </p>
     *
     * @see #getHeaderResource()
     * @see #onPrepareContentListView(ListView)
     */
    public int getContentResource() {
        return 0;
    }

    /**
     * Called if the content's parent is a {@link android.widget.ListView ListView}.
     *
     * @see #getContentResource()
     */
    public void onPrepareContentListView(ListView listView) { /* for my child */ }

    /**
     * Called if the content's parent is NOT a {@link android.widget.ListView ListView}.
     *
     * @see #getContentResource()
     */
    public void onPrepareContentView(View view) { /* for my child */ }

}
