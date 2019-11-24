package fr.umlv.square.endpoint;

import org.junit.jupiter.api.Test;

import fr.umlv.square.database.entities.Application;

import static io.restassured.RestAssured.given;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;


import javax.ws.rs.core.MediaType;
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

@QuarkusTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class endPointeTest {

    @Test
    @Transactional
    @Order(1)
    void testAppListEmpty() throws IOException {
        var app = get("/app/list").then()
                .statusCode(HttpStatus.SC_OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .asInputStream();             
        assertEquals(0,app.available());
           
    }
    
    @Test
    @Order(2)
    @Transactional
    void testAddingAnItem() {
       var app = given()
                .body("{\"app\" : \"todomvc:8080\"}")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .when()
                .post("/app/deploy")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .asInputStream();
    }
    
    @Test
    @Order(3)
    @Transactional
    void testStopItem() {
       var app = given()
                .body("{\"id\" : \"1\"}")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .when()
                .post("/app/stop")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .asInputStream();
    }
    
    
    private TypeRef<List<Application>> getAppTypeRef() {
        return new TypeRef<List<Application>>() {
        };
    }

}
