package edu.rentcar.service;

import edu.rentcar.extras.ExtraOption;
import edu.rentcar.extras.ExtrasAssembler;
import edu.rentcar.extras.ServiceComponent;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtrasAssemblerTest {

    @Test
    void noSelectionMeansNoExtraServices() {
        ServiceComponent service = ExtrasAssembler.assemble(EnumSet.noneOf(ExtraOption.class));

        assertEquals("No extra services", service.getDescription());
        assertEquals(0.0, service.getCost(3), 0.001);
    }

    @Test
    void severalDecoratorsAddCostAndDescription() {
        ServiceComponent service = ExtrasAssembler.assemble(
                EnumSet.of(ExtraOption.GPS, ExtraOption.CHILD_SEAT, ExtraOption.EXTRA_DRIVER));

        assertEquals("GPS, Child seat, Extra driver", service.getDescription());
        assertEquals(66.0, service.getCost(3), 0.001); // 5*3 + 7*3 + 30
    }

    @Test
    void nestedBundleCostIsCalculatedFromItsComponents() {
        ServiceComponent service = ExtrasAssembler.assemble(EnumSet.of(ExtraOption.ROAD_TRIP_BUNDLE));

        // (4*3 + 6*3 + (3+2)*3*0.95) * 0.90 = 39.825
        assertEquals(39.825, service.getCost(3), 0.001);
    }

    @Test
    void bundleCanBeCombinedWithDecorators() {
        ServiceComponent service = ExtrasAssembler.assemble(
                EnumSet.of(ExtraOption.ROAD_TRIP_BUNDLE, ExtraOption.GPS));

        assertEquals(39.825 + 15.0, service.getCost(3), 0.001);
        assertTrue(service.getDescription().contains("Family Pack"));
        assertTrue(service.getDescription().endsWith("GPS"));
    }
}