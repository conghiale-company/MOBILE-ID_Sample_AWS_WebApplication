package org.example.tool_tax_code;

public class TaxCodeInfo {
    private String taxCode;
    private String status;
    private int index;

    public TaxCodeInfo() {
    }

    public TaxCodeInfo(String taxCode, int index, String status) {
        this.taxCode = taxCode;
        this.status = status;
        this.index = index;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
