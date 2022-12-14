package pt.ulusofona.deisi.cm2122.g21904825_21904341

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ulusofona.deisi.cm2122.g21904825_21904341.databinding.FragmentRegisterBinding
import java.io.ByteArrayOutputStream
import java.util.*
import org.apache.commons.codec.binary.Base64
import pt.ulusofona.deisi.cm2122.g21904825_21904341.maps.FusedLocation
import pt.ulusofona.deisi.cm2122.g21904825_21904341.maps.OnLocationChangedListener
import java.io.IOException
import java.lang.NullPointerException

class RegisterFragment : Fragment(), OnLocationChangedListener {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var geocoder: Geocoder

    private val TAG = RegisterFragment::class.java.simpleName

    private var name = ""
    private var cc = 0
    private var timestamp = 0L
    private var photo : String? = null
    private var latitude : Double = 0.0
    private var longitude : Double = 0.0
    private var district : String = "NaN"
    private var county : String = "NaN"

    //Para não rodar o ecrã
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.register)
    }

    //Para não rodar o ecrã
    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        //Força esta orientação para resolver bug da foto desaparecer
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.register)
    }

    override fun onDestroy() {
        super.onDestroy()
        FusedLocation.unregisterListener(this)
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        binding = FragmentRegisterBinding.bind(view)
        geocoder = Geocoder(context, Locale.getDefault())
        FusedLocation.registerListener(this)

        //Tem que ser aqui se não da erro
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val bitmap = result.data?.extras?.get("data") as Bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                        CoroutineScope(Dispatchers.Main).launch {
                            val photoByteArray = baos.toByteArray()
                            photo = Base64.encodeBase64(photoByteArray).toString(Charsets.UTF_8)
                            binding.photo.setImageBitmap(BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size))
                        }
                    }
        }

        binding.photo.setOnClickListener {
            takePhoto()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.register)

        //Data pré preenchida
        timestamp = Date().time
        binding.date.text = getData(timestamp)
        //Data pré preenchida

        //Submeter
        binding.buttonSubmit.setOnClickListener {
            //name
            name = binding.name.editableText.toString()
            if(name == "") {
                binding.name.error = getString(R.string.error_empty_fill)
            }

            //cc
            if(binding.cc.editableText.toString() == "" ) {
                binding.cc.error = getString(R.string.error_empty_fill)
                cc = 0
            } else if (binding.cc.editableText.toString().length < 8){
                binding.cc.error = getString(R.string.error_cc_fill)
                cc = 0
            } else if (binding.cc.editableText.toString() != "" ){
                cc = binding.cc.editableText.toString().toInt()
            }

            //localização
            if (binding.localization.text == "") {
                binding.localization.error = getString(R.string.no_data)
            }

            //Submit
            if (name != "" && cc != 0 && (district != "NaN" || latitude != 0.0)) { //(district != "NaN" || latitude != 0.0) um destes dois para o caso de não haver internet
                val fire = FireRoom(false, name, cc, district, county, "NaN", 0, 0, 0, "NaN", timestamp, "NaN", photo, latitude, longitude)

                CoroutineScope(Dispatchers.IO).launch {
                    Singleton.dao?.insert(fire)
                }

                //Mensagem de sucesso
                Toast.makeText(this.context, getString(R.string.submitted), Toast.LENGTH_SHORT).show()

                //Ir para Lista
                NavigationManager.goToList(parentFragmentManager)
            }

        }

    }

    private fun takePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        permissionsBuilder(Manifest.permission.CAMERA).build().send { result->
            if (result.allGranted()) {
                resultLauncher.launch(cameraIntent)
            } else {
                Toast.makeText(context, getString(R.string.no_camera), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onLocationChanged(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        //Se não houver ligação a internet dá erro
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 5)

            //Para não dar erro pois as vezes a api do google maps não encontra o concelho
            try {
                this.district = addresses[0].adminArea
                this.county = addresses[0].locality
            } catch (ex2: NullPointerException) {
                Log.e(TAG, ex2.toString())
                //se não encontrar o concelho, mete o concelho igual ao distrito
                this.county = addresses[0].adminArea
            }
            binding.localization.text = "${district}, ${county}"
        } catch (ex: IOException) {
            Log.e(TAG, ex.toString())
            //se não houver internet mete no ecrã a latitude e a longitude
            binding.localization.text = "Lat:${this.latitude}, Lng:${this.longitude}"
        }

    }
}