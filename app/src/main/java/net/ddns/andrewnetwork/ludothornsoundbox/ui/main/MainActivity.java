package net.ddns.andrewnetwork.ludothornsoundbox.ui.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;


import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.ddns.andrewnetwork.ludothornsoundbox.R;
import net.ddns.andrewnetwork.ludothornsoundbox.databinding.ActivityMainBinding;
import net.ddns.andrewnetwork.ludothornsoundbox.di.component.ActivityComponent;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.MainViewPresenterBinder.IMainPresenter;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.MainViewPresenterBinder.IMainView;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.home.HomeFragment;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.preferiti.PreferitiFragment;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.random.RandomFragment;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.settings.SettingsActivity;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.settings.fragments.SettingsFragment;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.fragments.video.VideoFragment;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.utils.PreferencesListener;
import net.ddns.andrewnetwork.ludothornsoundbox.utils.CommonUtils;

import javax.inject.Inject;

import static net.ddns.andrewnetwork.ludothornsoundbox.ui.settings.SettingsActivity.REQUEST_SETTINGS_CHANGE;
import static net.ddns.andrewnetwork.ludothornsoundbox.utils.AppUtils.DAYS_LATER_ASKING_FEEDBACK;
import static net.ddns.andrewnetwork.ludothornsoundbox.utils.AppUtils.LINK_ASKING_FEEDBACK;


public class MainActivity extends ParentActivity
        implements NavigationView.OnNavigationItemSelectedListener, IMainView, PreferencesListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private ActivityMainBinding mBinding;
    @Inject
    IMainPresenter<IMainView> mPresenter;
    private boolean loadAtOnce;
    @IdRes int fragmentFirstSelection;

    @Override
    protected void setContentView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    protected int getFragmentContainerView() {
        return R.id.content_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mBinding.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ActivityComponent activityComponent = getActivityComponent();
        if (activityComponent != null) {
            activityComponent.inject(this);
            mPresenter.onAttach(this);
        }

        mBinding.navView.setNavigationItemSelectedListener(this);
        mBinding.appBarMain.navigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        mBinding.appBarMain.navigation.setSelectedItemId(fragmentFirstSelection);

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);

    }

    private void manageFeedbackAlert() {
        mPresenter.incrementUsageCounter();
        if (mPresenter.getUsageCounter() > mPresenter.getUsageThreshold()) {
            showFeedBackDialog();
        }
    }

    @Override
    protected void managePreferences(SharedPreferences settings) {
        super.managePreferences(settings);

        this.loadAtOnce = getLoadAtOnce(settings);
        this.fragmentFirstSelection = getFirstFragment(settings);
    }

    private @IdRes int getFirstFragment(SharedPreferences settings) {
        String fragment = settings.getString(getString(R.string.pag_iniziale_key), "Home");
        @IdRes int selection;

        switch (fragment) {
            default:
            case "Home":
                selection = R.id.action_home;
                break;
            case "Preferiti":
                selection = R.id.action_favorites;
                break;
            case "Casuale":
                selection = R.id.action_random;
                break;
            case "Video":
                selection = R.id.action_video;
                break;
        }

        return selection;
    }


    private boolean getLoadAtOnce(SharedPreferences settings) {
        return settings.getBoolean(getString(R.string.carica_audio_insieme_key), true);
    }

    private void showFeedBackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.do_you_like_label))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    CommonUtils.openLink(this, LINK_ASKING_FEEDBACK);
                    mPresenter.saveUsageThreshold(Long.MAX_VALUE);
                    dialog.dismiss();
                })
                .setNeutralButton(getString(R.string.giammai_label), (dialog, which) -> {
                    mPresenter.saveUsageThreshold(Long.MAX_VALUE);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.dopo_label), ((dialog, which) -> {
                    mPresenter.saveUsageThreshold(mPresenter.getUsageThreshold() + DAYS_LATER_ASKING_FEEDBACK);
                    dialog.dismiss();
                }));
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        Fragment fragment = null;

        switch (id) {
            //BOTTOM NAVIGATION MENU
            default:
            case R.id.action_home:
                fragment = HomeFragment.newInstance(loadAtOnce);
                break;
            case R.id.action_random:
                fragment = new RandomFragment();
                break;
            case R.id.action_video:
                fragment = new VideoFragment();
                break;
            case R.id.action_favorites:
                fragment = new PreferitiFragment();
                break;
            //NAVIGATION DRAWER
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }

        if (fragment != null) {
            replaceFragmentIfNotExists(fragment);
        }
        return true;
    }

    public void replaceFragmentIfNotExists(Fragment newFragment) {
        boolean found = false;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (newFragment.getClass().isInstance(fragment)) {
                fragmentTransaction.show(fragment);

                found = true;
            }
        }

        if (found) {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (!newFragment.getClass().isInstance(fragment)) {
                    fragmentTransaction.hide(fragment);
                }
            }

            fragmentTransaction.commit();
        } else addFragment(newFragment);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .detach(fragment)
                    .commit();
        }
    }

    public void onHomeFragmentReady() {
        manageFeedbackAlert();
    }

    @Override
    public void onFontChanged(boolean isFontFromApp) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.audio_nascosti_key))
                || key.equals(getString(R.string.dimensione_pulsanti_key))
                || key.equals(getString(R.string.mostra_info_in_audio_key))
        ) {
            HomeFragment fragment = getFragmentByClass(HomeFragment.class);

            if (fragment != null) {
                fragment.onAudioListChanged();
            }
        }
    }

    private <T extends Fragment> T getFragmentByClass(Class<T> fragmentClass) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment.getClass().equals(fragmentClass)) {
                return (T) fragment;
            }
        }

        return null;
    }
}
