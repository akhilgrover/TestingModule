package trade;

import java.io.Serializable;
import java.util.*;

public class Trade implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date startDate;
    private double startPrice;
    private Date closeDate;
    private double closePrice;
    private double profit;
    private boolean isIncluded;
    private String Share;
    private transient String name;
    /**
     * Method Trade
     *
     *
     *
     * @param startDate
     * @param startPrice
     * @param closeDate
     * @param closePrice
     */
    public Trade(Date startDate, double startPrice, Date closeDate, double closePrice) {
        this.startDate = startDate;
        this.startPrice = startPrice;
        this.closeDate = closeDate;
        this.closePrice = closePrice;
        this.isIncluded = true;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public void setIsValid(boolean valid) {
        this.isIncluded = valid;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public Date getStartDate() {
        return (this.startDate);
    }

    public double getStartPrice() {
        return (this.startPrice);
    }

    public Date getCloseDate() {
        return (this.closeDate);
    }

    public double getClosePrice() {
        return (this.closePrice);
    }

    public double getProfit() {
        return (this.profit);
    }

    public boolean isValid() {
        return isIncluded;
    }

    @Override
    public String toString() {
        if (name == null) {
            String sep = System.getProperty("line.separator");

            StringBuilder buffer = new StringBuilder();
            buffer.append(Share);
            buffer.append("\t");
            buffer.append(startDate);
            buffer.append("\t");
            buffer.append(startPrice);
            buffer.append("\t");
            buffer.append(closeDate);
            buffer.append("\t");
            buffer.append(closePrice);

            name = buffer.toString();
        }
        return name;
    }

    /**
     * @return the Share
     */
    public String getShare() {
        return Share;
    }

    /**
     * @param Sends the Share to set
     */
    public void setShare(String Share) {
        this.Share = Share;
    }
}
