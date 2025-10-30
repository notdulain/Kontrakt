import ast.*;
import java.util.*;

/**
 * Generates a minimal OpenAPI 3.0 YAML document from the Kontrakt AST.
 * The focus is structure correctness (servers/paths/responses) and
 * small examples built from request bodies and assertions.
 */
public class OpenApiGenerator {

    private final StringBuilder out = new StringBuilder();
    private final Map<String, String> variables = new HashMap<>();

    public String generate(Program program) {
        variables.clear();
        for (Variable v : program.getVariables()) {
            variables.put(v.getName(), v.getValue());
        }

        line("openapi: 3.0.3");
        line("info:"); indent(+1);
        line("title: Kontrakt Spec");
        line("version: 0.1.0");
        indent(-1);

        if (program.getConfig() != null && program.getConfig().getBaseUrl() != null) {
            line("servers:"); indent(+1);
            line("- url: " + quote(program.getConfig().getBaseUrl()));
            indent(-1);
        }

        Map<String, Map<String, Operation>> paths = collectOperations(program);

        line("paths:"); indent(+1);
        for (var pathEntry : paths.entrySet()) {
            line(escapePath(pathEntry.getKey()) + ":"); indent(+1);
            for (var methodEntry : pathEntry.getValue().entrySet()) {
                line(methodEntry.getKey() + ":"); indent(+1);
                emitOperation(methodEntry.getValue());
                indent(-1);
            }
            indent(-1);
        }
        indent(-1);

        return out.toString();
    }

    // ---------------------------------------------------------------------
    // Collection phase

    private Map<String, Map<String, Operation>> collectOperations(Program program) {
        Map<String, Map<String, Operation>> paths = new LinkedHashMap<>();

        for (Test test : program.getTests()) {
            Operation current = null;
            for (Statement stmt : test.getStatements()) {
                if (stmt instanceof Request req) {
                    String path = substitute(req.getPath());
                    String method = req.getMethod().name().toLowerCase(Locale.ROOT);

                    current = paths
                        .computeIfAbsent(path, k -> new LinkedHashMap<>())
                        .computeIfAbsent(method, k -> new Operation(method, path));

                    current.requestContentType = pickContentType(program.getConfig(), req);
                    current.rawRequestBody = substitute(req.getBody());
                    if (current.requestExample.isEmpty()) {
                        current.requestExample.putAll(parseSimpleJsonObject(current.rawRequestBody));
                    }
                } else if (stmt instanceof Assertion assertion && current != null) {
                    current.applyAssertion(assertion);
                }
            }
        }

        return paths;
    }

    // ---------------------------------------------------------------------
    // Emission

    private void emitOperation(Operation op) {
        line("operationId: " + quote(op.operationId()));

        if (!op.requestExample.isEmpty()) {
            line("requestBody:"); indent(+1);
            line("content:"); indent(+1);
            line(op.requestContentType + ":"); indent(+1);
            line("example:"); indent(+1);
            emitExampleMap(op.requestExample);
            indent(-1); // example
            indent(-1); // media type
            indent(-1); // content
            indent(-1); // requestBody
        } else if (op.rawRequestBody != null) {
            line("requestBody:"); indent(+1);
            line("content:"); indent(+1);
            line(op.requestContentType + ":"); indent(+1);
            line("example: |"); indent(+1);
            for (String ln : op.rawRequestBody.split("\\R")) {
                line(ln);
            }
            indent(-1); indent(-1); indent(-1); indent(-1);
        }

        line("responses:"); indent(+1);
        if (op.responses.isEmpty()) {
            line("\"200\":"); indent(+1);
            line("description: \"Response 200\"");
            indent(-1);
        } else {
            for (var entry : op.responses.entrySet()) {
                int status = entry.getKey();
                ResponseSpec spec = entry.getValue();
                line("\"" + status + "\":"); indent(+1);
                line("description: " + quote(spec.description()));
                if (!spec.headers.isEmpty()) {
                    line("headers:"); indent(+1);
                    for (var hdr : spec.headers.entrySet()) {
                        line(hdr.getKey() + ":"); indent(+1);
                        line("schema:"); indent(+1);
                        line("type: string");
                        indent(-1);
                        line("description: " + quote(hdr.getValue()));
                        indent(-1);
                    }
                    indent(-1);
                }
                if (!spec.bodyExample.isEmpty()) {
                    line("content:"); indent(+1);
                    line(op.responseContentType + ":"); indent(+1);
                    line("example:"); indent(+1);
                    emitExampleMap(spec.bodyExample);
                    indent(-1); indent(-1); indent(-1);
                }
                indent(-1);
            }
        }
        indent(-1);
    }

