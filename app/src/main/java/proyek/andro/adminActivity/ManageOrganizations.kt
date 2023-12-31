package proyek.andro.adminActivity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
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
import proyek.andro.model.Organization

class ManageOrganizations : AppCompatActivity() {
    private var organizations: ArrayList<Organization> = ArrayList()
    private var filteredOrganization: ArrayList<Organization> = ArrayList()
    private var filteredImages: ArrayList<String> = ArrayList()
    private var filteredNames: ArrayList<String> = ArrayList()

    private lateinit var organizationRV: RecyclerView

    private lateinit var search_view: SearchView
    private lateinit var etSearch: EditText
    private var searchText: String = ""

    private lateinit var adapterP: SimpleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_organizations)

        val backBtn: ImageView = findViewById(R.id.backBtn)
        val addOrgButton: FloatingActionButton = findViewById(R.id.addOrgButton)

        backBtn.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }

        addOrgButton.setOnClickListener {
            val intent = Intent(this, AddOrganizations::class.java)
            intent.putExtra("mode", "add")
            startActivity(intent)
        }

        organizationRV = findViewById(R.id.viewOrgs)
        CoroutineScope(Dispatchers.Main).launch {
            organizations = Organization().get(limit = 50)
            filteredImages = organizations.map { it.logo } as ArrayList<String>
            filteredNames = organizations.map { it.name } as ArrayList<String>

            Log.d("filterOrgs", "Orgs: " + organizations.map { it.name }.toString())
            Log.d("filterOrgs", "Name: " + filteredNames.toString())
            Log.d("filterOrgs", "Images: " + filteredImages.toString())

            filterOrganizations()

            adapterP = SimpleListAdapter(filteredImages, filteredNames, "logo/orgs/")

            adapterP.setOnItemClickCallback(object : SimpleListAdapter.OnItemClickCallback {
                override fun onItemClicked(data: String) {
                    val intent = Intent(this@ManageOrganizations, AddOrganizations::class.java)
                    intent.putExtra("mode", "edit")
                    intent.putExtra("name", data)
                    startActivity(intent)
                }

                override fun delData(pos: Int) {
                    val organization = filteredOrganization.get(pos)

                    val alert = MaterialAlertDialogBuilder(this@ManageOrganizations)
                        .setTitle("Delete Organization")
                        .setMessage("Are you sure you want to delete ${organization.name}?")
                        .setNegativeButton("Cancel") { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Delete") { dialog, which ->
                            CoroutineScope(Dispatchers.Main).launch {
                                StorageHelper().deleteFile(organization.logo)
                                organization.delete(organization.id)

                                organizations.removeAt(pos)
                                filteredImages.removeAt(pos)
                                filteredNames.removeAt(pos)
                                filteredOrganization.removeAt(pos)

                                adapterP.setData(filteredImages, filteredNames)

                                Snackbar.make(
                                    addOrgButton,
                                    R.string.organizationdeleted,
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


            organizationRV.adapter = adapterP
            organizationRV.layoutManager = LinearLayoutManager(
                this@ManageOrganizations,
                LinearLayoutManager.VERTICAL,
                false
            )
        }

        search_view = findViewById(R.id.search_view)
        etSearch = search_view.findViewById(androidx.appcompat.R.id.search_src_text)
        etSearch.setHintTextColor(resources.getColor(R.color.disabled, null))
        etSearch.setTextColor(resources.getColor(R.color.white, null))

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search_view.clearFocus()

                if (query.toString() != searchText) {
                    searchText = query.toString()
                    filterOrganizations()
                    CoroutineScope(Dispatchers.Main).launch {
                        adapterP.setData(filteredImages, filteredNames)
                    }
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.toString() == "" && searchText != "") {
                    searchText = newText.toString()
                    filterOrganizations()
                    CoroutineScope(Dispatchers.Main).launch {
                        adapterP.setData(filteredImages, filteredNames)
                    }
                }
                return true
            }
        })
    }

    fun filterOrganizations() {
        filteredOrganization.clear()
        filteredImages.clear()
        filteredNames.clear()

        if (searchText == "") {
            filteredOrganization.addAll(
                organizations.filter {
                    it.name != ""
                } as ArrayList<Organization>
            )
        }
        else {
            Log.d("filterOrgs", "Searching for $searchText")
            Log.d("filterOrgs", "Orgs: " + organizations.map { it.name }.toString())
            filteredOrganization = organizations.filter {
                it.name.lowercase().contains(searchText, ignoreCase = true)
            } as ArrayList<Organization>
        }

        filteredImages = filteredOrganization.map { it.logo } as ArrayList<String>
        filteredNames = filteredOrganization.map { it.name } as ArrayList<String>

        if (filteredOrganization.isEmpty()) {
            organizationRV.visibility = View.GONE
        } else {
            organizationRV.visibility = View.VISIBLE
        }

        Log.d("filterOrgs", "Orgs: " + filteredOrganization.map { it.name }.toString())
        Log.d("filterOrgs", "Name: " + filteredNames.toString())
        Log.d("filterOrgs", "Images: " + filteredImages.toString())

        // update adapter data
        CoroutineScope(Dispatchers.Main).launch {
            adapterP.setData(filteredImages, filteredNames)
        }
    }
}
