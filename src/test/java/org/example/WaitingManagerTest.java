package org.example;

import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.EnableEvent;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;

import static org.assertj.core.api.Assertions.assertThat;


@JfrEventTest
public class WaitingManagerTest {
    private final WaitingManager waitingManager = new WaitingManager();
    public JfrEvents jfrEvents = new JfrEvents();

    @Test
    @EnableEvent("jdk.VirtualThreadPinned")
    public void shouldProducePinnedEvent() {
        waitingManager.startWaiting(true);
        jfrEvents.awaitEvents();
        assertThat(jfrEvents.events())
                .extracting("eventType")
                .extracting("name")
                .containsAnyOf("jdk.VirtualThreadPinned");
    }

    @Test
    @EnableEvent("jdk.VirtualThreadPinned")
    public void shouldNotProducePinnedEvent() {
        waitingManager.startWaiting(false);
        jfrEvents.awaitEvents();
        assertThat(jfrEvents.events())
                .extracting("eventType")
                .extracting("name")
                .doesNotContain("jdk.VirtualThreadPinned");
    }
}