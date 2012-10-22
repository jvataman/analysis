package aboutlocal.analysis.data.dtos;

public class QuoteDTO {

    private Long timeStamp;
    private String name;
    private Double value;
    private Double changePercent;

    public QuoteDTO(Long ts, String name, Double value, Double changePercent) {
        this.timeStamp = ts;
        this.setName(name);
        this.value = value;
        this.changePercent = changePercent;
    }
    
    @Override
    public String toString() {
        return timeStamp+"\t"+name+"\t"+value+"\t"+changePercent;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }

}
