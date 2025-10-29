import ast.*;
import java.util.*;

public class OpenApiGenerator {
  private final StringBuilder out = new StringBuilder();
  private final Map<String,String> vars = new HashMap<>();

  public String generate(Program program) {
    // collect variables
    for (Variable v : program.getVariables()) vars.put(v.getName(), v.getValue());

    // header
    line("openapi: 3.0.3");
    line("info:"); indent(1); line("title: Kontrakt Spec"); line("version: 0.1.0"); indent(0);

    // servers
    if (program.getConfig() != null && program.getConfig().getBaseUrl() != null) {
      line("servers:"); indent(1); line("- url: " + quote(program.getConfig().getBaseUrl())); indent(0);
    }

    // collect operations
    line("paths:");
    indent(1);
    // map key: method + " " + path
    Map<String, Op> ops = collect(program);
    // emit
    Map<String, Map<String, List<Op>>> byPath = groupByPath(ops.values());
    for (var e : byPath.entrySet()) {
      line(escapePath(e.getKey()) + ":");
      indent(1);
      for (var m : e.getValue().entrySet()) {
        line(m.getKey() + ":"); // get/post/put/delete
        indent(1);
        Op op = merge(m.getValue());
        emitOperation(op);
        indent(-1);
      }
      indent(-1);
    }
    indent(-1);
    return out.toString();
  }

  private Map<String, Op> collect(Program program) {
    Map<String, Op> ops = new LinkedHashMap<>();
    for (Test t : program.getTests()) {
      Op current = null;
      for (Statement s : t.getStatements()) {
        if (s instanceof Request r) {
          current = new Op(r.getMethod().name().toLowerCase(), substitute(r.getPath()));
          current.exampleBody = substitute(r.getBody());
          current.reqContentType = pickContentType(program.getConfig(), r);
          ops.put(current.method + " " + current.path, current);
        } else if (s instanceof Assertion a && current != null) {
          current.applyAssertion(a);
        }
      }
    }
    return ops;
  }

  // helpers…
  private String substitute(String in) {
    if (in == null) return null;
    String res = in;
    for (var e : vars.entrySet()) res = res.replace("$" + e.getKey(), e.getValue());
    return res;
  }

  private void emitOperation(Op op) {
    line("operationId: " + quote(op.opId()));
    // requestBody
    if (op.exampleBody != null) {
      line("requestBody:"); indent(1);
      line("content:"); indent(1);
      line(op.reqContentType + ":"); indent(1);
      line("examples:"); indent(1);
      line("example:"); indent(1);
      line("value: |"); // multiline literal
      indent(1);
      for (String ln : op.exampleBody.split("\\R")) line(ln);
      indent(-2); // back from value and example
      indent(-2); // back from content types
    }
    // responses
    line("responses:"); indent(1);
    if (op.statuses.isEmpty()) {
      line("\"200\":"); indent(1); line("description: OK"); indent(-1);
    } else {
      for (Integer code : op.statuses) {
        line("\"" + code + "\":"); indent(1);
        line("description: " + quote(op.describe(code)));
        if (!op.responseHeaders.isEmpty()) {
          line("headers:"); indent(1);
          for (var h : op.responseHeaders.entrySet()) {
            line(h.getKey() + ":"); indent(1);
            line("schema: { type: string }");
            line("description: " + quote(h.getValue()));
            indent(-1);
          }
          indent(-1);
        }
        indent(-1);
      }
    }
    indent(-1);
  }

  // tiny YAML helpers
  private int ind = 0;
  private void indent(int d) { ind += d; if (ind < 0) ind = 0; }
  private void line(String s) { out.append("  ".repeat(ind)).append(s == null ? "" : s).append("\n"); }
  private String quote(String s) { return "\"" + (s == null ? "" : s.replace("\"","\\\"")) + "\""; }

  // Op aggregator
  static class Op {
    final String method, path;
    String exampleBody, reqContentType = "application/json";
    Set<Integer> statuses = new LinkedHashSet<>();
    Map<String,String> responseHeaders = new LinkedHashMap<>();
    List<String> bodyContains = new ArrayList<>();
    Op(String m, String p) { method=m; path=p; }
    void applyAssertion(Assertion a) {
      switch (a.getType()) {
        case STATUS -> statuses.add(a.getStatusCode());
        case HEADER_EQUALS -> responseHeaders.put(a.getHeaderName(), a.getExpectedValue());
        case HEADER_CONTAINS -> responseHeaders.put(a.getHeaderName(), "contains " + a.getExpectedValue());
        case BODY_CONTAINS -> bodyContains.add(a.getExpectedValue());
        default -> {}
      }
    }
    String opId() { return method + "_" + path.replaceAll("[^a-zA-Z0-9]+","_"); }
    String describe(int code) {
      if (!bodyContains.isEmpty()) return "Body contains: " + String.join(", ", bodyContains);
      return "Response " + code;
    }
  }

  private String pickContentType(Config cfg, Request r) {
    if (r != null && r.getHeaders() != null) {
      String ct = r.getHeaders().get("Content-Type");
      if (ct != null) return ct;
    }
    if (cfg != null && cfg.getDefaultHeaders() != null) {
      String ct = cfg.getDefaultHeaders().get("Content-Type");
      if (ct != null) return ct;
    }
    return "application/json";
  }

  private Map<String, Map<String, List<Op>>> groupByPath(Collection<Op> ops) {
    Map<String, Map<String, List<Op>>> res = new LinkedHashMap<>();
    for (Op op : ops) {
      res.computeIfAbsent(op.path, k -> new LinkedHashMap<>())
         .computeIfAbsent(op.method, k -> new ArrayList<>())
         .add(op);
    }
    return res;
  }

  private Op merge(List<Op> list) {
    Op base = list.get(0);
    for (int i=1;i<list.size();i++) {
      Op o = list.get(i);
      base.statuses.addAll(o.statuses);
      base.responseHeaders.putAll(o.responseHeaders);
      base.bodyContains.addAll(o.bodyContains);
      if (base.exampleBody == null) base.exampleBody = o.exampleBody;
    }
    return base;
  }

  private String escapePath(String p) {
    // optionally convert /api/users/$id → /api/users/{id}
    return p.replaceAll("\\$(\\w+)", "{$1}");
  }
}
