package com.practice.myapplication.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.practice.myapplication.R;
import com.practice.myapplication.RealmHelper;
import com.practice.myapplication.activity.HomeActivity;
import com.practice.myapplication.adapter.FavoriteAdapter;
import com.practice.myapplication.favorite.FavoriteDetailActivity;
import com.practice.myapplication.model.ItemProperty;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private List<ItemProperty> arrayList;
    public TextView tv_noFavorite;
    Realm realm;
    RealmHelper realmHelper;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Favorite Teams");
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Realm.init(getActivity());

        RealmConfiguration configuration = new RealmConfiguration.Builder().allowWritesOnUiThread(true).build();
        realm = Realm.getInstance(configuration);
        realmHelper = new RealmHelper(realm);

        tv_noFavorite = view.findViewById(R.id.tv_noFavorite);
        recyclerView = view.findViewById(R.id.recycler_view_favorite);

        arrayList = new ArrayList<>();
        arrayList = realmHelper.getAllMovies();

        show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    public void show() {
        adapter = new FavoriteAdapter(getActivity(), arrayList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        if (!arrayList.isEmpty()) {
            tv_noFavorite.setVisibility(View.INVISIBLE);
        }

        adapter.setOnItemClickListener(new FavoriteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getActivity(), FavoriteDetailActivity.class);
                intent.putExtra("id", arrayList.get(position).getId());
                intent.putExtra("team", arrayList.get(position).getTeam());
                intent.putExtra("fromHome", true);
                intent.putExtra("stadiumDesc", arrayList.get(position).getStadiumDesc());
                intent.putExtra("stadiumImage", arrayList.get(position).getStadiumImage());
                intent.putExtra("stadiumLocation", arrayList.get(position).getStadiumLocation());
                intent.putExtra("id", arrayList.get(position).getId());
                intent.putExtra("description", arrayList.get(position).getDescription());
                intent.putExtra("imageUrl", arrayList.get(position).getImageUrl());
                intent.putExtra("stadium", arrayList.get(position).getStadiumName());
                intent.putExtra("year", arrayList.get(position).getFormedYear());
                intent.putExtra("favorite", arrayList.get(position).getFavorite());

                Log.d("fav", ""+arrayList.get(position).getStadiumName());
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.btn_search);
        SearchView searchView = new SearchView(((HomeActivity) getActivity()).getSupportActionBar().getThemedContext());
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        // MenuItemCompat.setShowAsAction(item, //MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | //MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        //  MenuItemCompat.setActionView(item, searchView);
        // These lines are deprecated in API 26 use instead
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setActionView(searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
    }
}