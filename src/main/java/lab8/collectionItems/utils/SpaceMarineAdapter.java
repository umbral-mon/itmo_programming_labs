package lab8.collectionItems.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lab8.collectionItems.*;

import java.io.IOException;
import java.time.LocalDate;

public class SpaceMarineAdapter extends TypeAdapter<SpaceMarine> {

    private final IdGenerator idGenerator;
    private final CoordinatesAdapter coordinatesAdapter;
    private final ChapterAdapter chapterAdapter;

    public SpaceMarineAdapter(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        this.coordinatesAdapter = new CoordinatesAdapter();
        this.chapterAdapter = new ChapterAdapter();
    }

    @Override
    public void write(JsonWriter out, SpaceMarine value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("id").value(value.getID());
        out.name("name").value(value.getName());

        out.name("coordinates");
        coordinatesAdapter.write(out, value.getCoordinates());

        out.name("creationDate").value(value.getCreationDate().toString());
        out.name("health").value(value.getHealth());

        out.name("category");
        if (value.getCategory() == null) {
            out.nullValue();
        } else {
            out.value(value.getCategory().name());
        }

        out.name("weaponType").value(value.getWeaponType().name());

        out.name("meleeWeapon");
        if (value.getMeleeWeapon() == null) {
            out.nullValue();
        } else {
            out.value(value.getMeleeWeapon().name());
        }

        out.name("chapter");
        chapterAdapter.write(out, value.getChapter());

        out.name("owner").value(value.getOwner());

        out.endObject();
    }

    @Override
    public SpaceMarine read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        in.beginObject();

        Integer id = null;
        String name = null;
        Coordinates coordinates = null;
        LocalDate creationDate = null;
        Double health = null;
        AstartesCategory category = null;
        Weapon weaponType = null;
        MeleeWeapon meleeWeapon = null;
        Chapter chapter = null;
        String owner = null;

        while (in.hasNext()) {
            String fieldName = in.nextName();
            switch (fieldName) {
                case "id":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        id = in.nextInt();
                    }
                    break;
                case "name":
                    name = in.nextString();
                    break;
                case "coordinates":
                    coordinates = coordinatesAdapter.read(in);
                    break;
                case "creationDate":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        creationDate = LocalDate.parse(in.nextString());
                    }
                    break;
                case "health":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        health = in.nextDouble();
                    }
                    break;
                case "category":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        category = AstartesCategory.valueOf(in.nextString());
                    }
                    break;
                case "weaponType":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        weaponType = Weapon.valueOf(in.nextString());
                    }
                    break;
                case "meleeWeapon":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        meleeWeapon = MeleeWeapon.valueOf(in.nextString());
                    }
                    break;
                case "chapter":
                    chapter = chapterAdapter.read(in);
                    break;
                case "owner":
                    owner = in.nextString();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        Integer finalId = id;
        if (finalId == null) {
            finalId = idGenerator.generateId();
        } else {
            idGenerator.registerExistingId(finalId);
        }

        return new SpaceMarine(
                finalId,
                name,
                coordinates,
                creationDate,
                health,
                category,
                weaponType,
                meleeWeapon,
                chapter,
                owner
        );
    }
}