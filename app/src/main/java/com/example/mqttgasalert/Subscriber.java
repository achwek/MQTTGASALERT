package com.example.mqttgasalert;

public class Subscriber {
    private String subscriberId;
    private String nameConnection;
    private String adresseBroker;
    private int port;
    private String topic;
    private int seuil;

    // Constructeur de la classe
    public Subscriber(String subscriberId, String nameConnection, String adresseBroker,
                      int port, String topic, int seuil) {
        this.subscriberId = subscriberId;
        this.nameConnection = nameConnection;
        this.adresseBroker = adresseBroker;
        this.port = port;
        this.topic = topic;
        this.seuil = seuil;
    }
    public Subscriber(String nameConnection, String adresseBroker,
                      int port, String topic, int seuil) {
        this.nameConnection = nameConnection;
        this.adresseBroker = adresseBroker;
        this.port = port;
        this.topic = topic;
        this.seuil = seuil;
    }

    // Méthode pour récupérer le nom de la connexion
    public String getNameConnection() {
        return nameConnection;
    }

    // Méthode pour récupérer l'adresse du broker
    public String getAdresseBroker() {
        return adresseBroker;
    }

    // Méthode pour récupérer le port
    public int getPort() {
        return port;
    }

    // Méthode pour récupérer le sujet
    public String getTopic() {
        return topic;
    }

    // Méthode pour récupérer le seuil
    public int getSeuil() {
        return seuil;
    }
    //Méthode pour récupérer id
    public String getSubscriberId() {
        return subscriberId;
    }
}
