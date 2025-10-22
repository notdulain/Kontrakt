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
    client = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(5))
      .build();
    BASE = "http://localhost:8080";
    DEFAULT_HEADERS.put("X-App", "TestLangDemo");
    DEFAULT_HEADERS.put("Content-Type", "application/json");
  }

  @Test
  void test_Login() throws Exception {
    System.out.println("--> POST " + BASE + "/api/login");
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/login"))
      .timeout(Duration.ofSeconds(10))
      .POST(HttpRequest.BodyPublishers.ofString("{ \"username\": \"admin\", \"password\": \"1234\" }"));
    System.out.println("    body=" + "{ \"username\": \"admin\", \"password\": \"1234\" }");
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    b.header("Accept", "application/json");
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    System.out.println("<-- status=" + resp.statusCode());
    System.out.println(resp.body());

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("json"));
    String _bodyNoWs = resp.body().replace(" ", "").replace("\n", "").replace("\r", "").replace("\t", "");
    assertTrue(_bodyNoWs.contains("\"success\":true"));
    assertTrue(_bodyNoWs.contains("\"token\":"));
    assertTrue(_bodyNoWs.contains("\"message\":\"Loginsuccessful\""));
  }

  @Test
  void test_GetUser() throws Exception {
    System.out.println("--> GET " + BASE + "/api/users/42");
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/users/42"))
      .timeout(Duration.ofSeconds(10))
      .GET();
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    b.header("Accept", "application/json");
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    System.out.println("<-- status=" + resp.statusCode());
    System.out.println(resp.body());

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("json"));
    String _bodyNoWs = resp.body().replace(" ", "").replace("\n", "").replace("\r", "").replace("\t", "");
    assertTrue(_bodyNoWs.contains("\"id\":42"));
    assertTrue(_bodyNoWs.contains("\"username\":\"user42\""));
    assertTrue(_bodyNoWs.contains("\"email\":\"user42@example.com\""));
    assertTrue(_bodyNoWs.contains("\"role\":\"USER\""));
  }

  @Test
  void test_UpdateUser() throws Exception {
    System.out.println("--> PUT " + BASE + "/api/users/42");
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/users/42"))
      .timeout(Duration.ofSeconds(10))
      .PUT(HttpRequest.BodyPublishers.ofString("{ \"role\": \"ADMIN\" }"));
    System.out.println("    body=" + "{ \"role\": \"ADMIN\" }");
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    b.header("Accept", "application/json");
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    System.out.println("<-- status=" + resp.statusCode());
    System.out.println(resp.body());

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("json"));
    String _bodyNoWs = resp.body().replace(" ", "").replace("\n", "").replace("\r", "").replace("\t", "");
    assertTrue(_bodyNoWs.contains("\"id\":42"));
    assertTrue(_bodyNoWs.contains("\"updated\":true"));
    assertTrue(_bodyNoWs.contains("\"role\":\"ADMIN\""));
    assertTrue(_bodyNoWs.contains("\"message\":\"Userupdatedsuccessfully\""));
  }

  @Test
  void test_DeleteUser() throws Exception {
    System.out.println("--> DELETE " + BASE + "/api/users/99");
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/users/99"))
      .timeout(Duration.ofSeconds(10))
      .DELETE();
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    b.header("Accept", "application/json");
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    System.out.println("<-- status=" + resp.statusCode());
    System.out.println(resp.body());

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("json"));
    String _bodyNoWs = resp.body().replace(" ", "").replace("\n", "").replace("\r", "").replace("\t", "");
    assertTrue(_bodyNoWs.contains("\"id\":99"));
    assertTrue(_bodyNoWs.contains("\"deleted\":true"));
    assertTrue(_bodyNoWs.contains("\"message\":\"Userdeletedsuccessfully\""));
  }

  @Test
  void test_GetNonExistentUser() throws Exception {
    System.out.println("--> GET " + BASE + "/api/users/9999");
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/users/9999"))
      .timeout(Duration.ofSeconds(10))
      .GET();
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    b.header("Accept", "application/json");
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    System.out.println("<-- status=" + resp.statusCode());
    System.out.println(resp.body());

    assertEquals(404, resp.statusCode());
    String _bodyNoWs = resp.body().replace(" ", "").replace("\n", "").replace("\r", "").replace("\t", "");
    assertTrue(_bodyNoWs.contains("\"error\""));
    assertTrue(_bodyNoWs.contains("\"notfound\""));
  }

  @Test
  void test_LoginInvalidCredentials() throws Exception {
    System.out.println("--> POST " + BASE + "/api/login");
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + "/api/login"))
      .timeout(Duration.ofSeconds(10))
      .POST(HttpRequest.BodyPublishers.ofString("{ \"username\": \"wrong\", \"password\": \"wrong\" }"));
    System.out.println("    body=" + "{ \"username\": \"wrong\", \"password\": \"wrong\" }");
    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());
    b.header("Accept", "application/json");
    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    System.out.println("<-- status=" + resp.statusCode());
    System.out.println(resp.body());

    assertEquals(401, resp.statusCode());
    String _bodyNoWs = resp.body().replace(" ", "").replace("\n", "").replace("\r", "").replace("\t", "");
    assertTrue(_bodyNoWs.contains("\"success\":false"));
    assertTrue(_bodyNoWs.contains("\"message\":\"Invalidcredentials\""));
  }

}
