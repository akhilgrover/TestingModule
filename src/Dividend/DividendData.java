package Dividend;



import java.io.Serializable;
import java.util.*;

public class DividendData implements Serializable {

    private String Share;
    private Date date;
    private double amount;
    transient private String name;

    public DividendData(String Share, Date date, double amount) {

        this.Share = Share;
        this.date = date;
        this.amount = amount;
    }


    public String getShare() {
        return (this.Share);
    }

    public Date getDate() {
        return (this.date);
    }

    public double getAmount() {
        return (this.amount);
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        if(name==null){
        String sep = System.getProperty("line.separator");

        StringBuilder buffer = new StringBuilder();
        buffer.append(sep);
        buffer.append("Share = ");
        buffer.append(Share);
        buffer.append(sep);
        buffer.append("Date = ");
        buffer.append(date);
        buffer.append(sep);
        buffer.append("Amount = ");
        buffer.append(amount);
        name=buffer.toString();
        }
        return name;
    }

}
