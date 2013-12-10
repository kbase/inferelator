
package us.kbase.inferelator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: InferelatorRunResult</p>
 * <pre>
 * Represents data from a single run of Inferelator
 * string id - identifier of cMonkey run
 * string cmonkey_run_result_id - kbase id of input CmonkeyRunResult
 * string series_id - kbase id of expression data series
 * string organism - organism name
 * list <InferelatorInteraction> interactions
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "cmonkey_run_result_id",
    "series_id",
    "organism",
    "interactions"
})
public class InferelatorRunResult {

    @JsonProperty("id")
    private String id;
    @JsonProperty("cmonkey_run_result_id")
    private String cmonkeyRunResultId;
    @JsonProperty("series_id")
    private String seriesId;
    @JsonProperty("organism")
    private String organism;
    @JsonProperty("interactions")
    private List<InferelatorInteraction> interactions;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public InferelatorRunResult withId(String id) {
        this.id = id;
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

    public InferelatorRunResult withCmonkeyRunResultId(String cmonkeyRunResultId) {
        this.cmonkeyRunResultId = cmonkeyRunResultId;
        return this;
    }

    @JsonProperty("series_id")
    public String getSeriesId() {
        return seriesId;
    }

    @JsonProperty("series_id")
    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public InferelatorRunResult withSeriesId(String seriesId) {
        this.seriesId = seriesId;
        return this;
    }

    @JsonProperty("organism")
    public String getOrganism() {
        return organism;
    }

    @JsonProperty("organism")
    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public InferelatorRunResult withOrganism(String organism) {
        this.organism = organism;
        return this;
    }

    @JsonProperty("interactions")
    public List<InferelatorInteraction> getInteractions() {
        return interactions;
    }

    @JsonProperty("interactions")
    public void setInteractions(List<InferelatorInteraction> interactions) {
        this.interactions = interactions;
    }

    public InferelatorRunResult withInteractions(List<InferelatorInteraction> interactions) {
        this.interactions = interactions;
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
        return ((((((((((((("InferelatorRunResult"+" [id=")+ id)+", cmonkeyRunResultId=")+ cmonkeyRunResultId)+", seriesId=")+ seriesId)+", organism=")+ organism)+", interactions=")+ interactions)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
