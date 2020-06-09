package com.example.findmedevice;

public class User {
    private long id;
    private String name;
    private String LastName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }
    @Override
     public String toString(){
        return  "pico pal que lee";
    }
}
