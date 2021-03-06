package net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.child;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.ddns.andrewnetwork.ludothornsoundbox.R;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.LudoVideo;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.Thumbnail;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.preferiti.PreferitiListAdapter;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.FragmentAdapterVideoBinder;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.controller.VideoInformationManager;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.DateHourUtils;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.VideoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class VideoRecyclerAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<LudoVideo> videoList;
    private List<LudoVideo> itemsAll;

    private static final String DUMMY_LOADING_VIDEO_ID = "dummy";
    private static final int PROGRESS = 1;
    private static final int VIDEO = 0;
    private static boolean isLoadingMore = false;
    //private HashMap<String, Boolean> isPreferito;
    private FragmentAdapterVideoBinder mBinder;

    public VideoRecyclerAdapter(Context context, FragmentAdapterVideoBinder binder) {
        this.mContext = context;
        this.videoList = new ArrayList<>();
        //this.isPreferito = new HashMap<>();
        this.itemsAll = new ArrayList<>(videoList);
        this.mBinder = binder;

        //setPreferiti(preferitiVideoList);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingMore && position == videoList.size() - 1) {
            return PROGRESS;
        }
        return VIDEO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v;
        if (i == VIDEO) {
            v = inflater.inflate(R.layout.item_video, parent, false);
            return new VideoViewHolder(mContext, v);
        } else {
            v = inflater.inflate(R.layout.progress_dialog, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder.getItemViewType() == VIDEO) {
            if (viewHolder instanceof VideoAbstractViewHolder) {
                LudoVideo video = videoList.get(position);
                //Boolean isPreferitoValue = isPreferito.get(video.getId());

                //if (isPreferitoValue == null) {
                //    isPreferitoValue = false;
                //}

                ((VideoViewHolder) viewHolder).set(video, position);
            }
        } else {
            ProgressBar progressBar = viewHolder.itemView.findViewById(R.id.pb_loading);
            progressBar.getIndeterminateDrawable().setColorFilter(
                    mContext.getResources().getColor(R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    public class VideoViewHolder extends VideoAbstractViewHolder {

        VideoViewHolder(Context context, @NonNull View itemView) {
            super(context, itemView);
        }

        @Override
        public void set(LudoVideo item, int position) {
            super.set(item, position);

            ImageView thumbnailPlace = itemView.findViewById(R.id.icon);

            //setPreferitoListener(item, isPreferito);

            thumbnailPlace.setImageDrawable(null);

            if (item.getThumbnail() == null || item.getThumbnail().getImage() == null) {
                showVideoLoading();
                mBinder.loadThumbnail(item, thumbnail -> {
                    if (thumbnail.getImage() != null) {
                        thumbnailPlace.setImageBitmap(thumbnail.getImage());
                    } else {
                        thumbnailPlace.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_error_outline_white_24dp));
                    }

                    hideVideoLoading();
                });
            } else {
                thumbnailPlace.setImageBitmap(item.getThumbnail().getImage());
                hideVideoLoading();
            }

            itemView.setOnClickListener(v -> mBinder.apriVideo(item));
        }

        private void hideVideoLoading() {
            ProgressBar progressBar = itemView.findViewById(R.id.loading_icon);
            ImageView imageView = itemView.findViewById(R.id.icon);

            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }

        private void showVideoLoading() {
            ProgressBar progressBar = itemView.findViewById(R.id.loading_icon);
            ImageView imageView = itemView.findViewById(R.id.icon);

            imageView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);

        }

        /*private void setPreferitoListener(LudoVideo item, boolean isPreferitoValue) {
            ImageButton imageButton = itemView.findViewById(R.id.preferito_button);

            if (isPreferitoValue) {
                imageButton.setActivated(true);
                imageButton.setOnClickListener(v -> mBinder.rimuoviPreferito(item, preferitoDeleted -> {

                    isPreferito.put(item.getId(), false);

                    imageButton.setActivated(false);

                    setPreferitoListener(item, false);
                }));
            } else {
                imageButton.setActivated(false);
                imageButton.setOnClickListener(v -> mBinder.aggiungiPreferito(item, preferitoAdded -> {

                    isPreferito.put(item.getId(), true);

                    imageButton.setActivated(true);

                    setPreferitoListener(item, true);
                }));
            }
        }*/
    }

    public static class VideoAbstractViewHolder extends PreferitiListAdapter.ViewHolder<LudoVideo> {

        private Context mContext;

        public VideoAbstractViewHolder(Context context, @NonNull View itemView) {
            super(itemView);

            this.mContext = context;
        }

        @Override
        protected void set(LudoVideo item, int position) {

            ProgressBar progressBar = itemView.findViewById(R.id.loading_icon);

            progressBar.getIndeterminateDrawable().setColorFilter(
                    mContext.getResources().getColor(R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN);

            if (item != null) {
                TextView tt1 = itemView.findViewById(R.id.videotitle);
                TextView tt2 = itemView.findViewById(R.id.videodesc);
                TextView channel = itemView.findViewById(R.id.videochannel);
                TextView updated = itemView.findViewById(R.id.date_label);
                TextView views = itemView.findViewById(R.id.views);


                ImageView tt3 = itemView.findViewById(R.id.icon);

                if (tt1 != null && item.getTitle() != null) {
                    tt1.setText(Html.fromHtml(item.getTitle()));
                }

                if (tt2 != null) {
                    tt2.setText(item.getDescription());
                }

                if (channel != null && item.getChannel() != null) {
                    channel.setText(item.getChannel().getChannelName());
                }
                VideoInformationManager videoInformationManager = new VideoInformationManager(mContext, item);

                if (views != null) {
                    views.setText(videoInformationManager.getCompactedViews());

                }

                if (tt3 != null) {
                    Thumbnail thumbnail = item.getThumbnail();
                    Bitmap image = null;
                    if (thumbnail != null) image = thumbnail.getImage();
                    if (image != null) tt3.setImageBitmap(image);

                }
                if (updated != null)
                    updated.setText(DateHourUtils.convertToTimestamp(item.getDateTime()));
            }

        }
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void showLoading() {

        isLoadingMore = true;

        videoList.add(new LudoVideo(DUMMY_LOADING_VIDEO_ID));

        notifyItemInserted(videoList.size() - 1);
    }

    public void hideLoading() {

        isLoadingMore = false;

        videoList.remove(videoList.size() - 1);

        notifyItemRemoved(videoList.size() - 1);
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private ProgressViewHolder(View v) {
            super(v);
        }
    }

    public void addItems(List<LudoVideo> ludoVideoList) {
        //REMOVE THE PROGRESS BAR ITEM

        //ASSIGN TO ADAPTER VARIABLES
        videoList.addAll(ludoVideoList);
        itemsAll.addAll(ludoVideoList);

        //FILTER BOTH LISTS
        this.videoList = VideoUtils.filterList(videoList);
        this.itemsAll = VideoUtils.filterList(itemsAll);

        notifyDataSetChanged();
    }

    /*private void setPreferiti(List<LudoVideo> videoList) {
        isPreferito.clear();

        for (LudoVideo videoPreferito : videoList) {
            isPreferito.put(videoPreferito.getId(), videoPreferito.getPreferito());
        }
    }*/

    /*void setNewPreferiti(List<LudoVideo> videoList) {
        setPreferiti(videoList);
        notifyDataSetChanged();
    }*/
}
