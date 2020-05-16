package org.schabi.newpipe.extractor.utils;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IOUtils {
    public static void createFile(String path, String content) throws IOException {
        String[] dirs = path.split("/");
        if (dirs.length > 1) {
            String pathWithoutFileName = path.replace(dirs[dirs.length - 1], "");
            if (!Files.exists(Paths.get(pathWithoutFileName))) { //create dirs if they don't exist
                new File(pathWithoutFileName).mkdirs();
            }
        }
        writeFile(path, content);
    }

    private static void writeFile(String path, String content) throws IOException {
        //lower lever createFile. Doesn't create dirs
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(content);
        writer.flush();
        writer.close();
    }

    public static String jsonObjToString(JsonObject object) {
        return JsonWriter.string(object);
    }

    public static void createFile(String path, JsonObject content) throws IOException {
        createFile(path, jsonObjToString(content));
    }

    public static String jsonArrayToString(JsonArray array) {
        return JsonWriter.string(array);
    }

    public static void createFile(String path, JsonArray array) throws IOException {
        createFile(path, jsonArrayToString(array));
    }
}
