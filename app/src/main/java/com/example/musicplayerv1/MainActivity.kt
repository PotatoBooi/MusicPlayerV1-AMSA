package com.example.musicplayerv1


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerv1.R.id.rec_list
import com.example.musicplayerv1.data.MusicDataSource
import com.example.musicplayerv1.data.SongListItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {


    private lateinit var songList: List<SongListItem>
    private val dataSource = MusicDataSource(this)
    private val adapter = SongListAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPermissions()
        setAdapter()


    }

    override fun onStart() {
     //   setupPermissions()
        super.onStart()

    }

    override fun onCreateView(name: String?, context: Context?, attrs: AttributeSet?): View? {

        return super.onCreateView(name, context, attrs)

    }
    private fun startPlayingView(id:Long) {
        val i = Intent(this,PlayerActivity::class.java)
        i.putExtra("SONG_ID",id)
        startActivity(i)
    }

    private fun setAdapter(){
        rec_list.adapter = adapter

    }
    private fun loadList(){

        GlobalScope.launch(Dispatchers.Main) {

            songList = dataSource.getListOfSongs()
            adapter.submitList(songList)
        }

    }

    inner class SongListAdapter
    : ListAdapter<SongListItem, SongListAdapter.SongListViewHolder>(CustomDiffUtilCallback()){
        override fun onCreateViewHolder(rootView: ViewGroup, pos: Int): SongListViewHolder {
            val inflater = LayoutInflater.from(rootView.context)
            return SongListViewHolder(inflater.inflate(R.layout.listitem_layout,rootView,false))
        }

        override fun onBindViewHolder(holder: SongListViewHolder, pos: Int) {
            getItem(pos).let {
                holder.itemArtist.text = it.artist
                holder.itemTitle.text = it.title
                holder.itemDuration.text = it.duration
                if(it.coverArt != null) holder.itemCover.setImageBitmap(it.coverArt)

                holder.apply {  }
                holder.container.setOnClickListener{
                    startPlayingView(songList[pos].id)
                }
            }
        }


        inner class SongListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val container: ViewGroup = itemView.findViewById(R.id.rec_list_item)
            val itemTitle = itemView.findViewById(R.id.txt_list_title) as TextView
            val itemArtist = itemView.findViewById(R.id.txt_list_artist) as TextView
            val itemDuration = itemView.findViewById(R.id.txt_list_duration) as TextView
            val itemCover = itemView.findViewById(R.id.img_list_cover) as ImageView


        }

    }

    class CustomDiffUtilCallback: DiffUtil.ItemCallback<SongListItem>() {
        override fun areItemsTheSame(item1: SongListItem, item2: SongListItem): Boolean {
            return (item1.artist==item2.artist)&&(item1.title==item2.title)&&(item1.duration==item2.duration)
        }

        override fun areContentsTheSame(item1: SongListItem, item2: SongListItem): Boolean {
            return (item1.artist==item2.artist)&&(item1.title==item2.title)&&(item1.duration==item2.duration)
        }

    }

    private val TAG = "Permission"
    private val REQUEST_CODE = 101
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }else{

        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")
                    finish()
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                    loadList()
                }
            }
        }
    }
}
