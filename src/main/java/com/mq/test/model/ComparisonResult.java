package com.mq.test.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class to hold comparison results
 */
public class ComparisonResult {
    private boolean passed;
    private String message;
    private List<String> differences;
    
    public ComparisonResult(boolean passed, String message) {
        this.passed = passed;
        this.message = message;
        this.differences = new ArrayList<>();
    }
    
    public void addDifference(String diff) {
        differences.add(diff);
    }
    
    public boolean isPassed() { 
        return passed; 
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public List<String> getDifferences() { 
        return differences; 
    }
}