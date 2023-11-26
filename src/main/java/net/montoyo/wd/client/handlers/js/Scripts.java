package net.montoyo.wd.client.handlers.js;

import net.montoyo.wd.client.handlers.DisplayHandler;

import java.io.InputStream;
import java.lang.reflect.Field;

public class Scripts {
    private static int index = 1;

    @FileName("assets/webdisplays/js/pointer_lock.js")
    public static final String POINTER_LOCK = get();
    @FileName("assets/webdisplays/js/mouse_event.js")
    public static final String MOUSE_EVENT = get();

    private static String get() {
        Field field = Scripts.class.getDeclaredFields()[index++];
        FileName name = field.getAnnotation(FileName.class);

        String text;
        try {
            InputStream is = DisplayHandler.class.getClassLoader().getResourceAsStream(name.value());
            text = new String(is.readAllBytes());
            is.close();
        } catch (Throwable err) {
            throw new RuntimeException(err);
        }

        return text;
    }
}
