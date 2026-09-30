package sido2;

/**
 * Estado compartido entre los pasos (Stepdefs) y la capa de API (FrameworkAPI).
 * Permite pasar datos de un paso a otro: token, codigoSolicitud, nombre del PDF...
 */
public class Context {

    private static final ThreadLocal<java.util.Map<String, Object>> DATA =
            ThreadLocal.withInitial(java.util.HashMap::new);

    private Context() {
    }

    public static Object get(String clave) {
        return DATA.get().get(clave);
    }

    public static String getString(String clave) {
        Object valor = get(clave);
        return valor == null ? null : valor.toString();
    }

    public static void put(String clave, Object valor) {
        DATA.get().put(clave, valor);
    }

    public static void putIfAbsent(String clave, Object valor) {
        DATA.get().putIfAbsent(clave, valor);
    }

    public static boolean contains(String clave) {
        return DATA.get().containsKey(clave);
    }

    public static void clear() {
        DATA.get().clear();
    }
}
