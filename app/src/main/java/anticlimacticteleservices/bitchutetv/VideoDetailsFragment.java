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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.leanback.app.DetailsFragment;
import androidx.leanback.app.DetailsFragmentBackgroundController;
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

import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "VideoDetailsFragment";

    private static final int ACTION_WATCH_TRAILER = 1;
    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;
    private static final int NUM_COLS = 20;

    private Video mSelectedVideo;
    private Handler mHandler = new Handler();
    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    private DetailsFragmentBackgroundController mDetailsBackground;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        mDetailsBackground = new DetailsFragmentBackgroundController(this);

        mSelectedVideo =
                (Video) getActivity().getIntent().getSerializableExtra(DetailsActivity.VIDEO);
        if (mSelectedVideo != null) {
            mPresenterSelector = new ClassPresenterSelector();
            mAdapter = new ArrayObjectAdapter(mPresenterSelector);
            Video updatedVideo = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
            System.out.println("updated video returned "+updatedVideo.toCompactString());
            if (null == updatedVideo || updatedVideo.getMp4().isEmpty()){
                System.out.println("Video not in database, setting up to check again in 5");

                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        System.out.println("checking for mp4 again");
                        Video updatedVideo = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
                        if (null == updatedVideo || updatedVideo.getMp4().isEmpty()){
                            System.out.println("Video not in database after additional pause");
                            Toast.makeText(getActivity(),"Unable to reach video information", Toast.LENGTH_LONG);
                            return;
                        }
                        else {
                            mSelectedVideo = updatedVideo;
                            mAdapter.clear();
                            setupDetailsOverviewRow();
                            setupDetailsOverviewRowPresenter();
                            setAdapter(mAdapter);
                            initializeBackground(mSelectedVideo);
                            setupRelatedMovieListRow();
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
            System.out.println(mSelectedVideo.toCompactString());
        } else {
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

    private void setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedVideo.toString());
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedVideo);
        row.setImageDrawable(
                ContextCompat.getDrawable((Context)this.getActivity(), R.drawable.default_background));
        int width = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);
        Glide.with(getActivity())
                .load(mSelectedVideo.getThumbnail())
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

        actionAdapter.add(
                new Action(
                        ACTION_WATCH_TRAILER,
                        getResources().getString(R.string.watch_trailer_1)));

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

                Video updatedVideo = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
                System.out.println("updated video returned "+updatedVideo.toCompactString());
                if (null == updatedVideo){
                    System.out.println("Video not in database");
                }
                else {
                        if (updatedVideo.getMp4().isEmpty()) {
                            System.out.println("video exists but mp4 is still not set");
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    System.out.println("checking for mp4 again");
                                    Video updatedVideo = MainActivity.data.getVideo(mSelectedVideo.getSourceID());
                                    if (null == updatedVideo || updatedVideo.getMp4().isEmpty()){
                                        System.out.println("Video not in database after additional pause");
                                        Toast.makeText(getActivity(),"Unable to reach video information", Toast.LENGTH_LONG);
                                        return;
                                    }
                                    else {
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
                            Toast.makeText(getActivity(),"Fetching video information", Toast.LENGTH_SHORT);
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


                if (action.getId() == ACTION_WATCH_TRAILER) {
                    Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                    intent.putExtra(DetailsActivity.VIDEO, mSelectedVideo);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    public void setupRelatedMovieListRow() {
        String subcategories[] = {getString(R.string.related_movies)};
        List<Video> list = new ArrayList<>();
        for (String g: mSelectedVideo.getRelatedVideoArray()){
            Video candidate = MainActivity.data.getVideo(g);
            if (null == candidate) {
                list.add(new Video("https://www.bitchute.com/" + g));
            }
            else {
                list.add(candidate);
            }
        }

        if (null != list) {
            Collections.shuffle(list);
            System.out.println("setting "+ list.size()+" related videos");
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
            for (int j = 0; j < list.size(); j++) {
                listRowAdapter.add(list.get(j));
            }
            HeaderItem header = new HeaderItem(0, subcategories[0]);
            mAdapter.add(new ListRow(header, listRowAdapter));
            mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
           // MainActivity.data.getDescription().setText(mSelectedVideo.getDescription());
            System.out.println(mSelectedVideo.toCompactString());
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
}
