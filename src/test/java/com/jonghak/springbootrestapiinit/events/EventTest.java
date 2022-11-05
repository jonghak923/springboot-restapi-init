package com.jonghak.springbootrestapiinit.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {
    public void builder() {
        Event event = Event.builder().build();
        assertThat(event).isNotNull();
    }

    public void test(){
        Event event = new Event();
        event.setName("Event");
        event.setDescription("Event is test");
        String name = "Event";
        String description = "Spring";

        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }
}