    private void emitExampleMap(LinkedHashMap<String, ExampleValue> values) {
        for (var entry : values.entrySet()) {
            line(entry.getKey() + ": " + entry.getValue().toYaml());
        }
    }

    // ---------------------------------------------------------------------
    // Helpers

    private String substitute(String text) {
        if (text == null) return null;
        String result = text;
        for (var entry : variables.entrySet()) {
            result = result.replace("$" + entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String pickContentType(Config config, Request request) {
        if (request != null && request.getHeaders() != null) {
            String ct = request.getHeaders().get("Content-Type");
            if (ct != null) return ct;
        }
        if (config != null && config.getDefaultHeaders() != null) {
            String ct = config.getDefaultHeaders().get("Content-Type");
            if (ct != null) return ct;
        }
        return "application/json";
    }

    private LinkedHashMap<String, ExampleValue> parseSimpleJsonObject(String json) {
        LinkedHashMap<String, ExampleValue> result = new LinkedHashMap<>();
        if (json == null) return result;
        String trimmed = json.trim();
        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) return result;
        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) return result;

        List<String> parts = splitTopLevel(inner);
        for (String part : parts) {
            int colon = indexOfTopLevelColon(part);
            if (colon == -1) continue;
            String key = unquote(part.substring(0, colon).trim());
            String value = part.substring(colon + 1).trim();
            result.put(key, ExampleValue.fromJson(value));
        }
        return result;
    }

    private List<String> splitTopLevel(String text) {
        List<String> parts = new ArrayList<>();
        boolean inQuotes = false;
        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '{' || c == '[') depth++;
                if (c == '}' || c == ']') depth--;
                if (c == ',' && depth == 0) {
                    parts.add(current.toString().trim());
                    current.setLength(0);
                    continue;
                }
            }
            current.append(c);
        }
        if (current.length() > 0) parts.add(current.toString().trim());
        return parts;
    }

    private int indexOfTopLevelColon(String text) {
        boolean inQuotes = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes && c == ':') return i;
        }
        return -1;
    }

    private String unquote(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private void line(String text) {
        out.append("  ".repeat(Math.max(0, indent))).append(text == null ? "" : text).append("\n");
    }

    private void indent(int delta) {
        indent += delta;
        if (indent < 0) indent = 0;
    }

    private String quote(String text) {
        if (text == null) return "\"\"";
        return "\"" + text.replace("\"", "\\\"") + "\"";
    }

    private String escapePath(String path) {
        return path.replaceAll("\\$(\\w+)", "{$1}");
    }

    // ---------------------------------------------------------------------
    // Data structures

    private static final class Operation {
        final String method;
        final String path;
        String requestContentType = "application/json";
        String rawRequestBody;
        final LinkedHashMap<String, ExampleValue> requestExample = new LinkedHashMap<>();
        final Map<Integer, ResponseSpec> responses = new LinkedHashMap<>();
        Integer lastStatus;
        String responseContentType = "application/json";

        Operation(String method, String path) {
            this.method = method;
            this.path = path;
        }

        void applyAssertion(Assertion assertion) {
            switch (assertion.getType()) {
                case STATUS -> {
                    Integer code = assertion.getStatusCode();
                    if (code != null) {
                        lastStatus = code;
                        responses.computeIfAbsent(code, ResponseSpec::new);
                    }
                }
                case STATUS_RANGE -> {
                    // Range -> default to 200 for documentation purposes
                    lastStatus = 200;
                    responses.computeIfAbsent(200, ResponseSpec::new);
                }
                case HEADER_EQUALS -> ensureResponse().headers.put(assertion.getHeaderName(), assertion.getExpectedValue());
                case HEADER_CONTAINS -> ensureResponse().headers.put(assertion.getHeaderName(), "contains " + assertion.getExpectedValue());
                case BODY_CONTAINS -> ensureResponse().addBodyFragment(assertion.getExpectedValue());
                default -> {}
            }
        }

        ResponseSpec ensureResponse() {
            int code = lastStatus != null ? lastStatus : 200;
            return responses.computeIfAbsent(code, ResponseSpec::new);
        }

        String operationId() {
            return method + "_" + path.replaceAll("[^a-zA-Z0-9]+", "_");
        }

        String responseContentType() {
            return responseContentType;
        }
    }

    private static final class ResponseSpec {
        final int status;
        final LinkedHashMap<String, ExampleValue> bodyExample = new LinkedHashMap<>();
        final Map<String, String> headers = new LinkedHashMap<>();

        ResponseSpec(int status) {
            this.status = status;
        }

        void addBodyFragment(String fragment) {
            if (fragment == null) return;
            ExamplePair pair = ExamplePair.fromFragment(fragment.trim());
            if (pair != null) {
                bodyExample.put(pair.key, pair.value);
            }
        }

        String description() {
            ExampleValue msg = bodyExample.get("message");
            if (msg != null && msg.kind == ExampleValue.Kind.STRING) {
                return msg.literal;
            }
            return "Response " + status;
        }
    }

    private static final class ExamplePair {
        final String key;
        final ExampleValue value;

        ExamplePair(String key, ExampleValue value) {
            this.key = key;
            this.value = value;
        }

        static ExamplePair fromFragment(String text) {
            int colon = text.indexOf(':');
            if (colon == -1) return null;
            String keyPart = text.substring(0, colon).trim();
            String valuePart = text.substring(colon + 1).trim();
            String key = keyPart;
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }
            return new ExamplePair(key, ExampleValue.fromJson(valuePart));
        }
    }

    private static final class ExampleValue {
        enum Kind { STRING, NUMBER, BOOLEAN, NULL }

        final Kind kind;
        final String literal;

        ExampleValue(Kind kind, String literal) {
            this.kind = kind;
            this.literal = literal;
        }

        static ExampleValue fromJson(String jsonLiteral) {
            String trimmed = jsonLiteral.trim();
            if (trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("false")) {
                return new ExampleValue(Kind.BOOLEAN, trimmed.toLowerCase(Locale.ROOT));
            }
            if (trimmed.equalsIgnoreCase("null")) {
                return new ExampleValue(Kind.NULL, "null");
            }
            if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
                String inner = trimmed.substring(1, trimmed.length() - 1);
                return new ExampleValue(Kind.STRING, inner.replace("\\\"", "\""));
            }
            try {
                Double.parseDouble(trimmed);
                return new ExampleValue(Kind.NUMBER, trimmed);
            } catch (NumberFormatException ex) {
                return new ExampleValue(Kind.STRING, trimmed);
            }
        }

        String toYaml() {
            return switch (kind) {
                case STRING -> yamlQuote(literal);
                case NUMBER, BOOLEAN -> literal;
                case NULL -> "null";
            };
        }

        private String yamlQuote(String text) {
            if (text == null || text.isEmpty()) return "\"\"";
            if (text.contains("\"")) {
                return "'" + text.replace("'", "''") + "'";
            }
            return "\"" + text + "\"";
        }
    }

    private int indent = 0;
}
