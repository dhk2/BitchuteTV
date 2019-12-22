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

import android.os.Handler;
import android.text.Html;
import android.text.Spanned;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
    private Handler mHandler = new Handler();
    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
       if (item instanceof WebVideo) {
           WebVideo webVideo = (WebVideo) item;
           viewHolder.getTitle().setText(webVideo.getTitle());
           viewHolder.getSubtitle().setText(webVideo.getAuthor() + " " + webVideo.getHackDateString());
           String description = webVideo.getDescription();
           Spanned sp = Html.fromHtml(description);
           viewHolder.getBody().setText(sp);
       }
       if (item instanceof Channel) {
           Channel channel = (Channel) item;
           viewHolder.getTitle().setText(channel.getTitle());
           viewHolder.getSubtitle().setText(channel.getBitchuteID() + " " + channel.getDateHackString());
           String description = channel.getDescription();
           Spanned sp = Html.fromHtml(description);
           viewHolder.getBody().setText(sp);
           viewHolder.getBody().setHorizontallyScrolling(true);
       }
    }
}
