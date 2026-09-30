package sido2;

import sido2.API.FrameworkAPI;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.io.File;

/**
 * Pasos reutilizables. Cada feature nueva se escribe combinando estos pasos;
 * si hace falta uno nuevo, se anade aqui y el metodo correspondiente en FrameworkAPI.
 */
public class Stepdefs {

    // ------------------------------------------------------------------ auth


    @When("Llamamos al a la API {string} con un metodo {string} con el body {string}")  
    public void callApiWithBody(String api, String metodo, String json) {
        FrameworkAPI.endpointWithBody(api, metodo, json);
    }


    @Then("Se comprueba que devuelve un {string} y que el mensaje de error es {string}")
    public void checkResponse(String status, String json) {
        FrameworkAPI.checkResponseEndpoint(status, json);
    }

   
}
