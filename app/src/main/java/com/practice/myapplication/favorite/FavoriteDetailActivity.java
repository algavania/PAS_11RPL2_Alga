package com.practice.myapplication.favorite;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.practice.myapplication.R;
import com.practice.myapplication.RealmHelper;
import com.practice.myapplication.activity.FavoriteStadiumEditActivity;
import com.practice.myapplication.activity.FavoriteTeamEditActivity;
import com.practice.myapplication.activity.HomeActivity;
import com.practice.myapplication.favorite.ui.main.SectionsPagerAdapter;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class FavoriteDetailActivity extends AppCompatActivity {

    Button btn_detail_back, btn_detail_edit;
    Realm realm;
    RealmHelper realmHelper;
    Bundle extras;
    int id;
    String team, imageUrl, description, formedYear, stadiumName, stadiumDesc, stadiumImage, stadiumLocation;
    TextView tv_teamName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_detail);

        Realm.init(this);

        RealmConfiguration configuration = new RealmConfiguration.Builder().allowWritesOnUiThread(true).build();
        realm = Realm.getInstance(configuration);
        realmHelper = new RealmHelper(realm);

        btn_detail_edit = findViewById(R.id.btn_edit_favorite);
        btn_detail_back = findViewById(R.id.btn_detail_back_favorite);
        btn_detail_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tv_teamName = findViewById(R.id.tv_detail_title_favorite);

        extras = getIntent().getExtras();
        if (extras != null) {
            team = extras.getString("team");
            id = extras.getInt("id");
            stadiumDesc = extras.getString("stadiumDesc");
            stadiumName = extras.getString("stadium");
            formedYear = extras.getString("year");
            stadiumImage = extras.getString("stadiumImage");
            stadiumLocation = extras.getString("stadiumLocation");
            imageUrl = extras.getString("imageUrl");
            description = extras.getString("description");

            tv_teamName.setText(team);
        }

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager2);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs2);

        goToEdit(tabs);

        tabs.setupWithViewPager(viewPager);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                goToEdit(tabs);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void goToEdit(TabLayout tabs) {
        btn_detail_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (tabs.getSelectedTabPosition() == 0) {
                    Log.d("intent", "1");
                    intent = new Intent(getApplicationContext(), FavoriteTeamEditActivity.class);
                    intent.putExtra("team", team);
                    intent.putExtra("year", formedYear);
                    intent.putExtra("stadium", stadiumName);
                    intent.putExtra("imageUrl", imageUrl);
                    intent.putExtra("description", description);
                } else {
                    Log.d("intent", "2");
                    intent = new Intent(getApplicationContext(), FavoriteStadiumEditActivity.class);
                    intent.putExtra("stadium", stadiumName);
                    intent.putExtra("stadiumLocation", stadiumLocation);
                    intent.putExtra("stadiumDesc", stadiumDesc);
                    intent.putExtra("stadiumImage", stadiumImage);
                }
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("fromFavorite", true);
        startActivity(intent);
        finish();
    }
}