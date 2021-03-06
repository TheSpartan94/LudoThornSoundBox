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

package net.ddns.andrewnetwork.ludothornsoundbox.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;

import net.ddns.andrewnetwork.ludothornsoundbox.BuildConfig;
import net.ddns.andrewnetwork.ludothornsoundbox.R;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.base.BaseFragment;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.settings.activity.navigationItems.LudoNavigationItem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by janisharali on 27/01/17.
 */

public final class CommonUtils {

    private static final String TAG = "CommonUtils";
    private static final int EXPORT_REQUEST = 8081;

    private CommonUtils() {
        // This utility class is not publicly instantiable
    }

    public static ProgressDialog showLoadingDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    @SuppressLint("all")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean isEmailValid(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String loadJSONFromAsset(Context context, String jsonFileName)
            throws IOException {

        AssetManager manager = context.getAssets();
        InputStream is = manager.open(jsonFileName);

        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        return new String(buffer, StandardCharsets.UTF_8);
    }

    public static void showDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.positive_label, BuildConfig.SHORT_NAME), (dialog, id) ->
                        dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialog(Context context, @StringRes int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(message))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.positive_label, BuildConfig.SHORT_NAME), (dialog, id) ->
                        dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.positive_label, BuildConfig.SHORT_NAME), (dialog, id) ->
                        dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialog(Context context, @StyleRes int themeId, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, themeId);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.positive_label, BuildConfig.SHORT_NAME), (dialog, id) ->
                        dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialog(Context context, String message, DialogInterface.OnClickListener positiveListener, boolean showCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.positive_label, BuildConfig.SHORT_NAME), positiveListener);
        if (showCancel)
            builder.setNegativeButton("Annulla", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialog(Context context, String message, DialogInterface.OnClickListener positiveListener, String negativeText, boolean showCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.positive_label, BuildConfig.SHORT_NAME), positiveListener);
        if (showCancel)
            builder.setNegativeButton(negativeText, (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialog(Context context, String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.positive_label), positiveListener)
                .setNegativeButton("Annulla", negativeListener);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialog(Context context, String title, String message, DialogInterface.OnClickListener positiveListener, boolean showCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", positiveListener);
        if (showCancel)
            builder.setNegativeButton("Annulla", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialog(Context context, @StyleRes int themeId, String title, String message, DialogInterface.OnClickListener positiveListener, boolean showCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, themeId);
        builder.setTitle(title);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", positiveListener);
        if (showCancel)
            builder.setNegativeButton("Annulla", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialog(Context context, @StyleRes int themeId, String title, String message, DialogInterface.OnClickListener positiveListener, String negativeText, boolean showCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, themeId);
        builder.setTitle(title);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", positiveListener);
        if (showCancel)
            builder.setNegativeButton(negativeText, (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }


    public static void openLink(Context context, String link) {
        if(link == null) {
            return;
        }

        Uri uri = Uri.parse(link);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static void askForStoragePermission(BaseFragment fragment, PermissionListener permissionListener) {
        new PermissionManager.Builder().with(fragment).setResultListener(new PermissionManager.OnPermissionResultListener() {
            @Override
            public void onPermissionGranted() {
                permissionListener.onPermissionGranted();
            }

            @Override
            public void onPermissionDenied() {
            }

            @Override
            public void onBlockedRequest() {
            }

            @Override
            public void onPermissionRepeated() {
            }
        }).build().checkPermission(WRITE_EXTERNAL_STORAGE, EXPORT_REQUEST);
    }

    public static List<LudoNavigationItem> createNavigationItemsList(Context context) {
        List<LudoNavigationItem> navigationItemList = new ArrayList<>();

        navigationItemList.add(new LudoNavigationItem(R.id.action_home, R.drawable.ic_home_white_24dp, context.getString(R.string.home)));
        navigationItemList.add(new LudoNavigationItem(R.id.action_random,R.drawable.ic_dado_casuale_white, context.getString(R.string.random)));
        navigationItemList.add(new LudoNavigationItem(R.id.action_video, R.drawable.ic_video_white_24dp, context.getString(R.string.video)));
        navigationItemList.add(new LudoNavigationItem(R.id.action_favorites, R.drawable.ic_star_white_24dp, context.getString(R.string.favorites)));

        return navigationItemList;
    }
}
