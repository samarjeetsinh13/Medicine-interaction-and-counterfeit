
package com.pharmaguard.pharmaguard_backend.medicine;

import java.util.List;

public class MedicineRequest {

    private List<Medicine> medicines;

    public MedicineRequest() {
    }

    public List<Medicine> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
    }
}
