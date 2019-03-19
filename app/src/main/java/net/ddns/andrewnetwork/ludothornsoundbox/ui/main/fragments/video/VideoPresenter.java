package net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video;

import android.util.Log;

import net.ddns.andrewnetwork.ludothornsoundbox.data.DataManager;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.Channel;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.LudoVideo;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.Thumbnail;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.VideoInformation;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.base.BasePresenter;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.VideoViewPresenterBinder.IVideoPresenter;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.VideoViewPresenterBinder.IVideoView;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.rx.SchedulerProvider;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.wrapper.GenericWrapper2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

public class VideoPresenter<V extends IVideoView> extends BasePresenter<V> implements IVideoPresenter<V> {

    @Inject
    public VideoPresenter(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable) {
        super(dataManager, schedulerProvider, compositeDisposable);
    }

    /*@Override
    public void getVideoList(List<Channel> channelList) {
        getMvpView().showLoading();
        getCompositeDisposable().add(Observable.fromIterable(channelList)
                .flatMap(channel -> getDataManager().getVideoList(channel))
                .observeOn(getSchedulerProvider().ui())
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(videoList -> {
                            getMvpView().hideLoading();
                            getMvpView().onVideoListLoadSuccess(videoList);
                        }
                        , throwable -> {
                            Log.e("VideoListREST", throwable.getMessage());
                            getMvpView().onVideoListLoadFailed();
                            getMvpView().hideLoading();
                        }
                )
        );
    }*/

    @Override
    public void getChannels(List<Channel> channelList) {
        getMvpView().showLoading();
        getCompositeDisposable().add(
                Observable.fromIterable(channelList)
                        .flatMap(channel -> getDataManager().getChannel(channel)
                                .flatMap(channelModified -> getDataManager().getVideoList(channel)
                                        .flatMap(videoList -> Observable.fromIterable(videoList)
                                                .flatMap(video -> Observable.zip(
                                                        getDataManager().getThumbnail(video),
                                                        getDataManager().getVideoInformation(video),
                                                        GenericWrapper2::new
                                                        )
                                                )
                                        )
                                )
                        )
                        .observeOn(getSchedulerProvider().ui())
                        .subscribeOn(getSchedulerProvider().io())
                        .subscribe(wrapper2 -> {
                                }, throwable -> {
                                    Log.e("ChannelListREST", throwable.getMessage());
                                    getMvpView().onVideoListLoadFailed();
                                    getMvpView().hideLoading();
                                }, () -> {
                                    getMvpView().onVideoListLoadSuccess(channelList);
                                    getMvpView().hideLoading();
                                }
                        )
        );
    }

    @Override
    public void getMoreVideos(List<Channel> channelList, Date date) {
        getMvpView().showLoading();
        getCompositeDisposable().add(
                Observable.fromIterable(channelList)
                        .flatMap(channel -> getDataManager().getMoreVideos(channel, date)
                                .flatMap(videoListResponse -> {

                                    return Observable.fromIterable(videoListResponse)
                                                    .flatMap(video -> Observable.zip(
                                                            getDataManager().getThumbnail(video),
                                                            getDataManager().getVideoInformation(video),
                                                            GenericWrapper2::new
                                                            )
                                                    ).doOnComplete(() -> getMvpView().onMoreVideoListLoadSuccess(videoListResponse));
                                        }
                                )
                        )
                        .observeOn(getSchedulerProvider().ui())
                        .subscribeOn(getSchedulerProvider().io())
                        .subscribe(wrapper2 -> {
                                }, throwable -> {
                                    Log.e("ChannelListREST", throwable.getMessage());
                                    getMvpView().onVideoListLoadFailed();
                                    getMvpView().hideLoading();
                                }, () -> {
                                    getMvpView().hideLoading();
                                }
                        )
        );
    }

    @Override
    public void getMoreVideos(Channel channel, Date date) {
        List<Channel> channels = new ArrayList<>();

        channels.add(channel);

        getMoreVideos(channels, date);
    }
}