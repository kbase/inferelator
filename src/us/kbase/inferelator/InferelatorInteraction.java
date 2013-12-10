
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
 * <p>Original spec-file type: InferelatorInteraction</p>
 * <pre>
 * Represents a interaction found Inferelator
 * string cluster_id - identifier of CmonkeyCluster
 * string regulator_id - kbase id of regulatory gene
 * double coeff - coefficient
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "cluster_id",
    "regulator_id",
    "coeff"
})
public class InferelatorInteraction {

    @JsonProperty("cluster_id")
    private String clusterId;
    @JsonProperty("regulator_id")
    private String regulatorId;
    @JsonProperty("coeff")
    private Double coeff;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("cluster_id")
    public String getClusterId() {
        return clusterId;
    }

    @JsonProperty("cluster_id")
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public InferelatorInteraction withClusterId(String clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    @JsonProperty("regulator_id")
    public String getRegulatorId() {
        return regulatorId;
    }

    @JsonProperty("regulator_id")
    public void setRegulatorId(String regulatorId) {
        this.regulatorId = regulatorId;
    }

    public InferelatorInteraction withRegulatorId(String regulatorId) {
        this.regulatorId = regulatorId;
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

    public InferelatorInteraction withCoeff(Double coeff) {
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
        return ((((((((("InferelatorInteraction"+" [clusterId=")+ clusterId)+", regulatorId=")+ regulatorId)+", coeff=")+ coeff)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
