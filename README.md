# Tracking-BLE

Ce projet a pour objectif de créer une application Androïd en Kotlin afin de pouvoir localiser des balises beacons environnantes. Cette application contient trois pages:  
+ Entrepôt
+ Camion
+ Paramètres

## La page "Entrepôt"

Le page Entrepôt récupère la liste des balises beacons présentes en base de données et l'utilisateur peut sélectionner la balise qui l'intéresse dans une liste déroulante. Sinon, il peut saisir lui-même le couple major-minor. Une fois la balise sélectionnée, une distance relative s'affiche ainsi que le RSSI correspondant.  
Ceci lui permet donc de se repérer par rapport à la balise et ainsi la retrouver plus facilement.
