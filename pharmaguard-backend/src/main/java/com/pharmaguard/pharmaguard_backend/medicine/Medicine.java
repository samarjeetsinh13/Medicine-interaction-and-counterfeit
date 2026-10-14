
package com.pharmaguard.pharmaguard_backend.medicine;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String ingredient;
    private String batch;
    private String manufacturer;
    private String rxCui;

    public Medicine() {
    }

    public Medicine(String name, String ingredient,
                    String batch, String manufacturer) {
        this.name = name;
        this.ingredient = ingredient;
        this.batch = batch;
        this.manufacturer = manufacturer;
    }

    public String getRxCui() {
    return rxCui;
    }

    public void setRxCui(String rxCui) {
        this.rxCui = rxCui;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
