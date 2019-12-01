package fr.umlv.square.endpoint;

import org.junit.jupiter.api.Test;

import fr.umlv.square.database.entities.Application;

import static io.restassured.RestAssured.given;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.jupiter.api.*;

import javax.ws.rs.core.MediaType;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.HttpHeaders;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class endPointeTest {

	@Test
	@Order(1)
	void testAppListEmpty() throws IOException {
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
	void testAppListWith1Deploy() throws IOException {
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
	void testAppListWith2Deploy() throws IOException {
		get("/app/list").then().statusCode(HttpStatus.SC_OK)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.statusCode(HttpStatus.SC_OK)
				.body("size()", is(2));

	}


	@Test
	@Order(6)
	void testStopApp() {
		var app = given().body("{\"id\" : \"1\"}").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/stop").then()
				.statusCode(HttpStatus.SC_OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).extract()
				.body().asInputStream();
	}
	
	@Test
	void testStopAppBadId() {
		var app = given().body("{\"id\" : \"10\"}").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/stop").then()
				.statusCode(400).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
	}	
	
	@Test
	@Order(7)
	void testStopApp2() {
		var app = given().body("{\"id\" : \"2\"}").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON).when().post("/app/stop").then()
				.statusCode(HttpStatus.SC_OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).extract()
				.body().asInputStream();
	}
}
