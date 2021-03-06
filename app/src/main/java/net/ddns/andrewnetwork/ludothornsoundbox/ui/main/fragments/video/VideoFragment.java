package net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ddns.andrewnetwork.ludothornsoundbox.BuildConfig;
import net.ddns.andrewnetwork.ludothornsoundbox.R;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.Channel;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.LudoVideo;
import net.ddns.andrewnetwork.ludothornsoundbox.databinding.ContentVideoBinding;
import net.ddns.andrewnetwork.ludothornsoundbox.di.component.ActivityComponent;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.MainFragment;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.preferiti.PreferitiListAdapter;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.FragmentVideoChildBinder.FragmentVideoChild;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.VideoViewPresenterBinder.IVideoPresenter;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.VideoViewPresenterBinder.IVideoView;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.search.VideoSearchFragment;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.videoinfo.VideoInformationActivity;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.ColorUtils;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.VideoUtils;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

public class VideoFragment extends MainFragment implements IVideoView, FragmentAdapterVideoBinder, FragmentVideoChildBinder.FragmentVideoParent {

    private ContentVideoBinding mBinding;
    private static boolean loadingMoreVideos;
    @Inject
    IVideoPresenter<IVideoView> mPresenter;
    private boolean loadingFailed;
    public static final String ALL_CHANNELS = "Tutti";
    private List<Channel> channelList;
    private int currentPosition = -1;
    private TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            currentPosition = tab.getPosition();
            Channel channel = channelList.get(tab.getPosition());

            if (getView() != null) {
                getView().setBackgroundColor(ContextCompat.getColor(mActivity, ColorUtils.getByName(mContext, channel.getBackGroundColor())));
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };
    private VideoSearchFragment searchFragment;

    public static VideoFragment newInstance() {

        Bundle args = new Bundle();

        VideoFragment fragment = new VideoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mBinding = DataBindingUtil.inflate(inflater, R.layout.content_video, container, false);

        ActivityComponent activityComponent = getActivityComponent();
        if (activityComponent != null) {
            activityComponent.inject(this);
            mPresenter.onAttach(this);
        }

        //mPresenter.registerOnSharedPreferencesChangeListener(this);

        refresh(true);

        return mBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //mPresenter.unregisterOnSharedPreferencesChangeListener(this);
    }

    @Override
    public void onVideoListLoadFailed() {
        //mBinding.videoLayout.setRefreshing(false);
        showErrorMessage();

        mBinding.progressBar.setVisibility(View.INVISIBLE);
        mBinding.progressVideoLoadingLabel.setVisibility(View.INVISIBLE);

        loadingFailed = true;
    }

