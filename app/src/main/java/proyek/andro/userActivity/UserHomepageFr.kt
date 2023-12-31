package proyek.andro.userActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.HeroCarouselStrategy
import com.google.android.material.carousel.UncontainedCarouselStrategy
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.search.SearchBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import proyek.andro.R
import proyek.andro.adapter.GameCarouselAdapter
import proyek.andro.adapter.MatchCarouselAdapter
import proyek.andro.adapter.OrganizationsListAdapter
import proyek.andro.helper.StorageHelper
import proyek.andro.model.Game
import proyek.andro.model.Organization

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserHomepageFr.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserHomepageFr : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var rvGameCarousel: RecyclerView
    lateinit var rvMatchCarousel: RecyclerView
    lateinit var parent: UserActivity
    var job: Job? = null

//    private val organizations : ArrayList<Organization> = ArrayList()
    lateinit var organizationsRV : RecyclerView
//    private val players : ArrayList<Player> = ArrayList()
//    lateinit var playersRV : RecyclerView
//
//    private val organizations : ArrayList<Organization> = ArrayList()
//    lateinit var organizationsRV : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        parent = super.requireActivity() as UserActivity

        if (parent.getMatches().size == 0 || arguments?.getString("refresh") != null)
            job = parent.getData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_homepage, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parent.getNavbar().menu.getItem(0).isChecked = true

        view.findViewById<TextView>(R.id.gamesText).visibility = View.GONE
        view.findViewById<TextView>(R.id.orgsText).visibility = View.GONE

        rvGameCarousel = view.findViewById(R.id.games_recycler_view)
        rvMatchCarousel = view.findViewById(R.id.matches_recycler_view)
        organizationsRV = view.findViewById(R.id.orgs_recycler_view)

        rvGameCarousel.layoutManager = CarouselLayoutManager(UncontainedCarouselStrategy())
        rvMatchCarousel.layoutManager = CarouselLayoutManager(HeroCarouselStrategy())
        organizationsRV.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        CarouselSnapHelper().attachToRecyclerView(rvMatchCarousel)

        val search_bar = view.findViewById<SearchBar>(R.id.search_bar)
        search_bar.setOnClickListener {
            val intent = Intent(parent, Search::class.java)
            intent.putExtra("search_type", 0)
            startActivity(intent)
        }

        if (job == null && parent.getMatches().size > 0) {
            Log.d("fetch data", "data already fetched")
            CoroutineScope(Dispatchers.Main).launch {
                showData(view)
            }
        } else {
            Log.d("fetch data", "data not yet fetched")
            job?.invokeOnCompletion {
                CoroutineScope(Dispatchers.Main).launch {
                    showData(view)
                    job = null
                }
            }
        }
    }

    suspend fun showData(view : View) {
        if (parent.getGameBanners().size == 0 || arguments?.getString("refresh") != null) {
            parent.setGameBanners(
                StorageHelper().preloadImages(
                    parent.getGames().map { it.banner },
                    "banner/games"
                )
            )
        }

        val gameCarouselAdapter = GameCarouselAdapter(parent.getGames(), parent.getGameBanners())
        val mFragmentManager = parentFragmentManager
        val explore = ExploreFr()

        gameCarouselAdapter.setOnItemClickCallback(object :
            GameCarouselAdapter.OnItemClickCallback {
            override fun onItemClicked(data: Game) {
                Log.d("game clicked", data.toString())
                parent.setSelectedGame(parent.getGames().indexOfFirst { it.id == data.id })
                mFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, explore, explore::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        })

        view.findViewById<TextView>(R.id.orgsText).visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.gamesText).visibility = View.VISIBLE
        view.findViewById<CircularProgressIndicator>(R.id.loading_indicator).visibility = View.GONE

        rvGameCarousel.adapter = gameCarouselAdapter
        rvMatchCarousel.adapter = MatchCarouselAdapter(parent.getMatches(), parent.getTeams())
        organizationsRV.adapter = OrganizationsListAdapter(parent.getOrgs())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserHomepageFr.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserHomepageFr().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}