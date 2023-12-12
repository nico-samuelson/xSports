package proyek.andro.adminActivity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import proyek.andro.R
import proyek.andro.adapter.SimpleListAdapter
import proyek.andro.helper.StorageHelper
import proyek.andro.model.Player
import proyek.andro.model.Team

class ManagePlayers : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_players)

        val backBtn : ImageView = findViewById(R.id.backBtn)
        val addPlayerBtn : FloatingActionButton = findViewById(R.id.addPlayerBtn)
        val loadingView : LinearLayout = findViewById(R.id.loading_view)
        val emptyView : LinearLayout = findViewById(R.id.empty_view)
        val rvPlayer : RecyclerView = findViewById(R.id.viewPlayers)

        var players : ArrayList<Player> = ArrayList()
        var images : ArrayList<String> = ArrayList()
        var names : ArrayList<String> = ArrayList()

        backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // fab click listener
        addPlayerBtn.setOnClickListener {
            val intent = Intent(this, AddPlayer::class.java)
            intent.putExtra("mode", "add")
            startActivity(intent)
        }

        // fetch and show data
        CoroutineScope(Dispatchers.Main).launch {
            players = Player().get(limit=1000, order = arrayOf(arrayOf("team", "ASC"), arrayOf("name", "ASC")))

            if (players.size == 0) {
                loadingView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
            else {
                players.forEach {
                    images.add(Team().find<Team>(it.team).logo)
                    names.add(it.nickname)
                }

                rvPlayer.layoutManager = LinearLayoutManager(
                    this@ManagePlayers,
                    LinearLayoutManager.VERTICAL,
                    false
                )

                val adapterP = SimpleListAdapter(images, names, "logo/orgs/")

                adapterP.setOnItemClickCallback(object : SimpleListAdapter.OnItemClickCallback {
                    override fun onItemClicked(data: String) {
                        val intent = Intent(this@ManagePlayers, AddPlayer::class.java)
                        intent.putExtra("mode", "edit")
                        intent.putExtra("name", data)
                        startActivity(intent)
                    }

                    override fun delData(pos: Int) {
                        val player = players.get(pos)

                        val alert = MaterialAlertDialogBuilder(this@ManagePlayers)
                            .setTitle("Delete Player")
                            .setMessage("Are you sure you want to delete ${player.name}?")
                            .setNegativeButton("Cancel") { dialog, which ->
                                dialog.dismiss()
                            }
                            .setPositiveButton("Delete") { dialog, which ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    StorageHelper().deleteFile(player.photo)
                                    player.delete(player.id)

                                    players.removeAt(pos)
                                    images.removeAt(pos)
                                    names.removeAt(pos)
                                    adapterP.setData(images, names)

                                    Snackbar.make(
                                        addPlayerBtn,
                                        R.string.playerdeleted,
                                        Snackbar.LENGTH_SHORT
                                    ).apply {
                                        setBackgroundTint(resources.getColor(R.color.light, null))
                                        setTextColor(resources.getColor(R.color.black, null))
                                    }.show()
                                }
                            }

                        val dialog = alert.create()
                        dialog.show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                    }
                })

                loadingView.visibility = View.GONE
                rvPlayer.visibility = View.VISIBLE
                rvPlayer.adapter = adapterP
            }
        }
    }
}