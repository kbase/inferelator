
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
 * string id - identifier of Inferelator run result
 * string organism - organism name
 * InferelatorRunParameters params - run parameters
 * list <InferelatorHit> hits - list of hits
 * @optional organism hits
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "organism",
    "params",
    "hits"
})
public class InferelatorRunResult {

    @JsonProperty("id")
    private String id;
    @JsonProperty("organism")
    private String organism;
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
    @JsonProperty("params")
    private InferelatorRunParameters params;
    @JsonProperty("hits")
    private List<InferelatorHit> hits;
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
    @JsonProperty("params")
    public InferelatorRunParameters getParams() {
        return params;
    }

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
    @JsonProperty("params")
    public void setParams(InferelatorRunParameters params) {
        this.params = params;
    }

    public InferelatorRunResult withParams(InferelatorRunParameters params) {
        this.params = params;
        return this;
    }

    @JsonProperty("hits")
    public List<InferelatorHit> getHits() {
        return hits;
    }

    @JsonProperty("hits")
    public void setHits(List<InferelatorHit> hits) {
        this.hits = hits;
    }

    public InferelatorRunResult withHits(List<InferelatorHit> hits) {
        this.hits = hits;
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
        return ((((((((((("InferelatorRunResult"+" [id=")+ id)+", organism=")+ organism)+", params=")+ params)+", hits=")+ hits)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
