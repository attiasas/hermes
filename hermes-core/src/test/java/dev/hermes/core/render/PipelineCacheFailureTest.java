package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class PipelineCacheFailureTest {

    @Test
    void failedPipelineBuild_isCachedAndRethrown() {
        PipelineCache cache = new PipelineCache();
        String badPath = "render/does-not-exist.json";

        PipelineParseException first =
                assertThrows(PipelineParseException.class, () -> cache.get(badPath));
        PipelineParseException second =
                assertThrows(PipelineParseException.class, () -> cache.get(badPath));

        assertSame(first, second);
    }
}
