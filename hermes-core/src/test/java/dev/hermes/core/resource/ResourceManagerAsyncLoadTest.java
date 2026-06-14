package dev.hermes.core.resource;

import dev.hermes.api.resource.LoadTicket;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceManagerAsyncLoadTest {

    @Test
    void cooperativeLoadCompletesViaFramePump() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        ResourceManagerImpl mgr = ResourceManagerImpl.forTest("assets/resources/");
        mgr.useCooperativeStrategyForTests(true);
        LoadTicket ticket = mgr.loadBundleAsync("test-bundle");
        ResourceTestFrames.pumpUntilDone(mgr, ticket, 120);
        assertTrue(ticket.done());
        assertEquals(1.0f, ticket.progress(), 0.01f);
    }

    @Test
    void threadPoolLoadCompletes() throws Exception {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        ResourceManagerImpl mgr = ResourceManagerImpl.forTest("assets/resources/");
        mgr.useCooperativeStrategyForTests(false);
        LoadTicket ticket = mgr.loadBundleAsync("test-bundle");
        ResourceTestFrames.awaitWithFlush(ticket);
        assertTrue(ticket.done());
    }
}
