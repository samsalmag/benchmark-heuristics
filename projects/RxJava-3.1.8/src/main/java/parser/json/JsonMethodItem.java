package parser.json;

import com.google.gson.annotations.SerializedName;
import parser.ParsedMethod;

/**
 * A method item in a json file. Correctly structures the data from a ParsedMethod to be used in a json file.
 *
 * @author Sam Salek
 */
public class JsonMethodItem {

    private final String filePath;
    private final String methodName;

    @SerializedName("stats")
    private final ParsedMethod parsedMethod;

    // Use of class Double to avoid scientific notation in serialized file.
    private Double stabilityMetricValue;
    private Double codeCoverageMetricValue;

    public JsonMethodItem(ParsedMethod parsedMethod) {
        this.parsedMethod = parsedMethod;
        this.filePath = parsedMethod.getFilePath();
        this.methodName = parsedMethod.getMethodName();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setStabilityMetricValue(Double stabilityMetricValue) {
        this.stabilityMetricValue = stabilityMetricValue;
    }

    public void setCodeCoverageMetricValue(Double codeCoverageMetricValue) {
        this.codeCoverageMetricValue = codeCoverageMetricValue;
    }
}
