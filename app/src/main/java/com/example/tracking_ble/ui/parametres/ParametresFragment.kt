package com.example.tracking_ble.ui.parametres

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tracking_ble.R
import com.example.tracking_ble.databinding.FragmentParametresBinding
import java.util.regex.Matcher
import java.util.regex.Pattern
import androidx.lifecycle.asLiveData
import com.example.tracking_ble.UserManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ParametresFragment : Fragment() {

    // Variable pour le binding de la vue
    private var _binding: FragmentParametresBinding? = null

    // Accesseur pour le binding non nul
    private val binding get() = _binding!!

    // Déclaration des vues
    lateinit var immatriculationTexte: TextView
    lateinit var immatriculationValeur: EditText
    lateinit var validationImmat: Button
    lateinit var adresseServeurTexte: TextView
    lateinit var adresseServeurValeur: EditText
    lateinit var validationAdresseServeur: Button
    lateinit var immatValue: TextView

    // Déclaration du gestionnaire d'utilisateur
    lateinit var userManager: UserManager

    // Variables pour stocker les valeurs
    var immat = ""
    var adresse = ""

    // Méthode pour créer la vue du fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Initialiser le binding
        _binding = FragmentParametresBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialiser les vues
        immatriculationTexte = root.findViewById(R.id.immatriculationTexte)
        immatriculationValeur = root.findViewById(R.id.immatriculationValeur)
        validationImmat = root.findViewById(R.id.validationImmat)
        adresseServeurTexte = root.findViewById(R.id.adresseServeurTexte)
        adresseServeurValeur = root.findViewById(R.id.adresseServeurValeur)
        validationAdresseServeur = root.findViewById(R.id.validationAdresseServeur)
        immatValue = root.findViewById<Button>(R.id.immatValue)

        // Initialiser le gestionnaire d'utilisateur
        userManager = UserManager(requireContext())

        // Configurer les boutons de sauvegarde
        buttonSave()
        buttonSaveAdresse()

        // Observer les données
        observeData()
        observeDataAdresse()

        return root
    }

    // Méthode pour configurer le bouton de sauvegarde de l'immatriculation
    private fun buttonSave() {
        val saisieCorrect = ContextCompat.getColor(requireContext(), R.color.saisieCorrect)
        val saisieIncorrect = ContextCompat.getColor(requireContext(), R.color.saisieIncorrect)

        validationImmat.setOnClickListener {
            immat = immatriculationValeur.text.toString()

            // Vérifier si l'immatriculation est valide
            if (isValidEdit(immat)) {
                // Afficher un message de saisie correcte
                immatValue.text = getString(R.string.saisie_correcte)
                immatValue.setTextColor(saisieCorrect)
                immatValue.visibility = VISIBLE

                // Enregistrer l'immatriculation de l'utilisateur
                GlobalScope.launch {
                    userManager.storeUserImmat(immat)
                }
                Toast.makeText(requireContext(), "La plaque a été enregistrée.", Toast.LENGTH_SHORT).show()
            } else {
                // Afficher un message de saisie incorrecte
                immatValue.text = getString(R.string.saisie_incorrecte)
                immatValue.setTextColor(saisieIncorrect)
                immatValue.visibility = VISIBLE
            }
        }
    }

    // Méthode pour configurer le bouton de sauvegarde de l'adresse du serveur
    private fun buttonSaveAdresse() {
        validationAdresseServeur.setOnClickListener {
            adresse = adresseServeurValeur.text.toString()

            // Enregistrer l'adresse du serveur
            GlobalScope.launch {
                userManager.storeUserAdresse(adresse)
            }
            Toast.makeText(requireContext(), "L'adresse du serveur a été enregistrée.", Toast.LENGTH_SHORT).show()
        }
    }

    // Observer les données de l'immatriculation
    private fun observeData() {
        userManager.userImmatFlow.asLiveData().observe(viewLifecycleOwner) {
            immat = it
            immatriculationValeur.setText(it.toString())
        }
    }

    // Observer les données de l'adresse du serveur
    private fun observeDataAdresse() {
        userManager.userAdresseFlow.asLiveData().observe(viewLifecycleOwner) {
            adresse = it
            adresseServeurValeur.setText(it.toString())
        }
    }

    // Méthode pour vérifier si l'immatriculation est valide
    fun isValidEdit(immatriculation: String): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val IMMATRICULATION_PATTERN = "^[A-Z]{2}[-][0-9]{3}[-][A-Z]{2}$"
        pattern = Pattern.compile(IMMATRICULATION_PATTERN)
        matcher = pattern.matcher(immatriculation)
        return matcher.matches()
    }

    // Méthode appelée lors de la destruction de la vue
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
