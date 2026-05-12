package br.com.sistemamoedas;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class HomeControllerTest {

    @Test
    void deveRenderizarHome() {
        given()
                .when().get("/")
                .then()
                .statusCode(200)
                .body(containsString("Valoriza Aê"));
    }
}
