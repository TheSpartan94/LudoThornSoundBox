package net.ddns.andrewnetwork.ludothornsoundbox.utils;

import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.IdRes;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public abstract class ViewUtils {

    public static boolean hasItemId(BottomNavigationView navigation, @IdRes int id) {
        Menu menu = navigation.getMenu();

        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.getItemId() == id) {
                return true;
            }
        }

        return false;
    }

    public static void selectByItemId(NavigationView navigation, @IdRes int id) {
        selectByItemId(navigation, navigation.getMenu(), id);
    }

    public static void selectByItemId(NavigationView navigation, Menu menu, @IdRes int id) {

        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);

            if (menuItem.getItemId() == id) {
                navigation.setCheckedItem(menuItem);
            } else if (menuItem.getSubMenu() != null) {
                selectByItemId(navigation, menuItem.getSubMenu(), id);
            }
        }
    }

    public static boolean compareDrawables(Drawable drawable1, Drawable drawable2) {
        return drawable1 != null
                && drawable1.getConstantState() != null
                && drawable2 != null
                && drawable2.getConstantState() != null
                && drawable1.getConstantState().equals(drawable2.getConstantState());
    }
}
