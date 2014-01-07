
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
 * string source_id - id of source genome
 * string description
 * list <gene_id> genes; - list of gene ids 
 * @optional description
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "source_id",
    "description",
    "genes"
})
public class GeneList {

    @JsonProperty("id")
    private java.lang.String id;
    @JsonProperty("source_id")
    private java.lang.String sourceId;
    @JsonProperty("description")
    private java.lang.String description;
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

    @JsonProperty("source_id")
    public java.lang.String getSourceId() {
        return sourceId;
    }

    @JsonProperty("source_id")
    public void setSourceId(java.lang.String sourceId) {
        this.sourceId = sourceId;
    }

    public GeneList withSourceId(java.lang.String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    @JsonProperty("description")
    public java.lang.String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(java.lang.String description) {
        this.description = description;
    }

    public GeneList withDescription(java.lang.String description) {
        this.description = description;
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
        return ((((((((((("GeneList"+" [id=")+ id)+", sourceId=")+ sourceId)+", description=")+ description)+", genes=")+ genes)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
