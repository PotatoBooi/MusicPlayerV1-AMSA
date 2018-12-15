package com.example.musicplayerv1.data

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

class MusicDataSource(private val context: Context) {
    private val contentResolver
        get() = context.contentResolver

    suspend fun getPlaylist() : List<PlayerSongItem>{

        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3")
        val selectionArgsMp3 = arrayOf(mimeType)
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            proj,
            selectionMimeType,
            selectionArgsMp3,
            null
        )
        var id:Long = -1
        var title: String =""
        var artist: String = ""
        var album: Int = -1
        var duration: Long = 0
        val list = mutableListOf<PlayerSongItem>()
        if (cursor != null) {
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

            if (cursor.moveToFirst()) {
                do{
                    id = cursor.getLong(idColumn)
                    title = cursor.getString(titleColumn)
                    artist = cursor.getString(artistColumn)
                    album = cursor.getInt(albumColumn)
                    duration = cursor.getLong(durationColumn)
                    list.add(PlayerSongItem(id, ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id), title,artist,duration,getCover(album)))
                }while (cursor.moveToNext())


            }
        }
        cursor?.close()


        return list.toList()

    }

    suspend fun getSongById(id: Long): PlayerSongItem {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = MediaStore.Audio.Media._ID + "=?"
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            proj,
            selection,
            arrayOf(id.toString()),
            null
        )

        var title: String =""
        var artist: String = ""
        var album: Int = -1
        var duration: Long = 0

        if (cursor != null) {
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

            if (cursor.moveToFirst()) {
                title = cursor.getString(titleColumn)
                artist = cursor.getString(artistColumn)
                album = cursor.getInt(albumColumn)
                duration = cursor.getLong(durationColumn)

            }
        }
        cursor?.close()


        return PlayerSongItem(id, uri, title,artist,duration,getCover(album))
    }


    suspend fun getListOfSongs() : List<SongListItem> {
        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE
        )
        val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3")
        val selectionArgsMp3 = arrayOf(mimeType)
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            proj,
            selectionMimeType,
            selectionArgsMp3,
            null
        )

        val songList = mutableListOf<SongListItem>()

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val id = cursor.getLong(idColumn)
                    val albumId = cursor.getInt(albumColumn)
                    val duration = cursor.getString(durationColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    songList.add(SongListItem(id,title,artist,formatDuration(duration),getCover(albumId)))
                } while (cursor.moveToNext())
            }
        }
        cursor?.close()

        return songList.toList()
    }

    private fun getCover(id: Int): Bitmap? {
        val proj = arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART)
        val selection = MediaStore.Audio.Albums._ID + "=?"
        val cursor = contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            proj,
            selection,
            arrayOf("$id"),
            null
        )
        var coverUri: String? = ""
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val coverColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
                coverUri = cursor.getString(coverColumn) ?: ""
            }
        }
        cursor?.close()
        //return try {
        return BitmapFactory.decodeFile(coverUri!!)

//        }catch (ex: Exception){
//
//        }
    }
    private fun formatDuration(value: String): String {
        val milliseconds = value.toLong()
        val seconds = (milliseconds / 1000).toInt() % 60
        val minutes = (milliseconds / 1000).toInt() / 60
        val hours = minutes / 60
        if (hours > 0)
            return "$hours:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"

        return "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
    }
}