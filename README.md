# Tracking-BLE

Ce projet a pour objectif de créer une application Androïd en Kotlin afin de pouvoir localiser des balises beacons environnantes. Cette application contient trois pages:  
+ Entrepôt
+ Camion
+ Paramètres

## Les pages de l'application

### La page "Entrepôt"

La page **Entrepôt** récupère la liste des balises beacons présentes en base de données et l'utilisateur peut sélectionner la balise qui l'intéresse dans une liste déroulante. Sinon, il peut saisir lui-même le couple major-minor. Une fois la balise sélectionnée, une distance relative s'affiche ainsi que le RSSI correspondant.  
Ceci lui permet donc de se repérer par rapport à la balise et ainsi la retrouver plus facilement.

### La page "Camion"

La page **Camion** permet d'envoyer en base de données la liste des beacons autour de l'utilisateur. 

### La page "Paramètres"

La page **Paramètres** permet à l'utilisateur de saisir la plaque d'immatriculation s'il est chauffeur du camion ainsi que de saisir l'adresse du serveur vers laquelle envoyer les données pour le camion et récupérer les données s'il est dans l'entrepôt. Ces données sont stockées localement afin de ne pas avoir à les saisir à chaque lancement de l'application.

## Faire fonctionner l'application

Pour faire fonctionner l'application, il va vous manquer les fichiers PHP présents sur le serveur et qui font le lien entre la base de données et l'application. Si malgré tout vous voulez utiliser cette application tel quel, il vous faudra les coder. Ensuite, il faudra modifier dans le fichier **connexionBDD.kt** les informations de connexion à votre base de données. Pour le reste, il ne devrait pas y avoir de problèmes étant donné que les URL sont basés sur le champs de saisie de l'adresse du serveur.
