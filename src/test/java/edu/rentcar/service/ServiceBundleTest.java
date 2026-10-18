package edu.rentcar.service;

import edu.rentcar.extras.ServiceBundle;
import edu.rentcar.extras.ServiceComponent;
import edu.rentcar.extras.SingleService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceBundleTest {

    @Test
    void bundleAddsItsServicesAndAppliesDiscount() {
        ServiceComponent bundle = new ServiceBundle("Family pack", 10)
                .add(new SingleService("GPS", 5))
                .add(new SingleService("Child seat", 7));

        // (5 * 3 + 7 * 3) = 36, minus 10% = 32.4
        assertEquals(32.4, bundle.getCost(3), 0.001);
        assertEquals("Family pack (GPS, Child seat)", bundle.getDescription());
    }

    @Test
    void bundleCanContainAnotherBundle() {
        ServiceBundle inner = new ServiceBundle("Family pack", 10)
                .add(new SingleService("GPS", 5))
                .add(new SingleService("Child seat", 7));

        ServiceComponent outer = new ServiceBundle("Big pack", 0)
                .add(inner)
                .add(new SingleService("Extra driver", 10));

        // inner = 32.4, extra driver = 10 * 3 = 30, no extra discount = 62.4
        assertEquals(62.4, outer.getCost(3), 0.001);
    }
}