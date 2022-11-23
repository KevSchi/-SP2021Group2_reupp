package campingplatz.catalog;

import static org.salespointframework.core.Currencies.*;

import campingplatz.campsite.Campsite;
import campingplatz.campsite.CampsiteCatalog;

import org.javamoney.moneta.Money;
import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * A {@link DataInitializer} implementation that will create dummy data for the application on application startup.
 *
 * @author Paul Henke
 * @author Oliver Gierke
 * @see DataInitializer
 */
@Component
@Order(20)
class CatalogDataInitializer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(CatalogDataInitializer.class);

	private final CampsiteCatalog campsiteCatalog;

	CatalogDataInitializer(CampsiteCatalog campsiteCatalog) {

		Assert.notNull(campsiteCatalog, "SiteCatalog must not be null!");

		this.campsiteCatalog = campsiteCatalog;


	}
	

	/*
	 * (non-Javadoc)
	 * @see org.salespointframework.core.DataInitializer#initialize()
	 */
	@Override
	public void initialize() {

		if (campsiteCatalog.findAll().iterator().hasNext()) {
			return;
		}
		

		LOG.info("Creating default catalog entries.");

		campsiteCatalog.save(new Campsite("Am Hang 5", "platzhalter", Money.of(12.00, EURO), Campsite.Size.SMALL, Campsite.ParkingSpace.NO, "Diese Parzelle nah am Eingansbereich, und befindet sich mitten in der Zeltplatzregion. Grillen ist hier erlaubt!", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.NOT_PERMACAMPER));
		campsiteCatalog.save(new Campsite("In der Nische 76", "platzhalter", Money.of(14.00, EURO), Campsite.Size.SMALL, Campsite.ParkingSpace.BIKE, "Dieser Stellplatz eignet sich besonders gut für Radfahrer auf der Durchreise. Mit den direkt an den Platz angegliederten Fahrradständern ist Ihr Bike immer griffbereit.", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.NOT_PERMACAMPER));
		campsiteCatalog.save(new Campsite("Im Parkhaus 22", "platzhalter", Money.of(15.00, EURO), Campsite.Size.SMALL, Campsite.ParkingSpace.CAR, "Unser wunderschönes Parkhaus direkt am Kiosk wird Ihnen mit Sicherheit gefallen. Besonders die Nähe zu ihren Nachbarn schätzen unsere Gäste sehr. Aufgrund des harten Untergrundes nicht für zeltende Gäste emfpehlenswert.", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.NOT_PERMACAMPER));
		campsiteCatalog.save(new Campsite("In der Bucht 7", "platzhalter", Money.of(20.00, EURO), Campsite.Size.SMALL, Campsite.ParkingSpace.TRAILER, "Dieser wunderschöne Dauercampingplatz befindet sich direkt in der Bucht unseres Campingplatzes. Die Brandung des Meeres ist immer hörbar und wird Ihr Urlaubsgefühl enorm steigern. Achtung nasse Füße!!!", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.PERMACAMPER));

		campsiteCatalog.save(new Campsite("In der Mitte 1", "platzhalter", Money.of(16.00, EURO), Campsite.Size.MEDIUM, Campsite.ParkingSpace.NO, "Dieser Dauercampingplatz ist im absoluten Mittelpunkt unseres Campingplatzes. Ein Findling in Mitten Ihres Stellplatzes hebt den Wert Ihres Platzes ungemein an und sorgt für ein besonderes Feeling.", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.PERMACAMPER));
		campsiteCatalog.save(new Campsite("Auf dem Berg 36", "platzhalter", Money.of(18.00, EURO),Campsite.Size.MEDIUM, Campsite.ParkingSpace.BIKE, "Die Aussicht dieses Campingplatzes ist fenomenal. Mit dem Fahrrad oder einem Motorrad ist der Weg auch gut zu beschreiten. Außerdem bietet der Platz auch genug Fläche für eine Familie.", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.NOT_PERMACAMPER));
		campsiteCatalog.save(new Campsite("Auf dem alten Indianerfriedhof 4", "platzhalter", Money.of(19.00, EURO), Campsite.Size.MEDIUM, Campsite.ParkingSpace.CAR, "Auf diesem Campingplatz mit angeschlossenem Autoparkplatz lässt es sich besonders in der Nacht gut leben. Die natürliche Beleuchtung durch das Mondlicht in Kombination mit den Holzkreuzen am Horizont sorgt für eine angenehme Stimmung.", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.PERMACAMPER));
		campsiteCatalog.save(new Campsite("Auf der Insel 97", "platzhalter", Money.of(24.00, EURO), Campsite.Size.MEDIUM, Campsite.ParkingSpace.TRAILER, "Der Ort dieser tollen Parzelle bietet besonderen Schutz vor Diebstahl und auf unserer Insel sind Sie (fast) allein. ACHTUNG: die Überführungskosten für den Hubschrauber werden von Ihnen getragen!", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.NOT_PERMACAMPER));
		
		campsiteCatalog.save(new Campsite("Die Königsklasse 2", "platzhalter", Money.of(18.00, EURO),Campsite.Size.LARGE, Campsite.ParkingSpace.NO, "Dieser Platz ist die Premiumklasse aller Plätze. Der nicht vorhandene Stellplatz für Ihre Limousine ist selbstverständlich gratis und stielt Ihnen keinen Platz.", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.PERMACAMPER));
		campsiteCatalog.save(new Campsite("Am Südstrand 3", "platzhalter", Money.of(20.00, EURO), Campsite.Size.LARGE, Campsite.ParkingSpace.BIKE, "Diese Parzelle ist am südlichen Ende des Campingplatzes zu finden. Innerhalb weniger Minuten sind Sie zum Strand gelaufen. Außerdem befindet sich ein Waschhaus in der näheren Umgebung.", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.NOT_PERMACAMPER));
		campsiteCatalog.save(new Campsite("In der Höhle 19", "platzhalter", Money.of(21.00, EURO), Campsite.Size.LARGE, Campsite.ParkingSpace.CAR, "Hier benötigen Sie nichts! Der Berg schützt Sie vor jeder Witterung. Lediglich die flauschigen Kissen im Inneren haben ab und an die Angewohnheit unsere Gäste mit Nahrung zu verwechseln. Ein Testament ist von Vorteil.", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.PERMACAMPER));
		campsiteCatalog.save(new Campsite("In der Hölle 666", "platzhalter", Money.of(26.00, EURO), Campsite.Size.LARGE, Campsite.ParkingSpace.TRAILER, "Dieser Campingplatz hat einen kostenfreien Butler mit Pferdefuß. Die Massagen mit den etwas ausgefallenen Werkzeugen können besonders bei den extremen klimatischen Bedinungen genossen werden.", Campsite.Visibility.VISIBLE, Campsite.PermaCamper.PERMACAMPER));

		campsiteCatalog.save(new Campsite("DEFEKT_1", "platzhalter", Money.of(0.00, EURO),Campsite.Size.SMALL, Campsite.ParkingSpace.NO, "Platzhalter Beschreibung", Campsite.Visibility.NOT_VISIBLE, Campsite.PermaCamper.PERMACAMPER));
		campsiteCatalog.save(new Campsite("DEFEKT_2", "platzhalter", Money.of(0.00, EURO), Campsite.Size.SMALL, Campsite.ParkingSpace.BIKE, "Platzhalter Beschreibung", Campsite.Visibility.NOT_VISIBLE, Campsite.PermaCamper.NOT_PERMACAMPER));
		campsiteCatalog.save(new Campsite("DEFEKT_3", "platzhalter", Money.of(0.00, EURO), Campsite.Size.MEDIUM, Campsite.ParkingSpace.CAR, "Platzhalter Beschreibung", Campsite.Visibility.NOT_VISIBLE, Campsite.PermaCamper.PERMACAMPER));
		campsiteCatalog.save(new Campsite("DEFEKT_4", "platzhalter", Money.of(0.00, EURO), Campsite.Size.LARGE, Campsite.ParkingSpace.TRAILER, "Platzhalter Beschreibung", Campsite.Visibility.NOT_VISIBLE, Campsite.PermaCamper.NOT_PERMACAMPER));

	}
}