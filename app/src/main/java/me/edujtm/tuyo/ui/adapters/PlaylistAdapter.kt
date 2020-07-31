package me.edujtm.tuyo.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.channels.Channel
import me.edujtm.tuyo.R
import me.edujtm.tuyo.data.model.PlaylistItem

/**
 * This will be the main adapter for playlist videos list. It'll be used by
 * LikedVideosFragment, PlaylistDetails, etc.
 */
class PlaylistAdapter(val context: Context)
    : PagingDataAdapter<PlaylistItem, PlaylistAdapter.ViewHolder>(PlaylistItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val playlistItemView = inflater.inflate(R.layout.playlist_item, parent, false)
        return ViewHolder(playlistItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.titleTextView.text = item?.title

        Glide.with(context)
            .load(item?.thumbnail)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.thumbnailImageView)
    }

    inner class ViewHolder(playlistItemView: View) : RecyclerView.ViewHolder(playlistItemView) {
        val titleTextView: TextView = playlistItemView.findViewById(R.id.playlist_title_tv)
        val thumbnailImageView: ImageView = playlistItemView.findViewById(R.id.video_thumbnail_iv)
    }
}

