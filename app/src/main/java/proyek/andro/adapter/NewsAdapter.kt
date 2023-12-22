package proyek.andro.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import proyek.andro.R
import proyek.andro.model.News
import proyek.andro.userActivity.NewsDetail

class NewsAdapter (
    private val listNews: ArrayList<News>
) : RecyclerView.Adapter<NewsAdapter.ListViewHolder> () {
    private lateinit var onItemClickCallback: NewsAdapter.OnItemClickCallback
    val storageRef = FirebaseStorage.getInstance().reference
    interface OnItemClickCallback {
        fun onItemClicked(data : News)
        fun delData(pos: Int)
    }
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var _newsTitle : TextView = itemView.findViewById(R.id.News_title)
        var _newsAuthor : TextView = itemView.findViewById(R.id.News_author)
        var _newsDate : TextView = itemView.findViewById(R.id.News_date)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsAdapter.ListViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.rv_news, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsAdapter.ListViewHolder, position: Int) {
        var news = listNews[position]

        holder._newsTitle.setText(news.title)
        holder._newsAuthor.setText(news.author)
        holder._newsDate.setText(news.date)
        val context = holder.itemView.context
        holder.itemView.setOnClickListener { onItemClickCallback.onItemClicked(news) }
    }

    override fun getItemCount(): Int {
        return listNews.size
    }




}