package FlexID;

import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Value {
    private final String TAG = "FogOSValue";
    private String value;
    private String unit;

    public Value(String value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    @Override
    public String toString() {
        JSONObject ret = new JSONObject();
        try {
            ret.put("value", this.value);
            ret.put("unit", this.unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
