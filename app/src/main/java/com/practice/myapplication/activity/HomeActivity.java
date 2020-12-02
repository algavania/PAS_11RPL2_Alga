package com.practice.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.practice.myapplication.R;
import com.practice.myapplication.fragment.FavoriteFragment;
import com.practice.myapplication.fragment.HomeFragment;
import com.practice.myapplication.fragment.ProfileFragment;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Bundle extras;
    Boolean fromFavorite, fromProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        extras = getIntent().getExtras();
        if (extras != null) {
            fromFavorite = extras.getBoolean("fromFavorite", false);
            fromProfile = extras.getBoolean("fromProfile", false);
            if (fromFavorite) {
                bottomNavigationView.setSelectedItemId(R.id.nav_favorite);
            } else if (fromProfile) {
                bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            }
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_favorite:
                    selectedFragment = new FavoriteFragment();
                    break;
                case R.id.nav_profile:
                    selectedFragment = new ProfileFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
    };

}