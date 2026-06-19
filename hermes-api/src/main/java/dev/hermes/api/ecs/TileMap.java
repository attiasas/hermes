package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/** References a Hermes tilemap asset and layer for rendering. */
public final class TileMap implements Component {

    private String map;
    private String layer = "ground";

    public TileMap() {}

    public TileMap(String map) {
        this.map = map;
    }

    public TileMap(String map, String layer) {
        this.map = map;
        this.layer = layer;
    }

    public String map() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String layer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }
}
