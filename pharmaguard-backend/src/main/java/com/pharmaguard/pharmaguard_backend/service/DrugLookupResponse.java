
package com.pharmaguard.pharmaguard_backend.service;

import java.util.List;

public class DrugLookupResponse {

    private ApproximateGroup approximateGroup;

    public DrugLookupResponse() {
    }

    public ApproximateGroup getApproximateGroup() {
        return approximateGroup;
    }

    public void setApproximateGroup(
            ApproximateGroup approximateGroup) {
        this.approximateGroup = approximateGroup;
    }

    public static class ApproximateGroup {

        private String inputTerm;
        private List<DrugCandidate> candidate;

        public ApproximateGroup() {
        }

        public String getInputTerm() {
            return inputTerm;
        }

        public void setInputTerm(String inputTerm) {
            this.inputTerm = inputTerm;
        }

        public List<DrugCandidate> getCandidate() {
            return candidate;
        }

        public void setCandidate(List<DrugCandidate> candidate) {
            this.candidate = candidate;
        }
    }
}
