package org.openmrs.module.ugandaemrreports.common;

import java.util.HashMap;

/**
 */
public class SubstituteOrSwitch {
    private int obsGroupId;
    private HashMap<Integer, Integer> obs = new HashMap<Integer, Integer>();

    public SubstituteOrSwitch(int obsGroupId, Integer obsId, Integer conceptId) {
        this.obsGroupId = obsGroupId;
        this.add(obsId, conceptId);
    }

    public int getObsGroupId() {
        return obsGroupId;
    }

    public void setObsGroupId(int obsGroupId) {
        this.obsGroupId = obsGroupId;
    }

    public HashMap<Integer, Integer> getObs() {
        return obs;
    }

    public void setObs(HashMap<Integer, Integer> obs) {
        this.obs = obs;
    }

    public void add(Integer obsId, Integer conceptId) {
        HashMap<Integer, Integer> old = getObs();
        old.put(obsId, conceptId);
        setObs(old);
    }
}
