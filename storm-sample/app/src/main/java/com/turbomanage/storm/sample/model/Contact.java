package com.turbomanage.storm.sample.model;

import com.turbomanage.storm.api.Entity;

/**
 * Created by galex on 11/06/14.
 */

@Entity
public class Contact {

    private long id;
    private String firstName;
    private String lastName;

    public Contact() {
    }

    public Contact(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String toString(){

        return firstName +" "+ lastName;
    }
}
