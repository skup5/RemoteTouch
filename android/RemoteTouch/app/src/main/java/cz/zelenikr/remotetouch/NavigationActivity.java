package cz.zelenikr.remotetouch;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import cz.zelenikr.remotetouch.data.AppInfo;
import cz.zelenikr.remotetouch.fragment.ConnectionSettingsFragment;
import cz.zelenikr.remotetouch.fragment.InstalledAppsFragment;
import cz.zelenikr.remotetouch.fragment.DeveloperFragment;
import cz.zelenikr.remotetouch.fragment.MainSettingsFragment;

public class NavigationActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    DeveloperFragment.OnFragmentInteractionListener,
    InstalledAppsFragment.OnListItemStateChangedListener {

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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_developer) {
            fragment = new DeveloperFragment();
            replaceFragment(fragment);
        } else if (id == R.id.nav_settings) {
            fragment = new MainSettingsFragment();
            replaceFragment(fragment);
        }
        // Advanced settings
        else if (id == R.id.nav_notifications) {
            fragment = new InstalledAppsFragment();
            addFragment(fragment);
        } else if (id == R.id.nav_connection) {
            fragment = new ConnectionSettingsFragment();
            addFragment(fragment);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onStateChanged(AppInfo item, int position) {
        snackbar(item.getAppName(), Snackbar.LENGTH_SHORT);
    }

    private void addFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.navigation_content, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void replaceFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.navigation_content, fragment)
            .commit();
    }

    private void snackbar(String text, int duration) {
        View view = findViewById(R.id.navigation_content);
        Snackbar.make(view, text, duration).show();
    }


}
