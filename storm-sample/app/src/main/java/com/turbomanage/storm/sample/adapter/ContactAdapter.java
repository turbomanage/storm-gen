package com.turbomanage.storm.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.turbomanage.storm.sample.model.Contact;

import java.util.List;

/**
 * A custom array adapter.
 */
public class ContactAdapter extends BaseAdapter {

    private Context mContext;
    private List<Contact> mContacts;

    public ContactAdapter(Context context, List<Contact> contacts){

        mContext = context;
        mContacts = contacts;
    }

    public void remove(int position) {

        mContacts.remove(position);
        notifyDataSetChanged();
    }

    private static class ViewHolder {

        public TextView text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder holder;
        
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact item = getItem(position);
		holder.text.setText(item.toString());
        return convertView;
    }

    @Override
    public int getCount() {
        return mContacts == null ? 0 : mContacts.size();
    }

    @Override
    public Contact getItem(int position) {
        return mContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
