package lab8.collectionItems.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lab8.collectionItems.Chapter;

import java.io.IOException;
import java.lang.reflect.Field;

public class ChapterAdapter extends TypeAdapter<Chapter> {

    @Override
    public void write(JsonWriter out, Chapter value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        try {
            Field nameField = Chapter.class.getDeclaredField("name");
            nameField.setAccessible(true);
            out.name("name").value((String) nameField.get(value));

            Field parentField = Chapter.class.getDeclaredField("parentLegion");
            parentField.setAccessible(true);
            out.name("parentLegion").value((String) parentField.get(value));

            Field countField = Chapter.class.getDeclaredField("marinesCount");
            countField.setAccessible(true);
            Integer count = (Integer) countField.get(value);
            out.name("marinesCount");
            if (count == null) {
                out.nullValue();
            } else {
                out.value(count);
            }
        } catch (Exception e) {
            throw new IOException("Ошибка сериализации Chapter", e);
        }
        out.endObject();
    }

    @Override
    public Chapter read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        in.beginObject();
        String name = null;
        String parentLegion = null;
        Integer marinesCount = null;

        while (in.hasNext()) {
            String fn = in.nextName();
            switch (fn) {
                case "name":
                    name = in.nextString();
                    break;
                case "parentLegion":
                    if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        parentLegion = in.nextString();
                    }
                    break;
                case "marinesCount":
                    if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        marinesCount = in.nextInt();
                    }
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name mustn't be empty");
        }
        if (marinesCount != null && (marinesCount < 0 || marinesCount > 1000)) {
            throw new IllegalArgumentException("marines count must be in range [0..1000] or null");
        }

        return new Chapter(name, parentLegion, marinesCount);
    }
}