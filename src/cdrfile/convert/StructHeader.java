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
public class StructHeader {
    private String name;
    private String transmissionCheckSum;
    private String typeOfRecord;
    private String exchangeIdentity;
    private String source;
    private String totalNumberOfUsedRemunerationVerificationCounters;
    private String totalNumberOfUsedRemunerationCounters;
    private String yearTheLastTimePeriodicOutputWasMade;
    private String monthTheLastTimePeriodicOutputWasMade;
    private String dayTheLastTimePeriodicOutputWasMade;
    private String hourTheLastTimePeriodicOutputWasMade;
    private String minuteTheLastTimePeriodicOutputWasMade;
    private String yearForTheCurrentOutput;
    private String monthForTheCurrentOutput;
    private String dayForTheCurrentOutput;
    private String hourForTheCurrentOutput;
    private String minuteForTheCurrentOutput;
    public StructHeader() {
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

    public void setExchangeIdentity(String exchangeIdentity) {
        this.exchangeIdentity = exchangeIdentity;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTotalNumberOfUsedRemunerationVerificationCounters(String
            totalNumberOfUsedRemunerationVerificationCounters) {
        this.totalNumberOfUsedRemunerationVerificationCounters =
                totalNumberOfUsedRemunerationVerificationCounters;
    }

    public void setTotalNumberOfUsedRemunerationCounters(String
            totalNumberOfUsedRemunerationCounters) {
        this.totalNumberOfUsedRemunerationCounters =
                totalNumberOfUsedRemunerationCounters;
    }

    public void setYearTheLastTimePeriodicOutputWasMade(String
            yearTheLastTimePeriodicOutputWasMade) {
        this.yearTheLastTimePeriodicOutputWasMade =
                yearTheLastTimePeriodicOutputWasMade;
        this.yearTheLastTimePeriodicOutputWasMade =
                yearTheLastTimePeriodicOutputWasMade;
    }

    public void setMonthTheLastTimePeriodicOutputWasMade(String
            monthTheLastTimePeriodicOutputWasMade) {
        this.monthTheLastTimePeriodicOutputWasMade =
                monthTheLastTimePeriodicOutputWasMade;
    }

    public void setDayTheLastTimePeriodicOutputWasMade(String
            dayTheLastTimePeriodicOutputWasMade) {
        this.dayTheLastTimePeriodicOutputWasMade =
                dayTheLastTimePeriodicOutputWasMade;
    }

    public void setHourTheLastTimePeriodicOutputWasMade(String
            hourTheLastTimePeriodicOutputWasMade) {
        this.hourTheLastTimePeriodicOutputWasMade =
                hourTheLastTimePeriodicOutputWasMade;
    }

    public void setMinuteTheLastTimePeriodicOutputWasMade(String
            minuteTheLastTimePeriodicOutputWasMade) {
        this.minuteTheLastTimePeriodicOutputWasMade =
                minuteTheLastTimePeriodicOutputWasMade;
    }

    public void setYearForTheCurrentOutput(String yearForTheCurrentOutput) {
        this.yearForTheCurrentOutput = yearForTheCurrentOutput;
    }

    public void setMonthForTheCurrentOutput(String monthForTheCurrentOutput) {
        this.monthForTheCurrentOutput = monthForTheCurrentOutput;
    }

    public void setDayForTheCurrentOutput(String dayForTheCurrentOutput) {
        this.dayForTheCurrentOutput = dayForTheCurrentOutput;
    }

    public void setHourForTheCurrentOutput(String hourForTheCurrentOutput) {
        this.hourForTheCurrentOutput = hourForTheCurrentOutput;
    }

    public void setMinuteForTheCurrentOutput(String minuteForTheCurrentOutput) {
        this.minuteForTheCurrentOutput = minuteForTheCurrentOutput;
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

    public String getExchangeIdentity() {
        return exchangeIdentity;
    }

    public String getSource() {
        return source;
    }

    public String getTotalNumberOfUsedRemunerationVerificationCounters() {
        return totalNumberOfUsedRemunerationVerificationCounters;
    }

    public String getTotalNumberOfUsedRemunerationCounters() {
        return totalNumberOfUsedRemunerationCounters;
    }

    public String getYearTheLastTimePeriodicOutputWasMade() {

        return yearTheLastTimePeriodicOutputWasMade;
    }

    public String getMonthTheLastTimePeriodicOutputWasMade() {
        return monthTheLastTimePeriodicOutputWasMade;
    }

    public String getDayTheLastTimePeriodicOutputWasMade() {
        return dayTheLastTimePeriodicOutputWasMade;
    }

    public String getHourTheLastTimePeriodicOutputWasMade() {
        return hourTheLastTimePeriodicOutputWasMade;
    }

    public String getMinuteTheLastTimePeriodicOutputWasMade() {
        return minuteTheLastTimePeriodicOutputWasMade;
    }

    public String getYearForTheCurrentOutput() {
        return yearForTheCurrentOutput;
    }

    public String getMonthForTheCurrentOutput() {
        return monthForTheCurrentOutput;
    }

    public String getDayForTheCurrentOutput() {
        return dayForTheCurrentOutput;
    }

    public String getHourForTheCurrentOutput() {
        return hourForTheCurrentOutput;
    }

    public String getMinuteForTheCurrentOutput() {
        return minuteForTheCurrentOutput;
    }
}
