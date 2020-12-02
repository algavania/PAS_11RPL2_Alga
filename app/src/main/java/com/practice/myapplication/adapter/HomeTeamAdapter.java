package com.practice.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.practice.myapplication.R;
import com.practice.myapplication.model.ItemProperty;

import java.util.List;

public class HomeTeamAdapter extends RecyclerView.Adapter<HomeTeamAdapter.UserViewHolder> {

    private List<ItemProperty> dataList;
    private OnItemClickListener mListener;
    Context mContext;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public HomeTeamAdapter(Context mContext, List<ItemProperty> dataList) {
        this.mContext = mContext;
        this.dataList = dataList;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.home_team_list, parent, false);
        return new UserViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {
        Glide.with(mContext).load(dataList.get(position).getImageUrl())
                .placeholder(R.drawable.icon)
                .fitCenter()
                .into(holder.img_homeList);
    }

    @Override
    public int getItemCount() {
        return (dataList != null) ? dataList.size() : 0;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView img_homeList;
        private RelativeLayout relativeLayout;

        public UserViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            img_homeList = itemView.findViewById(R.id.img_homeList);
            relativeLayout = itemView.findViewById(R.id.home_layout);

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
}
