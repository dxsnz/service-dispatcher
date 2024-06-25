package com.example.ordercheck.entity;

public class CpoeOrderCheckRule {

    protected String ruleId;
    protected String ruleName;
    protected String ruleContent;
    protected String className;
    protected String opType;
    protected String ordinal;
    protected String limitLevel;
    protected String inoutFlag;

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleContent() {
        return ruleContent;
    }

    public void setRuleContent(String ruleContent) {
        this.ruleContent = ruleContent;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public String getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(String ordinal) {
        this.ordinal = ordinal;
    }

    public String getLimitLevel() {
        return limitLevel;
    }

    public void setLimitLevel(String limitLevel) {
        this.limitLevel = limitLevel;
    }

    public String getInoutFlag() {
        return inoutFlag;
    }

    public void setInoutFlag(String inoutFlag) {
        this.inoutFlag = inoutFlag;
    }
}
