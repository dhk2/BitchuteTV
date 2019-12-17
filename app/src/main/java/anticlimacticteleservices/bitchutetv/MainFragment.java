/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package anticlimacticteleservices.bitchutetv;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseFragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainFragment extends BrowseSupportFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = 4;
    private static final int NUM_COLS = 80;

    private final Handler mHandler = new Handler();
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundUri;
    private BackgroundManager mBackgroundManager;
    private List popular;
    private List trending;
    private List subscriptions;
    private List history ;
    private List favorites;
    private List all;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        VideoViewModel vvm = ViewModelProvider.AndroidViewModelFactory.getInstance(MainActivity.data.getApplication()  ).create(VideoViewModel.class);
        System.out.println(("view model attached successfully"));
        vvm.getAllVideos().observe(this, new Observer<List<Video>>(){
            @Override
            public void onChanged(List<Video> videos) {
                System.out.println("something changed in the data "+videos.size());
                all =videos;
                popular = new ArrayList<Video>();
                trending = new ArrayList<Video>();
                subscriptions = new ArrayList<Video>();
                history= new ArrayList<Video>();
                favorites = new ArrayList<Video>();
                for (Video v: videos){
                    if (v.getCategory().equals("popular")) {popular.add(v);}
                    if (v.getCategory().equals("trending")) {trending.add(v);}
                    if (v.isWatched()) {history.add(v);}
                }
            }
        });
        System.out.println(("view model observer attached successfully"));

        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        setupUIElements();

        new BitchuteHomePage().execute("https://www.bitchute.com/#listing-popular");
        setupEventListeners();
        loadRows();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    private void loadRows() {
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        if (popular == null){
            return;
        }
        if (popular.size()>0) {
            for (Object v : popular) {
                listRowAdapter.add(v);
            }
            HeaderItem header = new HeaderItem(0, "Popular");
            rowsAdapter.add(new ListRow(header, listRowAdapter));
            rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        }
        if (trending.size()>0) {
            for (Object v : trending) {
                listRowAdapter.add(v);
            }
            HeaderItem header = new HeaderItem(0, "Trending");
            rowsAdapter.add(new ListRow(header, listRowAdapter));
            rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        }
        if (history.size()>0) {
            for (Object v : trending) {
                listRowAdapter.add(v);
            }
            HeaderItem header = new HeaderItem(0, "History");
            rowsAdapter.add(new ListRow(header, listRowAdapter));
            rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        }
        if (all.size()>0) {
            for (Object v : all) {
                listRowAdapter.add(v);
            }
            HeaderItem header = new HeaderItem(0, "All");
            rowsAdapter.add(new ListRow(header, listRowAdapter));
            rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        }

    /*
        List <Channel> cList = MainActivity.data.getAllChannels();
        listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        for (int j = 0; j < cList.size(); j++) {
            listRowAdapter.add(cList.get(j));
        }
        header = new HeaderItem(3,"Suggested Channels");
        rowsAdapter.add(new ListRow(header,listRowAdapter));


     */
        HeaderItem gridHeader = new HeaderItem(4, "PREFERENCES");
        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
       // gridRowAdapter.add(getResources().getString(R.string.grid_view));
      //  gridRowAdapter.add(getString(R.string.error_fragment));
        gridRowAdapter.add("Refresh");
        gridRowAdapter.add("Authenticate");
        gridRowAdapter.add(("Import"));
        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(rowsAdapter);
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = ContextCompat.getDrawable((Context)this.getActivity(), R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor((Context)this.getActivity(), R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(ContextCompat.getColor((Context)this.getActivity(), R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
            }
        });
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }
    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
        mBackgroundTimer.cancel();
    }
    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }
    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Video) {
                Video video = (Video) item;
                Log.d(TAG, "Item: " + item.toString());
                if (video.getMp4().isEmpty()) {
                    Video v = MainActivity.data.getVideo(video.getSourceID());
                    if (null == v || v.getMp4().isEmpty()) {
                        ForeGroundVideoScrape task = new ForeGroundVideoScrape();
                        task.execute((Video) item);
                    } else {
                        video = v;
                    }
                }
                System.out.println("meaning to launch" + video.toCompactString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.VIDEO, video);
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                if (item.equals("Authenticate")) {
                    final Dialog dialog = new Dialog((Context) getActivity());
                    dialog.setContentView(R.layout.importdialog);
                    final WebView webView = dialog.findViewById(R.id.idplayer_window);
                    webView.setWebViewClient(new WebViewClient());
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setJavaScriptEnabled(true);
                    webView.getSettings().setUseWideViewPort(true);
                    webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
                    webView.setScrollbarFadingEnabled(false);
                    webView.loadUrl("https://www.bitchute.com/subscriptions/");
                    dialog.show();
                }
                if (item.equals("Refresh")) {
                    //    new BitchuteHomePage().execute("https://www.bitchute.com/#listing-popular");
                    item = "no";
                }
                if (((String) item).contains(getString(R.string.error_fragment))) {
                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT).show();
                }
            } else if (item instanceof Channel) {

                Channel channel = (Channel) item;
                System.out.println("someone clicked on channel "+channel.toCompactString());
                Log.d(TAG, "Item: " + item.toString());
                ForeGroundChannelScrape task = new ForeGroundChannelScrape();
                task.execute((Channel) item);

                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.VIDEO, channel);
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }
    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {
            if (item instanceof Video) {
                mBackgroundUri = ((Video) item).getThumbnailurl();
                startBackgroundTimer();
            }
            if (item instanceof String){

                System.out.println(item);
            }
        }
    }
    private class UpdateBackgroundTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateBackground(mBackgroundUri);
                }
            });
        }
    }
    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(
                    ContextCompat.getColor((Context)getActivity(), R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }
        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }
}
