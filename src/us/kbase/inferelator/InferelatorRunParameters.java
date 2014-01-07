
package us.kbase.inferelator;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: InferelatorRunParameters</p>
 * <pre>
 * Contains parameters of Inferelator run
 * gene_list_ref tf_list_ws_ref - ref to transcription factor ids list
 * cmonkey_run_result_ref cmonkey_run_result_ws_ref - ref to cMonkey run result
 * expression_series_ref expression_series_ws_ref - ref to expression data series
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "tf_list_ws_ref",
    "cmonkey_run_result_ws_ref",
    "expression_series_ws_ref"
})
public class InferelatorRunParameters {

    @JsonProperty("tf_list_ws_ref")
    private String tfListWsRef;
    @JsonProperty("cmonkey_run_result_ws_ref")
    private String cmonkeyRunResultWsRef;
    @JsonProperty("expression_series_ws_ref")
    private String expressionSeriesWsRef;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("tf_list_ws_ref")
    public String getTfListWsRef() {
        return tfListWsRef;
    }

    @JsonProperty("tf_list_ws_ref")
    public void setTfListWsRef(String tfListWsRef) {
        this.tfListWsRef = tfListWsRef;
    }

    public InferelatorRunParameters withTfListWsRef(String tfListWsRef) {
        this.tfListWsRef = tfListWsRef;
        return this;
    }

    @JsonProperty("cmonkey_run_result_ws_ref")
    public String getCmonkeyRunResultWsRef() {
        return cmonkeyRunResultWsRef;
    }

    @JsonProperty("cmonkey_run_result_ws_ref")
    public void setCmonkeyRunResultWsRef(String cmonkeyRunResultWsRef) {
        this.cmonkeyRunResultWsRef = cmonkeyRunResultWsRef;
    }

    public InferelatorRunParameters withCmonkeyRunResultWsRef(String cmonkeyRunResultWsRef) {
        this.cmonkeyRunResultWsRef = cmonkeyRunResultWsRef;
        return this;
    }

    @JsonProperty("expression_series_ws_ref")
    public String getExpressionSeriesWsRef() {
        return expressionSeriesWsRef;
    }

    @JsonProperty("expression_series_ws_ref")
    public void setExpressionSeriesWsRef(String expressionSeriesWsRef) {
        this.expressionSeriesWsRef = expressionSeriesWsRef;
    }

    public InferelatorRunParameters withExpressionSeriesWsRef(String expressionSeriesWsRef) {
        this.expressionSeriesWsRef = expressionSeriesWsRef;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return ((((((((("InferelatorRunParameters"+" [tfListWsRef=")+ tfListWsRef)+", cmonkeyRunResultWsRef=")+ cmonkeyRunResultWsRef)+", expressionSeriesWsRef=")+ expressionSeriesWsRef)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
