package com.practice.myapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.practice.myapplication.R;
import com.practice.myapplication.RealmHelper;
import com.practice.myapplication.fragment.FavoriteFragment;
import com.practice.myapplication.model.ItemProperty;
import com.practice.myapplication.model.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.UserViewHolder> implements Filterable {

    public List<ItemProperty> dataList;
    public List<ItemProperty> dataListFull;
    public ArrayList<Integer> selectedItems = new ArrayList<>();
    OnItemClickListener mListener;
    Context mContext;
    Realm realm;
    RealmHelper realmHelper;
    Preferences preferences;
    private ActionMode actionMode;
    private boolean multiSelect = false;
    boolean isAll = false;
    boolean isCAB = false;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public FavoriteAdapter(final Context mContext, final List<ItemProperty> dataList) {
        Realm.init(mContext);
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);
        realmHelper = new RealmHelper(realm);
        preferences = new Preferences();
        this.mContext = mContext;
        this.dataList = dataList;
        dataListFull = new ArrayList<>(dataList);
        final RealmResults<ItemProperty> allItems = realm.where(ItemProperty.class).findAll();
        dataListFull = realm.copyFromRealm(allItems);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.team_list, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {
        Realm.init(mContext);
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);
        realmHelper = new RealmHelper(realm);
        preferences = new Preferences();

        if (isAll) {
            holder.relativeLayout.setBackgroundColor(Color.LTGRAY);
            holder.img_listImage.setImageResource(R.drawable.ic_baseline_done_24);
        } else {
            holder.relativeLayout.setBackgroundColor(Color.WHITE);
            Glide.with(mContext).load(dataList.get(position).getImageUrl())
                    .placeholder(R.drawable.icon)
                    .fitCenter()
                    .into(holder.img_listImage);
        }

        holder.update(position, mListener);
        holder.tv_listTitle.setText(dataList.get(position).getTeam());
        holder.tv_listDescription.setText(dataList.get(position).getDescription());
        holder.img_favorite.setImageResource(R.drawable.ic_baseline_favorite_red);
        holder.img_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("isCAB", ""+isCAB);
                if (!isCAB) {
                    deleteItem(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (dataList != null) ? dataList.size() : 0;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_listTitle;
        private TextView tv_listDescription;
        private ImageView img_listImage;
        private ImageView img_favorite;
        private RelativeLayout relativeLayout;

        public UserViewHolder(View itemView) {
            super(itemView);

            tv_listTitle = itemView.findViewById(R.id.tv_listTitle);
            tv_listDescription = itemView.findViewById(R.id.tv_listDescription);
            img_favorite = itemView.findViewById(R.id.img_favorite);
            img_listImage = itemView.findViewById(R.id.img_listImage);
            relativeLayout = itemView.findViewById(R.id.relative_list);
        }

        public void update(final Integer value, final OnItemClickListener listener) {
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        Log.d("clicked", "yes");
                        if (multiSelect) {
                            selectItem(value);
                        } else {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                listener.onItemClick(position);
                            }
                        }
                    }
                }
            });

            relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (actionMode != null) {
                        return false;
                    }
                    actionMode = ((AppCompatActivity) view.getContext()).startSupportActionMode(actionModeCallback);
                    selectItem(value);
                    isCAB = true;
                    return true;
                }
            });
        }

        public void selectItem(Integer item) {
            if (multiSelect) {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                    relativeLayout.setBackgroundColor(Color.WHITE);
                    Glide.with(mContext).load(dataListFull.get(item).getImageUrl()).placeholder(R.drawable.icon).fitCenter().into(img_listImage);
                    if (selectedItems.isEmpty()) {
                        actionMode.finish();
                        actionMode = null;
                        multiSelect = false;
                        isCAB = false;
                        selectedItems.clear();
                        notifyDataSetChanged();
                    } else {
                        if (actionMode != null) {
                            actionMode.setTitle(selectedItems.size() + " Team(s) Selected");
                        }
                    }
                } else {
                    selectedItems.add(item);
                    img_listImage.setImageResource(R.drawable.ic_baseline_done_24);
                    relativeLayout.setBackgroundColor(Color.LTGRAY);
                    if (actionMode != null) {
                        actionMode.setTitle(selectedItems.size() + " Team(s) Selected");
                    }
                    if (selectedItems.size() == dataList.size()) {
                        isAll = true;
                    }
                }
            } else {
                relativeLayout.setBackgroundColor(Color.WHITE);
                isCAB = false;
            }
        }
    }

    // NotifyDataSetChanged in callback is required for accessing userViewHolder.

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            multiSelect = true;
            mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);
            mode.setTitle("Select Team(s)");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.btn_delete:

                    // We reverse the order because if we don't do that, the smaller ID will be removed first
                    // and it'll change the other IDs.
                    Comparator c = Collections.reverseOrder();
                    Collections.sort(selectedItems, c);

                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            for (int key : selectedItems) {
                                deleteItem(key);
                            }
                        }
                    };

                    run.run();
                    notifyDataSetChanged();

                    FavoriteFragment favoriteActivity = new FavoriteFragment();
                    favoriteActivity.tv_noFavorite = ((Activity) mContext).findViewById(R.id.tv_noFavorite);

                    if (dataList.isEmpty()) {
                        favoriteActivity.tv_noFavorite.setVisibility(View.VISIBLE);
                    } else {
                        favoriteActivity.tv_noFavorite.setVisibility(View.INVISIBLE);
                    }

                    Toast.makeText(mContext, "Team(s) has been deleted", Toast.LENGTH_SHORT).show();

                    mode.finish();
                    break;
                case R.id.btn_selectAll:
                    selectedItems.clear();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (!isAll) {
                                for (int i = 0; i < dataList.size(); i++) {
                                    selectedItems.add(i);
                                }
                                isAll = true;
                            } else {
                                mode.finish();
                            }
                            notifyDataSetChanged();
                        }
                    };
                    runnable.run();
                    if (actionMode != null) {
                        actionMode.setTitle(selectedItems.size() + " Team(s) Selected");
                    }
                    break;
                default:
                    return false;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            isAll = false;
            actionMode = null;
            multiSelect = false;
            selectedItems.clear();
            isCAB = false;
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return dataListFilter;
    }

    private Filter dataListFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<ItemProperty> filteredList = new ArrayList<>();
            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(dataListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (ItemProperty item : dataListFull) {
                    if (item.getTeam().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            dataList = (List) filterResults.values;
            notifyDataSetChanged();
        }
    };

    private void deleteItem(int position) {
        List<ItemProperty> storeList = realmHelper.delete(dataList.get(position));
        dataList = storeList;
        FavoriteFragment favoriteActivity = new FavoriteFragment();
        favoriteActivity.tv_noFavorite = ((Activity) mContext).findViewById(R.id.tv_noFavorite);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());

        dataListFull = new ArrayList<>(dataList);

        if (dataList.isEmpty()) {
            favoriteActivity.tv_noFavorite.setVisibility(View.VISIBLE);
        } else {
            favoriteActivity.tv_noFavorite.setVisibility(View.INVISIBLE);
        }
    }
}
