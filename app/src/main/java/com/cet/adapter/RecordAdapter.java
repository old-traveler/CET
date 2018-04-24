package com.cet.adapter;



import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cet.R;
import com.cet.bean.Record;
import com.cet.utils.DbUtil;

import java.util.List;

/**
 * Created by hyc on 2018/4/23 18:10
 */

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder>{

    public List<Record> records;

    public RecordAdapter(List<Record> records){
        this.records = records;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.load(records.get(position).getWord());
        if(listener!=null)
            holder.itemView.setOnClickListener(v
                    ->listener.onClick(records.get(position)));
        holder.itemView.setOnLongClickListener(view -> {
            DbUtil.getDbUtil().deleteRecord(records.get(position).getWord());
            records.remove(position);
            notifyItemRemoved(position);
            return true;
        });

    }


    @Override
    public int getItemCount() {
        return records==null?0:records.size();
    }

    public void setData(List<Record> records1){
        this.records = records1;
        notifyDataSetChanged();
    }


    private OnItemClickListener listener;

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void onClick(Record record);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_word;

        public ViewHolder(View view ){
            super(view);
            tv_word = view.findViewById(R.id.tv_word);
        }

        public void load(String word){
            tv_word.setText(word);
        }


    }


}
