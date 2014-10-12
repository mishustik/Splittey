package com.example.splitprice;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class CustomAdapter extends BaseAdapter {

    Context context;
    List<String> data = new ArrayList<String>();
    private static LayoutInflater inflater = null;
    
    private GradientDrawable listItemStyle;

    public CustomAdapter(Context context, List<String> data) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.custom_lv_item, null);
        TextView text = (TextView) vi.findViewById(R.id.element);
        
        listItemStyle = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
		        new int[] {context.getResources().getColor(R.color.sv_gradient_from),context.getResources().getColor(R.color.sv_gradient_to)});
		listItemStyle.setCornerRadius(15);
		text.setBackground(listItemStyle);
        text.setText(data.get(position));
        return vi;
    }
}