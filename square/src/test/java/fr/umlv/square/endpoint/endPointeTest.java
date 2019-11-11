package fr.umlv.square.endpoint;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class endPointeTest {


	    @Test
	    public void appList() {
	        given()
	          .when().get("/app/list")
	          .then()
	             .statusCode(200);
	    }
	}