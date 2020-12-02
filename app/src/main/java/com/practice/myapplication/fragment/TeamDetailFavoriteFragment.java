package com.practice.myapplication.fragment;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.practice.myapplication.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeamDetailFavoriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeamDetailFavoriteFragment extends Fragment {

    Bundle extras;
    ImageView img_team;
    String name, year, stadium, description, imageTeam;
    TextView tv_name, tv_year, tv_stadium, tv_description;

    RelativeLayout relativeLayout;
    LinearLayout linearLayout;
    AppBarLayout appBarLayout;
    TabLayout tabLayout;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TeamDetailFavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeamDetailFavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TeamDetailFavoriteFragment newInstance(String param1, String param2) {
        TeamDetailFavoriteFragment fragment = new TeamDetailFavoriteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("team");
            description = extras.getString("description");
            year = extras.getString("year");
            stadium = extras.getString("stadium");
            imageTeam = extras.getString("imageUrl");
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_name = view.findViewById(R.id.tv_team_name_detail_favorite);
        tv_description = view.findViewById(R.id.tv_team_description_detail_favorite);
        tv_stadium = view.findViewById(R.id.tv_team_stadium_detail_favorite);
        tv_year = view.findViewById(R.id.tv_team_year_detail_favorite);
        img_team = view.findViewById(R.id.img_team_detail_favorite);

        linearLayout = view.findViewById(R.id.team_layout_favorite);
        relativeLayout = view.findViewById(R.id.team_layout_favorite_2);
        tabLayout = getActivity().findViewById(R.id.tabs2);
        appBarLayout = getActivity().findViewById(R.id.toolbar_detail2);

        tv_name.setText(name);
        tv_year.setText("Since "+year);
        tv_stadium.setText(stadium);
        tv_description.setText(description);
        if (isAdded()) {
            Glide.with(getActivity()).load(imageTeam)
                    .placeholder(R.drawable.icon)
                    .fitCenter()
                    .into(img_team);
            setDominantColor();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_team_detail_favorite, container, false);
    }

    public Palette createPaletteSync(Bitmap bitmap) {
        return Palette.from(bitmap).generate();
    }

    private void setDominantColor() {
        Glide.with(this)
                .asBitmap().load(imageTeam)
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                  Toast.makeText(getActivity(), "There's an error, try again", Toast.LENGTH_SHORT).show();
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  Palette palette = createPaletteSync(bitmap);
                                  Palette.Swatch vibrant = palette.getDarkVibrantSwatch();
                                  Palette.Swatch lightVibrant = palette.getLightVibrantSwatch();
                                  if (vibrant != null) {
                                      int titleColor = vibrant.getRgb();

                                      linearLayout.setBackgroundColor(titleColor);
                                      relativeLayout.setBackgroundColor(titleColor);
                                      appBarLayout.setBackgroundColor(titleColor);
                                      tabLayout.setBackgroundColor(titleColor);

                                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                          getActivity().getWindow().setStatusBarColor(titleColor);
                                      }
                                  }

                                  if (lightVibrant != null) {
                                      int titleColor = lightVibrant.getRgb();
                                      tabLayout.setSelectedTabIndicatorColor(titleColor);
                                  }
                                  return false;
                              }
                          }
                ).submit();
    }

}