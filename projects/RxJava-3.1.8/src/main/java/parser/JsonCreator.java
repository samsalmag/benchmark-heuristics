package parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a json file from ParsedMethod's data.
 * Use add() to add ParsedMethod's to export. Multiple can be added.
 * Then use createJson() to export the json file.
 *
 * @author Sam Salek
 */
public class JsonCreator {

    private final Map<String, ParsedMethod> parsedMethodMap = new HashMap<>();
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
     * @param parsedMethods one or more ParsedMethod instances.
     */
    public void add(ParsedMethod... parsedMethods) {
        Arrays.stream(parsedMethods).forEach(this::add);
    }


    private void add(ParsedMethod parsedMethod) {
        String filePath = parsedMethod.getFilePath().replace("\\", ".");
        String methodName = parsedMethod.getMethodName();
        String key = filePath.substring(0, filePath.indexOf(".java")).trim() + "." + methodName;
        parsedMethodMap.put(key, parsedMethod);
    }

    /**
     * Creates and exports a json file containing the data of
     * all ParsedMethod instances that were added to this JsonCreator.
     */
    public void createJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Writer writer = new FileWriter(filePath);
            gson.toJson(parsedMethodMap, writer);
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
