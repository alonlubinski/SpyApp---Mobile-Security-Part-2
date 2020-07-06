package com.alon.spyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import com.alon.spyapp.fragments.HistoryFragment;
import com.alon.spyapp.fragments.HomeFragment;
import com.alon.spyapp.fragments.HotZonesFragment;
import com.alon.spyapp.fragments.LiveLocationFragment;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private View navHeader;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private Fragment fragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findAll();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.main_FL, new HomeFragment()).commit();
    }

    // Method that finds all the views by id.
    private void findAll(){
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.main_DL);
        navigationView = findViewById(R.id.main_NV);
        navHeader = navigationView.getHeaderView(0);
    }

    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });
    }

    private void selectDrawerItem(MenuItem item) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_home:
                fragment = new HomeFragment();
                break;

            case R.id.menu_live_location:
                fragment = new LiveLocationFragment();
                break;

            case R.id.menu_history:
                fragment = new HistoryFragment();
                break;

            case R.id.menu_hot_zones:
                fragment = new HotZonesFragment();
                break;
        }
        if(fragment != null){
            changeFragment(fragment, item);
        }
        return false;
    }

    // Method that changes the layout to the chosen fragment.
    private void changeFragment(Fragment fragment, MenuItem item){
        if(fragment != null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_FL, fragment).commit();
            setTitle(item.getTitle());
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

}


