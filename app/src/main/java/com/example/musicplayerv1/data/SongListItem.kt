package com.example.musicplayerv1.data

import android.graphics.Bitmap

data class SongListItem(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: String,
    val coverArt: Bitmap?
)