package proyek.andro.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import proyek.andro.R
import proyek.andro.helper.StorageHelper

class ListAdapter (
    private var images : List<String>,
    private var titles : List<String>,
    private var descriptions : List<String>,
    private var imagePath : String,
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback
    val storageRef = FirebaseStorage.getInstance().reference

    interface OnItemClickCallback {
        fun onItemClicked(data: String)
        fun delData(pos : Int)
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.listImage)
        val title : TextView = itemView.findViewById(R.id.listHeadline)
        val desc : TextView = itemView.findViewById(R.id.listDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.rv_list, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.title.text = titles.get(position)
        holder.desc.text = descriptions.get(position)

        CoroutineScope(Dispatchers.Main).launch {
            val imageURI = StorageHelper().getImageURI(images.get(position), imagePath)

            Picasso.get()
                .load(imageURI)
                .resize(60, 60)
                .centerInside()
                .placeholder(R.drawable.bg_gradient_1)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.image, object : Callback {
                    override fun onSuccess() {
                    }

                    override fun onError(e: Exception?) {
                        Picasso.get()
                            .load(imageURI)
                            .resize(60, 60)
                            .centerInside()
                            .placeholder(R.drawable.bg_gradient_1)
                            .into(holder.image)
                    }
                })
        }

        holder.itemView.setOnClickListener { onItemClickCallback.onItemClicked(titles.get(position)) }
    }

    fun setData(images : List<String>, titles : List<String>, descriptions : List<String>) {
        this.images = images
        this.titles = titles
        this.descriptions = descriptions
        notifyDataSetChanged()
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun getItemViewType(position: Int): Int {
        return 2
    }
}