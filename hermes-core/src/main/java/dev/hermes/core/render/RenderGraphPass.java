package dev.hermes.core.render;

import dev.hermes.api.ecs.World;

interface RenderGraphPass {

    String id();

    void resize(int width, int height);

    void render(World world);

    void dispose();
}
