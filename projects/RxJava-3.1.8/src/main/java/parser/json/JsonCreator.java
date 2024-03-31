package parser.json;

import com.google.gson.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a json file from JsonMethodItem's data.
 * Use add() to add JsonMethodItem's to export. Multiple can be added.
 * Then use createJson() to export the json file.
 *
 * @author Sam Salek
 */
public class JsonCreator {

    private final Map<String, JsonMethodItem> jsonMethodItemMap = new HashMap<>();
    private final String filePath;

    /**
     * Creates a new JsonCreator.
     *
     * @param filePath Where the exported json file should be saved.
     */
    public JsonCreator(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Add ParsedMethod's to this JsonCreator. Their data will be in the exported json file.
     *
     * @param jsonMethodItems one or more ParsedMethod instances.
     */
    public void add(JsonMethodItem... jsonMethodItems) {
        Arrays.stream(jsonMethodItems).forEach(this::add);
    }

    private void add(JsonMethodItem jsonMethodItem) {
        String filePath = jsonMethodItem.getFilePath().replace("\\", ".");
        String methodName = jsonMethodItem.getMethodName();
        String key = filePath.substring(0, filePath.indexOf(".java")).trim() + "." + methodName;
        jsonMethodItemMap.put(key, jsonMethodItem);
    }

    /**
     * Creates and exports a json file containing the data of
     * all JsonMethodItem instances that were added to this JsonCreator.
     */
    public void createJson() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
            BigDecimal value = BigDecimal.valueOf(src);
            try {
                value = new BigDecimal(value.toBigIntegerExact());
            }
            catch (ArithmeticException e) {
                // Ignore.
            }
            return new JsonPrimitive(value);
        });

        Gson gson = gsonBuilder.setPrettyPrinting().create();

        try {
            Writer writer = new FileWriter(new File(filePath).getAbsoluteFile());
            gson.toJson(jsonMethodItemMap, writer);
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
