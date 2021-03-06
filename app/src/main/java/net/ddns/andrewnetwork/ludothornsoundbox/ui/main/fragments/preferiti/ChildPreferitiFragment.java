package net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.preferiti;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.ddns.andrewnetwork.ludothornsoundbox.ui.base.BaseFragment;

public abstract class ChildPreferitiFragment extends BaseFragment implements IAudioVideoAdaptersBinder {

    private PreferitiFragment parent;

    public void onParentHiddenChanged(boolean hidden) { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.parent = (PreferitiFragment) getParentFragment();
    }

    protected PreferitiFragment getParent() {
        return parent;
    }

    @Override
    public void notifySettingPreferito(Class<? extends PreferitiListAdapter> tClass, int position, boolean isSettingPreferito) {
        PreferitiListAdapter adapter = getListAdapter();
        if(adapter != null) {
            if (adapter.getClass().equals(tClass)) {
                adapter.notifyOtherItemsChanged(position);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    protected abstract PreferitiListAdapter getListAdapter();

    public abstract void onPreferenceChanged(SharedPreferences sharedPreferences) ;
}
