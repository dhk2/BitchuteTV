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
import android.os.Bundle;
import android.os.Handler;

import androidx.leanback.app.BackgroundManager;
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

import java.util.ArrayList;
import java.util.Collections;
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
    private List allVideos;
    private boolean upToDate=false;
    private boolean rowsSetup=false;
    private VideoViewModel vvm;
    private ChannelViewModel cvm;
    private ArrayList <Channel> allChannels;
    private ArrayList <Channel> suggested;
    private ArrayList <String> trendingHashtags;
    private ArrayList <String> followingHashtags;
    private boolean debug;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        debug=true;
        popular = new ArrayList<WebVideo>();
        trending = new ArrayList<WebVideo>();
        subscriptions = new ArrayList<WebVideo>();
        history= new ArrayList<WebVideo>();
        favorites = new ArrayList<WebVideo>();
        suggested = new ArrayList<Channel>();

        vvm =   ViewModelProvider.AndroidViewModelFactory.getInstance(MainActivity.data.getApplication()).create(VideoViewModel.class);
        cvm = ViewModelProvider.AndroidViewModelFactory.getInstance(MainActivity.data.getApplication()).create(ChannelViewModel.class);

        if (debug) System.out.println(("view model attached successfully"));
        vvm.getAllVideos().observe(this, new Observer<List<WebVideo>>(){
            @Override
            public void onChanged(List<WebVideo> webVideos) {
                if (debug)System.out.println("something changed in the data "+ webVideos.size());
                followingHashtags = (ArrayList)MainActivity.data.followingHashtags;
                trendingHashtags = (ArrayList)MainActivity.data.trendingHashtags;
                allVideos = webVideos;
                popular = new ArrayList<WebVideo>();
                trending = new ArrayList<WebVideo>();
                subscriptions = new ArrayList<WebVideo>();
                history= new ArrayList<WebVideo>();
                favorites = new ArrayList<WebVideo>();
                trendingHashtags = (ArrayList)MainActivity.data.trendingHashtags;
                followingHashtags = (ArrayList)MainActivity .data.followingHashtags;
                for (WebVideo v: webVideos){
                    if (v.getCategory().equals("popular")) {popular.add(v);}
                    if (v.getCategory().equals("trending")) {trending.add(v);}
                    if (v.isWatched()) {history.add(v);}
                }
                Collections.sort(popular);
                if (debug) System.out.println("popular:"+popular.size()+" trending"+trending.size()+ "history:"+history.size());
                if (!rowsSetup){
                    loadRows();
                    rowsSetup=true;
                    upToDate=true;
                }
                else {
                    upToDate=false;
                }
            }
        });

        cvm.getAllChannels().observe(this, new Observer<List<Channel>>(){
            @Override
            public void onChanged(List<Channel> channels) {
                if (debug) System.out.println("something changed in the channel data "+channels.size());
                allChannels = (ArrayList)channels;

                suggested = new ArrayList<Channel>();
                for (Channel c:allChannels){
                    if (c.getYoutubeID().equals("suggested")) {suggested.add(c);}
                }
            }
        });
        allChannels=(ArrayList)cvm.getDeadChannels();
        allVideos=(ArrayList)vvm.getDeadVideos();
        popular = new ArrayList<WebVideo>();
        trending = new ArrayList<WebVideo>();
        subscriptions = new ArrayList<WebVideo>();
        history= new ArrayList<WebVideo>();
        favorites = new ArrayList<WebVideo>();
        for (WebVideo v: (ArrayList<WebVideo>) allVideos){
            if (v.getCategory().equals("popular")) {popular.add(v);}
            if (v.getCategory().equals("trending")) {trending.add(v);}
            if (v.isWatched()) {history.add(v);}
        }
        System.out.println(popular.size()+" popular videos "+trending.size()+"trending, of total "+allVideos.size());
        super.onActivityCreated(savedInstanceState);
        new Bitchute.BitchuteHomePage().execute("https://www.bitchute.com/#listing-popular");
        if ((followingHashtags != null) && (followingHashtags.size()>0)) {
            for (String g : followingHashtags) {
                new Bitchute.GetVideos().execute("/hashtag/" + g);
            }
        }
        prepareBackgroundManager();

        setupUIElements();

        setupEventListeners();

        loadRows();
    }

    @Override
    public void onStart() {
        super.onStart();
        //TODO make this work without losing place in row on reload
        if (rowsSetup && !upToDate){
         loadRows();
         rowsSetup=true;
         upToDate=true;
       }
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
        int headerID=0;
        if (allVideos == null  || allVideos.size()<1){
            return;
        }

        if (popular.size()>0) {
            for (Object v : popular) {
                listRowAdapter.add(v);
            }
            HeaderItem header = new HeaderItem(headerID, "Popular");
            rowsAdapter.add(new ListRow(header, listRowAdapter));
            //rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            headerID++;
        }
        if (trending.size()>0) {
            for (Object v : trending) {
                listRowAdapter.add(v);
            }
            HeaderItem header = new HeaderItem(headerID, "Trending");
            rowsAdapter.add(new ListRow(header, listRowAdapter));
           // rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            headerID++;
        }
        if (history.size()>0) {
            for (Object v : trending) {
                listRowAdapter.add(v);
            }
            HeaderItem header = new HeaderItem(headerID, "History");
            rowsAdapter.add(new ListRow(header, listRowAdapter));
           // rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            headerID++;
        }
        if (MainActivity.data.followingHashtags.size()>0) {
            for (Object g : MainActivity.data.followingHashtags) {
                String tag=(String)g;
                for (Object v:allVideos){
                    WebVideo vid =(WebVideo)v;
                    System.out.println(((WebVideo) v).toDebugString());
                    if ((vid.getHashtags()).contains(tag)){
                        listRowAdapter.add(v);
                    }
                }

                HeaderItem header = new HeaderItem(headerID,tag );
                rowsAdapter.add(new ListRow(header, listRowAdapter));
                listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                headerID++;
            }
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        }

        if (allVideos.size()>0) {
            for (Object v : allVideos) {
                listRowAdapter.add(v);
            }
            HeaderItem header = new HeaderItem(headerID, "All");
            rowsAdapter.add(new ListRow(header, listRowAdapter));
          //  rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            headerID++;
        }
        if (suggested.size()>0) {
            for (int j = 0; j < suggested.size(); j++) {
                listRowAdapter.add(suggested.get(j));
            }
            HeaderItem header = new HeaderItem(headerID, "Suggested Channels");
            rowsAdapter.add(new ListRow(header, listRowAdapter));
            cardPresenter = new CardPresenter();
            listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            headerID++;
        }
        if ((trendingHashtags != null) && (trendingHashtags.size()>0)) {
            HeaderItem gridHeader = new HeaderItem(headerID, "Trending Hashtags");
            GridItemPresenter mGridPresenter = new GridItemPresenter();
            ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
            // gridRowAdapter.add(getResources().getString(R.string.grid_view));
            //  gridRowAdapter.add(getString(R.string.error_fragment));
            boolean found = false;
            for (String g : trendingHashtags) {
                gridRowAdapter.add(g);
                found=true;
            }
            if (found) {
                rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));
                headerID++;
            }
        }

        setAdapter(rowsAdapter);




         HeaderItem gridHeader = new HeaderItem(headerID, "PREFERENCES");
        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
       // gridRowAdapter.add(getResources().getString(R.string.grid_view));
      //  gridRowAdapter.add(getString(R.string.error_fragment));
        gridRowAdapter.add("Refresh");
        gridRowAdapter.add("Authenticate");
        gridRowAdapter.add(("Import"));
        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));
        headerID++;
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
            System.out.println(row.toString()+"><"+rowViewHolder.toString());
            if (item instanceof WebVideo) {
                WebVideo webVideo = (WebVideo) item;
                Log.d(TAG, "Item: " + item.toString());
                System.out.println("meaning to launch" + webVideo.toCompactString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.VIDEO, webVideo);
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                String g = (String) item;
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
                if (g.equals("Refresh")) {
                    System.out.println("refreshed "+allVideos.size()+" "+allChannels.size());
                    System.out.println("maindata:"+MainActivity.data.trendingHashtags.size()+"  mainfragment:"+trendingHashtags.size());
                }
                if (g.startsWith("#")){
                    System.out.println("clicked on a hashtag");
                    System.out.println(((String) item).substring(1));
                    MainActivity.data.followingHashtags.add(g.substring(1));
                    followingHashtags=(ArrayList) MainActivity.data.followingHashtags;
                    new Bitchute.GetVideos().execute("/hashtag/"+g.substring(1));
                }
                item = "no";

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
            if (item instanceof WebVideo) {
                mBackgroundUri = ((WebVideo) item).getThumbnailurl();
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
