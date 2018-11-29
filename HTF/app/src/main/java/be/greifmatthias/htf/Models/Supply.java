package be.greifmatthias.htf.Models;

public class Supply {
    public String _id;
    public String name;
    public double lat;
    public double lng;
    public String image;
    public String author;
    public String destined_user;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
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
        return lng;
    }

    public void setLon(double lon) {
        this.lng = lon;
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
