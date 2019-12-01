package fr.umlv.square.endpoint;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.HttpHeaders;
import static io.restassured.RestAssured.*;
import static org.hamcrest.core.Is.is;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class endPointeTest {

	@Test
	@Order(1)
	void testAppListEmpty() {
		get("/app/list").then().statusCode(HttpStatus.SC_OK)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.body("size()", is(0));
	}

	@Test
	@Order(2)
	void testDeployingApp() {
		given().body("{\"app\" : \"todomvc:8080\"}")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/deploy").then()
				.statusCode(HttpStatus.SC_CREATED).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.body("id", is(Integer.valueOf(1)))
				.body("app", is("todomvc:8080"))
				.body("port", is(Integer.valueOf(8080)))
				.body("docker-instance", is("todomvc-1"));
	}
	
	@Test
	void testDeployingBadApp() {
		given().body("{\"app\" : \"todo:8080\"}")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/deploy").then()
				.statusCode(400).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

	}
	
	@Test
	void testDeployingAppWithoutPort() {
		given().body("{\"app\" : \"todo:\"}")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/deploy").then()
				.statusCode(400).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

	}
	
	@Test
	@Order(3)
	void testAppListWith1Deploy() {
		get("/app/list").then().statusCode(HttpStatus.SC_OK)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.statusCode(HttpStatus.SC_OK)
				.body("size()", is(1));

	}
	
	@Test
	@Order(4)
	void testDeployingApp_2() {
		given().body("{\"app\" : \"todomvc:8080\"}")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/deploy").then()
				.statusCode(HttpStatus.SC_CREATED).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.body("id", is(Integer.valueOf(2)))
				.body("app", is("todomvc:8080"))
				.body("port", is(Integer.valueOf(8080)))
				.body("docker-instance", is("todomvc-2"));
	}
	
	@Test
	@Order(5)
	void testAppListWith2Deploy() {
		get("/app/list").then().statusCode(HttpStatus.SC_OK)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.statusCode(HttpStatus.SC_OK)
				.body("size()", is(2));

	}


	@Test
	@Order(6)
	void testStopApp() {
		given().body("{\"id\" : \"1\"}").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/stop").then()
				.statusCode(HttpStatus.SC_OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).extract()
				.body().asInputStream();
	}
	
	@Test
	void testStopAppBadId() {
		given().body("{\"id\" : \"10\"}").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/stop").then()
				.statusCode(400).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
	}	
	
	@Test
	@Order(7)
	void testStopApp2() {
		given().body("{\"id\" : \"2\"}").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/stop").then()
				.statusCode(HttpStatus.SC_OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
	}
}
