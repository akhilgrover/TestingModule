/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trade;

import Share.ShareData;
import Share.ShareList;
import indicator.IndicatorList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author admin
 */
public class PairTrade extends Trade {

    private String Buy = "";
    private String Sell = "";
    private double buyStartPrice;
    private double buyClosePrice;
    private double p1;
    private double p2;
    private double indValue;
    protected HashMap<String, Double> values;
    private String comment;

    public PairTrade(String pair, Date startDate, Date closeDate) {
        super(startDate, 0, closeDate, 0);
        this.setShare(pair);
        String[] str = pair.split("-");
        Buy = str[1];
        Sell = str[0];
    }

    public PairTrade(PairTrade pair, Date closeDate) {
        super(pair.getStartDate(), 0, closeDate, 0);
        this.setShare(pair.getShare());
        String[] str = pair.getShare().split("-");
        Buy = str[1];
        Sell = str[0];
        this.values = pair.values;
        this.indValue = pair.indValue;
    }

    public PairTrade(String pair, Date startDate, double sellOpen, double buyOpen, Date closeDate, double sellClose, double buyClose) {
        super(startDate, sellOpen, closeDate, sellClose);
        this.setShare(pair);
        String[] str = pair.split("-");
        Buy = str[1];
        Sell = str[0];
        buyStartPrice = buyOpen;
        buyClosePrice = buyClose;
    }

    /**
     * @return the Buy
     */
    public String getBuy() {
        return Buy;
    }

    /**
     * @param Buy the Buy to set
     */
    public void setBuy(String Buy) {
        this.Buy = Buy;
    }

    /**
     * @return the Sell
     */
    public String getSell() {
        return Sell;
    }

    /**
     * @param Sell the Sell to set
     */
    public void setSell(String Sell) {
        this.Sell = Sell;
    }

    /**
     * @return the buyStartPrice
     */
    public double getBuyStartPrice() {
        return buyStartPrice;
    }

    /**
     * @param buyStartPrice the buyStartPrice to set
     */
    public void setBuyStartPrice(double buyStartPrice) {
        this.buyStartPrice = buyStartPrice;
    }

    /**
     * @return the buyClosePrice
     */
    public double getBuyClosePrice() {
        return buyClosePrice;
    }

    /**
     * @param buyClosePrice the buyClosePrice to set
     */
    public void setBuyClosePrice(double buyClosePrice) {
        this.buyClosePrice = buyClosePrice;
    }

    /**
     * @return the p1
     */
    public double getP1() {
        return p1;
    }

    /**
     * @param p1 the p1 to set
     */
    public void setP1(double p1) {
        this.p1 = p1;
    }

    /**
     * @return the p2
     */
    public double getP2() {
        return p2;
    }

    /**
     * @param p2 the p2 to set
     */
    public void setP2(double p2) {
        this.p2 = p2;
    }

    /**
     * @return the indValue
     */
    public double getIndValue() {
        return indValue;
    }

    /**
     * @param indValue the indValue to set
     */
    public void setIndValue(double indValue) {
        this.indValue = indValue;
    }

    /**
     * @param param
     * @return the values
     */
    public Double getValues(String param) {
        return (values == null) ? null : values.get(param);
    }

    /**
     * @param param
     * @param value
     */
    public void setValues(String param, Double value) {
        if (values == null) {
            values = new HashMap<String, Double>();
        }
        this.values.put(param, value);
    }

    public void calcProfit(TradeCalculator tc) throws Exception {
        double pr1 = 0, pr2 = 0;
        ShareList sl1 = tc.getSL(this.Sell,"UKX");
        ShareList sl2 = tc.getSL(this.Buy,"UKX");


        Date from = this.getStartDate();
        Date to = this.getCloseDate();

        int startInd = sl1.isDatePresent(from);
        int endInd = sl1.isDatePresent(to);

        ShareData sdEnd = null;
        ShareData sdStart = sl1.getSharedata(startInd);
        if (endInd > -1) {
            sdEnd = sl1.getSharedata(endInd);
        } else {
            if (sl1.isHigherDatePresent(to) > -1) {
                sdEnd = sl1.getSharedata(sl1.isHigherDatePresent(to));
            } else if (sl1.isLowerDatePresent(to) > -1) {
                sdEnd = sl1.getSharedata(sl1.isLowerDatePresent(to));
            }
        }
        this.setStartPrice(sdStart.getClosePrice());
        this.setClosePrice(sdEnd.getClosePrice());
        Trade trd = new Trade(sdStart.getDate(), sdStart.getClosePrice(), sdEnd.getDate(), sdEnd.getClosePrice());
        trd.setShare(this.Sell);
        Trade trdNew = tc.calcDividendTrade(trd, 0, 1);
        pr1 = trdNew.getProfit();

        int startInd1 = sl2.isDatePresent(from);
        int endInd1 = sl2.isDatePresent(to);

        ShareData sdEnd1 = null;
        ShareData sdStart1 = sl2.getSharedata(startInd1);
        if (endInd1 > -1) {
            sdEnd1 = sl2.getSharedata(endInd1);
        } else {
            if (sl2.isHigherDatePresent(to) > -1) {
                sdEnd1 = sl2.getSharedata(sl2.isHigherDatePresent(to));
            } else if (sl2.isLowerDatePresent(to) > -1) {
                sdEnd1 = sl2.getSharedata(sl2.isLowerDatePresent(to));
            }
        }
        this.buyStartPrice = sdStart1.getClosePrice();
        this.buyClosePrice = sdEnd1.getClosePrice();
        Trade trd2 = new Trade(sdStart1.getDate(), sdStart1.getClosePrice(), sdEnd1.getDate(), sdEnd1.getClosePrice());
        trd2.setShare(this.Buy);
        Trade trdNew2 = tc.calcDividendTrade(trd2, 1, 1);
        pr2 = trdNew2.getProfit();

        this.p1 = pr1;
        this.p2 = pr2;
        this.setProfit(pr1 + pr2);
    }

    public void fillInd(TradeCalculator tc, boolean corr) throws Exception {
        if (indValue == 0 || values.isEmpty()) {
            Date d = new Date();
            ShareList sl1 = tc.getSL(this.Sell,"UKX");
            ShareList sl2 = tc.getSL(this.Buy,"UKX");
            if (!corr) {
                IndicatorList ilB = tc.getIndicatorListFromDB(this.Buy, sl2.getSharedata(0).getDate(), d, "RSI/2");
                IndicatorList ilS = tc.getIndicatorListFromDB(this.Sell, sl1.getSharedata(0).getDate(), d, "RSI/2");
                if (ilS.isDatePresent(getStartDate()) == -1) {
                    System.out.println("errCorr");
                } else {
                    setValues("RSIS", ilS.getSharedata(ilS.isDatePresent(getStartDate())).getValue());
                }
                if (ilB.isDatePresent(getStartDate()) == -1) {
                    System.out.println("errCorr");
                } else {
                    setValues("RSIB", ilB.getSharedata(ilB.isDatePresent(getStartDate())).getValue());
                }
            } else if (corr) {
                IndicatorList ilCorr = tc.getIndicatorListFromDB(this.getShare(), getStartDate(), d,"Correlation/85");
                //IndicatorList ilCorr = tc.getIndicatorListBuild(this.getShare(), "Correlation/85");
                if (ilCorr.isDatePresent(getStartDate()) == -1) {
                    System.out.println("errCorr");
                } else {
                    this.indValue = ilCorr.getSharedata(ilCorr.isDatePresent(getStartDate())).getValue();
                }
            }

        }
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}
