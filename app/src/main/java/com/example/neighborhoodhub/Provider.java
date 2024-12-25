package com.example.neighborhoodhub;

public class Provider {
    private String name;
    private String phone;
    private String address;
    private String hours;

    public String getAddress() {
        return address;
    }

    public String getHours() {
        return hours;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public void setHours(String hours) {
        this.hours = hours;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    // getters and setters
}
