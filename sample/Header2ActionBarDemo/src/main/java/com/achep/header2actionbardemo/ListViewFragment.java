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
package com.achep.header2actionbardemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.achep.header2actionbar.FadingActionBarActivity;
import com.achep.header2actionbar.HeaderFragment;

import java.lang.ref.WeakReference;

/**
 * Created by Artem on 06.12.13.
 */
public class ListViewFragment extends HeaderFragment {

    private ListView mListView;
    private String[] mListViewTitles;
    private boolean mLoaded;

    private AsyncLoadSomething mAsyncLoadSomething;
    private ProgressBar mProgressBar;

    @Override
    public boolean isHeaderHeightFloating() {
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        setOnHeaderScrollChangeListener(new OnHeaderScrollChangeListener() {
            @Override
            public void onHeaderScrollChanged(float progress, int height, int scroll) {
                height -= getActivity().getActionBar().getHeight();
                progress = (float) scroll / height;
                if (progress > 1f) progress = 1f;
                ((FadingActionBarActivity) getActivity()).setActionBarAlpha((int) (255 * progress));
            }
        });

        cancelAsyncTask(mAsyncLoadSomething);
        mAsyncLoadSomething = new AsyncLoadSomething(this);
        mAsyncLoadSomething.execute();
    }

    @Override
    public void onDetach() {
        cancelAsyncTask(mAsyncLoadSomething);
        super.onDetach();
    }

    @Override
    public int getHeaderResource() {
        return R.layout.fragment_header;
    }

    @Override
    public int getContentResource() {
        return R.layout.fragment_listview;
    }

    @Override
    public View onCreateContentOverlayView() {
        mProgressBar = new ProgressBar(getActivity());
        if (mLoaded) mProgressBar.setVisibility(View.GONE);

        final FrameLayout frameLayout = new FrameLayout(getActivity());
        frameLayout.addView(mProgressBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        return frameLayout;
    }

    @Override
    public void onPrepareContentListView(ListView listView) {
        super.onPrepareContentListView(listView);

        mListView = listView;
        if (mLoaded) setListViewTitles(mListViewTitles);
    }

    private void setListViewTitles(String[] titles) {
        mLoaded = true;
        mListViewTitles = titles;
        if (mListView == null) return;

        mListView.setVisibility(View.VISIBLE);
        setListViewAdapter(mListView, new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_1,
                mListViewTitles));
    }

    private void cancelAsyncTask(AsyncTask task) {
        if (task != null) task.cancel(false);
    }

    // //////////////////////////////////////////
    // ///////////// -- LOADER -- ///////////////
    // //////////////////////////////////////////

    private static class AsyncLoadSomething extends AsyncTask<Void, Void, String[]> {

        private static final String TAG = "AsyncLoadSomething";

        final WeakReference<ListViewFragment> weakFragment;

        public AsyncLoadSomething(ListViewFragment fragment) {
            this.weakFragment = new WeakReference<ListViewFragment>(fragment);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            final ListViewFragment audioFragment = weakFragment.get();
            if (audioFragment.mListView != null) audioFragment.mListView.setVisibility(View.INVISIBLE);
            if (audioFragment.mProgressBar != null) audioFragment.mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(Void... voids) {

            try {
                // Emulate long downloading
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return new String[]{"Placeholder", "Placeholder", "Placeholder", "Placeholder",
                    "Placeholder", "Placeholder", "Placeholder", "Placeholder",
                    "Placeholder", "Placeholder", "Placeholder", "Placeholder",
                    "Placeholder", "Placeholder", "Placeholder", "Placeholder",
                    "Placeholder", "Placeholder", "Placeholder", "Placeholder",
                    "Placeholder", "Placeholder", "Placeholder", "Placeholder"};
        }

        @Override
        protected void onPostExecute(String[] titles) {
            super.onPostExecute(titles);
            final ListViewFragment audioFragment = weakFragment.get();
            if (audioFragment == null) {
                if (Project.DEBUG) Log.d(TAG, "Skipping.., because there is no fragment anymore.");
                return;
            }

            if (audioFragment.mProgressBar != null) audioFragment.mProgressBar.setVisibility(View.GONE);
            audioFragment.setListViewTitles(titles);
        }
    }
}
