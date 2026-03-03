import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOKWithValidToken() {
        // Arrange
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;

        // Act & Assert
        Response response = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract().response();

        System.out.println("Generated Token: " + response.jsonPath().getString("token"));
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        // Arrange
        String loginPayload = """
                {
                    "email": "invalid_user@test.com",
                    "password": "wrongPassword"
                }
                """;

        // Act & Arrange
        Response response = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .extract().response();

        System.out.println("Status Code: " + response.statusCode());
    }
}
