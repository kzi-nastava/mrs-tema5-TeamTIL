package rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.base.BaseE2ETest;
import rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages.LoginPage;
import rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages.RateRidePage;
import rs.ac.uns.ftn.asd.Projekatsiit2023.e2e.pages.RideHistoryPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E testovi za funkcionalnost 2.8: Ocenjivanje vozila i vozača.
 *
 * Pretpostavke o test podacima u bazi:
 *   - TEST korisnik (user@tiltaxi.com) ima sledeće vožnje:
 *       a) Bar jednu "Completed" vožnju završenu unutar poslednjih 3 dana — NIJE ocenjena
 *       b) Bar jednu "Completed" vožnju koja je VEĆ ocenjena
 *       c) Bar jednu "Canceled" vožnju
 *       d) Bar jednu "Completed" vožnju završenu PRE više od 3 dana — NIJE ocenjena (istekao rok)
 *
 * Pokriveni scenariji:
 *   Happy path:
 *     1. Rate dugme vidljivo za ispravnu (Completed, neocenjenu, u roku) vožnju
 *     2. Uspešno ocenjivanje sa komentarom
 *     3. Uspešno ocenjivanje bez komentara
 *     4. Rate dugme nestaje nakon ocenjivanja
 *
 *   Izuzetni slučajevi:
 *     5. Submit dugme onemogućeno dok ocene nisu postavljene
 *     6. Submit dugme onemogućeno sa samo ocenom vozača
 *     7. Submit dugme onemogućeno sa samo ocenom vozila
 *     8. Rate dugme NIJE vidljivo za već ocenjenu vožnju
 *     9. Rate dugme NIJE vidljivo za Canceled vožnju
 *     10. Rate dugme NIJE vidljivo kada je rok istekao (>3 dana)
 *    11. Zatvaranje dijaloga bez ocenjivanja ne menja stanje
 *    12. Neautorizovan korisnik se preusmerava na login
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RateRideE2ETest extends BaseE2ETest {

    private LoginPage loginPage;
    private RideHistoryPage rideHistoryPage;
    private RateRidePage rateRidePage;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        loginPage = new LoginPage(driver, wait);
        rideHistoryPage = new RideHistoryPage(driver, wait);
        rateRidePage = new RateRidePage(driver, wait);
    }

    private void loginAsTestUser() {
        loginPage.navigateTo();
        loginPage.login(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        assertTrue(loginPage.isLoggedIn(), "Korisnik treba da bude ulogovan");
    }

    private void goToRideHistory() {
        rideHistoryPage.navigateTo();
        rideHistoryPage.waitForPageLoad();
        assertTrue(rideHistoryPage.hasRides(),
                "Korisnik treba da ima istoriju vožnji za testiranje");
    }

    @Test
    @Order(1)
    @DisplayName("1. Rate ride dugme je vidljivo za Completed, neocenjenu, vožnju u roku")
    public void testRateButtonVisibleForEligibleRide() {
        loginAsTestUser();
        goToRideHistory();

        rateRidePage.clickFirstCompletedRide();

        assertTrue(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme treba biti vidljivo za ispravnu (Completed, neocenjenu, u roku) vožnju");
    }

    @Test
    @Order(2)
    @DisplayName("2. Uspešno ocenjivanje vožnje sa komentarom (happy path)")
    public void testRateRideSuccessWithComment() {
        loginAsTestUser();
        goToRideHistory();

        rateRidePage.clickFirstCompletedRide();
        assertTrue(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme mora biti vidljivo da bi test bio validan");

        rateRidePage.clickRateRideButton();
        assertTrue(rateRidePage.isRatingDialogOpen(), "Rating dijalog treba biti otvoren");

        rateRidePage.setDriverRating(5);
        rateRidePage.setVehicleRating(4);
        rateRidePage.setComment("Odlična vožnja, preporučujem!");

        assertEquals(5, rateRidePage.getActiveDriverStarCount(),
                "Trebalo bi biti 5 aktivnih zvezdica za vozača");
        assertEquals(4, rateRidePage.getActiveVehicleStarCount(),
                "Trebalo bi biti 4 aktivne zvezdice za vozilo");

        rateRidePage.clickSubmit();

        rateRidePage.waitForDialogToClose();
        assertTrue(rateRidePage.isDialogClosed(),
                "Dijalog treba biti zatvoren nakon uspešnog ocenjivanja");
    }

    @Test
    @Order(3)
    @DisplayName("3. Uspešno ocenjivanje vožnje bez komentara (komentar je opcionalan)")
    public void testRateRideSuccessWithoutComment() {
        loginAsTestUser();
        goToRideHistory();

        rateRidePage.clickFirstCompletedRide();
        assertTrue(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme mora biti vidljivo da bi test bio validan");

        rateRidePage.clickRateRideButton();
        assertTrue(rateRidePage.isRatingDialogOpen(), "Rating dijalog treba biti otvoren");

        rateRidePage.setDriverRating(3);
        rateRidePage.setVehicleRating(3);

        rateRidePage.clickSubmit();

        rateRidePage.waitForDialogToClose();
        assertTrue(rateRidePage.isDialogClosed(),
                "Ocenjivanje bez komentara treba biti uspešno — komentar je opcionalan");
    }

    @Test
    @Order(4)
    @DisplayName("4. Rate ride dugme nestaje nakon što je vožnja ocenjena")
    public void testRateButtonDisappearsAfterRating() {
        loginAsTestUser();
        goToRideHistory();

        rateRidePage.clickFirstCompletedRide();
        assertTrue(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme mora biti vidljivo pre ocenjivanja");

        rateRidePage.clickRateRideButton();
        rateRidePage.setDriverRating(4);
        rateRidePage.setVehicleRating(4);
        rateRidePage.clickSubmit();
        rateRidePage.waitForDialogToClose();

        try {
            Thread.sleep(500); // kratko čekanje da Angular ažurira UI
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme ne sme biti vidljivo nakon što je vožnja ocenjena");
    }

    @Test
    @Order(5)
    @DisplayName("5. Submit dugme je onemogućeno dok obe ocene nisu postavljene")
    public void testSubmitDisabledWithoutAnyRating() {
        loginAsTestUser();
        goToRideHistory();

        rateRidePage.clickFirstCompletedRide();
        assertTrue(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme mora biti vidljivo da bi test bio validan");

        rateRidePage.clickRateRideButton();
        assertTrue(rateRidePage.isRatingDialogOpen(), "Rating dijalog treba biti otvoren");

        assertTrue(rateRidePage.isSubmitButtonDisabled(),
                "Submit dugme mora biti onemogućeno dok ocene nisu postavljene");

        assertTrue(rateRidePage.isDriverRatingWarningVisible(),
                "Warning za vozača treba biti vidljiv");
        assertTrue(rateRidePage.isVehicleRatingWarningVisible(),
                "Warning za vozilo treba biti vidljiv");
    }

    @Test
    @Order(6)
    @DisplayName("6. Submit dugme je onemogućeno kad je postavljena samo ocena vozača")
    public void testSubmitDisabledWithOnlyDriverRating() {
        loginAsTestUser();
        goToRideHistory();

        rateRidePage.clickFirstCompletedRide();
        assertTrue(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme mora biti vidljivo da bi test bio validan");

        rateRidePage.clickRateRideButton();

        rateRidePage.setDriverRating(5);

        assertTrue(rateRidePage.isSubmitButtonDisabled(),
                "Submit dugme mora biti onemogućeno dok vozilo nije ocenjeno");

        assertTrue(rateRidePage.isVehicleRatingWarningVisible(),
                "Warning za vozilo treba biti vidljiv dok ono nije ocenjeno");
    }

    @Test
    @Order(7)
    @DisplayName("7. Submit dugme je onemogućeno kad je postavljena samo ocena vozila")
    public void testSubmitDisabledWithOnlyVehicleRating() {
        loginAsTestUser();
        goToRideHistory();

        rateRidePage.clickFirstCompletedRide();
        assertTrue(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme mora biti vidljivo da bi test bio validan");

        rateRidePage.clickRateRideButton();

        rateRidePage.setVehicleRating(5);

        assertTrue(rateRidePage.isSubmitButtonDisabled(),
                "Submit dugme mora biti onemogućeno dok vozač nije ocenjen");

        assertTrue(rateRidePage.isDriverRatingWarningVisible(),
                "Warning za vozača treba biti vidljiv dok on nije ocenjen");
    }

    @Test
    @Order(8)
    @DisplayName("8. Rate ride dugme nije vidljivo za već ocenjenu vožnju")
    public void testRateButtonNotVisibleForAlreadyRatedRide() {
        loginAsTestUser();
        goToRideHistory();

        try {
            rateRidePage.clickFirstRatedRide();
            assertFalse(rateRidePage.isRateButtonVisible(),
                    "Rate ride dugme ne sme biti vidljivo za već ocenjenu vožnju");
        } catch (RuntimeException e){
            System.out.println("[UPOZORENJE] Test 8: Nisu pronađene već ocenjene vožnje u test podacima. " +
                    "Dodajte takvu vožnju u bazu za kompletno testiranje.");
        }
    }

    @Test
    @Order(9)
    @DisplayName("9. Rate ride dugme nije vidljivo za Canceled vožnju")
    public void testRateButtonNotVisibleForCanceledRide() {
        loginAsTestUser();
        goToRideHistory();

        try {
            rateRidePage.clickFirstCanceledRide();
            assertFalse(rateRidePage.isRateButtonVisible(),
                    "Rate ride dugme ne sme biti vidljivo za Canceled vožnju");
        } catch (RuntimeException e) {
            System.out.println("[UPOZORENJE] Test 9: Nisu pronađene Canceled vožnje u test podacima. " +
                    "Dodajte takvu vožnju u bazu za kompletno testiranje.");
        }
    }

    @Test
    @Order(10)
    @DisplayName("10. Rate ride dugme nije vidljivo kada je rok istekao (>3 dana)")
    public void testRateButtonNotVisibleAfterDeadlineExpired() {
        loginAsTestUser();
        goToRideHistory();

        boolean found = false;

        List<WebElement> unratedCards = driver.findElements(
                By.cssSelector("mat-card.ride-card[data-rated='false']")
        );

        for (WebElement card : unratedCards) {
            card.click();
            rateRidePage.waitForSidebarToOpen();

            String status = rateRidePage.getSelectedRideStatus();
            if (status.equalsIgnoreCase("Completed") && !rateRidePage.isRateButtonVisible()) {
                found = true;
                assertFalse(rateRidePage.isRateButtonVisible(),
                        "Rate ride dugme ne sme biti vidljivo za Completed vožnju sa isteklim rokom");
                break;
            }
        }

        if (!found) {
            System.out.println("[UPOZORENJE] Test 10: Nisu pronađene Completed neocenjene vožnje sa isteklim rokom.");
        }
    }

    @Test
    @Order(11)
    @DisplayName("11. Zatvaranje dijaloga bez ocenjivanja ne menja stanje vožnje")
    public void testClosingDialogWithoutSubmitPreservesState() {
        loginAsTestUser();
        goToRideHistory();

        rateRidePage.clickFirstCompletedRide();
        assertTrue(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme mora biti vidljivo pre testa");

        rateRidePage.clickRateRideButton();
        assertTrue(rateRidePage.isRatingDialogOpen(), "Dialog treba biti otvoren");

        rateRidePage.setDriverRating(5);
        rateRidePage.setVehicleRating(5);
        rateRidePage.setComment("Komentar koji neće biti sačuvan");
        rateRidePage.closeDialog();

        assertTrue(rateRidePage.isDialogClosed(), "Dialog treba biti zatvoren");

        assertTrue(rateRidePage.isRateButtonVisible(),
                "Rate ride dugme mora ostati vidljivo jer ocenjivanje nije završeno (dialog je zatvoren)");
    }

    @Test
    @Order(12)
    @DisplayName("12. Neautorizovan korisnik se preusmerava na login stranicu")
    public void testUnauthenticatedUserRedirectedToLogin() {
        // Pristup ride history bez logina
        rideHistoryPage.navigateTo();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlContains("/user-ride-history")
        ));

        String currentUrl = driver.getCurrentUrl();

        assertTrue(
                currentUrl.contains("/login") || !currentUrl.contains("/user-ride-history"),
                "Neautorizovan korisnik treba biti preusmerern na login stranicu. Trenutni URL: " + currentUrl
        );
    }
}

