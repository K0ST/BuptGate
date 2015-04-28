package pro.kost.bupt.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.edu.bupt.IPLocationMap;
import pro.kost.bupt.R;

/**
 * Created by kost on 14/11/24.
 */
public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> implements View.OnClickListener {
    public ArrayList<String> titles = null;
    public ArrayList<String> details = null;
    public InfoAdapter(ArrayList<String> titles,ArrayList<String> details) {
        this.titles = titles;
        this.details = details;
    }
    public ArrayList<String> getTitles(){
        return titles;
    }
    public ArrayList<String> getDetails(){
        return details;
    }
    public void setDatas(ArrayList<String> titles,ArrayList<String> details) {
        this.titles = titles;
        this.details = details;
        //this.notifyDataSetChanged();
    }
    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.info_item,viewGroup,false);
        ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener(this);
        return vh;
    }
    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        if (i >= titles.size())
            return;
        viewHolder.mTitle.setText(titles.get(i) + " ");
        viewHolder.mDetail.setText(details.get(i) + " ");
        viewHolder.itemView.setTag(details.get(i) + " ");

        if (titles.get(i).contains("终端") && !IPLocationMap.getLocation(details.get(i)).contains("未知")) {
            viewHolder.mLoc.setVisibility(View.VISIBLE);
            viewHolder.mLoc.setText( IPLocationMap.getLocation(details.get(i)) + " ");
        } else {
            viewHolder.mLoc.setVisibility(View.GONE);
        }
    }
    //获取数据的数量
    @Override
    public int getItemCount() {
        return titles.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onClick(v,(String)v.getTag());
        }
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;
        public TextView mDetail;
        public TextView mLoc;
        public ViewHolder(View view){
            super(view);
            mTitle = (TextView) view.findViewById(R.id.title);
            mDetail = (TextView) view.findViewById(R.id.detail);
            mLoc = (TextView) view.findViewById(R.id.loc);
        }
    }
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
    public static interface OnRecyclerViewItemClickListener {
        void onClick(View view ,String detail);
    }
}