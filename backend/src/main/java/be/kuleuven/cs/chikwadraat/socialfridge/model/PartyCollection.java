package be.kuleuven.cs.chikwadraat.socialfridge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Party collection. Used as endpoint entity.
 */
public class PartyCollection {

    private List<Party> list;

    public PartyCollection() {
        this.list = new ArrayList<Party>();
    }

    public PartyCollection(List<Party> list) {
        this.list = list;
    }

    public List<Party> getList() {
        return list;
    }

    public void setList(List<Party> list) {
        this.list = list;
    }

}
