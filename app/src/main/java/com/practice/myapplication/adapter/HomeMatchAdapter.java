package com.practice.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.practice.myapplication.R;
import com.practice.myapplication.model.MatchProperty;

import java.util.List;

public class HomeMatchAdapter extends RecyclerView.Adapter<HomeMatchAdapter.UserViewHolder> {

    private List<MatchProperty> dataList;
    private OnItemClickListener mListener;
    Context mContext;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public HomeMatchAdapter(Context mContext, List<MatchProperty> dataList) {
        this.mContext = mContext;
        this.dataList = dataList;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.home_match_list, parent, false);
        return new UserViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {
        holder.tv_match_name.setText(dataList.get(position).getEventName());
        holder.tv_match_date.setText(dataList.get(position).getDate());
        Glide.with(mContext).load(dataList.get(position).getImg_away())
                .placeholder(R.drawable.icon)
                .fitCenter()
                .into(holder.img_away);
        Glide.with(mContext).load(dataList.get(position).getImg_home())
                .placeholder(R.drawable.icon)
                .fitCenter()
                .into(holder.img_home);
    }

    @Override
    public int getItemCount() {
        return (dataList != null) ? dataList.size() : 0;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tv_match_name, tv_match_date;
        ImageView img_home, img_away;

        public UserViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            img_home = itemView.findViewById(R.id.img_match_home);
            img_away = itemView.findViewById(R.id.img_match_away);
            tv_match_date = itemView.findViewById(R.id.tv_match_date);
            tv_match_name = itemView.findViewById(R.id.tv_match_name);

        }
    }
}
