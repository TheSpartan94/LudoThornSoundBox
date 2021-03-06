/*
 * Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://mindorks.com/license/apache-v2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package net.ddns.andrewnetwork.ludothornsoundbox.data.network;

import android.util.Log;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;

import net.ddns.andrewnetwork.ludothornsoundbox.BuildConfig;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.Channel;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.LudoAudio;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.LudoVideo;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.Thumbnail;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.VideoInformation;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.AppUtils;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.JsonUtil;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.VideoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

import static net.ddns.andrewnetwork.ludothornsoundbox.utils.AppConstants.LOOKUP_TYPE_VIDEO;
import static net.ddns.andrewnetwork.ludothornsoundbox.utils.AppConstants.ORDER_TYPE_VIDEO;
import static net.ddns.andrewnetwork.ludothornsoundbox.utils.AppConstants.VIDEO_PER_CHANNEL_LOADED;
import static net.ddns.andrewnetwork.ludothornsoundbox.utils.StringUtils.nonEmptyNonNull;
import static net.ddns.andrewnetwork.ludothornsoundbox.utils.VideoUtils.castToLudoVideo;


@Singleton
public class AppApiHelper implements ApiHelper {

    private ApiHeader mApiHeader;

    @Inject
    AppApiHelper(ApiHeader apiHeader) {
        mApiHeader = apiHeader;
    }

    @Override
    public ApiHeader getApiHeader() {
        return mApiHeader;
    }

    @Override
    public Observable<List<LudoVideo>> getVideoList(Channel channel) {
        return Observable.create(emitter -> {
            Log.d("ChannelREST", "getting Videos for channel" + JsonUtil.getGson().toJson(channel));

            YouTube.Search.List search;
            search = createTubeService().search().list("id,snippet");
            search.setKey(AppUtils.getApiKey());
            search.setChannelId(channel.getId());
            search.setType(LOOKUP_TYPE_VIDEO);
            search.setOrder(ORDER_TYPE_VIDEO);
            search.setMaxResults(VIDEO_PER_CHANNEL_LOADED);
            final SearchListResponse searchResponse = search.execute();

            List<SearchResult> searchResultList = searchResponse.getItems();
            Log.d("ChannelREST", JsonUtil.getGson().toJson(searchResultList));
            List<LudoVideo> videoList = castToLudoVideo(searchResultList);
            VideoUtils.addVideosToChannels(channel, videoList);
            emitter.onNext(videoList);
            emitter.onComplete();
        });
    }

    @Override
    public Observable<Channel> getChannel(Channel channel) {
        return Observable.create(emitter -> {
            Log.d("ChannelREST", "getting channel " + JsonUtil.getGson().toJson(channel));

            YouTube.Channels.List channels;
            channels = createTubeService().channels().list("statistics");
            channels.setKey(AppUtils.getApiKey());

            if (nonEmptyNonNull(channel.getChannelUsername())) {
                channels.setForUsername(channel.getChannelUsername());
            } else if (nonEmptyNonNull(channel.getId())) {
                channels.setId(channel.getId());
            }
            final com.google.api.services.youtube.model.Channel channelListResponse = channels.execute().getItems().get(0);
            channel.setId(channelListResponse.getId());
            channel.setTotalNumberOfVideos(channelListResponse.getStatistics().getVideoCount().longValue());
            emitter.onNext(channel);
            emitter.onComplete();
        });
    }

    @Override
    public Single<Channel> getChannel(LudoVideo video) {
        return Single.create(emitter -> {
            Log.d("ChannelREST", "getting Channel for video " + video.getTitle());

            YouTube.Videos.List channels = createTubeService().videos().list("snippet");
            channels.setKey(AppUtils.getApiKey())
                    .setId(video.getId());

            final Video videoResponse = channels.execute().getItems().get(0);
            VideoSnippet videoSnippet = videoResponse.getSnippet();
            Channel channel = new Channel(videoSnippet.getChannelId());
            channel.setChannelName(videoSnippet.getChannelTitle());
            emitter.onSuccess(channel);
        });
    }

    @Override
    public Observable<Thumbnail> getThumbnail(LudoVideo video) {
        return Observable.create(emitter -> {
            Log.v("ChannelREST", "getting Thumbnails.");

            Thumbnail thumbnail = video.getThumbnail();
            if (thumbnail != null) {
                InputStream input = null;
                URL url = new URL(video.getThumbnail().getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                int response = connection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK)
                    input = connection.getInputStream();
                if (input == null) {
                    thumbnail = new Thumbnail(video.getThumbnail().getUrl());

                } else thumbnail = new Thumbnail(input, video.getThumbnail().getUrl());
            }

            video.setThumbnail(thumbnail);
            emitter.onNext(thumbnail);
            emitter.onComplete();
        });
    }

    @Override
    public Observable<VideoInformation> getVideoInformation(LudoVideo video) {
        return Observable.create(emitter -> {
            Log.v("VideoInfoREST", "getting VideoInformation.");
            List<Video> videos;

            YouTube.Videos.List videoSearch = createTubeService().videos().list("id,snippet,statistics");
            final VideoListResponse videoListResponse = videoSearch
                    .setKey(AppUtils.getApiKey())
                    .setId(video.getId())
                    .execute();

            videos = videoListResponse.getItems();

            if (videos.size() == 1) {
                VideoInformation videoInformation = VideoUtils.extractVideoInformation(videos.get(0));
                video.setVideoInformation(videoInformation);

                emitter.onNext(videoInformation);
            } else emitter.onError(new IllegalArgumentException("Video results are not 1."));

            emitter.onComplete();
        });
    }

    @Override
    public Observable<List<LudoVideo>> getMoreVideos(Channel channel, Date beforeDate) {
        return Observable.create(emitter -> {
            Log.v("ChannelREST", "getting More Videos.");

            YouTube.Search.List search = createTubeService().search().list("id,snippet");
            final SearchListResponse searchResponse = search.setKey(AppUtils.getApiKey())
                    .setChannelId(channel != null ? channel.getId() : null)
                    .setPublishedBefore(new DateTime(beforeDate))
                    .setType(LOOKUP_TYPE_VIDEO)
                    .setOrder(ORDER_TYPE_VIDEO)
                    .setMaxResults(VIDEO_PER_CHANNEL_LOADED)
                    .execute();

            List<SearchResult> searchResultList = new ArrayList<>(searchResponse.getItems());
            List<LudoVideo> videoList = castToLudoVideo(searchResultList);
            VideoUtils.addVideosToChannels(channel, videoList);
            emitter.onNext(videoList);
            emitter.onComplete();
        });
    }

    @Override
    public Observable<LudoAudio> getVideoById(LudoAudio audio) {
        return Observable.create(emitter -> {
            Log.d("VideoFromAudioREST", "getting Video for audio: " + audio.getTitle() + " video:" + audio.getVideo());
            Log.d("ApiKey", AppUtils.getApiKey());

            YouTube.Videos.List search;
            search = createTubeService().videos().list("id,snippet,statistics");
            search.setKey(AppUtils.getApiKey());

            search.setId(audio.getVideo().getId());
            search.setMaxResults(1L);
            final VideoListResponse videoListResponse = search.execute();
            Log.d("VideoResponse", videoListResponse.toString());

            List<Video> searchResultList = new ArrayList<>(videoListResponse.getItems());
            if (searchResultList.isEmpty()) {
                Throwable throwable = new IllegalArgumentException("No Videos were found for audio " + JsonUtil.getGson().toJson(audio));
                throwable.printStackTrace();
                emitter.onNext(audio);
            } else {
                LudoVideo video = castToLudoVideo(searchResultList.get(0));
                LudoVideo oldVideo = audio.getVideo();
                List<LudoAudio> oldAudioList = new ArrayList<>();
                if (oldVideo != null) {
                    oldAudioList = oldVideo.getConnectedAudioList();
                }

                video.setAudioList(oldAudioList);
                audio.setVideo(video);

                emitter.onNext(audio);

            }

            emitter.onComplete();
        });
    }

    @Override
    public Single<List<LudoVideo>> searchMoreVideosAllChannels(String searchString, Date date, List<Channel> channelList) {
        return Single.create(emitter -> {
            List<LudoVideo> videoList = new ArrayList<>();

            for (Channel channel : channelList) {
                videoList.addAll(searchVideosChannel(searchString, date, channel));
            }

            emitter.onSuccess(videoList);
        });
    }

    @Override
    public Single<List<LudoVideo>> searchVideosAllChannels(String searchString, List<Channel> channelList) {
        return Single.create(emitter -> {
            List<LudoVideo> videoList = new ArrayList<>();

            for (Channel channel : channelList) {
                videoList.addAll(searchVideosChannel(searchString, channel));
            }

            emitter.onSuccess(videoList);
        });
    }

    private List<LudoVideo> searchVideosChannel(String searchString, Channel channel) throws IOException {
        return searchVideosChannel(searchString, null, channel);
    }

    private List<LudoVideo> searchVideosChannel(String searchString, Date date, Channel channel) throws IOException {
        YouTube.Search.List search;
        search = createTubeService().search().list("id,snippet");
        search.setKey(AppUtils.getApiKey());

        final SearchListResponse videoListResponse = search.setQ(searchString)
                .setOrder(ORDER_TYPE_VIDEO)
                .setPublishedBefore(date != null ? new DateTime(date) : null)
                .setChannelId(channel.getId())
                .execute();

        List<SearchResult> searchResultList = new ArrayList<>(videoListResponse.getItems());

        return castToLudoVideo(searchResultList);
    }

    @Override
    public Single<LudoVideo> getVideoById(String url) {
        return Single.create(emitter -> {
            Log.d("VideoFromUrlREST", "getting Video for URL: " + url);
            Log.d("ApiKey", AppUtils.getApiKey());

            YouTube.Videos.List search = createTubeService().videos().list("id,snippet,statistics");
            final VideoListResponse videoListResponse = search.setKey(AppUtils.getApiKey())
                    .setId(url)
                    .setMaxResults(1L)
                    .execute();

            Log.d("VideoResponse", videoListResponse.toString());

            List<Video> searchResultList = new ArrayList<>(videoListResponse.getItems());
            if (searchResultList.isEmpty()) {
                Throwable throwable = new IllegalArgumentException("No Videos were found for URL: " + url);
                throwable.printStackTrace();
                emitter.onError(throwable);
            } else {
                LudoVideo video = castToLudoVideo(searchResultList.get(0));

                emitter.onSuccess(video);

            }
        });
    }

    private YouTube createTubeService() {
        return new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {
        }).setApplicationName(BuildConfig.LONG_NAME).build();
    }
}

