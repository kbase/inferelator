
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
 * string gene_list_id - id of transcription factor ids list
 * string cmonkey_run_result_id - id of cMonkey run result
 * string expression_data_series_id - id of expression data series
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "gene_list_id",
    "cmonkey_run_result_id",
    "expression_data_series_id"
})
public class InferelatorRunParameters {

    @JsonProperty("gene_list_id")
    private String geneListId;
    @JsonProperty("cmonkey_run_result_id")
    private String cmonkeyRunResultId;
    @JsonProperty("expression_data_series_id")
    private String expressionDataSeriesId;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("gene_list_id")
    public String getGeneListId() {
        return geneListId;
    }

    @JsonProperty("gene_list_id")
    public void setGeneListId(String geneListId) {
        this.geneListId = geneListId;
    }

    public InferelatorRunParameters withGeneListId(String geneListId) {
        this.geneListId = geneListId;
        return this;
    }

    @JsonProperty("cmonkey_run_result_id")
    public String getCmonkeyRunResultId() {
        return cmonkeyRunResultId;
    }

    @JsonProperty("cmonkey_run_result_id")
    public void setCmonkeyRunResultId(String cmonkeyRunResultId) {
        this.cmonkeyRunResultId = cmonkeyRunResultId;
    }

    public InferelatorRunParameters withCmonkeyRunResultId(String cmonkeyRunResultId) {
        this.cmonkeyRunResultId = cmonkeyRunResultId;
        return this;
    }

    @JsonProperty("expression_data_series_id")
    public String getExpressionDataSeriesId() {
        return expressionDataSeriesId;
    }

    @JsonProperty("expression_data_series_id")
    public void setExpressionDataSeriesId(String expressionDataSeriesId) {
        this.expressionDataSeriesId = expressionDataSeriesId;
    }

    public InferelatorRunParameters withExpressionDataSeriesId(String expressionDataSeriesId) {
        this.expressionDataSeriesId = expressionDataSeriesId;
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
        return ((((((((("InferelatorRunParameters"+" [geneListId=")+ geneListId)+", cmonkeyRunResultId=")+ cmonkeyRunResultId)+", expressionDataSeriesId=")+ expressionDataSeriesId)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
