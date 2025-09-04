package org.fiap.updown.util;

public final class EntityUtils {

    private EntityUtils() {
    }

    public static String getBaseNameFromEntity(String fullClassName) {
        int lastDotIndex = fullClassName.lastIndexOf('.');
        String simpleName = fullClassName.substring(lastDotIndex + 1);

        return simpleName.replace("Entity", "");
    }
}
