package br.com.integranotas.sdk.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitário nativo para serialização e manipulação básica de JSON sem dependências externas.
 */
public final class JsonUtils {

    private static final Pattern CHAVE_PATTERN = Pattern.compile("\"chave\"\\s*:\\s*\"([^\"]+)\"");

    private JsonUtils() {
    }

    /**
     * Converte um objeto (Map, Collection, String, Number, Boolean) em representação JSON.
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            String str = (String) obj;
            // Se já for uma string formatada em JSON, retorna como está
            if (isValidJson(str)) {
                return str;
            }
            return "\"" + escape(str) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> entry = it.next();
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection<?> coll) {
            StringBuilder sb = new StringBuilder("[");
            Iterator<?> it = coll.iterator();
            while (it.hasNext()) {
                sb.append(toJson(it.next()));
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escape(obj.toString()) + "\"";
    }

    /**
     * Verifica de forma estrita se a string representa um objeto ou array JSON válido.
     */
    public static boolean isValidJson(String str) {
        if (str == null) {
            return false;
        }
        String trimmed = str.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    /**
     * Extrai a propriedade "chave" a partir de um Map ou String JSON.
     */
    public static String extractChave(Object payload) {
        if (payload == null) {
            return "";
        }
        if (payload instanceof Map<?, ?> map) {
            Object val = map.get("chave");
            return val != null ? val.toString() : "";
        }
        if (payload instanceof String str) {
            Matcher matcher = CHAVE_PATTERN.matcher(str);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch <= '\u001F') {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }
}
