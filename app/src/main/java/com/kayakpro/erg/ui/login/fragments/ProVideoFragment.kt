package com.kayakpro.erg.ui.login.fragments
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.kayakpro.erg.databinding.FragmentProVideoBinding

class ProVideoFragment : Fragment() {

    private lateinit var binding: FragmentProVideoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProVideoBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        setupVideoThumbnails()

        // Your existing click (Pro card)
        binding.ivYoutube.setOnClickListener {
            openPlaylist()
        }
    }

    /**
     * Loads YouTube thumbnails and handles click to play
     */
    private fun setupVideoThumbnails() {

        val videoIds = listOf(
            "NUE-UZf1b5Y",
            "vS6ucaKr8Jw",
            "sFAWfiLLdUM",
            "FxuFL0Zet6M",
            "whrkcuO1EvI"
        )

        val imageViews = listOf(
            binding.imgVideo1,
            binding.imgVideo2,
            binding.imgVideo3,
            binding.imgVideo4,
            binding.imgVideo5
        )

        imageViews.forEachIndexed { index, imageView ->

            val videoId = videoIds[index]
            val thumbnailUrl =
                "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

            // Load thumbnail
            Glide.with(this)
                .load(thumbnailUrl)
                .centerCrop()
                .into(imageView)

            // Click → open YouTube
            imageView.setOnClickListener {
                openYouTubeVideo(videoId)
            }
        }
    }

    /**
     * Opens a single YouTube video
     */
    private fun openYouTubeVideo(videoId: String) {
        val appIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("vnd.youtube:$videoId")
        )
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=$videoId")
        )

        try {
            startActivity(appIntent)
        } catch (e: Exception) {
            startActivity(webIntent)
        }
    }

    private fun openPlaylist() {
        val playlistUrl =
            "https://www.youtube.com/watch?v=CmRu462iWPk&list=PLPjCM7a7B85ka4i2Ba2JW-SPsD6saq5Fn"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playlistUrl))
        startActivity(intent)
    }
}
