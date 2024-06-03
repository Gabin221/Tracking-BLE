package com.example.tracking_ble

import java.sql.*
import java.util.Properties

object ExempleBaseDonneesMySQLKotlin {

    // Déclaration des variables de connexion
    internal var conn: Connection? = null
    internal var nomUtilisateur = ""
    internal var motDePasse = ""

    // Fonction principale
    @JvmStatic fun main(args: Array<String>) {
        getConnection() // Obtenir la connexion à la base de données
        executeMySQLQuery() // Exécuter la requête MySQL
    }

    // Fonction pour exécuter une requête MySQL
    fun executeMySQLQuery() {
        var stmt: Statement? = null
        var resultset: ResultSet? = null

        try {
            stmt = conn!!.createStatement()
            resultset = stmt!!.executeQuery("SHOW DATABASES;") // Exécuter la requête MySQL pour afficher les bases de données

            // Vérifier si la requête a été exécutée avec succès
            if (stmt.execute("SHOW DATABASES;")) {
                resultset = stmt.resultSet
            }

            // Parcourir les résultats et afficher le nom de chaque base de données
            while (resultset!!.next()) {
                println(resultset.getString("Database"))
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        } finally {
            // Fermeture des ressources
            if (resultset != null) {
                try {
                    resultset.close()
                } catch (sqlEx: SQLException) {
                }

                resultset = null
            }

            if (stmt != null) {
                try {
                    stmt.close()
                } catch (sqlEx: SQLException) {
                }

                stmt = null
            }

            if (conn != null) {
                try {
                    conn!!.close()
                } catch (sqlEx: SQLException) {
                }

                conn = null
            }
        }
    }

    // Fonction pour établir la connexion à la base de données MySQL
    fun getConnection() {
        val connectionProps = Properties()
        connectionProps.put("user", nomUtilisateur)
        connectionProps.put("password", motDePasse)
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance()
            conn = DriverManager.getConnection(
                "jdbc:" + "mysql" + "://" +
                        "" + // Adresse et port du serveur MySQL
                        ":" + "" + "/" + // Nom de la base de données
                        "",
                connectionProps)
        } catch (ex: SQLException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
