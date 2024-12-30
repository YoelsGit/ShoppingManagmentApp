package com.example.shoppingmanagment;

public class DataModel {

    private String name; // The name of the item
    private int count;   // The quantity of the item
    private String id_;     // The unique identifier for the item

    public DataModel() {

    }

    public DataModel(String name, int count, String id_) {
        this.name = name;
        this.count = count;
        this.id_ = id_;
    }

    public DataModel(String name, int count, int id_) {
        this.name = name;
        this.count = count;
        this.id_ = String.valueOf(id_);
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getId_() {
        return id_;
    }

    public void setId_(String id_) {
        this.id_ = id_;
    }
}
