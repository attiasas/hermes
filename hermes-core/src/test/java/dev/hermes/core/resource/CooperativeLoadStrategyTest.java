package dev.hermes.core.resource;

import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooperativeLoadStrategyTest {

    @Test
    void tickProcessesAtMostAssetsPerFrame() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        CooperativeLoadStrategy strategy = new CooperativeLoadStrategy(1);
        List<Integer> phaseAOrder = new ArrayList<>();
        LoadTicketImpl ticket = new LoadTicketImpl("batch", 3);

        for (int index = 0; index < 3; index++) {
            int jobIndex = index;
            strategy.enqueue(
                    new AsyncJob(
                            new ResourceKey(
                                    dev.hermes.api.resource.ResourceRef.of("textures/hermes-logo.png"),
                                    dev.hermes.api.resource.ResourceKind.TEXTURE),
                            "textures/hermes-logo.png",
                            new CountingLoader(phaseAOrder, jobIndex),
                            new ResourceCache(),
                            ticket));
        }

        strategy.tick();
        assertEquals(1, phaseAOrder.size());
        ResourceTestFrames.flushPostedRunnables();

        strategy.tick();
        assertEquals(2, phaseAOrder.size());
        ResourceTestFrames.flushPostedRunnables();

        strategy.tick();
        assertEquals(3, phaseAOrder.size());
        ResourceTestFrames.flushPostedRunnables();

        assertTrue(ticket.done());
    }

    private static final class CountingLoader implements ResourceLoader {
        private final List<Integer> phaseAOrder;
        private final int jobIndex;

        private CountingLoader(List<Integer> phaseAOrder, int jobIndex) {
            this.phaseAOrder = phaseAOrder;
            this.jobIndex = jobIndex;
        }

        @Override
        public dev.hermes.api.resource.ResourceKind kind() {
            return dev.hermes.api.resource.ResourceKind.TEXTURE;
        }

        @Override
        public DecodedPayload decode(String path) {
            phaseAOrder.add(jobIndex);
            return DecodedPayload.fromBytes(new byte[] {1});
        }

        @Override
        public Object upload(DecodedPayload decoded) {
            return decoded.bytes();
        }

        @Override
        public void dispose(Object resource) {}
    }
}
