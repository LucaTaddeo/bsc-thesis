package com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer;

import android.os.Build;
import android.os.Environment;
import androidx.annotation.RequiresApi;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer.measurements.Measurement;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileWriter {
    final static String FILE_EXTENSION = ".json";
    final static File APP_DIR = new File(Environment.getExternalStorageDirectory(), "Movesense");
    private static File logsSubdirectory;
    static Map<String, JsonGenerator> generators = new HashMap<>();
    static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    static ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

    public static boolean initializeParentFolderForLogs() {
        String subdirectoryName = new Timestamp(System.currentTimeMillis()).toString().replace(":", "").split("\\.")[0];

        System.out.println(subdirectoryName);

        logsSubdirectory = new File(APP_DIR, subdirectoryName);
        if (!logsSubdirectory.exists()) return logsSubdirectory.mkdirs();
        else return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void initializeSensorsLogFiles() {
        MovesenseConnectedDevices.getConnectedDevices()
                .parallelStream()
                .forEach(movesenseDevice -> {
                    try {
                        String deviceSerial = movesenseDevice.getSerial();
                        File file = new File(logsSubdirectory, deviceSerial + FILE_EXTENSION);
                        if (!file.exists()) file.createNewFile();
                        JsonGenerator generator = mapper.getFactory().createGenerator(file, JsonEncoding.UTF8);
                        generator.writeStartArray();
                        generators.put(deviceSerial, generator);
                    } catch (IOException e) {
                        System.out.println(e);
                        throw new RuntimeException(e);
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void fakeInitializeSensors() {
        new ArrayList<>(Arrays.asList("serialA", "serialB", "serialC")).parallelStream().forEach(deviceSerial -> {
            try {
                File file = new File(logsSubdirectory, deviceSerial + FILE_EXTENSION);
                if (!file.exists()) file.createNewFile();
                JsonGenerator generator = mapper.getFactory().createGenerator(file, JsonEncoding.UTF8);
                generator.writeStartArray();
                generators.put(deviceSerial, generator);
            } catch (IOException e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        });

    }

    public static boolean writeMeasurementToJsonFile(String deviceSerial, Measurement measurement){
        return appendToFile(deviceSerial, measurement);
    }

    private static boolean appendToFile(String deviceSerial, Measurement measurement) {
        boolean operationSucceeded = false;
        try {
            if (generators.containsKey(deviceSerial)) {
                writer.writeValue(generators.get(deviceSerial), measurement);
                operationSucceeded = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return operationSucceeded;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void closeAllArrays() {
        generators.forEach((s, jsonGenerator) -> {
            try {
                jsonGenerator.writeEndArray();
                jsonGenerator.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


//    @Deprecated
//    public static JsonWriter createFile(String dataType) {
//        try {
//            //String fileName = dataType + "_" + (new Timestamp(System.currentTimeMillis())) + ".json";
//            String fileName = dataType + ".json";
//            File logFile = new File(APP_DIR, fileName);
//            java.io.FileWriter fileWriter = new java.io.FileWriter(logFile);
//            JsonWriter jsonWriter = new JsonWriter(fileWriter);
//            jsonWriter.beginObject();
//            return jsonWriter;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static File getJsonArrayFromFile(String fileNameWithoutExtension) {
//        File logFile = new File(APP_DIR, fileNameWithoutExtension + FILE_EXTENSION);
//        if (!logFile.exists()) {
//            try {
//                System.out.println("FILE CREATED!!!!");
//                new JsonWriter(new java.io.FileWriter(logFile)).beginArray();
//            } catch (IOException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
//        } else System.out.println("FILE OPENED!!!!");
//        return logFile;
//    }
//
//    public static void test(File file, String value) {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
//        try {
//            JsonGenerator g = objectMapper.getFactory().createGenerator(file, JsonEncoding.UTF8);
//            writer.writeValue(g, value);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @Deprecated
//    public static void appendToFile(Map<String, Double> dataToAppend, String deviceSerial, JsonWriter jsonWriter) {
//        String currentTimestamp = new Timestamp(System.currentTimeMillis()).toString();
//        try {
//            jsonWriter.name("currentTimestamp");
//            jsonWriter.beginObject();
//            jsonWriter.name(deviceSerial);
//            jsonWriter.beginObject();
//            dataToAppend.entrySet().forEach(entry -> {
//                try {
//                    jsonWriter.name(entry.getKey()).value(entry.getValue());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            jsonWriter.endObject();
//            jsonWriter.endObject();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Deprecated
//    public static void closeFile(JsonWriter jsonWriter, Context context) {
//        System.out.println("............. CLOSING FILE ..............");
//        try {
//            jsonWriter.endObject();
//            jsonWriter.close();
//
//            Toast.makeText(context, "Data Saved in " + APP_DIR.getCanonicalPath(), Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            Toast.makeText(context, "Data Saved in Movesense Folder", Toast.LENGTH_LONG).show();
//        }
//    }
}
