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
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
    private Handler mHandler = new Handler();
    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
       if (item instanceof Video) {
           Video video = (Video) item;
           Video v = MainActivity.data.getVideo(video.getSourceID());
           if (v != null) {
               System.out.println("video already added, version in db " + v.toCompactString());
               if (v.getMp4().isEmpty()) {
                   System.out.println("Mp4 still not set");
               } else {
                   video = v;
               }
           }
           // System.out.println(video.toCompactString());
           viewHolder.getTitle().setText(video.getTitle());
           viewHolder.getSubtitle().setText(video.getAuthor() + " " + video.getHackDateString());
           String description = video.getDescription();
           Spanned sp = Html.fromHtml(description);
           viewHolder.getBody().setText(sp);
       }
       if (item instanceof Channel) {
           Channel channel = (Channel) item;
           System.out.println(channel);
           Channel c = MainActivity.data.getChannelById(channel.getSourceID());
           if (null == c){
               MainActivity.data.addChannel(channel);
           } else if (channel.getDescription().length()>c.getDescription().length()){
               MainActivity.data.updateChannel(channel);
           }
           else {
               channel = c;
           }
           viewHolder.getTitle().setText(channel.getTitle());
           viewHolder.getSubtitle().setText(channel.getBitchuteID() + " " + channel.getDateHackString());
           String description = channel.getDescription();
           Spanned sp = Html.fromHtml(description);
           viewHolder.getBody().setText(sp);
       }
    }
}
