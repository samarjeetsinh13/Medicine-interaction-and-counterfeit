
package com.pharmaguard.pharmaguard_backend.illness;

import java.util.List;

public class IllnessRequest {

    private List<Illness> illnesses;

    public IllnessRequest() {
    }

    public List<Illness> getIllnesses() {
        return illnesses;
    }

    public void setIllnesses(List<Illness> illnesses) {
        this.illnesses = illnesses;
    }
}
