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

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
    private Handler mHandler = new Handler();
    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Video video = (Video) item;
        Video v = MainActivity.data.getVideo(video.getSourceID());
        if (v != null) {
            System.out.println("video already added " + video.toCompactString());
            if (v.getMp4().isEmpty()) {
                System.out.println("Mp4 still not set");
            } else {
                video = v;
            }
        }
       // System.out.println(video.toCompactString());
        viewHolder.getTitle().setText(video.getTitle());
        viewHolder.getSubtitle().setText(video.getAuthor());
        viewHolder.getBody().setText(video.getDescription());
    }
}
