package com.creativespacefinder.manhattan.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ActivityTest {

    @Test
    void gettersAndSettersWork() {
        Activity a = new Activity();
        UUID id = UUID.randomUUID();
        a.setId(id);
        a.setName("Hiking");

        assertThat(a.getId()).isEqualTo(id);
        assertThat(a.getName()).isEqualTo("Hiking");
    }

    @Test
    void convenienceConstructorSetsName() {
        Activity a = new Activity("Cycling");
        assertThat(a.getName()).isEqualTo("Cycling");
        assertThat(a.getId()).isNull();
    }

    @Test
    void equalsReflexive() {
        Activity a = new Activity();
        assertThat(a).isEqualTo(a);
    }

    @Test
    void equalsAgainstNullAndOtherClass() {
        Activity a = new Activity();
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("foo");
    }

    @Test
    void equalsAndHashCodeBasedOnId() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Activity a1 = new Activity();
        a1.setId(id1);

        Activity a2 = new Activity();
        a2.setId(id1);

        Activity a3 = new Activity();
        a3.setId(id2);

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());

        assertThat(a1).isNotEqualTo(a3);
        assertThat(a1.hashCode()).isNotEqualTo(a3.hashCode());
    }

    @Test
    void toStringContainsIdAndName() {
        UUID id = UUID.randomUUID();
        Activity a = new Activity("Painting");
        a.setId(id);

        String s = a.toString();
        assertThat(s).contains("Activity{id=" + id);
        assertThat(s).contains("name='Painting'");
    }
}
