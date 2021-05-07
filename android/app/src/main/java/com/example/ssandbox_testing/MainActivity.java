package com.example.ssandbox_testing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.drm.DrmStore;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private int requestCode;
    private int grantResults[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);



        //create a nav view object and a nav item selected listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        //implement this function
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        //sync the rotation and the drawer animation
        toggle.syncState();

        //if we rotate the device, the activity is refreshed, this will cause us to load the fragment once again
        //thus, this way, we will not reload the fragment after rotating the device
        if (savedInstanceState == null) {
            //set to open as default fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new localInferenceFragment()).commit();
            navigationView.setCheckedItem(R.id.localInference);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //switch case to change the fragment container layout based on each selected item
        switch(item.getItemId()){
            case R.id.nav_addimagedata:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new addimagedataFragment()).commit();

                break;
//            case R.id.nav_configuration:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        new configurationFragment()).commit();
//                break;
            case R.id.cloudInference:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new cloudInferenceFragment()).commit();
                break;
            case R.id.localInference:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new localInferenceFragment()).commit();
                break;
        }
        //close drawer after selection
        drawer.closeDrawer(GravityCompat.START);

        //return true when the item is selected
        return true;
    }

    //Alter the backbutton status as we dont want to clsoe the activity on pressing the back button
    //but to close the drawer instead
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}