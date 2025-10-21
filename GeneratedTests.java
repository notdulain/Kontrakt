import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.http.*;
import java.net.*;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GeneratedTests {
  static String BASE = "";
  static Map<String,String> DEFAULT_HEADERS = new HashMap<>();
  static HttpClient client;

  @BeforeAll
  static void setup() {
    client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    BASE = "http://localhost:8080";
    DEFAULT_HEADERS.put("X-App", "TestLangDemo");
    DEFAULT_HEADERS.put("Content-Type", "application/json");
  }

  @Test
  void test_Login() throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/login"))
      .timeout(Duration.ofSeconds(10))
      .POST(HttpRequest.BodyPublishers.ofString("{ \"username\": \"admin\", \"password\": \"1234\" }"));
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("json"));
    assertTrue(resp.body().contains("\"success\": true"));
    assertTrue(resp.body().contains("\"token\":"));
    assertTrue(resp.body().contains("\"message\": \"Login successful\""));
  }

  @Test
  void test_GetUser() throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/users/42"))
      .timeout(Duration.ofSeconds(10))
      .GET();
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("json"));
    assertTrue(resp.body().contains("\"id\": 42"));
    assertTrue(resp.body().contains("\"username\": \"user42\""));
    assertTrue(resp.body().contains("\"email\": \"user42@example.com\""));
    assertTrue(resp.body().contains("\"role\": \"USER\""));
  }

  @Test
  void test_UpdateUser() throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/users/42"))
      .timeout(Duration.ofSeconds(10))
      .PUT(HttpRequest.BodyPublishers.ofString("{ \"role\": \"ADMIN\" }"));
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("json"));
    assertTrue(resp.body().contains("\"id\": 42"));
    assertTrue(resp.body().contains("\"updated\": true"));
    assertTrue(resp.body().contains("\"role\": \"ADMIN\""));
    assertTrue(resp.body().contains("\"message\": \"User updated successfully\""));
  }

  @Test
  void test_DeleteUser() throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/users/99"))
      .timeout(Duration.ofSeconds(10))
      .DELETE();
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("json"));
    assertTrue(resp.body().contains("\"id\": 99"));
    assertTrue(resp.body().contains("\"deleted\": true"));
    assertTrue(resp.body().contains("\"message\": \"User deleted successfully\""));
  }

  @Test
  void test_GetNonExistentUser() throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/users/9999"))
      .timeout(Duration.ofSeconds(10))
      .GET();
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    assertEquals(404, resp.statusCode());
    assertTrue(resp.body().contains("\"error\""));
    assertTrue(resp.body().contains("\"not found\""));
  }

  @Test
  void test_LoginInvalidCredentials() throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/login"))
      .timeout(Duration.ofSeconds(10))
      .POST(HttpRequest.BodyPublishers.ofString("{ \"username\": \"wrong\", \"password\": \"wrong\" }"));
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    assertEquals(401, resp.statusCode());
    assertTrue(resp.body().contains("\"success\": false"));
    assertTrue(resp.body().contains("\"error\""));
  }

}
