package cdrfile.convert;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class StructCounter {
    private String name;
    private String transmissionCheckSum;
    private String typeOfRecord;
    private String accountingClassNumber;
    private String typeOfAccounting;
    private String messageCounter;
    private String octetCounter;
    public StructCounter() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTransmissionCheckSum(String transmissionCheckSum) {
        this.transmissionCheckSum = transmissionCheckSum;
    }

    public void setTypeOfRecord(String typeOfRecord) {
        this.typeOfRecord = typeOfRecord;
    }

    public void setAccountingClassNumber(String accountingClassNumber) {
        this.accountingClassNumber = accountingClassNumber;
    }

    public void setTypeOfAccounting(String typeOfAccounting) {
        this.typeOfAccounting = typeOfAccounting;
    }

    public void setMessageCounter(String messageCounter) {
        this.messageCounter = messageCounter;
    }

    public void setOctetCounter(String octetCounter) {
        this.octetCounter = octetCounter;
    }

    public String getName() {
        return name;
    }

    public String getTransmissionCheckSum() {
        return transmissionCheckSum;
    }

    public String getTypeOfRecord() {
        return typeOfRecord;
    }

    public String getAccountingClassNumber() {
        return accountingClassNumber;
    }

    public String getTypeOfAccounting() {
        return typeOfAccounting;
    }

    public String getMessageCounter() {
        return messageCounter;
    }

    public String getOctetCounter() {
        return octetCounter;
    }
}
