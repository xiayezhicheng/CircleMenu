package com.dlut.wanghao.circlemenu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wanghao on 2015/11/25.
 */
public class CircleMenuAdapter extends BaseAdapter {

    List<CircleMenuItem> mCircleMenuItems;

    public CircleMenuAdapter(List<CircleMenuItem> mCircleMenuItems) {
        this.mCircleMenuItems = mCircleMenuItems;
    }

    @Override
    public int getCount() {
        return mCircleMenuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mCircleMenuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.circle_menu_item,parent,false);
        ImageView iv = (ImageView)itemView.findViewById(R.id.img_item);
        TextView tv = (TextView)itemView.findViewById(R.id.txt_item);
        CircleMenuItem circleMenuItem = mCircleMenuItems.get(position);
        iv.setImageResource(circleMenuItem.imageId);
        tv.setText(circleMenuItem.title);
        return itemView;
    }
}
