package com.example.musicplayerv1


import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayerv1.data.MusicDataSource
import com.example.musicplayerv1.data.PlayerSongItem
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PlayerActivity : AppCompatActivity() {
    private val SONG_ID_EXTRA = "SONG_ID"
    private lateinit var mp: MediaPlayer
    private lateinit var currentSong: PlayerSongItem

    private val dataSource = MusicDataSource(this)
    private lateinit var playlist : List<PlayerSongItem>


    override fun onStart() {
        super.onStart()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        val songId = intent.getLongExtra(SONG_ID_EXTRA, -1)

        currentSong = runBlocking {
            dataSource.getSongById(songId)
        }


        GlobalScope.launch(Dispatchers.Main){
            playlist = dataSource.getPlaylist()

        }


        val handler = Handler()
        var played: Boolean = false

        setSongDataInPlayer(currentSong)
        createPlayer(currentSong.uri)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setTxtRealTime(progress)
                if (fromUser == true) {
                    handler.removeCallbacksAndMessages(null)
                }
                if (seekBar!!.progress >= currentSong.duration && !fromUser) {
                    played = false
                    restartPlayer(handler, currentSong.uri)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacksAndMessages(null)
                mp.seekTo(seekBar!!.progress)
                if (played == true) {
                    timerStart(handler)
                }
                if (seekBar!!.progress >= currentSong.duration) {
                    played = false
                    restartPlayer(handler, currentSong.uri)
                }
            }
        })

        btPlayStop.setOnClickListener() {
            if (played == false) {
                mp.start()
                timerStart(handler)
                played = true
                btPlayStop.setBackgroundResource(R.drawable.ic_pause_black_24dp)
            } else if (played == true) {
                mp.pause()
                handler.removeCallbacksAndMessages(null)
                btPlayStop.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
                played = false
            }
        }

        btPrevious.setOnClickListener() {
            val currentSongPosition = playlist.indexOf(currentSong)
            if(currentSongPosition != 0){
                currentSong = playlist[currentSongPosition-1]
                setSongDataInPlayer(currentSong)
                played = false
                restartPlayer(handler, currentSong.uri)
            }

        }

        btNext.setOnClickListener() {

            val currentSongPosition = playlist.indexOf(currentSong)
            if(currentSongPosition!= playlist.lastIndex) {
                currentSong = playlist[currentSongPosition + 1]
                setSongDataInPlayer(currentSong)
                played = false
                restartPlayer(handler, currentSong.uri)
            }
        }

        btMusicList.setOnClickListener{
            super.onBackPressed()
        }


    }



    private fun createPlayer(songUri: Uri) {

        mp = MediaPlayer.create(this, songUri)
    }

    private fun timerStart(handler: Handler) {
        val runnable = object : Runnable {
            override fun run() {
                handler.postDelayed(this, 1000)
                seekBar.progress += 1000
            }
        }
        handler.postDelayed(runnable, 1000)
    }

    private fun setTxtRealTime(progress: Int) {
        val minutes: Int = progress / 60000 % 60000
        val seconds: Int = progress % 60000 / 1000
        val realTime: String = String.format("%02d:%02d", minutes, seconds)
        txtRealTime.text = realTime
    }

    private fun restartPlayer(handler: Handler, songUri: Uri) {
        mp.stop()
        handler.removeCallbacksAndMessages(null)
        createPlayer(songUri)
        seekBar.progress = 0
        btPlayStop.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
    }

    private fun setSongDataInPlayer(song: PlayerSongItem) {
        //setting songDuration

//        metaRetriever.setDataSource(fileUri.path)
        val minutes = (song.duration / 60000) % 60000
        val seconds = song.duration % 60000 / 1000
        val songTime = String.format("%02d:%02d", minutes, seconds)
        txtSongDuration.text = songTime
//
//        //setting seekBar
        seekBar.max = song.duration.toInt()
//
//        //setting songTitle and songArtist
//        if(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) != null
//            && metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) != null) {
//            val songTitle: String = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
//            val songArtist: String = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        txtSongName.text = "${song.artist} - ${song.title}"
//        } else {
//            val songFullName = song.substringAfterLast("/")
//            val songFileName = songFullName.substringBeforeLast(".")
//            txtSongName.text = "$songFileName"
//
//        }
//
//        //setting songImage
        if (song.coverArt != null) {
            imgSongImage.setImageBitmap(song.coverArt)
        }else imgSongImage.setImageResource(R.drawable.ic_launcher_background)

//        val artBytes = metaRetriever.getEmbeddedPicture()
//        if(artBytes != null) {
//            val bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
//            imgSongImage.setImageBitmap(bitmap)
//        } else {
//            imgSongImage.setImageResource(song_image)
//        }
    }


}
