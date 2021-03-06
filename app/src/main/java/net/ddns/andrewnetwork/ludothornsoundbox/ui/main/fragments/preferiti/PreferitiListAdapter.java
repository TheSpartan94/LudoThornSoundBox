package net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.preferiti;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.ddns.andrewnetwork.ludothornsoundbox.data.model.Thumbnail;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.preferiti.audio.IFragmentAudioPreferitiAdapterBinder;

import java.util.List;

public abstract class PreferitiListAdapter<T> extends RecyclerView.Adapter {

    protected List<T> list;
    protected final IFragmentPreferitiAdapterBinder<T> mBinder;
    protected final Context mContext;
    private final IAudioVideoAdaptersBinder audioVideoBinder;
    protected static boolean isSettingPreferito = false;

    public interface PreferitoDeletedListener<T> {

        void onPreferitoDeleted(T item);
    }

    public interface ThumbnailLoadedListener {

        void onThumbnailLoaded(Thumbnail thumbnail);
    }

    public PreferitiListAdapter(IFragmentPreferitiAdapterBinder<T> binder, IAudioVideoAdaptersBinder audioVideoBinder, Context context, List<T> list) {
        this.mBinder = binder;
        this.mContext = context;
        this.list = list;
        this.audioVideoBinder = audioVideoBinder;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return onCreateCustomViewHolder(parent, i);
    }

    protected abstract ViewHolder onCreateCustomViewHolder(ViewGroup parent, int i);

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        T item = list.get(position);
        if (viewHolder instanceof ViewHolder) {
            ((ViewHolder) viewHolder).set(item, position);
        }
    }

    public static abstract class ViewHolder<V> extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        protected abstract void set(V item, int position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    void notifyOtherItemsChanged(int position) {
        for (int i = 0; i < list.size(); i++) {
            if (i != position) {
                notifyItemChanged(i);
            }
        }
    }

    protected void setSettingPreferito(int position, boolean bool) {

        isSettingPreferito = bool;

        audioVideoBinder.notifySettingPreferito(getClass(), position, bool);
    }
}
