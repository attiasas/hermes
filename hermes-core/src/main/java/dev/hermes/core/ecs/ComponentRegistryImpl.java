package dev.hermes.core.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.ecs.ComponentContext;
import dev.hermes.api.ecs.ComponentDeserializer;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.ecs.ComponentData;
import dev.hermes.api.resource.ResourceService;

import java.util.HashMap;
import java.util.Map;

public final class ComponentRegistryImpl implements ComponentRegistry {

    private static final Logger log = Logs.get(ComponentRegistryImpl.class);

    private final Map<String, Registration> registrations = new HashMap<>();
    private ResourceService resources;

    public ComponentRegistryImpl() {
    }

    public void setResources(ResourceService resources) {
        this.resources = resources;
    }

    ResourceService resources() {
        return resources;
    }

    @Override
    public void register(String typeName, Class<? extends Component> type, ComponentDeserializer deserializer) {
        if (typeName == null || typeName.isBlank()) {
            throw new IllegalArgumentException("typeName is required");
        }
        log.debug("Registering component: " + typeName + " with type: " + type.getName());
        registrations.put(typeName, new Registration(type, deserializer));
    }

    @Override
    public boolean isRegistered(String typeName) {
        return registrations.containsKey(typeName);
    }

    Component deserialize(String typeName, ComponentData data, ComponentContext context) {
        return deserialize("", "", typeName, data, context);
    }

    Component deserialize(
            String scenePath, String entityName, String typeName, ComponentData data, ComponentContext context) {
        Registration registration = registrations.get(typeName);
        if (registration == null) {
            throw new SceneLoadException(
                    formatUnknownComponent(scenePath, entityName, typeName));
        }
        log.debug("Deserializing component: " + typeName + " for entity: " + entityName + " in scene: " + scenePath);
        return registration.deserializer().deserialize(data, context);
    }

    private static String formatUnknownComponent(String scenePath, String entityName, String typeName) {
        String entityLabel = entityName == null || entityName.isBlank() ? "<unnamed>" : "'" + entityName + "'";
        return "Scene '"
                + scenePath
                + "': entity "
                + entityLabel
                + ": unknown component '"
                + typeName
                + "' (register via ComponentRegistry or ServiceLoader)";
    }

    private static final class Registration {
        private final Class<? extends Component> type;
        private final ComponentDeserializer deserializer;

        private Registration(Class<? extends Component> type, ComponentDeserializer deserializer) {
            this.type = type;
            this.deserializer = deserializer;
        }

        private ComponentDeserializer deserializer() {
            return deserializer;
        }
    }
}
