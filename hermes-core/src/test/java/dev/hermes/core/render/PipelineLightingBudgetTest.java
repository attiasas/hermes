package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.core.lighting.LightingBudgets;

import org.junit.jupiter.api.Test;

final class PipelineLightingBudgetTest {

    @Test
    void parse_passWithMaxPoint4() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    {\n"
                        + "      \"id\": \"world3d\",\n"
                        + "      \"type\": \"world3d\",\n"
                        + "      \"target\": \"screen\",\n"
                        + "      \"lighting\": { \"maxPoint\": 4 }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n";

        PipelineDocument doc = PipelineDocument.parse(json);
        PipelineDocument.PassDef.LightingBudgets passBudgets = doc.passes().get(0).lighting();

        assertEquals(1, passBudgets.maxDirectional());
        assertEquals(4, passBudgets.maxPoint());
        assertEquals(0, passBudgets.maxSpot());

        LightingBudgets max = PipelineDocument.maxWorld3dLightingBudgets(doc);
        assertEquals(1, max.maxDirectional());
        assertEquals(4, max.maxPoint());
        assertEquals(0, max.maxSpot());
    }

    @Test
    void maxWorld3dLightingBudgets_elementWiseMaxAcrossPasses() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    {\n"
                        + "      \"id\": \"world3d-a\",\n"
                        + "      \"type\": \"world3d\",\n"
                        + "      \"target\": \"screen\",\n"
                        + "      \"lighting\": { \"maxDirectional\": 2, \"maxPoint\": 4 }\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"id\": \"sprites\",\n"
                        + "      \"type\": \"sprites\",\n"
                        + "      \"target\": \"screen\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"id\": \"world3d-b\",\n"
                        + "      \"type\": \"world3d\",\n"
                        + "      \"target\": \"screen\",\n"
                        + "      \"lighting\": { \"maxPoint\": 8, \"maxSpot\": 2 }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n";

        LightingBudgets max = PipelineDocument.maxWorld3dLightingBudgets(PipelineDocument.parse(json));

        assertEquals(2, max.maxDirectional());
        assertEquals(8, max.maxPoint());
        assertEquals(2, max.maxSpot());
    }

    @Test
    void maxWorld3dLightingBudgets_noWorld3dPassesUsesDefaults() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"ui\", \"type\": \"ui\", \"target\": \"screen\" }\n"
                        + "  ]\n"
                        + "}\n";

        LightingBudgets max = PipelineDocument.maxWorld3dLightingBudgets(PipelineDocument.parse(json));

        assertEquals(1, max.maxDirectional());
        assertEquals(0, max.maxPoint());
        assertEquals(0, max.maxSpot());
    }
}
