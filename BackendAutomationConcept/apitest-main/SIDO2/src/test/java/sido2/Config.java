package sido2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carga unica de la configuracion (config/*.properties).
 * Las rutas son relativas a la raiz del proyecto, por lo que las pruebas
 * deben ejecutarse desde ahi (mvn test / mvn verify).
 */
public class Config {

    private static final String CONFIG = "config/config.properties";
    private static final String AUTH = "config/auth.properties";
    private static final String JSONS = "config/jsons.properties";
    private static final String VARIABLES = "config/variables.properties";

    private static final Properties config = cargar(CONFIG, true);
    private static final Properties auth = cargar(AUTH, false);
    private static final Properties jsons = cargar(JSONS, true);
    private static final Properties variables = cargar(VARIABLES, false);

    private Config() {
    }

    /** Carga un properties. Si no es obligatorio y no existe, se deja vacio. */
    private static Properties cargar(String ruta, boolean obligatorio) {
        Properties properties = new Properties();
        try (InputStream entrada = new FileInputStream(ruta)) {
            properties.load(entrada);
        } catch (FileNotFoundException e) {
            if (obligatorio) {
                throw new IllegalStateException(
                        "No se encontro " + ruta + ". Ejecuta las pruebas desde la raiz del proyecto.", e);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ruta + ".", e);
        }
        return properties;
    }

    /** URL real de un alias de endpoint definido en config.properties. */
    public static String url(String alias) {
        return obligatorio(config, alias, CONFIG);
    }

    /** Valor de un parametro de config.properties, o null si no existe. */
    public static String parametro(String clave) {
        return vacioANull(config.getProperty(clave));
    }

    /** Credencial de auth.properties; si esta vacia, usa la variable de entorno homonima en mayusculas. */
    public static String credencial(String clave) {
        String valor = vacioANull(auth.getProperty(clave));
        return valor != null ? valor : vacioANull(System.getenv(clave.toUpperCase()));
    }

    /** Ruta del fichero JSON asociado a un alias de jsons.properties. */
    public static String rutaJson(String alias) {
        return obligatorio(jsons, alias, JSONS);
    }

    /** Texto esperado asociado a un alias de variables.properties. */
    public static String textoEsperado(String alias) {
        return obligatorio(variables, alias, VARIABLES);
    }

    public static boolean booleano(String clave, boolean porDefecto) {
        String valor = parametro(clave);
        return valor == null ? porDefecto : Boolean.parseBoolean(valor);
    }

    public static int entero(String clave, int porDefecto) {
        String valor = parametro(clave);
        return valor == null ? porDefecto : Integer.parseInt(valor.trim());
    }

    private static String obligatorio(Properties properties, String clave, String origen) {
        String valor = vacioANull(properties.getProperty(clave));
        if (valor == null) {
            throw new IllegalStateException(
                    "Falta '" + clave + "' en " + origen + ". Revisa la configuracion de este entorno.");
        }
        return valor;
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }
}
