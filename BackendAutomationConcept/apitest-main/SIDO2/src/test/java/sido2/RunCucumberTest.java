package sido2;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Punto de entrada: JUnit lanza aqui todas las features de src/test/resources/sido2.
 *
 *   mvn test     -> ejecuta todo menos lo marcado con @WIP (sanidad.feature)
 *   mvn verify   -> lo anterior + informe HTML en
 *                   target/cucumber-report-html/cucumber-html-reports
 *
 * Para ejecutar solo un subconjunto, por tags:
 *   mvn test "-Dcucumber.options=--tags @WIP"     (ejemplo.feature)
 *   mvn test "-Dcucumber.options=--tags @smoke"   (features propias)
 *   mvn test "-Dcucumber.options=--name 'nombre del escenario'"
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:sido2",
        tags = "@test",
        plugin = {
                "pretty",
                "json:target/cucumber.json"
        }
)
public class RunCucumberTest {
}
