/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Results;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author gnisoft
 */
public class ResultData implements Serializable {

    private Date date;
    private String type;
    private double eps;
    transient private String name;

    public ResultData(Date date, String type, double eps) {
        this.date = date;
        this.type = type;
        this.eps = eps;
    }



    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the eps
     */
    public double getEps() {
        return eps;
    }

    /**
     * @param eps the eps to set
     */
    public void setEps(double eps) {
        this.eps = eps;
    }

    @Override
    public String toString() {
        if (name == null) {
            String sep = System.getProperty("line.separator");

            StringBuilder buffer = new StringBuilder();
            buffer.append(sep);
            buffer.append("Date = ");
            buffer.append(date);
            buffer.append(sep);
            buffer.append("Type = ");
            buffer.append(type);
            buffer.append(sep);
            buffer.append("Eps = ");
            buffer.append(eps);
            name = buffer.toString();
        }
        return name;
    }


}
