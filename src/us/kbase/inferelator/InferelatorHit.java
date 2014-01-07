
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
 * <p>Original spec-file type: InferelatorHit</p>
 * <pre>
 * Represents a single interaction found by Inferelator
 * gene_id tf_id - id of regulatory gene
 * string bicluster_id - id of bicluster
 * double coeff - coefficient
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "tf_id",
    "bicluster_id",
    "coeff"
})
public class InferelatorHit {

    @JsonProperty("tf_id")
    private String tfId;
    @JsonProperty("bicluster_id")
    private String biclusterId;
    @JsonProperty("coeff")
    private Double coeff;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("tf_id")
    public String getTfId() {
        return tfId;
    }

    @JsonProperty("tf_id")
    public void setTfId(String tfId) {
        this.tfId = tfId;
    }

    public InferelatorHit withTfId(String tfId) {
        this.tfId = tfId;
        return this;
    }

    @JsonProperty("bicluster_id")
    public String getBiclusterId() {
        return biclusterId;
    }

    @JsonProperty("bicluster_id")
    public void setBiclusterId(String biclusterId) {
        this.biclusterId = biclusterId;
    }

    public InferelatorHit withBiclusterId(String biclusterId) {
        this.biclusterId = biclusterId;
        return this;
    }

    @JsonProperty("coeff")
    public Double getCoeff() {
        return coeff;
    }

    @JsonProperty("coeff")
    public void setCoeff(Double coeff) {
        this.coeff = coeff;
    }

    public InferelatorHit withCoeff(Double coeff) {
        this.coeff = coeff;
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
        return ((((((((("InferelatorHit"+" [tfId=")+ tfId)+", biclusterId=")+ biclusterId)+", coeff=")+ coeff)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
