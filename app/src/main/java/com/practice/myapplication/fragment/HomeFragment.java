package com.practice.myapplication.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.practice.myapplication.R;
import com.practice.myapplication.RealmHelper;
import com.practice.myapplication.activity.DetailActivity;
import com.practice.myapplication.activity.HomeActivity;
import com.practice.myapplication.activity.TeamActivity;
import com.practice.myapplication.adapter.HomeMatchAdapter;
import com.practice.myapplication.adapter.HomeTeamAdapter;
import com.practice.myapplication.model.ItemProperty;
import com.practice.myapplication.model.MatchProperty;
import com.practice.myapplication.model.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView recyclerView, matchRv;
    private HomeTeamAdapter adapter;
    private HomeMatchAdapter matchAdapter;
    private List<ItemProperty> arrayList;
    private List<MatchProperty> matchList;
    String imageUrl, title, description, formedYear, stadiumName, stadiumDesc, stadiumImage, stadiumLocation;
    String eventName, date, img_home, img_away, id_home, id_away, away, home;
    String imageProfileUrl, loginMedia, userId;
    ProgressBar progressBar, progressBar_two;
    RelativeLayout layout_team;
    TextView tv_username;
    CircularImageView img_user;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    AccessToken accessToken;
    GoogleSignInAccount account;
    GoogleSignInClient mGoogleSignInClient;
    Preferences preferences;
    boolean isLoggedIn;
    Realm realm;
    RealmHelper realmHelper;

    DatabaseReference databaseReference;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Realm.init(getActivity());

        RealmConfiguration configuration = new RealmConfiguration.Builder().allowWritesOnUiThread(true).build();
        realm = Realm.getInstance(configuration);
        realmHelper = new RealmHelper(realm);

        accessToken = AccessToken.getCurrentAccessToken();
        isLoggedIn = accessToken != null && !accessToken.isExpired();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        account = GoogleSignIn.getLastSignedInAccount(getActivity());
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        preferences = new Preferences();

        recyclerView = view.findViewById(R.id.recycler_home);
        matchRv = view.findViewById(R.id.recycler_home_match);
        progressBar = view.findViewById(R.id.home_progress_bar_1);
        progressBar_two = view.findViewById(R.id.home_progress_bar_2);
        layout_team = view.findViewById(R.id.layout_home_team);
        tv_username = view.findViewById(R.id.tv_home_username);
        img_user = view.findViewById(R.id.img_home_user);

        if (account != null) {
            userId = account.getId();
            loginMedia = "google";
            String personName = account.getDisplayName();
            imageProfileUrl = account.getPhotoUrl().toString();
            if (isAdded()) {
                Glide.with(getActivity()).load(imageProfileUrl)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .fitCenter()
                        .into(img_user);
            }
            tv_username.setText(personName);
            getAllProfileInfo();
        } else if (user != null) {
            userId = user.getUid();
            loginMedia = "firebase";
            String email = user.getEmail();
            if (user.getPhotoUrl() != null) {
                imageProfileUrl = user.getPhotoUrl().toString();
                if (isAdded()) {
                    Glide.with(getActivity()).load(imageProfileUrl)
                            .placeholder(R.drawable.ic_baseline_person_24)
                            .fitCenter()
                            .into(img_user);
                }
            }
            tv_username.setText(email);
            getAllProfileInfo();
        } else if (isLoggedIn) {
            loginMedia = "facebook";
            facebookLogin();
        }

        layout_team.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TeamActivity.class);
                startActivity(intent);
            }
        });

        img_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                intent.putExtra("fromProfile", true);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

        AndroidNetworking.initialize(getActivity());
        addTeamData();
        addEventData();
    }

    private void addTeamData() {
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
                            adapter = new HomeTeamAdapter(getActivity(), arrayList);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setItemViewCacheSize(20);
                            recyclerView.setDrawingCacheEnabled(true);
                            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(adapter);

                            adapter.setOnItemClickListener(position -> {
                                Intent intent = new Intent(getActivity(), DetailActivity.class);
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
                                startActivity(intent);
                            });
                        } catch (Exception e) {
                            Log.d("Error: ", e.toString());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Something error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addEventData() {
        progressBar_two.setVisibility(View.VISIBLE);
        matchList = new ArrayList<>();
        AndroidNetworking.get("https://www.thesportsdb.com/api/v1/json/1/eventsnextleague.php?id=4328")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar_two.setVisibility(View.INVISIBLE);
                        try {
                            JSONArray resultArray = response.getJSONArray("events");
                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject resultObj = resultArray.getJSONObject(i);
                                eventName = resultObj.getString("strEvent");
                                date = resultObj.getString("dateEvent");

                                String inputPattern = "yyyy-MM-dd";
                                String outputPattern = "d MMMM yyyy";
                                SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                                SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

                                try {
                                    Date dateTime = inputFormat.parse(date);
                                    String str = outputFormat.format(dateTime);
                                    date = str;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                id_home = resultObj.getString("idHomeTeam");
                                id_away = resultObj.getString("idAwayTeam");
                                setImageTeam(eventName, date, id_home, true);
                                setImageTeam(eventName, date, id_away, false);
                            }
                        } catch (Exception e) {
                            Log.d("Error: ", e.toString());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Something error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setImageTeam(String eventName, String date, String id, boolean isHome) {
        progressBar_two.setVisibility(View.VISIBLE);
        AndroidNetworking.get("https://www.thesportsdb.com/api/v1/json/1/lookupteam.php?id=" + id)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar_two.setVisibility(View.INVISIBLE);
                        try {
                            JSONArray resultArray = response.getJSONArray("teams");
                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject resultObj = resultArray.getJSONObject(i);
                                if (isHome) {
                                    img_home = resultObj.getString("strTeamBadge");
                                    home = resultObj.getString("strTeam");
                                } else {
                                    img_away = resultObj.getString("strTeamBadge");
                                    away = resultObj.getString("strTeam");
                                }

                                String name = home + " vs " + away;

                                if (eventName.equals(name)) {
                                    matchList.add(new MatchProperty(eventName, date, img_home, img_away));
                                }
                            }
                            matchAdapter = new HomeMatchAdapter(getActivity(), matchList);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                            matchRv.setHasFixedSize(true);
                            matchRv.setItemViewCacheSize(20);
                            matchRv.setDrawingCacheEnabled(true);
                            matchRv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                            matchRv.setLayoutManager(layoutManager);
                            matchRv.setAdapter(matchAdapter);

                        } catch (Exception e) {
                            progressBar_two.setVisibility(View.INVISIBLE);
                            Log.d("Error: ", e.toString());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressBar_two.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Something error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void facebookLogin() {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        if (object != null) {
                            Log.d("test", object.toString());
                            try {
                                userId = object.getString("id");
                                String name = object.getString("name");
                                tv_username.setText(name);
                                if (object.has("picture")) {
                                    imageProfileUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    if (isAdded()) {
                                        Glide.with(getActivity()).load(imageProfileUrl)
                                                .placeholder(R.drawable.ic_baseline_person_24)
                                                .fitCenter()
                                                .into(img_user);
                                    }
                                }
                                getAllProfileInfo();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d("test", "null");
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,cover,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void getProfileInfo(DatabaseReference reference) {
        if (reference != null) {
            Log.d("firebase", "info");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String info = snapshot.getValue(String.class);
                    if (info != null) {
                        if (!info.trim().equals("")){
                            tv_username.setText(info);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getImageInfo(DatabaseReference reference) {
        if (reference != null) {
            Log.d("image", "firebase");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String info = snapshot.getValue(String.class);
                    if (info != null) {
                        if (!info.trim().equals("")) {
                            if (isAdded()) {
                                Glide.with(getActivity()).load(info)
                                        .placeholder(R.drawable.ic_baseline_person_24)
                                        .fitCenter()
                                        .into(img_user);
                            }
                            imageProfileUrl = info;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getAllProfileInfo() {
        DatabaseReference dataName = databaseReference.child(loginMedia).child(userId).child("name");
        getProfileInfo(dataName);
        DatabaseReference dataImage = databaseReference.child(loginMedia).child(userId).child("image");
        getImageInfo(dataImage);
    }
}