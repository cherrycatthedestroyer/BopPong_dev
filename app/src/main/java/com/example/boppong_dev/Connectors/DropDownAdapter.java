package com.example.boppong_dev.Connectors;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import com.example.boppong_dev.Model.Song;

import com.example.boppong_dev.R;

import java.util.List;
//SOURCED but modified
public class DropDownAdapter<T>
        extends ArrayAdapter<T>
{
    private Filter filter = new KNoFilter();
    public List<Song> items;

    @Override
    public Filter getFilter() {
        return filter;
    }

    public DropDownAdapter(Context context, int textViewResourceId,
                           List<Song> objects) {
        super(context, textViewResourceId, (List<T>) objects);
        Log.v("Krzys", "Adapter created " + filter);
        items = objects;
    }



    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.two_list_items, null);
        }

        Song l = items.get(position);

        if (l != null) {

            TextView name = (TextView) v.findViewById(R.id.dropName);
            TextView artist = (TextView) v.findViewById(R.id.dropArtist);

            if (name != null){
                name.setText(l.getName());
            }
            if (artist != null){
                artist.setText(l.getArtist());
            }
        }

        return v;
    }



    private class KNoFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence arg0) {
            FilterResults result = new FilterResults();
            result.values = items;
            result.count = items.size();
            return result;
        }

        @Override
        protected void publishResults(CharSequence arg0, FilterResults arg1) {
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object result) {
            if(result instanceof Song) {
                return ((Song) result).getName();
            }

            return super.convertResultToString(result);
        }
    }

    public void onBindViewHolder(){

    }


}
