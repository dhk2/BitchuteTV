/*
 * Copyright (C) 2014 The Android Open Source Project
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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.app.DetailsSupportFragmentBackgroundController;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsSupportFragment {

    private static final String TAG = "VideoDetailsFragment";

    private static final int ACTION_WATCH = 1;
    private static final int ACTION_SUBSCRIBE = 2;
    private static final int ACTION_GOTO_CHANNEL = 3;
    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;
    private static final int NUM_COLS = 20;

    private WebVideo mSelectedWebVideo;
    private Handler mHandler = new Handler();
    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;
    private Object mSelectedItem;
    private Channel mSelectedChannel;
    private DetailsSupportFragmentBackgroundController mDetailsBackground;
    private String mp4;
    private boolean waitingForUpdate;
    VideoViewModel vvm;
    ChannelViewModel cvm;
//    ChannelRepository repository;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate DetailsFragment");
        waitingForUpdate=false;
        setOnItemViewClickedListener(new VideoDetailsFragment.ItemViewClickedListener());
        vvm = ViewModelProvider.AndroidViewModelFactory.getInstance(MainActivity.data.getApplication()).create(VideoViewModel.class);
        cvm = ViewModelProvider.AndroidViewModelFactory.getInstance(MainActivity.data.getApplication()).create(ChannelViewModel.class);
        vvm.getAllVideos().observe(this, new Observer<List<WebVideo>>(){
            @Override
            public void onChanged(List<WebVideo> webVideos) {
                System.out.println("something changed in teh data");

                if (mSelectedWebVideo != null) {

                    System.out.println("something changed, currently displaying " + mSelectedWebVideo.toCompactString());
                    System.out.println("waiting for update:" + waitingForUpdate);
                    if (waitingForUpdate) {
                        if (!mSelectedWebVideo.getMp4().isEmpty()) {
                            mAdapter.clear();
                            setupDetailsOverviewRow();
                            setupDetailsOverviewRowPresenter();
                            setAdapter(mAdapter);
                            initializeBackground(mSelectedWebVideo);
                            setupRelatedMovieListRow();
                            waitingForUpdate = false;
                        } else {
                            System.out.println("not updating display view because we still dont have a n mp4");
                        }
                    }
                }
            }
        });
        System.out.println(("vvm completed"));
        mDetailsBackground = new DetailsSupportFragmentBackgroundController(this);
        //looks ugly
        System.out.println("set backgrojnd controller");
        mSelectedItem = getActivity().getIntent().getSerializableExtra(DetailsActivity.VIDEO);
        if (mSelectedItem instanceof WebVideo){
            System.out.println("it's a video");
            mSelectedWebVideo = (WebVideo) mSelectedItem;
        }
        if (mSelectedItem instanceof Channel){
            System.out.println("it's a channel");
            mSelectedChannel = (Channel) mSelectedItem;
        }
        if (mSelectedWebVideo != null) {
            System.out.println("processing video");

            if (!mSelectedWebVideo.getAuthorSourceID().isEmpty()) {
                //
                Channel chan=null;
                for (Channel c :cvm.getDeadChannels()){
                    if (c.getSourceID().equals(mSelectedWebVideo.getAuthorSourceID())){
                        chan = c;
                        if (c.getDescription().isEmpty()){
                            new ForeGroundChannelScrape().execute(c);
                        }
                    }
                }
                if (chan == null) {
                    chan = new Channel("http://www.bitchute.com/channel/" + mSelectedWebVideo.getAuthorSourceID());
                }
                if (chan.getDescription().isEmpty()){
                    new ForeGroundChannelScrape().execute(chan);
                }
            }

            mPresenterSelector = new ClassPresenterSelector();
            mAdapter = new ArrayObjectAdapter(mPresenterSelector);
            if (mSelectedWebVideo.getMp4().isEmpty()){
                ForeGroundVideoScrape task = new ForeGroundVideoScrape();
                task.execute(mSelectedWebVideo);
                waitingForUpdate=true;
            }
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setAdapter(mAdapter);
            initializeBackground(mSelectedWebVideo);
            setupRelatedMovieListRow();
           // System.out.println(mSelectedWebVideo.toCompactString());
        }
        else if(mSelectedChannel != null){
            mPresenterSelector = new ClassPresenterSelector();
            mAdapter = new ArrayObjectAdapter(mPresenterSelector);
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setAdapter(mAdapter);
            initializeBackground(mSelectedChannel);
            setupRelatedMovieListRow();
           // System.out.println(mSelectedChannel.toCompactString());

            if (mSelectedChannel.getDescription().isEmpty()) {
                System.out.println("channel is missing data "+mSelectedChannel);
                ForeGroundChannelScrape task = new ForeGroundChannelScrape();
                task.execute(mSelectedChannel);
                waitingForUpdate=true;
            }
        }
        else {
           Intent intent = new Intent(getActivity(), MainActivity.class);
           startActivity(intent);
        }
    }

    private void initializeBackground(WebVideo data) {
        mDetailsBackground.enableParallax();
        Glide.with(getActivity())
                .load(data.getThumbnailurl())
                .asBitmap()
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap,
                                                GlideAnimation<? super Bitmap> glideAnimation) {
                        mDetailsBackground.setCoverBitmap(bitmap);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });
    }

    private void initializeBackground(Channel data) {
        mDetailsBackground.enableParallax();
        Glide.with(getActivity())
                .load(data.getThumbnailurl())
                .asBitmap()
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap,
                                                GlideAnimation<? super Bitmap> glideAnimation) {
                        mDetailsBackground.setCoverBitmap(bitmap);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });
    }


    private void setupDetailsOverviewRow() {
        final DetailsOverviewRow row;
        if (null==this.getActivity()){
            System.out.println("ghost context");
            return;}
        String thumbnail="";
        if (null != mSelectedWebVideo) {
            row = new DetailsOverviewRow(mSelectedWebVideo);
            thumbnail= mSelectedWebVideo.getThumbnail();
        }
        else if (null !=mSelectedChannel){
            row = new DetailsOverviewRow(mSelectedChannel);
            thumbnail = mSelectedChannel.getThumbnail();
        }
        else {
            System.out.println("no channels or vidoes mselected ");
            return;
        }

        row.setImageDrawable(
                ContextCompat.getDrawable((Context) this.getActivity(), R.drawable.default_background));
        int width = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);
        Glide.with(getActivity())
            .load(thumbnail)
            .centerCrop()
            .error(R.drawable.default_background)
            .into(new SimpleTarget<GlideDrawable>(width, height) {
                @Override
                public void onResourceReady(GlideDrawable resource,
                                            GlideAnimation<? super GlideDrawable>
                                                    glideAnimation) {
                    Log.d(TAG, "details overview card image url ready: " + resource);
                    row.setImageDrawable(resource);
                    mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                }
            });
        ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter();
        if (mSelectedWebVideo !=null && !mSelectedWebVideo.getMp4().isEmpty()) {
            actionAdapter.add(
                                new Action(
                                        ACTION_WATCH,
                                        getResources().getString(R.string.watch_trailer_1)));
            if (!mSelectedWebVideo.getAuthorSourceID().isEmpty()){
                actionAdapter.add(
                    new Action (
                                ACTION_GOTO_CHANNEL,
                                getResources().getString(R.string.goto_channel)));
            }
        }
        if (mSelectedChannel !=null) {
            String command;
            if (mSelectedChannel.isSubscribed()) {
                command = "UnSubscribe";
            }
            else {
                command="Subscribe";
            }
            actionAdapter.add(
                    new Action(
                            ACTION_SUBSCRIBE,
                            command));
        }
        row.setActionsAdapter(actionAdapter);
        mAdapter.add(row);
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail background.
        FullWidthDetailsOverviewRowPresenter detailsPresenter =
                new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(
                ContextCompat.getColor((Context)this.getActivity(), R.color.selected_background));

        // Hook up transition element.
        FullWidthDetailsOverviewSharedElementHelper sharedElementHelper =
                new FullWidthDetailsOverviewSharedElementHelper();
        sharedElementHelper.setSharedElementEnterTransition(
                getActivity(), DetailsActivity.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(sharedElementHelper);
        detailsPresenter.setParticipatingEntranceTransition(true);

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                System.out.println("something clicked on vdf dp.soacl");

                if (action.getId() == ACTION_SUBSCRIBE){
                    if (mSelectedChannel.isSubscribed()){
                        mSelectedChannel.setSubscribed(false);
                    }
                    else{
                        mSelectedChannel.setSubscribed(true);
                    }
                    cvm.update(mSelectedChannel);
                    System.out.println(mSelectedChannel.toDebugString());
                }
                if (action.getId() == ACTION_WATCH) {
                    if (!mSelectedWebVideo.getMp4().isEmpty()) {
                        Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                        intent.putExtra(DetailsActivity.VIDEO, mSelectedWebVideo);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getActivity(), "missing information to access file remotely, please back out and try again", Toast.LENGTH_SHORT).show();
                    }
                }
                if (action.getId() == ACTION_GOTO_CHANNEL){
                    Channel targetChannel=null;
                    for (Channel c : cvm.getDeadChannels()){
                        System.out.println(mSelectedWebVideo.getAuthorSourceID()+" = "+c.getSourceID());
                        if (c.getSourceID().equals(mSelectedWebVideo.getAuthorSourceID())){
                            targetChannel = c;
                            System.out.println("found channel "+targetChannel.toDebugString());
                        }
                    }
                    if (null == targetChannel || targetChannel.getDescription().isEmpty()){
                        System.out.println("no channel found, nex scrape launched");
                        targetChannel=new Channel("http://www.bitchute.com/channel/"+ mSelectedWebVideo.getAuthorSourceID());
                        new ForeGroundChannelScrape().execute(targetChannel);
                        Toast.makeText(getActivity(), "channel data missing, try again momentarily", Toast.LENGTH_SHORT).show();
                        waitingForUpdate=true;
                        return;
                    }
                    System.out.println("channel should be "+targetChannel

                    );
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra(DetailsActivity.VIDEO, targetChannel);
                    startActivity(intent);
                }

            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    public void setupRelatedMovieListRow() {
        String subcategories[] = {getString(R.string.related_movies)};
        List<WebVideo> list = new ArrayList<>();
        WebVideo candidate;
        if (mSelectedWebVideo != null){
            for (String g: mSelectedWebVideo.getRelatedVideoArray()){
                System.out.println("setting up related video row for "+ mSelectedWebVideo.getRelatedVideoArray().size());
                candidate = null;
                for (WebVideo v: vvm.getDeadVideos()){
                    if (v.getSourceID().equals(g)){
                        candidate = v;
                    }
                }
                if (null == candidate) {

                    System.out.println("failed to find existing copy of video for "+g);
                    if (g.contains("null")){
                        System.out.println("the null is coming from creating a new related video somehow");
                    }
                    WebVideo newRelated = (new WebVideo("https://www.bitchute.com/video/" + g));
                    list.add(newRelated);

                    //new ForeGroundVideoScrape().execute(newRelated);
                }
                else {
                    list.add(candidate);
                }
            }
        }
        if (mSelectedChannel !=null){
            for (WebVideo v : vvm.getDeadVideos()){
                if (v.getAuthorSourceID().equals(mSelectedChannel.getSourceID())){
                    list.add(v);
                }
            }
        }
        if (null != list) {
            Collections.shuffle(list);
            System.out.println("setting "+ list.size()+" objects");

            CardPresenter bob=new CardPresenter();
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(bob);
            for (int j = 0; j < list.size(); j++) {
                listRowAdapter.add(list.get(j));

            }
            HeaderItem header = new HeaderItem(0, subcategories[0]);
            mAdapter.add(new ListRow(header, listRowAdapter));
            mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
        }
    }


    private int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
System.out.println("key pressed damit");
return true;
    }


    public void onItemClicked(
            Presenter.ViewHolder itemViewHolder,
            Object item,
            RowPresenter.ViewHolder rowViewHolder,
            Row row) {
        System.out.println("something clicked in vdf oic");
        if (item instanceof WebVideo) {
            Log.d(TAG, "Item: " + item.toString());
            WebVideo webVideo = (WebVideo) item;
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            intent.putExtra(getResources().getString(R.string.movie), webVideo);

            Bundle bundle =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            getActivity(),
                            ((ImageCardView) itemViewHolder.view).getMainImageView(),
                            DetailsActivity.SHARED_ELEMENT_NAME)
                            .toBundle();
            getActivity().startActivity(intent, bundle);
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof WebVideo) {
                WebVideo webVideo = (WebVideo) item;
                Log.d(TAG, "Item: " + item.toString());
                if (webVideo.getMp4().isEmpty()) {
                    ForeGroundVideoScrape task = new ForeGroundVideoScrape();
                    task.execute((WebVideo) item);
                    waitingForUpdate=true;
                }
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
                System.out.println("someone clicked on channel ");
                Log.d(TAG, "Item: " + item.toString());
                if (channel.getDescription().isEmpty()) {
                    ForeGroundChannelScrape task = new ForeGroundChannelScrape();
                    task.execute((Channel) item);
                    waitingForUpdate=true;
                    Toast.makeText(getActivity(), "waiting on channel data, try again in a moment", Toast.LENGTH_SHORT).show();
                }
                else {
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
    }


}
