package edu.rentcar.app;

import edu.rentcar.data.SampleData;
import edu.rentcar.factory.PlanCatalog;
import edu.rentcar.model.Customer;
import edu.rentcar.payment.PaymentApiAdapter;
import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.proxy.CustomerVerificationProxy;
import edu.rentcar.proxy.CustomerVerifier;
import edu.rentcar.repository.ReservationRepository;
import edu.rentcar.repository.VehicleRepository;
import edu.rentcar.service.RentalFacade;
import edu.rentcar.service.VehicleService;

/**
 * Service Locator: controllers ask this global class for the services they need.
 * It also wires the reservation workflow: the Adapter (payment API) is wrapped by the Proxy
 * (customer verification) and used by the RentalFacade.
 */
public final class ApplicationServices {
    private static final String PAYMENT_API_URL = "http://localhost:8080";

    private static final VehicleRepository VEHICLE_REPOSITORY = new VehicleRepository();
    private static final VehicleService VEHICLE_SERVICE = new VehicleService(VEHICLE_REPOSITORY);
    private static final ReservationRepository RESERVATION_REPOSITORY = new ReservationRepository();
    private static final PlanCatalog PLAN_CATALOG = new PlanCatalog();
    private static final RentalFacade RENTAL_FACADE = new RentalFacade(RESERVATION_REPOSITORY, createPaymentGateway());

    private ApplicationServices() {
    }

    public static VehicleService vehicleService() {
        return VEHICLE_SERVICE;
    }

    public static RentalFacade rentalFacade() {
        return RENTAL_FACADE;
    }

    public static PlanCatalog planCatalog() {
        return PLAN_CATALOG;
    }

    private static PaymentGateway createPaymentGateway() {
        CustomerVerifier verifier = customerId -> SampleData.customers().stream()
                .filter(customer -> customer.getId().equals(customerId))
                .findFirst()
                .map(Customer::isAuthorized)
                .orElse(false);
        return new CustomerVerificationProxy(verifier, new PaymentApiAdapter(PAYMENT_API_URL));
    }
}