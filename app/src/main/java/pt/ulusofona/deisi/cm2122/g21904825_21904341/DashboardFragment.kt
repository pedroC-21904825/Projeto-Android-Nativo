package pt.ulusofona.deisi.cm2122.g21904825_21904341

import android.content.pm.ActivityInfo
import android.content.pm.Signature
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import pt.ulusofona.deisi.cm2122.g21904825_21904341.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding

    //Para não rodar o ecrã
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.home)
    }

    //Para não rodar o ecrã
    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.home)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        binding = FragmentDashboardBinding.bind(view)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.home)
        Singleton.getList { updateVarsAndRefresh() }

    }

    private fun updateVarsAndRefresh() {

        Singleton.activeFires{
            binding.fireActiveNumber.text = it.toString()
        }

        Singleton.getDistrict {
            binding.fireActiveDistrict.text = getString(R.string.fire_active_district, it)
        }

        Singleton.activeDistrictAndCounty("d") {
            binding.fireActiveDistrictNumber.text = it.toString()
        }

        Singleton.getCounty {
            binding.fireActiveMunicipality.text = getString(R.string.fire_active_municipality, it)
        }

        Singleton.activeDistrictAndCounty("m") {
            binding.fireActiveMunicipalityNumber.text = it.toString()
        }

        fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit();

    }


}