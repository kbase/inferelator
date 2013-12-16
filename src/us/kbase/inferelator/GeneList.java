
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
 * <p>Original spec-file type: GeneList</p>
 * <pre>
 * Represents a list of gene ids
 * string id - workspace name of the list
 * list <gene_id> genes; - list of gene ids
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "genes"
})
public class GeneList {

    @JsonProperty("id")
    private java.lang.String id;
    @JsonProperty("genes")
    private List<String> genes;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("id")
    public java.lang.String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(java.lang.String id) {
        this.id = id;
    }

    public GeneList withId(java.lang.String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("genes")
    public List<String> getGenes() {
        return genes;
    }

    @JsonProperty("genes")
    public void setGenes(List<String> genes) {
        this.genes = genes;
    }

    public GeneList withGenes(List<String> genes) {
        this.genes = genes;
        return this;
    }

    @JsonAnyGetter
    public Map<java.lang.String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(java.lang.String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public java.lang.String toString() {
        return ((((((("GeneList"+" [id=")+ id)+", genes=")+ genes)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
