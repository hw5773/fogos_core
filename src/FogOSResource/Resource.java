package FogOSResource;

import FlexID.FlexID;

public abstract class Resource implements FogOSResourceAPI {
    private String name;        // The name of the resource
    private String max;         // The maximum value of the resource
    private String curr;        // The current value of the resource
    private String unit;        // The unit of the value
    private boolean onDemand;   // The flag whether to monitor the resource on-demand or not
    private FlexID flexID;

    // Monitor the current status of this resource;
    public abstract void monitorResource();

    public Resource(String name, String max, String unit, boolean onDemand) {
        this.name = name;
        this.max = max;
        this.curr = max;
        this.unit = unit;
        this.onDemand = onDemand;
    }

    public String getCurr() {
        return curr;
    }

    public boolean isOnDemand() {
        return onDemand;
    }

    public String getName() {
        return name;
    }

    public String getMax() {
        return max;
    }

    public String getUnit() {
        return unit;
    }
    
    public void setMax(String newmax) {
    	this.max = newmax;
    }
    
    public void setCurr(String newcur) {
    	this.curr = newcur;
    }
}
