package me.edujtm.tuyo.ui.likedvideos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import me.edujtm.tuyo.MainViewModel
import me.edujtm.tuyo.R
import me.edujtm.tuyo.data.PlaylistItem
import me.edujtm.tuyo.repository.http.RequestState
import me.edujtm.tuyo.ui.adapters.PlaylistAdapter
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

// TODO: refactor this class so that it represents all playlists (not only liked videos)
class LikedVideosFragment : Fragment() {

    private val mainViewModel: MainViewModel by sharedViewModel()
    private val likedVideosViewModel: LikedVideosViewModel by viewModel()
    private lateinit var textView: TextView
    private lateinit var playlistView: RecyclerView
    private lateinit var playlistItems: MutableList<PlaylistItem>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_liked_videos, container, false)
        textView = root.findViewById(R.id.text_dashboard)
        playlistItems = mutableListOf()
        playlistView = root.findViewById(R.id.liked_videos_rv)
        with(playlistView) {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = PlaylistAdapter(playlistItems)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        likedVideosViewModel.likedVideos.observe(viewLifecycleOwner, Observer { request ->
            when (request) {
                is RequestState.Loading -> textView.text = "Loading request"
                is RequestState.Success -> {
                    textView.text = "Your Liked Videos"
                    (playlistView.adapter as PlaylistAdapter).replacePlaylistItems(request.data)
                }
                is RequestState.Failure -> handleYoutubeError(request.e)
            }
        })

        likedVideosViewModel.getLikedVideos()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_AUTHORIZATION && resultCode == Activity.RESULT_OK) {
            likedVideosViewModel.getLikedVideos()
        }
    }

    private fun handleYoutubeError(error: Throwable) {
        when (error) {
            is GooglePlayServicesAvailabilityIOException -> mainViewModel.checkGoogleApiServices()
            is UserRecoverableAuthIOException -> startActivityForResult(error.intent, REQUEST_AUTHORIZATION)
            else -> textView.text = "The following error occurred ${error.message}"
        }
    }

    companion object {
        const val REQUEST_AUTHORIZATION = 1001
    }

}