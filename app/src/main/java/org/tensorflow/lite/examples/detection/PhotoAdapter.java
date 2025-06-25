package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewHolder> implements View.OnClickListener {

    Context context;

    public PhotoAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_view;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final String[] name = context.getResources().getStringArray(R.array.breeds);
        final String url_image = Configure.getServerPath(context) + "train/"+ (position) +"/1.jpg";

        holder.name.setText(name[position]);

        Glide.with(context)
                .load(url_image)
                .centerCrop()
               // .placeholder(R.drawable.loading_spinner)
                .into( holder.imageView);
    }

    @Override
    public int getItemCount() {
        return context.getResources().getStringArray(R.array.breeds).length;
    }

    @Override
    public void onClick(View v) {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public TextView name;
        public ImageView imageView;

        MyViewHolder(View view) {
            super(view);

            name  = view.findViewById(R.id.text_breed_name);
            imageView  = view.findViewById(R.id.image);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            //recyclerViewListener.clickedOnPatient(arrayList.get(getAdapterPosition()));
        }
    }
}