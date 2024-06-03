package com.example.tracking_ble

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserManager(context: Context) {
    // Création d'une instance de DataStore
    private val dataStore = context.createDataStore(name = "user_prefs")

    companion object {
        // Clés pour stocker les données de l'utilisateur
        val IMMAT_KEY = preferencesKey<String>("IMMAT_KEY") // Clé pour l'immatriculation de l'utilisateur
        val ADRESSE_KEY = preferencesKey<String>("ADRESSE_KEY") // Clé pour l'adresse de l'utilisateur
    }

    // Fonction pour stocker l'immatriculation de l'utilisateur
    suspend fun storeUserImmat(immat: String) {
        dataStore.edit {
            it[IMMAT_KEY] = immat
        }
    }

    // Fonction pour stocker l'adresse de l'utilisateur
    suspend fun storeUserAdresse(adresse: String) {
        dataStore.edit {
            it[ADRESSE_KEY] = adresse
        }
    }

    // Flux de données pour récupérer l'immatriculation de l'utilisateur
    val userImmatFlow: Flow<String> = dataStore.data.map {
        it[IMMAT_KEY] ?: "" // Si la clé n'existe pas, retourne une chaîne vide
    }

    // Flux de données pour récupérer l'adresse de l'utilisateur
    val userAdresseFlow: Flow<String> = dataStore.data.map {
        it[ADRESSE_KEY] ?: "" // Si la clé n'existe pas, retourne une chaîne vide
    }
}
