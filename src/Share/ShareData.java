package Share;

import java.io.Serializable;
import java.util.*;

public class ShareData implements Serializable {

    private String Share;
    private long date;
    private double closePrice;
    private double high;
    private double low;
    private double openPrice;
    private long vol;
    private transient String name;

    public ShareData() {
    }


     /**
     * Method ShareDate
     *
     *
     */
    public ShareData(String code, Date dat, double close, double high, double low, double open, long vol) {

        this.Share = code.intern();
        this.date = dat.getTime();
        this.closePrice = close;
        this.high = high;
        this.low = low;
        this.openPrice = open;
        this.vol = vol;
    }

    public String getShare() {
        return (new String(this.Share));
    }

    public Date getDate() {
        return (new Date(this.date));
    }

    public long getDateLong() {
        return this.date;
    }

    public double getClosePrice() {
        return (this.closePrice);
    }

    public double getHigh() {
        return (this.high);
    }

    public double getLow() {
        return (this.low);
    }

    public double getOpenPrice() {
        return (this.openPrice);
    }

    public long getVol() {
        return (this.vol);
    }

    @Override
    public String toString() {
        if (name == null) {
            String sep = System.getProperty("line.separator");

            StringBuilder buffer = new StringBuilder();
            buffer.append(sep);
            buffer.append("Share = ");
            buffer.append(Share);
            buffer.append(sep);
            buffer.append("Date = ");
            buffer.append(new Date(date));
            buffer.append(sep);
            buffer.append("Close Price = ");
            buffer.append(closePrice);
            buffer.append(sep);
            buffer.append("High = ");
            buffer.append(high);
            buffer.append(sep);
            buffer.append("Low = ");
            buffer.append(low);
            buffer.append(sep);
            buffer.append("Open Price = ");
            buffer.append(openPrice);
            buffer.append(sep);
            buffer.append("Volume = ");
            buffer.append(vol);
            buffer.append(sep);
            name=buffer.toString().intern();
        }
        return name;
    }

    /**
     * @param closePrice the closePrice to set
     */
    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }
}
