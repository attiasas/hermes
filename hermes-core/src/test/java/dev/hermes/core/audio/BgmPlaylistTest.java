package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class BgmPlaylistTest {

    @Test
    void randomModeParsed() {
        BgmPlaylist playlist =
                BgmPlaylist.parse("{\"version\":1,\"mode\":\"random\",\"tracks\":[\"a.ogg\"]}");
        assertEquals(BgmPlaylist.Mode.RANDOM, playlist.mode());
    }

    @Test
    void sequentialModeParsed() {
        BgmPlaylist playlist =
                BgmPlaylist.parse(
                        "{\"version\":1,\"mode\":\"sequential\",\"tracks\":[\"a.ogg\",\"b.ogg\"]}");
        assertEquals(BgmPlaylist.Mode.SEQUENTIAL, playlist.mode());
        assertEquals(2, playlist.tracks().size());
    }

    @Test
    void singleModeParsed() {
        BgmPlaylist playlist =
                BgmPlaylist.parse("{\"version\":1,\"mode\":\"single\",\"tracks\":[\"a.ogg\"]}");
        assertEquals(BgmPlaylist.Mode.SINGLE, playlist.mode());
    }
}
