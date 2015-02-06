/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trade;

import java.util.Date;

/**
 *
 * @author admin
 */
public class NegPairTrade extends PairTrade{

    private Date OrignalStartDate;
    private Date OrignalCloseDate;

    public NegPairTrade(String pair, Date startDate, Date closeDate) {
        super(pair, startDate, closeDate);
    }

    public NegPairTrade(String pair, Date startDate, double sellOpen, double buyOpen, Date closeDate, double sellClose, double buyClose) {
        super(pair, startDate, sellOpen, buyOpen, closeDate, sellClose, buyClose);
    }

    /**
     * @param pt Old Trade
     * @param StartDate New Start Date
     * @param CloseDate New Close Date
     */
    public NegPairTrade(PairTrade pt,Date StartDate,Date CloseDate)
    {
        super(pt.getShare(), StartDate, CloseDate);
        this.OrignalCloseDate=pt.getCloseDate();
        this.OrignalStartDate=pt.getStartDate();
        this.setBuyClosePrice(pt.getBuyClosePrice());
        this.setClosePrice(pt.getClosePrice());
    }

    /**
     * @return the OrignalStartDate
     */
    public Date getOrignalStartDate() {
        return OrignalStartDate;
    }

    /**
     * @param OrignalStartDate the OrignalStartDate to set
     */
    public void setOrignalStartDate(Date OrignalStartDate) {
        this.OrignalStartDate = OrignalStartDate;
    }

    /**
     * @return the OrignalCloseDate
     */
    public Date getOrignalCloseDate() {
        return OrignalCloseDate;
    }

    /**
     * @param OrignalCloseDate the OrignalCloseDate to set
     */
    public void setOrignalCloseDate(Date OrignalCloseDate) {
        this.OrignalCloseDate = OrignalCloseDate;
    }

}
