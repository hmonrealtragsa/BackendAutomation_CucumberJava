package sido2.API;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import sido2.Config;
import sido2.Context;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Acciones reutilizables que ejecutan las features.
 * Los endpoints, payloads y credenciales se referencian por alias
 * (config/*.properties) para no depender de URLs concretas.
 *
 * Convencion: los pasos devuelven "OK" / "KO" y el paso Then hace el assert,
 * de forma que la feature muestra el resultado esperado de forma explicita.
 */
public class FrameworkAPI {

    public static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIxMzA2Yjg3ZS1lYzRmLTRiYzctOWNjZS01NWRmZTU0NWMxNTYiLCJpYXQiOjE3OTA3NDc1MzIsInVuaXF1ZV9uYW1lIjoiaG1vbnJlYWwiLCJuYmYiOjE3OTA3NDc1MzIsImV4cCI6MTc5MDgzMzkzMn0.vOgfYLfeCv93dkxiBVdCnb3OSIIhyQwZLEp_8kWpOrM";
    public static final String TOKEN_USUARIO = "******";
    public static final String CUERPO = "response_body";
    public static final String RESPONSE = "response";
    public static final String RESPONSE_STATUS = "response_status";
    public static final String AUTHORIZATION_HEADER = "authorization_header";

    private static HttpResponse<String> response;

    static {
        Unirest.config()
                .verifySsl(Config.booleano("verifySsl", false))
                .connectTimeout(Config.entero("timeout", 60000))
                .socketTimeout(Config.entero("timeout", 60000));
    }

    private FrameworkAPI() {
    }

    // ------------------------------------------------------------------ auth

    public static void endpointWithBody(String api, String metodo, String json) {
        String url = resolverUrl(api);
        String body = cuerpoJson(json);
        HttpRequestWithBody peticion = Unirest.request(normalizarMetodo(metodo), url);
        String token = TOKEN;
        String authorizationHeader = construirAuthorizationHeader(token);

        peticion.header("content-type", "application/json");
        if (authorizationHeader != null) {
            peticion.header("authorization", authorizationHeader);
        }

        Context.put(AUTHORIZATION_HEADER, authorizationHeader);
        System.out.println("Authorization utilizada: "
                + (authorizationHeader == null ? "<sin token>" : authorizationHeader));

        response = peticion.body(body).asString();

        Context.put(RESPONSE, response);
        Context.put(RESPONSE_STATUS, String.valueOf(response.getStatus()));
        Context.put(CUERPO, response.getBody());
    }

    public static HttpResponse<String> getResponse() {
        return response;
    }

    public static void checkResponseEndpoint(String status, String json) {
        if (response == null) {
            throw new IllegalStateException("No hay ninguna response guardada. Ejecuta antes endpointWithBody.");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("El status esperado es obligatorio.");
        }

        Assert.assertEquals(
                "El status code no coincide. Authorization usada: " + Context.getString(AUTHORIZATION_HEADER),
                Integer.parseInt(status.trim()),
                response.getStatus());

        JsonElement bodyEsperado = new JsonParser().parse(cuerpoEsperado(json));
        JsonElement bodyReal = new JsonParser().parse(response.getBody());
        Assert.assertEquals("El body de la respuesta no coincide con el JSON esperado.", bodyEsperado, bodyReal);
    }

    public static String contiene(String texto) {
        String cuerpo = Context.getString(CUERPO);
        return cuerpo != null && cuerpo.contains(texto) ? "OK" : "KO";
    }

    private static String resolverUrl(String api) {
        if (api == null || api.trim().isEmpty()) {
            throw new IllegalArgumentException("La API a invocar es obligatoria.");
        }

        String endpoint = api.trim();
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        return Config.url(endpoint);
    }

    private static String normalizarMetodo(String metodo) {
        if (metodo == null || metodo.trim().isEmpty()) {
            throw new IllegalArgumentException("El metodo HTTP es obligatorio.");
        }

        String metodoNormalizado = metodo.trim().toUpperCase();
        switch (metodoNormalizado) {
            case "GET":
            case "POST":
            case "PUT":
            case "PATCH":
            case "DELETE":
                return metodoNormalizado;
            default:
                throw new IllegalArgumentException("Metodo HTTP no soportado: " + metodo);
        }
    }

    private static String construirAuthorizationHeader(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        String tokenNormalizado = token.trim();
        if (tokenNormalizado.regionMatches(true, 0, "bearer ", 0, "bearer ".length())) {
            return tokenNormalizado;
        }

        return "bearer " + tokenNormalizado;
    }

    private static String cuerpoJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("El body de la peticion es obligatorio.");
        }

        String body = json.trim();
        if (body.startsWith("{") || body.startsWith("[")) {
            return reemplazarMarcadores(body);
        }

        return reemplazarMarcadores(leerFichero(Config.rutaJson(body)));
    }

    private static String cuerpoEsperado(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("El JSON esperado es obligatorio.");
        }

        String body = json.trim();
        if (body.startsWith("{") || body.startsWith("[")) {
            return reemplazarMarcadores(body);
        }

        File fichero = new File(body);
        if (fichero.exists() && fichero.isFile()) {
            return reemplazarMarcadores(leerFichero(fichero.getPath()));
        }

        return reemplazarMarcadores(leerFichero(Config.rutaJson(body)));
    }

    private static String leerFichero(String ruta) {
        StringBuilder contenido = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                contenido.append(linea).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el fichero JSON " + ruta + ".", e);
        }

        return contenido.toString().trim();
    }

    private static String reemplazarMarcadores(String json) {
        JsonElement elemento = new JsonParser().parse(json);
        reemplazarMarcadores(elemento);
        return elemento.toString();
    }

    private static void reemplazarMarcadores(JsonElement elemento) {
        if (elemento == null || elemento.isJsonNull()) {
            return;
        }

        if (elemento.isJsonObject()) {
            JsonObject objeto = elemento.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : objeto.entrySet()) {
                JsonElement valor = entry.getValue();
                if (valor.isJsonPrimitive() && valor.getAsJsonPrimitive().isString()) {
                    String texto = valor.getAsString();
                    if (texto.startsWith("{{") && texto.endsWith("}}")) {
                        String clave = texto.substring(2, texto.length() - 2).trim();
                        String reemplazo = Context.getString(clave);
                        if (reemplazo == null) {
                            throw new IllegalStateException(
                                    "No existe el valor '" + clave + "' en el contexto para construir el body.");
                        }
                        entry.setValue(new JsonPrimitive(reemplazo));
                    }
                } else {
                    reemplazarMarcadores(valor);
                }
            }
            return;
        }

        if (elemento.isJsonArray()) {
            for (JsonElement item : elemento.getAsJsonArray()) {
                reemplazarMarcadores(item);
            }
        }
    }
}
