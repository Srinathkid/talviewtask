package com.android.talviewtask.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.talviewtask.Model.SongsLists;
import com.android.talviewtask.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AudioPlaylistAdapter extends RecyclerView.Adapter<AudioPlaylistAdapter.ViewHolder> {
    Context context;
    List<SongsLists> audio_list;

    public AudioPlaylistAdapter(Context context, List<SongsLists> audio_list) {
        this.context = context;
        this.audio_list = audio_list;

    }

    @NonNull
    @Override
    public AudioPlaylistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.playlist_row, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AudioPlaylistAdapter.ViewHolder viewHolder, final int i) {
        SongsLists videoModel = audio_list.get(i);

        viewHolder.video_thumb_img.layout(0, 0, 0, 0);
        Glide.with(context)
                .load(videoModel.getThumbnail())
                .asBitmap()
                .placeholder(R.drawable.thumb_img)
                .error(R.drawable.thumb_img)
                .into(viewHolder.video_thumb_img);

        viewHolder.title.setText(audio_list.get(i).getTitle());
       /* viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoPlayActivity.class);
                intent.putExtra("video_id", audio_list.get(i).getId());
                intent.putParcelableArrayListExtra("video_list", (ArrayList<? extends Parcelable>) audio_list);
                context.startActivity(intent);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return audio_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView video_thumb_img;
        TextView title, desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            video_thumb_img = (ImageView) itemView.findViewById(R.id.pl_row_img);
            title = (TextView) itemView.findViewById(R.id.pl_row_title);

        }
    }
}
