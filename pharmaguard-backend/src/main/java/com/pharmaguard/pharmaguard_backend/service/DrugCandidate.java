
package com.pharmaguard.pharmaguard_backend.service;

public class DrugCandidate {

    private String rxcui;
    private String rxaui;
    private String score;
    private String rank;
    private String name;
    private String source;

    public DrugCandidate() {
    }

    public String getRxcui() {
        return rxcui;
    }

    public void setRxcui(String rxcui) {
        this.rxcui = rxcui;
    }

    public String getRxaui() {
        return rxaui;
    }

    public void setRxaui(String rxaui) {
        this.rxaui = rxaui;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
