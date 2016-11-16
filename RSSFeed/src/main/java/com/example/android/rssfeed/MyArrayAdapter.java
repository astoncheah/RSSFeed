package com.example.android.rssfeed;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.android.rssfeed.R.id.txtDate;
import static com.example.android.rssfeed.R.id.txtItemNo;
import static com.example.android.rssfeed.R.id.txtSectionName;
import static com.example.android.rssfeed.R.id.txtTitle;

/**
 * Created by cheah on 21/10/16.
 */

public class MyArrayAdapter extends ArrayAdapter<RSSFeed> {
    private Context context;
    private ViewHolder holder;

    public MyArrayAdapter(Context context, int resource, ArrayList<RSSFeed> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.txtItemNo = (TextView) convertView.findViewById(txtItemNo);
            holder.txtTitle = (TextView) convertView.findViewById(txtTitle);
            holder.txtSectionName = (TextView) convertView.findViewById(txtSectionName);
            holder.txtDate = (TextView) convertView.findViewById(txtDate);
            convertView.setTag(holder);
        }
        final RSSFeed info = this.getItem(position);

        holder = (ViewHolder)convertView.getTag();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = info.getInfoLink();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);
            }
        });

        holder.txtItemNo.setText(position+1+")");
        holder.txtTitle.setText(checktext(info.getTitle()));
        holder.txtSectionName.setText(checktext(info.getSectionName()));
        holder.txtDate.setText(checktext(info.getPublishedDate()));
        return convertView;
    }
    private String checktext(String str){
        if(!str.isEmpty()){
            return str;
        }
        return context.getString(R.string.unknown);
    }
    private static class ViewHolder {
        TextView txtItemNo;
        TextView txtTitle;
        TextView txtSectionName;
        TextView txtDate;
    }
}
