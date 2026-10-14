package com.pharmaguard.pharmaguard_backend.service;

import java.util.List;

public class DrugSearchResult {

    private String searchedName;
    private List<DrugCandidate> candidates;

    public DrugSearchResult() {
    }

    public DrugSearchResult(
            String searchedName,
            List<DrugCandidate> candidates) {
        this.searchedName = searchedName;
        this.candidates = candidates;
    }

    public String getSearchedName() {
        return searchedName;
    }

    public void setSearchedName(String searchedName) {
        this.searchedName = searchedName;
    }

    public List<DrugCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<DrugCandidate> candidates) {
        this.candidates = candidates;
    }
}