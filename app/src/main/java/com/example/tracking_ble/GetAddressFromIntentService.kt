package com.example.tracking_ble

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import java.util.*

// Service d'intention pour obtenir une adresse à partir de coordonnées géographiques
class GetAddressIntentService : IntentService(IDENTIFIER) {
    private var addressResultReceiver: ResultReceiver? = null

    // Méthode appelée lors de la réception d'une intention
    override fun onHandleIntent(intent: Intent?) {
        val msg: String
        // Récupérer le destinataire des résultats de l'adresse depuis l'intention
        addressResultReceiver = Objects.requireNonNull(intent)!!.getParcelableExtra("add_receiver")

        // Vérifier si le destinataire des résultats est null
        if (addressResultReceiver == null) {
            Log.e("GetAddressIntentService", "Aucun destinataire, la demande ne peut pas être traitée")
            return
        }

        // Récupérer les coordonnées géographiques depuis l'intention
        val location = intent!!.getParcelableExtra<Location>("add_location")

        // Vérifier si les coordonnées géographiques sont null
        if (location == null) {
            msg = "Aucune localisation, impossible de continuer sans localisation"
            sendResultsToReceiver(0, msg)
            return
        }

        // Géocodeur pour obtenir l'adresse à partir des coordonnées
        val geoCoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioException: Exception) {
            Log.e("", "Erreur lors de l'obtention de l'adresse pour la localisation")
        }

        // Vérifier si aucune adresse n'a été trouvée
        if (addresses == null || addresses.isEmpty()) {
            msg = "Aucune adresse trouvée pour la localisation"
            sendResultsToReceiver(1, msg)
        } else {
            val address = addresses[0]
            // Détails de l'adresse formatés
            val addressDetails = """
                ${address.featureName}
                ${address.thoroughfare}
                Localité: ${address.locality}
                Comté: ${address.subAdminArea}
                État: ${address.adminArea}
                Pays: ${address.countryName}
                Code postal: ${address.postalCode}
                """.trimIndent()
            sendResultsToReceiver(2, addressDetails)
        }
    }

    // Méthode pour envoyer les résultats au destinataire
    private fun sendResultsToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle()
        bundle.putString("address_result", message)
        addressResultReceiver!!.send(resultCode, bundle)
    }

    // Objet compagnon pour l'identifiant du service
    companion object {
        private const val IDENTIFIER = "GetAddressIntentService"
    }
}
