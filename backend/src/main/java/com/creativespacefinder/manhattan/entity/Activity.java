package com.creativespacefinder.manhattan.entity;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "activities")
public class Activity {

    // Columns
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    // Constructors
    public Activity() { }                 // default constructor

    public Activity(String name) {        // convenience constructor
        this.name = name;
    }

    // Getters and Setters
    public UUID getId()                { return id; }
    public void setId(UUID id)         { this.id = id; }

    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }

    // Equals, hashing and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return Objects.equals(id, activity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Activity{id=%s, name='%s'}".formatted(id, name);
    }
}
