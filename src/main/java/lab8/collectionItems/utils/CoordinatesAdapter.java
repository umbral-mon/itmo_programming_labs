package lab8.collectionItems.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lab8.collectionItems.Coordinates;

import java.io.IOException;
import java.lang.reflect.Field;

public class CoordinatesAdapter extends TypeAdapter<Coordinates> {

    @Override
    public void write(JsonWriter out, Coordinates value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        // Используем рефлексию, так как геттеров может не быть (как в оригинале)
        try {
            Field xField = Coordinates.class.getDeclaredField("x");
            xField.setAccessible(true);
            out.name("x").value((Long) xField.get(value));

            Field yField = Coordinates.class.getDeclaredField("y");
            yField.setAccessible(true);
            out.name("y").value((Long) yField.get(value));
        } catch (Exception e) {
            throw new IOException("Ошибка сериализации Coordinates", e);
        }
        out.endObject();
    }

    @Override
    public Coordinates read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        in.beginObject();
        Long x = null;
        long y = 0L;

        while (in.hasNext()) {
            String fn = in.nextName();
            switch (fn) {
                case "x":
                    x = in.nextLong();
                    break;
                case "y":
                    y = in.nextLong();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        if (x == null || x <= -201) {
            throw new IllegalArgumentException("x mustn't be null and be greater than -201");
        }
        return new Coordinates(x, y);
    }
}