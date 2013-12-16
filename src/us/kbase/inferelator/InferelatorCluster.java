
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
 * <p>Original spec-file type: InferelatorCluster</p>
 * <pre>
 * Represents a interaction found Inferelator
 * string id - identifier of InferelatorCluster
 * list <InferelatorInteraction> interactions
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "interactions"
})
public class InferelatorCluster {

    @JsonProperty("id")
    private String id;
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

    public InferelatorCluster withId(String id) {
        this.id = id;
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

    public InferelatorCluster withInteractions(List<InferelatorInteraction> interactions) {
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
        return ((((((("InferelatorCluster"+" [id=")+ id)+", interactions=")+ interactions)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
