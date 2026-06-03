package lab8.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lab8.collectionItems.SpaceMarine;
import lab8.collectionItems.utils.IdGenerator;
import lab8.collectionItems.utils.SpaceMarineAdapter;

import java.io.IOException;
import java.time.LocalDate;

public class MyGsonFactory {

    public static Gson get(){
        return new GsonBuilder()
                .registerTypeAdapter(
                        LocalDate.class,
                        new TypeAdapter<LocalDate>(){

                            @Override
                            public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
                                jsonWriter.value(localDate.toString());
                            }

                            @Override
                            public LocalDate read(JsonReader jsonReader) throws IOException {
                                return LocalDate.parse(jsonReader.nextString());
                            }
                        }
                )
                .registerTypeAdapter(SpaceMarine.class, new SpaceMarineAdapter(IdGenerator.getInstance())).create();
    }

}
