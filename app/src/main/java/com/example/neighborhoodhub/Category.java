package com.example.neighborhoodhub;

import java.util.List;

public class Category {
    private String name;
    private List<Provider> providers;
    public String getName() {
        return name;
    }
    public List<Provider> getProviders() {
        return providers;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }
    // getters and setters
}