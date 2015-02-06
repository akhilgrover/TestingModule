package indicator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class IndicatorField implements Serializable {

    private static final long serialVersionUID = 1L;

    private long dDate;
    private double value;
    private double signal;
    private transient String name;

    public IndicatorField() {
    }

    /**
     * Method IndicatorField
     *
     *
     */
    public IndicatorField(Date dat, double val, double sig) {

        this.dDate = dat.getTime();
        this.value = val;
        this.signal = sig;
    }

    public void setDDate(Date dDate) {
        this.dDate = dDate.getTime();
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setSignal(double signal) {
        this.signal = signal;
    }

    public Date getDDate() {
        return (new Date(this.dDate));
    }

    public long getDDateLong() {
        return this.dDate;
    }

    public double getValue() {
        return (this.value);
    }

    public double getSignal() {
        return (this.signal);
    }

    @Override
    public String toString() {
        if (name == null) {
            name = new SimpleDateFormat("dd/MM/yyyy").format(getDDate()) + "," + value + "," + signal;
        }
        return name;
    }

}
