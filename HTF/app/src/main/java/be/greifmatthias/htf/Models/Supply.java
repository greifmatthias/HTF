package be.greifmatthias.htf.Models;

public class Supply {
    private int _id;
    private String name;
    private double lat;
    private double lon;
    private String image;
    private String author;
    private String destined_user;

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDestined_user() {
        return destined_user;
    }

    public void setDestined_user(String destined_user) {
        this.destined_user = destined_user;
    }
}
