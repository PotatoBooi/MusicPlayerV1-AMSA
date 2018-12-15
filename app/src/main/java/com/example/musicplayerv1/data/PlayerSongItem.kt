package com.example.musicplayerv1.data

import android.graphics.Bitmap
import android.net.Uri


data class PlayerSongItem(val id: Long, val uri: Uri, val title:String, val artist:String, val duration: Long,var coverArt: Bitmap?)