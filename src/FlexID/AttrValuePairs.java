package FlexID;

import java.util.Hashtable;

public class AttrValuePairs {
    int numberOfAVPs;
    Hashtable<String, Value> table;

    public AttrValuePairs() {
        numberOfAVPs = 0;
        table = new Hashtable<String, Value>();
    }

    public void addAttrValuePair(String attr, Value value) {
        table.put(attr, value);
    }

    public Value getValueByAttr(String attr) {
        return table.get(attr);
    }

    public int getNumberOfAVPs() {
        return numberOfAVPs;
    }

    public Hashtable<String, Value> getTable() {
        return table;
    }
}
