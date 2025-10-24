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

    assertTrue(resp.statusCode() >= 199 && resp.statusCode() <= 200);
    assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("json"));
    String _bodyNoWs = resp.body().replace(" ", "").replace("\n", "").replace("\r", "").replace("\t", "");
    assertTrue(_bodyNoWs.contains("\"success\":true"));
    assertTrue(_bodyNoWs.contains("\"token\":"));
    assertTrue(_bodyNoWs.contains("\"message\":\"Loginsuccessful\""));
  }

}
