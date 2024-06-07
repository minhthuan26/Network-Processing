package com.ltm2022client.models;

public class Film {
    private String description;
    private String director;
    private String actor;
    private String gern;
    private String poster;
    private String imdb;
    private String trailer;
    private String year;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDirector() {
        return director;
    }

    @Override
    public String toString() {
        return "Film{" +
                " \n description='" + description + '\'' +
                ", \n director='" + director + '\'' +
                ", \n actor='" + actor + '\'' +
                ", \n gern='" + gern + '\'' +
                ", \n poster='" + poster + '\'' +
                ", \n imdb='" + imdb + '\'' +
                ", \n trailer='" + trailer + '\'' +
                ", \n year='" + year + '\'' +
                ", \n name='" + name + '\'' +
                "\n}";
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getGern() {
        return gern;
    }

    public void setGern(String gern) {
        this.gern = gern;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }
}
