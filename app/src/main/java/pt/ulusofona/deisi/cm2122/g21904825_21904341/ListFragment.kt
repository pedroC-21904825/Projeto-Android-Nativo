package pt.ulusofona.deisi.cm2122.g21904825_21904341

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ulusofona.deisi.cm2122.g21904825_21904341.databinding.FragmentListBinding

class ListFragment : Fragment() {
    private lateinit var binding: FragmentListBinding
    private  var fires = arrayListOf<Fire>()
    private val adapter = ListAdapter(::onFireClick)

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.list)
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.list)
    }

    override fun onStart() {
        super.onStart()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //Para poder rodar o ecrã depois de vir do Register
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.list)
        Singleton.getList{updateAdapter()}
        binding.rvHistoricFragment.layoutManager = LinearLayoutManager(activity as Context)
        binding.rvHistoricFragment.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        binding = FragmentListBinding.bind(view)
        return binding.root
    }


    private fun onFireClick(fire: Fire) {
        NavigationManager.goToDetails(
            parentFragmentManager, fire
        )
    }

    private fun updateAdapter() {
        fires = Singleton.getList {}
        CoroutineScope(Dispatchers.Main).launch {
            adapter.updateItems(fires)
        }

    }
}