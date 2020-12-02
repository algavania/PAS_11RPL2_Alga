package com.practice.myapplication.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.practice.myapplication.R;
import com.practice.myapplication.RealmHelper;
import com.practice.myapplication.model.ItemProperty;
import com.practice.myapplication.model.Preferences;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.UserViewHolder> implements Filterable {

    private List<ItemProperty> dataList;
    private List<ItemProperty> dataListFull;
    private OnItemClickListener mListener;
    private Context mContext;
    Realm realm;
    RealmHelper realmHelper;
    Preferences preferences;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public ItemAdapter(Context mContext, List<ItemProperty> dataList) {
        this.mContext = mContext;
        this.dataList = dataList;
        dataListFull = new ArrayList<>(dataList);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.team_list, parent, false);
        return new UserViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {
        Realm.init(mContext);
        RealmConfiguration configuration = new RealmConfiguration.Builder().allowWritesOnUiThread(true).build();
        configuration.isAllowWritesOnUiThread();
        realm = Realm.getInstance(configuration);
        realmHelper = new RealmHelper(realm);
        preferences = new Preferences();

        holder.tv_listTitle.setText(dataList.get(position).getTeam());
        holder.tv_listDescription.setText(dataList.get(position).getDescription());
        if (dataList.get(position).getFavorite()) {
            holder.img_favorite.setImageResource(R.drawable.ic_baseline_favorite_red);
        } else {
            holder.img_favorite.setImageResource(R.drawable.ic_baseline_favorite_gray);
        }
        holder.img_favorite.setOnClickListener(view -> {
            if (dataList.get(position).getFavorite()) {
                holder.img_favorite.setImageResource(R.drawable.ic_baseline_favorite_gray);
                Toast.makeText(mContext, "Team has been removed from favorites.", Toast.LENGTH_SHORT).show();
                Log.d("Pos: ", "" + position);
                dataList.get(position).setFavorite(false);
                Runnable runnable = () -> realmHelper.delete(dataList.get(position));
                runnable.run();
            } else {
                dataList.get(position).setFavorite(true);
                holder.img_favorite.setImageResource(R.drawable.ic_baseline_favorite_red);
                Toast.makeText(mContext, "Team has been added to favorites.", Toast.LENGTH_SHORT).show();
                Runnable runnable = () -> realmHelper.save(dataList.get(position));
                runnable.run();
            }
        });
        Glide.with(mContext).load(dataList.get(position).getImageUrl())
                .placeholder(R.drawable.icon)
                .fitCenter()
                .into(holder.img_listImage);
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

        public UserViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            tv_listTitle = itemView.findViewById(R.id.tv_listTitle);
            tv_listDescription = itemView.findViewById(R.id.tv_listDescription);
            img_favorite = itemView.findViewById(R.id.img_favorite);
            img_listImage = itemView.findViewById(R.id.img_listImage);
            relativeLayout = itemView.findViewById(R.id.relative_layout);

            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

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
            dataList.clear();
            dataList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };
}
