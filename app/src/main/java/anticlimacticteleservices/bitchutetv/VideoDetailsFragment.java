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
import android.os.Bundle;

import androidx.leanback.app.DetailsFragment;
import androidx.leanback.app.DetailsFragmentBackgroundController;
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

    private Video mSelectedVideo;
    private Handler mHandler = new Handler();
    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;
    private Object mSelectedItem;
    private Channel mSelectedChannel;
    private DetailsSupportFragmentBackgroundController mDetailsBackground;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);
        setOnItemViewClickedListener(new VideoDetailsFragment.ItemViewClickedListener());
        VideoViewModel vvm = ViewModelProvider.AndroidViewModelFactory.getInstance(MainActivity.data.getApplication()).create(VideoViewModel.class);
        vvm.getAllVideos().observe(this, new Observer<List<Video>>(){
            @Override
            public void onChanged(List<Video> videos) {
                System.out.println("something changed in teh data");
                if (mSelectedVideo != null){
                    System.out.println("something changed, currently displaying "+mSelectedVideo.toDebugString());
                    for (Video v :  videos){
                        if (v.getSourceID().equals(mSelectedVideo.getSourceID())){
                            System.out.println("this is the latest version "+v.toDebugString());
                            mSelectedVideo=v;
                            mAdapter.clear();
                            setupDetailsOverviewRow();
                            setupDetailsOverviewRowPresenter();
                            setAdapter(mAdapter);
                            initializeBackground(mSelectedVideo);
                            setupRelatedMovieListRow();
                        }
                    }
                }
            }
        });

        mDetailsBackground = new DetailsSupportFragmentBackgroundController(this);
        //looks ugly
        mSelectedItem = getActivity().getIntent().getSerializableExtra(DetailsActivity.VIDEO);
        if (mSelectedItem instanceof Video){
            System.out.println("it's a video");
            mSelectedVideo= (Video) mSelectedItem;
        }
        if (mSelectedItem instanceof Channel){
            System.out.println("it's a channel");
            mSelectedChannel = (Channel) mSelectedItem;
        }
        if (mSelectedVideo != null) {
            System.out.println("processing video");
            if (!mSelectedVideo.getAuthorSourceID().isEmpty()) {
                Channel chan = MainActivity.data.getChannelById(mSelectedVideo.getSourceID());
                if (chan == null) {
                    chan = new Channel("http://www.bitchute.com/channel/" + mSelectedVideo.getAuthorSourceID());
                }
                if (chan.getDescription().isEmpty()){
                    new ForeGroundChannelScrape().execute(chan);
                }
            }
            mPresenterSelector = new ClassPresenterSelector();
            mAdapter = new ArrayObjectAdapter(mPresenterSelector);
            Video updatedVideo = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
            if (null == updatedVideo || updatedVideo.getMp4().isEmpty()){
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (true) {
                            Video updatedVideo = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
                            System.out.println("current version of video"+updatedVideo.toCompactString());
                            if (null == updatedVideo || updatedVideo.getMp4().isEmpty()) {
                            } else {
                                mSelectedVideo = updatedVideo;
                                mAdapter.clear();
                                setupDetailsOverviewRow();
                                setupDetailsOverviewRowPresenter();
                                setAdapter(mAdapter);
                                initializeBackground(mSelectedVideo);
                                setupRelatedMovieListRow();
                            }
                        }
                    }
                }, 5000);
            }
            else {
                if (updatedVideo.getMp4().isEmpty()){
                    System.out.println("video exists but mp4 is still not set");
                }
                else {
                    mSelectedVideo = updatedVideo;
                }
            }
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setAdapter(mAdapter);
            initializeBackground(mSelectedVideo);
            setupRelatedMovieListRow();
           // System.out.println(mSelectedVideo.toCompactString());
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
                System.out.println("channel is missing data, waiting 5 seconds "+mSelectedChannel);
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        System.out.println("checking for channel description again");
                        Channel updatedChannel = MainActivity.data.getChannelById(mSelectedChannel.getSourceID());
                        if (null == updatedChannel || updatedChannel.getDescription().isEmpty()) {
                            System.out.println("Channel not in database after additional pause");
                            return;
                        } else {
                            mSelectedChannel = updatedChannel;
                            mAdapter.clear();
                            setupDetailsOverviewRow();
                            setupDetailsOverviewRowPresenter();
                            setAdapter(mAdapter);
                            initializeBackground(mSelectedChannel);
                            setupRelatedMovieListRow();
                        }
                    }
                }, 5000);
            }
        }
        else {
           Intent intent = new Intent(getActivity(), MainActivity.class);
           startActivity(intent);
        }
    }

    private void initializeBackground(Video data) {
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
        if (null !=mSelectedVideo) {
            row = new DetailsOverviewRow(mSelectedVideo);
            thumbnail=mSelectedVideo.getThumbnail();
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
        if (mSelectedVideo!=null && !mSelectedVideo.getMp4().isEmpty()) {
            actionAdapter.add(
                                new Action(
                                        ACTION_WATCH,
                                        getResources().getString(R.string.watch_trailer_1)));
            if (!mSelectedVideo.getAuthorSourceID().isEmpty()){
                actionAdapter.add(
                    new Action (
                                ACTION_GOTO_CHANNEL,
                                getResources().getString(R.string.goto_channel)));
            }
        }
        if (mSelectedChannel !=null) {
            actionAdapter.add(
                    new Action(
                            ACTION_SUBSCRIBE,
                            getResources().getString(R.string.subscribe_1)));
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
                if (null != mSelectedVideo) {
                    Video updatedVideo = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
                    //System.out.println("updated video returned " + updatedVideo.toCompactString());
                    if (null == updatedVideo) {
                        System.out.println("Video not in database");
                    } else {
                        if (updatedVideo.getMp4().isEmpty()) {
                            System.out.println("video exists but mp4 is still not set");
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                   // System.out.println("checking for mp4 again");
                                    Video updatedVideo = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
                                    if (null == updatedVideo || updatedVideo.getMp4().isEmpty()) {
                                        System.out.println("Video not in database after additional pause");
                                    } else {
                                        mSelectedVideo = updatedVideo;
                                        mAdapter.clear();
                                        setupDetailsOverviewRow();
                                        setupDetailsOverviewRowPresenter();
                                        setAdapter(mAdapter);
                                        initializeBackground(mSelectedVideo);
                                        setupRelatedMovieListRow();
                                    }
                                }
                            }, 2000);
                            Toast.makeText(getActivity(), "Fetching video information", Toast.LENGTH_SHORT);
                        } else {
                            mSelectedVideo = updatedVideo;
                            mAdapter.clear();
                            setupDetailsOverviewRow();
                            setupDetailsOverviewRowPresenter();
                            setAdapter(mAdapter);
                            initializeBackground(mSelectedVideo);
                            setupRelatedMovieListRow();
                        }

                    }
                }
                if (action.getId() == ACTION_SUBSCRIBE){

                    for (Video fuck:MainActivity.data.getAllVideos()){
                        System.out.println(fuck.toDebugString());
                    }
                    MainActivity.data.refreshVideos();
                    for (Video fuck:MainActivity.data.getAllVideos()) {
                        System.out.println(fuck.toDebugString());
                    }
                }
                if (action.getId() == ACTION_WATCH) {
                    if (mSelectedVideo.getMp4().isEmpty()){
                        Video updated = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
                        if (null == updated) {
                            System.out.println("video not found in database " + mSelectedVideo.toCompactString());
                        } else {
                            if (updated.getMp4().isEmpty()) {
                                System.out.println("still not updated with Mp4 value");
                            }
                            else {
                                mSelectedVideo=updated;
                            }
                        }
                    }
                    if (!mSelectedVideo.getMp4().isEmpty()) {
                        Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                        intent.putExtra(DetailsActivity.VIDEO, mSelectedVideo);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getActivity(), "missing information to access file remotely, please back out and try again", Toast.LENGTH_SHORT).show();
                    }
                }
                if (action.getId() == ACTION_GOTO_CHANNEL){
                    System.out.println("attemtping to go to channel for video "+mSelectedVideo.toCompactString());
                    Channel targetChannel=MainActivity.data.getChannelById(mSelectedVideo.getAuthorSourceID());
                    if (null == targetChannel){
                        System.out.println("no channel found, nex scrape launched");
                        targetChannel=new Channel("www.bitchute.com/channel/"+mSelectedVideo.getAuthorSourceID());
                        new ForeGroundChannelScrape().execute(targetChannel);
                        Toast.makeText(getActivity(), "channel data missing, try again momentarily", Toast.LENGTH_SHORT).show();
                    }
                    System.out.println("channel should be "+targetChannel

                    );
                    if (!targetChannel.getThumbnail().isEmpty()) {
                        Intent intent = new Intent(getActivity(), DetailsActivity.class);
                        intent.putExtra(DetailsActivity.VIDEO, targetChannel);
                        startActivity(intent);
                    }
                }

            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    public void setupRelatedMovieListRow() {
        String subcategories[] = {getString(R.string.related_movies)};
        List<Video> list = new ArrayList<>();
        if (mSelectedVideo != null){
            for (String g: mSelectedVideo.getRelatedVideoArray()){
                Video candidate = MainActivity.data.getVideo(g);
                if (null == candidate) {
                    list.add(new Video("https://www.bitchute.com/" + g));
                }
                else {
                    list.add(candidate);
                }
            }
        }
        if (mSelectedChannel !=null){
            for (Video v :MainActivity.data.getAllVideos()){
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
        Video updated = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
        if (null == updated) {
            System.out.println("video not found in database " + mSelectedVideo.toCompactString());
        } else {
            if (updated.getMp4().isEmpty()) {
                System.out.println("still not updated with Mp4 value");
            }
            else {
                mSelectedVideo=updated;
            }
        }
        if (item instanceof Video) {
            Log.d(TAG, "Item: " + item.toString());
            Video video = (Video) item;
            updated = MainActivity.data.getVideo(video.getSourceID());
            if (null == updated) {
                System.out.println("video not found in database " + video.toCompactString());
            } else {
                if (updated.getMp4().isEmpty()) {
                    System.out.println("still not updated with Mp4 value");
                }
                else {
                    video=updated;
                }
            }
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            intent.putExtra(getResources().getString(R.string.movie), video);

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
                System.out.println("someone clicked on channel ");
                Log.d(TAG, "Item: " + item.toString());
                if (channel.getDescription().isEmpty()) {
                    ForeGroundChannelScrape task = new ForeGroundChannelScrape();
                    task.execute((Channel) item);
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
