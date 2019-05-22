package net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import net.ddns.andrewnetwork.ludothornsoundbox.R;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.LudoVideo;
import net.ddns.andrewnetwork.ludothornsoundbox.data.model.Thumbnail;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.preferiti.PreferitiListAdapter;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.controller.VideoInformationManager;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.CommonUtils;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.DateHourUtils;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.VideoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import static net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.controller.VideoManager.buildVideoUrl;

public class VideoRecyclerAdapter extends RecyclerView.Adapter implements Filterable {

    private Context mContext;
    private List<LudoVideo> videoList;
    private List<LudoVideo> itemsAll;

    private static final String DUMMY_LOADING_VIDEO_ID = "dummy";
    private static final int PROGRESS = 1;
    private static final int VIDEO = 0;
    private boolean isLoadingMore = false;
    private HashMap<String, Boolean> isPreferito;
    private FragmentAdapterVideoBinder mBinder;

    VideoRecyclerAdapter(Context context, FragmentAdapterVideoBinder binder, List<LudoVideo> preferitiVideoList) {
        this.mContext = context;
        this.videoList = new ArrayList<>();
        this.isPreferito = new HashMap<>();
        this.itemsAll = new ArrayList<>(videoList);
        this.mBinder = binder;

        setPreferiti(preferitiVideoList);
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
            if (viewHolder instanceof VideoViewHolder) {
                LudoVideo video = videoList.get(position);
                Boolean isPreferitoValue = isPreferito.get(video.getId());

                if (isPreferitoValue == null) {
                    isPreferitoValue = false;
                }

                ((VideoViewHolder) viewHolder).set(video, position, isPreferitoValue, mBinder);
            }
        } else {
            ProgressBar progressBar = viewHolder.itemView.findViewById(R.id.pb_loading);
            progressBar.getIndeterminateDrawable().setColorFilter(
                    mContext.getResources().getColor(R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    public static class VideoViewHolder extends PreferitiListAdapter.ViewHolder<LudoVideo> {

        private Context mContext;


        public VideoViewHolder(Context context, @NonNull View itemView) {
            super(itemView);

            this.mContext = context;
        }

        private void set(LudoVideo item, int position, boolean isPreferito, FragmentAdapterVideoBinder binder) {
            set(item, position);

            ImageButton imageButton = itemView.findViewById(R.id.preferito_button);

            if (isPreferito) {
                imageButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_yellow_24dp));
                imageButton.setOnClickListener(v -> Toast.makeText(mContext, "Questo video è già nei preferiti!", Toast.LENGTH_SHORT).show());
            } else {
                imageButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_border_yellow_24dp));
                imageButton.setOnClickListener(v -> {
                    binder.aggiungiPreferito(item);

                });
            }
        }

        @Override
        protected void set(LudoVideo item, int position) {
            if (item != null) {
                TextView tt1 = itemView.findViewById(R.id.videotitle);
                TextView tt2 = itemView.findViewById(R.id.videodesc);
                TextView channel = itemView.findViewById(R.id.videochannel);
                TextView likes = itemView.findViewById(R.id.likes);
                TextView dislikes = itemView.findViewById(R.id.dislikes);
                TextView views = itemView.findViewById(R.id.views);
                TextView updated = itemView.findViewById(R.id.Updated);


                ImageView tt3 = itemView.findViewById(R.id.icon);

                if (tt1 != null) {
                    tt1.setText(Html.fromHtml(item.getTitle()));
                }

                if (tt2 != null) {
                    tt2.setText(item.getDescription());
                }

                if (channel != null && item.getChannel() != null) {
                    channel.setText(item.getChannel().getChannelName());
                }
                VideoInformationManager videoInformationManager = new VideoInformationManager(mContext, item);
                if (likes != null) {
                    likes.setText(videoInformationManager.getCompactedLikes());
                }

                if (dislikes != null) {
                    dislikes.setText(videoInformationManager.getCompactedDislikes());
                }

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
            itemView.setOnClickListener(v -> {
                if (item != null) {
                    CommonUtils.openLink(mContext, buildVideoUrl(item.getId()));
                } else {
                    CommonUtils.showDialog(mContext, "Link non disponibile.");
                }
            });
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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<LudoVideo> FilteredArrayNames = new ArrayList<>();
                if (constraint == null) {
                    FilteredArrayNames = itemsAll;
                } else {
                    // perform your search here using the searchConstraint String.

                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < itemsAll.size(); i++) {
                        LudoVideo dataNames = itemsAll.get(i);
                        if (dataNames.getChannel() != null && dataNames.getChannel().getId().toLowerCase().equals(constraint.toString().toLowerCase())) {
                            FilteredArrayNames.add(dataNames);
                        }
                    }
                }

                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null)
                    videoList = (List<LudoVideo>) results.values;


                notifyDataSetChanged();
            }
        };
    }

    void addItems(List<LudoVideo> ludoVideoList) {

        //REMOVE THE PROGRESS BAR ITEM

        //ASSIGN TO ADAPTER VARIABLES
        videoList.addAll(ludoVideoList);
        itemsAll.addAll(ludoVideoList);

        //FILTER BOTH LISTS
        this.videoList = VideoUtils.filterList(videoList);
        this.itemsAll = VideoUtils.filterList(itemsAll);

        notifyDataSetChanged();
    }

    private void setPreferiti(List<LudoVideo> videoList) {
        isPreferito.clear();

        for (LudoVideo videoPreferito : videoList) {
            isPreferito.put(videoPreferito.getId(), videoPreferito.getPreferito());
        }
    }

    public void setNewPreferiti(List<LudoVideo> videoList) {
        setPreferiti(videoList);
        notifyDataSetChanged();
    }
}
