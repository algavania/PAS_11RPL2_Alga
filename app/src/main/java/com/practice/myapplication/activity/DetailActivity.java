package com.practice.myapplication.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.practice.myapplication.R;
import com.practice.myapplication.RealmHelper;
import com.practice.myapplication.activity.ui.main.SectionsPagerAdapter;
import com.practice.myapplication.model.ItemProperty;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DetailActivity extends AppCompatActivity {

    Button btn_detail_back, btn_detail_favorite;
    Bundle extras;
    TextView tv_teamName;
    Boolean fromHome, isFavorite;
    Realm realm;
    RealmHelper realmHelper;
    int id;
    String team, imageUrl, description, formedYear, stadiumName, stadiumDesc, stadiumImage, stadiumLocation;

    final static int RESULT_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tv_teamName = findViewById(R.id.tv_detail_title);
        fromHome = false;

        Realm.init(this);

        RealmConfiguration configuration = new RealmConfiguration.Builder().allowWritesOnUiThread(true).build();
        realm = Realm.getInstance(configuration);
        realmHelper = new RealmHelper(realm);

        btn_detail_favorite = findViewById(R.id.btn_detail_favorite);
        btn_detail_back = findViewById(R.id.btn_detail_back);

        extras = getIntent().getExtras();
        if (extras != null) {
            fromHome = extras.getBoolean("fromHome", false);
            isFavorite = extras.getBoolean("favorite", false);

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

        if (isFavorite) {
            btn_detail_favorite.setBackground(getResources().getDrawable(R.drawable.ic_baseline_favorite_red));
        } else {
            btn_detail_favorite.setBackground(getResources().getDrawable(R.drawable.ic_baseline_favorite_gray));
        }

        btn_detail_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_detail_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemProperty itemProperty = new ItemProperty(id, imageUrl, team, description, formedYear, stadiumName, stadiumDesc, stadiumImage, stadiumLocation, isFavorite);
                if (isFavorite) {
                    Log.d("Delete", "yes");
                    realmHelper.delete(itemProperty);
                    isFavorite = false;
                    btn_detail_favorite.setBackground(getResources().getDrawable(R.drawable.ic_baseline_favorite_gray));
                    Toast.makeText(getApplicationContext(), "Team has been removed from favorites.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("Save", "yes");
                    realmHelper.save(itemProperty);
                    isFavorite = true;
                    btn_detail_favorite.setBackground(getResources().getDrawable(R.drawable.ic_baseline_favorite_red));
                    Toast.makeText(getApplicationContext(), "Team has been added to favorites.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("id", id);
        intent.putExtra("isFavorite", isFavorite);
        setResult(RESULT_CODE, intent);
        super.onBackPressed();
    }

    public Palette createPaletteSync(Bitmap bitmap) {
        return Palette.from(bitmap).generate();
    }
}