package com.example.tracking_ble.ui.camion

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.tracking_ble.R
import com.example.tracking_ble.UserManager
import com.example.tracking_ble.databinding.FragmentCamionBinding
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region

class CamionFragment : Fragment() {
    private var _binding: FragmentCamionBinding? = null
    private val binding get() = _binding!!
    lateinit var beaconCountTextView: TextView
    lateinit var beaconListView: ListView
    lateinit var tempValeur: TextView
    lateinit var buttonDepart: Button
    lateinit var buttonArrivee: Button
    lateinit var userManager: UserManager
    lateinit var enRouteTexte: TextView
    var immat = ""
    var adresse = ""
    var envoieEnCours = false
    var latitude = ""
    var longitude = ""

    var listeBeacons = ""

    private var mBeaconManager: BeaconManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private val DELAY_BETWEEN_SENDING = 5000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCamionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialisation des vues
        beaconCountTextView = root.findViewById(R.id.beaconTexte)
        beaconListView = root.findViewById(R.id.beaconList)
        tempValeur = root.findViewById(R.id.tempValeur)
        buttonDepart = root.findViewById(R.id.buttonDepart)
        buttonArrivee = root.findViewById(R.id.buttonArrivee)
        userManager = UserManager(requireContext())
        enRouteTexte = root.findViewById(R.id.enRouteTexte)

        // Création du texte "Camion en route"
        creationTexteCamionenRoute()

        // Gestion du clic sur le bouton départ
        buttonDepart.setOnClickListener {
            userManager.userImmatFlow.asLiveData().observe(viewLifecycleOwner) {
                immat = it
            }
            userManager.userAdresseFlow.asLiveData().observe(viewLifecycleOwner) {
                adresse = it
            }
            Toast.makeText(requireContext(), "Départ du camion", Toast.LENGTH_SHORT).show()
            envoieEnCours = true

            val temperature = tempValeur.text.toString()
            enRouteTexte.visibility = VISIBLE
            startSendingData(temperature)
        }

        // Gestion du clic sur le bouton arrivée
        buttonArrivee.setOnClickListener {
            userManager.userImmatFlow.asLiveData().observe(viewLifecycleOwner) {
                immat = it
                Toast.makeText(requireContext(), "Arrivée du camion", Toast.LENGTH_SHORT).show()
            }
            envoieEnCours = false
            stopSendingData()
            enRouteTexte.visibility = GONE
        }

        // Recherche des beacons
        findBeacons()

        return root
    }

    // Création du texte "Le camion est en route..." avec des images au début et à la fin
    private fun creationTexteCamionenRoute() {
        val text = " Le camion est en route... "

        val spannableString = SpannableString(" $text ")

        // Ajout de l'image au début du texte
        val drawableStart = ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu_enroute)
        drawableStart?.setBounds(0, 0, drawableStart.intrinsicWidth, drawableStart.intrinsicHeight)
        val imageSpanStart = ImageSpan(drawableStart!!, ImageSpan.ALIGN_BOTTOM)
        spannableString.setSpan(imageSpanStart, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Ajout de l'image à la fin du texte
        val drawableEnd = ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu_enroute)
        drawableEnd?.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
        val imageSpanEnd = ImageSpan(drawableEnd!!, ImageSpan.ALIGN_BOTTOM)
        spannableString.setSpan(imageSpanEnd, text.length + 1, text.length + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        enRouteTexte.text = spannableString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopSendingData()
    }

    // Démarrer l'envoi des données
    private fun startSendingData(temperature: String) {
        handler.postDelayed({
            if (envoieEnCours) {
                getLastKnownLocation(requireContext())
                sendDataToServer(temperature)
                startSendingData(temperature)
            }
        }, DELAY_BETWEEN_SENDING)
    }

    // Arrêter l'envoi des données
    private fun stopSendingData() {
        handler.removeCallbacksAndMessages(null)
    }

    // Envoyer les données au serveur
    private fun sendDataToServer(temperature: String) {
        var trueImmat = ""
        for (i in immat){
            if (i != '-'){
                trueImmat += i
            }
        }
        val url = "${adresse}/donneesApp.php?plaque=$trueImmat&longitude=$longitude&latitude=$latitude&temperature=$temperature&listeBeacons=$listeBeacons"
        val queue = Volley.newRequestQueue(requireContext())

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response -> // Réponse du serveur

            },
            {
                Toast.makeText(requireContext(), "Les données ne s'envoient pas", Toast.LENGTH_SHORT).show()
            })
        queue.add(stringRequest)
    }

    // Obtenir la dernière localisation connue
    fun getLastKnownLocation(context: Context) {
        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationManager.getProviders(true)
        var location: Location? = null
        for (i in providers.size - 1 downTo 0) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            location= locationManager.getLastKnownLocation(providers[i])
            if (location != null)
                break
        }
        val gps = DoubleArray(2)
        if (location != null) {
            gps[0] = location.getLatitude()
            gps[1] = location.getLongitude()
            latitude = gps[0].toString()
            longitude = gps[1].toString()
        }
    }

    // Observer les beacons trouvés
    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        listeBeacons = ""
        beaconCountTextView.text = "Nombre de beacons: " + beacons.size.toString()
        if (BeaconManager.getInstanceForApplication(requireActivity()).rangedRegions.size > 0) {
            beaconListView.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1,
                beacons
                    .sortedBy { it.distance }
                    .map { String.format("%04x", it.id2.toInt()).uppercase() +
                            " - " +
                            String.format("%04x", it.id3.toInt()).uppercase()
                    }.toTypedArray())
        }

        // Ajout des beacons à la liste
        beacons.forEach { beacon ->
            val beaconId = String.format("%04x", beacon.id2.toInt()).uppercase() +
                    String.format("%04x", beacon.id3.toInt()).uppercase()

            listeBeacons += beaconId
        }
    }

    // Recherche des beacons
    fun findBeacons() {
        beaconListView.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, arrayOf("--"))
        val beaconManager = BeaconManager.getInstanceForApplication(requireActivity())
        val intervalle_scan: Long = (1000 * resources.getInteger(R.integer.intervalle_scan)).toLong()
        beaconManager.setForegroundScanPeriod(intervalle_scan)
        val region = Region("all-beacons-region", null, Identifier.parse("0001"), null)
        val parser = BeaconParser()
            .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        beaconManager.getBeaconParsers().add(parser)
        beaconManager.startRangingBeacons(region)
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(requireParentFragment().viewLifecycleOwner, rangingObserver)
    }

    companion object {
        val TAG = "ui.camion.CamionFragment"
        val PERMISSION_REQUEST_BACKGROUND_LOCATION = 0
        val PERMISSION_REQUEST_BLUETOOTH_SCAN = 1
        val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 2
        val PERMISSION_REQUEST_FINE_LOCATION = 3
    }
}
