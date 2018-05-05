package cz.zelenikr.remotetouch;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import cz.zelenikr.remotetouch.data.AppInfo;
import cz.zelenikr.remotetouch.fragment.AboutFragment;
import cz.zelenikr.remotetouch.fragment.ConnectionSettingsFragment;
import cz.zelenikr.remotetouch.fragment.DeveloperFragment;
import cz.zelenikr.remotetouch.fragment.InstalledAppsFragment;
import cz.zelenikr.remotetouch.fragment.MainSettingsFragment;
import cz.zelenikr.remotetouch.fragment.NotificationSettingsFragment;
import cz.zelenikr.remotetouch.fragment.OpenFragmentListener;

public class NavigationActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    FragmentManager.OnBackStackChangedListener,
    DeveloperFragment.OnFragmentInteractionListener,
    InstalledAppsFragment.OnListItemStateChangedListener,
    OpenFragmentListener {

    public static final String TAG = NavigationActivity.class.getSimpleName();

    private NotificationSettingsFragment notificationSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Fragment fragment = new MainSettingsFragment();
//        Fragment fragment = new InstalledAppsFragment();
        replaceFragment(fragment);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.Navigation_Drawer_Open, R.string.Navigation_Drawer_Close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_settings);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed: ");
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_developer) {
            fragment = new DeveloperFragment();
        } else if (id == R.id.nav_settings) {
            fragment = new MainSettingsFragment();
        }
        // Advanced settings
        else if (id == R.id.nav_notifications) {
            fragment = new NotificationSettingsFragment();
            notificationSettingsFragment = (NotificationSettingsFragment) fragment;
        } else if (id == R.id.nav_connection) {
            fragment = new ConnectionSettingsFragment();
        } else if (id == R.id.nav_about) {
            fragment = new AboutFragment();
        }

        if (fragment != null) addFragment(fragment);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onItemStateChanged(AppInfo item, int position) {
        if (notificationSettingsFragment != null) {
            notificationSettingsFragment.onItemStateChanged(item, position);
        }
        snackbar(item.getAppName(), Snackbar.LENGTH_SHORT);
    }

    @Override
    public void onItemsStateChanged(List<AppInfo> items) {
        if (notificationSettingsFragment != null) {
            notificationSettingsFragment.onItemsStateChanged(items);
        }
        snackbar(items.size() + " items", Snackbar.LENGTH_SHORT);
    }

    @Override
    public void openFragment(Fragment fragment) {
        addFragment(fragment);
    }

    @Override
    public void onBackStackChanged() {
        int id = getNavItemIdByFragment(getCurrentFragment());
        if (id > 0) {
            NavigationView navigation = findViewById(R.id.nav_view);
            if (navigation != null) navigation.setCheckedItem(id);
        }
    }

    /**
     * Returns resource menu item id for the specific fragment or -1
     * if this {@code fragment} doesn't bellow any navigation item.
     *
     * @param fragment
     * @return resource id or -1
     */
    private int getNavItemIdByFragment(Fragment fragment) {
        if (fragment instanceof MainSettingsFragment) return R.id.nav_settings;
        if (fragment instanceof DeveloperFragment) return R.id.nav_developer;
        if (fragment instanceof NotificationSettingsFragment) return R.id.nav_notifications;
        if (fragment instanceof InstalledAppsFragment) return R.id.nav_notifications;
        if (fragment instanceof ConnectionSettingsFragment) return R.id.nav_connection;
        if (fragment instanceof AboutFragment) return R.id.nav_about;
        return -1;
    }

    /**
     * Adds the specific {@link Fragment} in to the {@link android.support.v4.app.FragmentManager} back stack
     * and sets it as the current visible fragment.
     *
     * @param fragment the given fragment
     */
    private void addFragment(@NonNull Fragment fragment) {
        if (!isCurrentFragment(fragment)) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navigation_content, fragment, getFragmentTag(fragment))
                .addToBackStack(null)
                .commit();
        }
    }

    private String getFragmentTag(@NonNull Fragment fragment) {
        return fragment.getClass().getSimpleName();
    }

    private int getFragmentsCount() {
        return getSupportFragmentManager().getBackStackEntryCount();
    }

    /**
     * @param fragment
     * @return true if the given fragment is the current fragment displayed to the user
     */
    private boolean isCurrentFragment(@NonNull Fragment fragment) {
        Fragment founded = getSupportFragmentManager().findFragmentByTag(getFragmentTag(fragment));
        return (founded != null) && founded.isVisible();
    }

    /**
     * Finds current visible fragment stored in fragment manager.
     *
     * @return current fragment or null
     */
    private Fragment getCurrentFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment.isVisible()) return fragment;
        }
        return null;
    }

    /**
     * Replaces current visible {@link Fragment}.
     *
     * @param fragment the fragment that will be displayed
     */
    private void replaceFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.navigation_content, fragment, getFragmentTag(fragment))
            .commit();
    }

    private void snackbar(String text, int duration) {
        View view = findViewById(R.id.navigation_content);
        Snackbar.make(view, text, duration).show();
    }


}
