package com.practice.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.practice.myapplication.R;
import com.practice.myapplication.RealmHelper;
import com.practice.myapplication.adapter.ItemAdapter;
import com.practice.myapplication.model.ItemProperty;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class TeamActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<ItemProperty> arrayList;
    String imageUrl, title, description, formedYear, stadiumName, stadiumDesc, stadiumImage, stadiumLocation;
    ProgressBar progressBar;
    Realm realm;
    RealmHelper realmHelper;

    final static int RESULT_CODE = 101;
    final static int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Teams");

        Realm.init(this);

        RealmConfiguration configuration = new RealmConfiguration.Builder().allowWritesOnUiThread(true).build();
        realm = Realm.getInstance(configuration);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        realmHelper = new RealmHelper(realm);

        AndroidNetworking.initialize(this);

        addData();
    }

    private void addData() {
        progressBar.setVisibility(View.VISIBLE);
        arrayList = new ArrayList<>();
        AndroidNetworking.get("https://www.thesportsdb.com/api/v1/json/1/search_all_teams.php?l=English%20Premier%20League")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response: ", "yes");
                        progressBar.setVisibility(View.INVISIBLE);
                        try {
                            JSONArray resultArray = response.getJSONArray("teams");
                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject resultObj = resultArray.getJSONObject(i);
                                imageUrl = resultObj.getString("strTeamBadge");
                                title = resultObj.getString("strTeam");
                                description = resultObj.getString("strDescriptionEN");
                                formedYear = resultObj.getString("intFormedYear");
                                stadiumName = resultObj.getString("strStadium");
                                stadiumDesc = resultObj.getString("strStadiumDescription");
                                stadiumImage = resultObj.getString("strStadiumThumb");
                                stadiumLocation = resultObj.getString("strStadiumLocation");
                                arrayList.add(new ItemProperty(i, imageUrl, title, description, formedYear, stadiumName, stadiumDesc, stadiumImage, stadiumLocation, false));
                                final RealmResults<ItemProperty> model = realm.where(ItemProperty.class).equalTo("description", description).findAll();
                                if (!model.isEmpty()) {
                                    arrayList.get(i).setFavorite(true);
                                }
                            }
                            setAdapter();
                        } catch (Exception e) {
                            Log.d("Error: ", e.toString());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Something error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.btn_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                try {
                    adapter.getFilter().filter(s);
                } catch (Exception e) {
                    Log.d("error", "" + e.toString());
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CODE) {
            int id = data.getIntExtra("id", -1);
            boolean isFavorite = data.getBooleanExtra("isFavorite", false);
            arrayList.get(id).setFavorite(isFavorite);
            Log.d("fav", ""+arrayList.get(id).getFavorite());
            Log.d("name", ""+arrayList.get(id).getTeam());
            setAdapter();
        }
    }

    private void setAdapter() {
        adapter = new ItemAdapter(getApplicationContext(), arrayList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
            intent.putExtra("team", arrayList.get(position).getTeam());
            intent.putExtra("fromHome", false);
            intent.putExtra("stadiumDesc", arrayList.get(position).getStadiumDesc());
            intent.putExtra("stadiumImage", arrayList.get(position).getStadiumImage());
            intent.putExtra("stadiumLocation", arrayList.get(position).getStadiumLocation());
            intent.putExtra("id", arrayList.get(position).getId());
            intent.putExtra("description", arrayList.get(position).getDescription());
            intent.putExtra("imageUrl", arrayList.get(position).getImageUrl());
            intent.putExtra("stadium", arrayList.get(position).getStadiumName());
            intent.putExtra("year", arrayList.get(position).getFormedYear());
            intent.putExtra("favorite", arrayList.get(position).getFavorite());
            startActivityForResult(intent, REQUEST_CODE);
        });
    }
}