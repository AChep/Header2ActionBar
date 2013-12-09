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
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Space;

/**
 * Little header fragment.
 * <p>
 * Created by AChep@xda <artemchep@gmail.com>
 * </p>
 */
public class HeaderFragment extends Fragment {

    private static final String TAG = "HeaderFragment";

    private FrameLayout mRoot;
    private View mContentOverlay;

    private View mHeader;
    private int mHeaderHeight;
    private int mCurrentHeaderHeight;
    private int mCurrentHeaderTranslateY;

    private Space mFakeHeader;
    private boolean mListViewEmpty;

    private AbsListView.OnScrollListener mOnScrollListener;
    private OnHeaderScrollChangeListener mOnHeaderScrollChangeListener;

    public interface OnHeaderScrollChangeListener {
        public void onHeaderScrollChanged(float progress, int height, int scroll);
    }

    public void setOnHeaderScrollChangeListener(OnHeaderScrollChangeListener listener) {
        mOnHeaderScrollChangeListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Activity activity = getActivity();

        mHeader = inflater.inflate(getHeaderResource(), container, false);
        mHeaderHeight = mHeader.getLayoutParams().height;
        onPrepareHeaderView(mHeader);

        // Perform fake header view.
        mFakeHeader = new Space(activity);
        mFakeHeader.setLayoutParams(new ListView.LayoutParams(
                0, mHeaderHeight));

        View content = inflater.inflate(getContentResource(), container, false);
        assert content != null;
        if (content instanceof ListView) {
            final ListView listView = (ListView) content;

            mListViewEmpty = true;
            listView.addHeaderView(mFakeHeader);
            onPrepareContentListView(listView);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                    if (mOnScrollListener != null) {
                        mOnScrollListener.onScrollStateChanged(absListView, scrollState);
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    if (mListViewEmpty) { // poor poor listview :(
                        updateHeaderScroll(0);
                    } else {
                        final View child = absListView.getChildAt(0);
                        if (child == mFakeHeader) {
                            updateHeaderScroll(child.getTop());
                        } else {
                            updateHeaderScroll(-mHeaderHeight);
                        }
                    }

                    if (mOnScrollListener != null) {
                        mOnScrollListener.onScroll(absListView, firstVisibleItem,
                                visibleItemCount, totalItemCount);
                    }
                }
            });
        } else {
            onPrepareContentView(content);

            // Merge fake header view and content view
            final LinearLayout ll = new LinearLayout(activity);
            ll.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(mFakeHeader);
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

        mRoot = new FrameLayout(activity);
        mRoot.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mRoot.addView(mHeader);

        // Overlay view always shows at the top of content.
        mContentOverlay = onCreateContentOverlayView();
        if (mContentOverlay != null) {
            mRoot.addView(mContentOverlay, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        // Initial scroll
        mRoot.post(new Runnable() {
            @Override
            public void run() {
                mCurrentHeaderHeight = Integer.MIN_VALUE;
                mCurrentHeaderTranslateY = Integer.MIN_VALUE;
                updateHeaderScroll(0);
            }
        });

        return mRoot;
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
            mHeader.setTranslationY(transY);
            mCurrentHeaderTranslateY = transY;

            if (mContentOverlay != null) {
                final ViewGroup.LayoutParams lp = mContentOverlay.getLayoutParams();
                final int delta = mHeaderHeight + scrollTo;
                lp.height = mRoot.getHeight() - delta;
                mContentOverlay.setLayoutParams(lp);
                mContentOverlay.setTranslationY(delta);
            }

            notifyOnHeaderScrollChangeListener((float) -scrollTo / mHeaderHeight,
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
     * @see #setListViewAdapter(android.widget.ListView, android.widget.ListAdapter)
     */
    public void onPrepareContentListView(ListView listView) { /* for my child */ }

    public void setListViewAdapter(ListView listView, ListAdapter adapter) {
        mListViewEmpty = adapter == null;
        listView.removeHeaderView(mFakeHeader);
        listView.addHeaderView(mFakeHeader);
        listView.setAdapter(adapter);
    }

    public void setListViewOnScrollListener(AbsListView.OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    /**
     * Called if the content's parent is NOT a {@link android.widget.ListView ListView}.
     *
     * @see #getContentResource()
     */
    public void onPrepareContentView(View view) { /* for my child */ }

    public View onCreateContentOverlayView() {
        return null;
    }

}
