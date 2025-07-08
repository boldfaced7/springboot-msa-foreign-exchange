package com.boldfaced7.fxexchange.exchange.domain.model;

import com.boldfaced7.fxexchange.exchange.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventDomain {
    private final List<DomainEvent> events = new ArrayList<>();

    public void addEvent(DomainEvent event) {
        events.add(event);
    }

    public void addEvents(Collection<DomainEvent> events) {
        this.events.addAll(events);
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

}