    private void showErrorMessage() {
        mBinding.errorConnectionLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            if (searchFragment != null) {
                mActivity.getSupportFragmentManager().beginTransaction().show(searchFragment).commit();
            } else if (loadingFailed) {
                refresh(true);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View viewCreated, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(viewCreated, savedInstanceState);

        loadingFailed = false;

        mBinding.progressBar.getIndeterminateDrawable().setColorFilter(
                mContext.getResources().getColor(R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN);

        mBinding.progressVideoLoadingLabel.setText(mContext.getString(R.string.progress_video_loading_label, BuildConfig.SHORT_NAME));
        mBinding.errorConnectionButton.setOnClickListener(v -> refresh(true));
    }

    @Override
    public void onVideoListLoadSuccess(List<Channel> channelList) {

        this.channelList = channelList;

        notifyFragmentsRefreshing(false);
        if (channelList.size() > 1) {
            channelList.add(0, new Channel(ALL_CHANNELS, null, ColorUtils.getByColorResource(mContext, R.color.colorAccent)));
        } else {
            mBinding.tabLayout.setVisibility(View.GONE);
        }

        setUpTabLayout();
        mBinding.progressBar.setVisibility(View.INVISIBLE);
        mBinding.progressVideoLoadingLabel.setVisibility(View.INVISIBLE);
        onMoreVideoListLoadSuccess(channelList);

        if (currentPosition != -1) {
            TabLayout.Tab tab = mBinding.tabLayout.getTabAt(currentPosition);
            if (tab != null) {
                tab.select();
            }
        }

        mBinding.tabLayout.addOnTabSelectedListener(onTabSelectedListener);
    }

    private void setUpTabLayout() {


        mBinding.tabLayout.removeAllTabs();

        for (Channel channel : channelList) {
            TabLayout.Tab tab = mBinding.tabLayout.newTab();
            mBinding.tabLayout.addTab(tab.setText(channel.getChannelName()));
        }

        mBinding.viewPager.setAdapter(new VideoPagerAdapter(getChildFragmentManager(), channelList));
        mBinding.viewPager.setOffscreenPageLimit(channelList.size());
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);

    }

    /*@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_KEY_PREFERITI_VIDEO)) {
            notifyRefreshPreferiti(mPresenter.getPreferitiList());
        }
    }*/

    @Override
    public void onMoreVideoListLoadSuccess(List<Channel> videoList) {
        mActivity.runOnUiThread(() -> {
            notifyMoreVideosLoaded(videoList);
            VideoFragment.loadingMoreVideos = false;
        });
    }

    /*private void notifyRefreshPreferiti(List<LudoVideo> preferitiList) {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof FragmentVideoChild) {
                ((FragmentVideoChild) fragment).refreshPrefiti(preferitiList);
            }
        }
    }*/

    private void notifyMoreVideosLoaded(List<Channel> videoList) {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof FragmentVideoChild) {
                ((FragmentVideoChild) fragment).onMoreVideosLoaded(videoList);
            }
        }
    }


    @Override
    public void refresh(boolean usesGlobalLoading) {
        mBinding.errorConnectionLayout.setVisibility(View.GONE);
        mBinding.tabLayout.removeOnTabSelectedListener(onTabSelectedListener);

        if (!usesGlobalLoading) {
            notifyFragmentsRefreshing(true);
        } else {
            mBinding.progressVideoLoadingLabel.setVisibility(View.VISIBLE);
            mBinding.progressBar.setVisibility(View.VISIBLE);
        }
        mPresenter.getChannels(VideoUtils.getChannels());
    }

    @Override
    public void refreshSearch(String text) {
        mBinding.errorConnectionLayout.setVisibility(View.GONE);

        mPresenter.searchVideos(text, VideoUtils.removeNullIds(channelList));
    }

    private void notifyFragmentsRefreshing(boolean refreshing) {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof FragmentVideoChild) {
                ((FragmentVideoChild) fragment).setRecyclerViewRefreshing(refreshing);
            }
        }
    }


    @Override
    public void loadThumbnail(LudoVideo item, PreferitiListAdapter.ThumbnailLoadedListener thumbnailLoadedListener) {
        mPresenter.loadThumbnail(item, thumbnailLoadedListener);
    }

    @Override
    public void apriVideo(LudoVideo item) {
        VideoInformationActivity.newInstance(mActivity, item);
    }

    public static boolean isLoadingMoreVideos() {
        return loadingMoreVideos;
    }

    public static void setLoadingMoreVideos(boolean loadingMoreVideos) {
        VideoFragment.loadingMoreVideos = loadingMoreVideos;
    }

    @Override
    public void getMoreVideos(Channel channel, Date mostRecentDate, MoreChannelsLoadedListener moreChannelsLoadedListener) {
        mPresenter.getMoreVideos(channel, mostRecentDate, moreChannelsLoadedListener);
    }

    @Override
    public void getMoreVideos(MoreChannelsLoadedListener moreChannelsLoadedListener) {

        mPresenter.getMoreVideos(channelList, VideoUtils.getMostRecentDateFromChannels(channelList), moreChannelsLoadedListener);
    }

    @Override
    public void getMoreVideos(String searchString, Date date, MoreVideosLoadedListener moreChannelsLoadedListener) {
        List<Channel> list = VideoUtils.removeNullIds(channelList);
        mPresenter.getMoreVideos(searchString, date, moreChannelsLoadedListener, list);
    }

    @Override
    public void onSearchVideosLoaded(List<LudoVideo> videoList) {
        if (searchFragment != null) {
            searchFragment.initVideos();
            searchFragment.onSearchMoreVideosLoaded(videoList);
        }
    }

    @Override
    public void onSearchMoreVideosLoaded(List<LudoVideo> videoList) {
        if (searchFragment != null) {
            searchFragment.onSearchMoreVideosLoaded(videoList);
        }
    }

    public void initSearchFragment(String text) {
        if (mActivity != null && searchFragment == null) {
            searchFragment = VideoSearchFragment.newInstance(text);
            mActivity.addFragment(searchFragment);
        } else if(searchFragment != null) {
            refreshSearch(text);
        }
    }

    public void clearSearchFragment() {
        this.searchFragment = null;
    }

    private interface MoreLoadedListener<T> {

        void onMoreLoaded(List<T> list);
    }

    public interface MoreChannelsLoadedListener extends MoreLoadedListener<Channel> {
    }

    public interface MoreVideosLoadedListener extends MoreLoadedListener<LudoVideo> {
    }
}
