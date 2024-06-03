package com.example.tracking_ble.ui.entrepot

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.tracking_ble.R
import com.example.tracking_ble.UserManager
import com.example.tracking_ble.databinding.FragmentEntrepotBinding
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region
import java.text.DecimalFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

class EntrepotFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentEntrepotBinding? = null

    // Liaison au layout
    private val binding get() = _binding!!
    lateinit var beaconCountTextView: TextView
    lateinit var beaconRechercheAvecEstimation: TextView
    lateinit var boutonValiderEntrepot: Button
    lateinit var beaconListView: ListView
    lateinit var valeurBeaconChampSaisie: EditText
    lateinit var rechercheValue: TextView
    val listeBeacons = arrayListOf<String>()
    val NEW_SPINNER_ID = 1
    lateinit var listeBeaconsBDD: Spinner
    var listeBeaconsPosition = ""
    var valeurChampRecherche = ""

    lateinit var userManager: UserManager
    var adresse = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEntrepotBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialisation des vues
        beaconCountTextView = root.findViewById(R.id.beaconTexte)
        beaconRechercheAvecEstimation = root.findViewById(R.id.beaconRechercheAvecEstimation)
        boutonValiderEntrepot = root.findViewById(R.id.boutonValiderEntrepot)
        beaconListView = root.findViewById(R.id.beaconList)
        valeurBeaconChampSaisie = root.findViewById(R.id.valeurBeaconChampSaisie)
        rechercheValue = root.findViewById(R.id.rechercheValue)
        listeBeaconsBDD = root.findViewById(R.id.listeBeaconsBDD)

        userManager = UserManager(requireContext())

        // Configuration de l'adaptateur pour le spinner
        val aa = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listeBeacons)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner = Spinner(requireContext())
        spinner.id = NEW_SPINNER_ID
        val ll = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        ll.setMargins(10, 40, 10, 10)

        listeBeacons += listOf("Choisissez votre beacon")

        // Configuration du spinner
        with(listeBeaconsBDD) {
            adapter = aa
            setSelection(0, false)
            onItemSelectedListener = this@EntrepotFragment
            prompt = "Choisissez votre beacon"
            gravity = Gravity.CENTER
        }

        // Configuration du bouton de validation
        boutonValiderEntrepot.setOnClickListener {
            valeurChampRecherche = valeurBeaconChampSaisie.text.toString()
            val saisieCorrect = ContextCompat.getColor(requireContext(), R.color.saisieCorrect)
            val saisieIncorrect = ContextCompat.getColor(requireContext(), R.color.saisieIncorrect)

            if (isValidEdit(valeurChampRecherche)) {
                rechercheValue.text = getString(R.string.saisie_correcte)
                rechercheValue.setTextColor(saisieCorrect)
                rechercheValue.visibility = View.VISIBLE

                showToast(message="Beacon: ${valeurBeaconChampSaisie.text}")
                listeBeaconsPosition = valeurBeaconChampSaisie.text.toString()
                beaconRechercheAvecEstimation.text = "Chargement..."
                findBeacons()
            } else {
                rechercheValue.text = getString(R.string.saisie_incorrecte)
                rechercheValue.setTextColor(saisieIncorrect)
                rechercheValue.visibility = View.VISIBLE
            }
        }

        requete()

        return root
    }

    // Validation du champ de texte
    fun isValidEdit(majorMinor: String): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val MAJORMINOR_PATTERN = "^[0-9A-F]{4}[-][0-9A-F]{4}$"
        pattern = Pattern.compile(MAJORMINOR_PATTERN)
        matcher = pattern.matcher(majorMinor)
        return matcher.matches()
    }

    // Méthode appelée lorsque rien n'est sélectionné dans le spinner
    override fun onNothingSelected(parent: AdapterView<*>?) {
        showToast(message = "Rien n'a été sélectionné")
    }

    // Méthode appelée lorsqu'un item est sélectionné dans le spinner
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        showToast(message="Beacon: ${listeBeacons[position]}")
        listeBeaconsPosition = listeBeacons[position]
        beaconRechercheAvecEstimation.text = "Chargement..."
            findBeacons()
    }

    // Observateur pour le suivi des beacons
    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        if (BeaconManager.getInstanceForApplication(requireActivity()).rangedRegions.size > 0) {
            beaconListView.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1,
                beacons
                    .sortedBy { it.distance }
                    .map { String.format("%04x", it.id2.toInt()).uppercase() +
                            " - " +
                            String.format("%04x", it.id3.toInt()).uppercase()
                    }.toTypedArray())
        }

        val values = listeBeaconsPosition.split("-")
        val major = "0x${values[0]}"
        val minor = "0x${values[1]}"
        val df = DecimalFormat("##.###")
        val it = beacons.iterator()
        it.forEach {
            val id2 = String.format("0x%04x", it.id2.toInt())
            val id3 = String.format("0x%04x", it.id3.toInt())
            if (id2.equals(major) and id3.equals(minor)) {
                val dist = df.format(it.distance.toFloat())
                beaconRechercheAvecEstimation.text = "Beacon ${values[0]} - ${values[1]}:\n\ndistance = ${dist} mètre(s) \nRSSI = ${it.rssi}"
            }
        }
    }

    // Méthode pour trouver les beacons
    fun findBeacons() {
        beaconListView.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, arrayOf("--"))
        val beaconManager = BeaconManager.getInstanceForApplication(requireActivity())
        val intervalle_scan: Long = (1000 * resources.getInteger(R.integer.intervalle_scan)).toLong()
        beaconManager.setForegroundScanPeriod(intervalle_scan)
        val region = Region("all-beacons-region", null, Identifier.parse("0001"), null)
        val parser = BeaconParser().
        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        parser.setHardwareAssistManufacturerCodes(arrayOf(0x004c).toIntArray())
        beaconManager.getBeaconParsers().add(parser)
        beaconManager.startRangingBeacons(region)
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(requireParentFragment().viewLifecycleOwner, rangingObserver)
    }

    // Affichage d'un Toast
    private fun showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    // Requête pour récupérer la liste des beacons
    private fun requete() {
        userManager.userAdresseFlow.asLiveData().observe(viewLifecycleOwner) {
            adresse = it

            val url = "${adresse}/maliste_beacons.php"
            val queue = Volley.newRequestQueue(requireContext())

            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    var reponse = response.toString().split(";")
                    reponse = reponse.toList()

                    for (i in reponse) {
                        listeBeacons += listOf(i)
                    }

                },
                {
                    beaconCountTextView.text = "Erreur de récupération des données"
                })
            queue.add(stringRequest)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = "ui.entrepot.EntrepotFragment"
        val PERMISSION_REQUEST_BACKGROUND_LOCATION = 0
        val PERMISSION_REQUEST_BLUETOOTH_SCAN = 1
        val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 2
        val PERMISSION_REQUEST_FINE_LOCATION = 3
    }
}
