/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade;

import indicator.*;
import Share.*;
import java.io.Serializable;
import trade.close.*;
import trade.filter.*;
import trade.open.*;

/**
 *
 * @author admin
 */
public class TradeParameters implements Serializable
{
    private static final long serialVersionUID = 1L;
    private boolean buy;
    private BaseIndicator indList;
    private BaseIndicator indCloseList;
    private ShareList openSL;
    private ShareList closeSL;
    private Open open;
    private Filters openFilter;
    private Close close;
    private int consec;
    private double shareStop;
    private double indStop;
    private boolean topShares;
    private String index;
    private int shareCount;
    private int shareSkipCount;
    private int backPeriod;
    private int topCap;
    private boolean EMA;
    private IndividualClose iClose;
    private int result;
    private int sector;
    private int shortDays;
    private int shortDaysRank;
    private int indPeriod;
    private double indValue;
    private double indValueHigh;
    private int indDays;
    private String name;

    private TradeParameters()
    {

    }

    /**
     *
     * @param buy Boolean buy or not
     * @param indList Open Indicator
     * @param indCloseList Close Indicator
     * @param openSL Open ShareList
     * @param closeSL Close ShareList
     * @param open Open Algorithm
     * @param openFilter Open Filter
     * @param close Close Algorithm
     * @param consec Consecutive Trades
     * @param shareStop Share StopLoss
     * @param indStop Index StopLoss
     * @param topShares Boolean include Top or not
     * @param index UKX or ASx
     * @param shareCount Top Share Count
     * @param backPeriod Back Test Period
     * @param ema EMA Indicator or not
     * @param iclose Individual Close e.g. RSI close params.
     * @param topCap Top Capitalisation number for gspc shares
     * @return
     */
    public static TradeParameters buildParameter(boolean buy,BaseIndicator indList,BaseIndicator indCloseList,
            ShareList openSL, ShareList closeSL, Open open, Filters openFilter, Close close, int consec, double shareStop,
             double indStop, boolean topShares,String index, int shareCount, int backPeriod, boolean ema,IndividualClose iclose, int topCap)
    {
        TradeParameters tp=new TradeParameters();
        tp.setBackPeriod(backPeriod);
        tp.setBuy(buy);
        tp.setClose(close);
        if(closeSL!=null)
            tp.setCloseSL(closeSL);
        else
            tp.setCloseSL(openSL);
        tp.setConsec(consec);
        if(indCloseList!=null)
            tp.setIndCloseList(indCloseList);
        else
            tp.setIndCloseList(indList);
        tp.setIndList(indList);
        tp.setIndStop(indStop);
        tp.setOpen(open);
        tp.setOpenFilter(openFilter);
        tp.setShareCount(shareCount);
        tp.setShareStop(shareStop);
        tp.setSl(openSL);
        tp.setTopShares(topShares);
        tp.setEMA(ema);
        tp.setIndex(index);
        tp.setiClose(iclose);
        tp.setTopCap(topCap);
        return tp;
    }

    /**
     *
     * @param tpold to copy
     * @return
     */
    public static TradeParameters buildParameter(TradeParameters tpold)
    {
        TradeParameters tp=new TradeParameters();
        tp.setBackPeriod(tpold.getBackPeriod());
        tp.setBuy(tpold.isBuy());
        tp.setClose(tpold.getClose());
        if(tpold.getCloseSL()!=null)
            tp.setCloseSL(tpold.getCloseSL());
        else
            tp.setCloseSL(tpold.getSl());
        tp.setConsec(tpold.getConsec());
        if(tpold.getIndCloseList()!=null)
            tp.setIndCloseList(tpold.getIndCloseList());
        else
            tp.setIndCloseList(tpold.getIndList());
        tp.setIndList(tpold.getIndList());
        tp.setIndStop(tpold.getIndStop());
        tp.setOpen(tpold.getOpen());
        tp.setOpenFilter(tpold.getOpenFilter());
        tp.setShareCount(tpold.getShareCount());
        tp.setShareStop(tpold.getShareStop());
        tp.setSl(tpold.getSl());
        tp.setTopShares(tpold.isTopShares());
        tp.setEMA(tpold.isEMA());
        tp.setIndex(tpold.getIndex());
        tp.setiClose(tpold.getiClose());
        tp.setTopCap(tpold.getTopCap());
        return tp;
    }

    /**
     * @return the indList
     */
    public BaseIndicator getIndList() {
        return indList;
    }

    /**
     * @param indList the indList to set
     */
    public void setIndList(BaseIndicator indList) {
        this.indList = indList;
    }

    /**
     * @return the indCloseList
     */
    public BaseIndicator getIndCloseList() {
        return indCloseList;
    }

    /**
     * @param indCloseList the indCloseList to set
     */
    public void setIndCloseList(BaseIndicator indCloseList) {
        this.indCloseList = indCloseList;
    }

    /**
     * @return the sl
     */
    public ShareList getSl() {
        return openSL;
    }

    /**
     * @param sl the sl to set
     */
    public void setSl(ShareList sl) {
        this.openSL = sl;
    }

    /**
     * @return the open
     */
    public Open getOpen() {
        return open;
    }

    /**
     * @param open the open to set
     */
    public void setOpen(Open open) {
        this.open = open;
    }

    /**
     * @return the openFilter
     */
    public Filters getOpenFilter() {
        return openFilter;
    }

    /**
     * @param openFilter the openFilter to set
     */
    public void setOpenFilter(Filters openFilter) {
        this.openFilter = openFilter;
    }

    /**
     * @return the close
     */
    public Close getClose() {
        return close;
    }

    /**
     * @param close the close to set
     */
    public void setClose(Close close) {
        this.close = close;
    }

    /**
     * @return the consec
     */
    public int getConsec() {
        return consec;
    }

    /**
     * @param consec the consec to set
     */
    public void setConsec(int consec) {
        this.consec = consec;
    }

    /**
     * @return the shareStop
     */
    public double getShareStop() {
        return shareStop;
    }

    /**
     * @param shareStop the shareStop to set
     */
    public void setShareStop(double shareStop) {
        this.shareStop = shareStop;
    }

    /**
     * @return the indStop
     */
    public double getIndStop() {
        return indStop;
    }

    /**
     * @param indStop the indStop to set
     */
    public void setIndStop(double indStop) {
        this.indStop = indStop;
    }

    /**
     * @return the topShares
     */
    public boolean isTopShares() {
        return topShares;
    }

    /**
     * @param topShares the topShares to set
     */
    public void setTopShares(boolean topShares) {
        this.topShares = topShares;
    }

    /**
     * @return the shareCount
     */
    public int getShareCount() {
        return shareCount;
    }

    /**
     * @param shareCount the shareCount to set
     */
    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    /**
     * @return the backPeriod
     */
    public int getBackPeriod() {
        return backPeriod;
    }

    /**
     * @param backPeriod the backPeriod to set
     */
    public void setBackPeriod(int backPeriod) {
        this.backPeriod = backPeriod;
    }

    /**
     * @return the buy
     */
    public boolean isBuy() {
        return buy;
    }

     /**
     * @return the buy
     */
    public int getBuy() {
        int b=1;
        if(!buy)
            b=0;
        return b;
    }

    /**
     * @param buy the buy to set
     */
    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    /**
     * @return the closeSL
     */
    public ShareList getCloseSL() {
        return closeSL;
    }

    /**
     * @param closeSL the closeSL to set
     */
    public void setCloseSL(ShareList closeSL) {
        this.closeSL = closeSL;
    }

    /**
     *
     * @return String Representation of the trade system
     */
    @Override
    public String toString() {
        if(getName()==null)
        {
            StringBuilder ret=new StringBuilder();
            ret.append(openSL.getShare());
            ret.append(getIndList().toString());
            ret.append(getOpen().toString());
            if(getOpenFilter()!=null)
                ret.append(getOpenFilter().toString());
            if(!getCloseSL().toString().equals(getSl().toString()) && getCloseSL()!=null)
                ret.append(getCloseSL().getShare());
            if(!getIndList().toString().equals(getIndCloseList().toString()))
                ret.append(getIndCloseList().toString());
            ret.append(getClose().toString());
            ret.append(backPeriod).append(" Back ");
            if(topShares)
                ret.append(shareCount).append(" Shares ");
            if(topCap>0)
                ret.append(topCap).append(" Cap ");
            if(consec>0)
                ret.append(consec).append(" consec ");
            if(shareStop>0)
                ret.append(shareStop).append(" SL ");
            if(indStop>0)
                ret.append(indStop).append(" IL ");
            if(buy)
                ret.append("Buy");
            else
                ret.append("Sell");
            setName(ret.toString());
        }
        return getName();
    }

    /**
     * @return the EMA
     */
    public boolean isEMA() {
        return EMA;
    }

    /**
     * @param EMA the EMA to set
     */
    public void setEMA(boolean EMA) {
        this.EMA = EMA;
    }

    /**
     * @return the index
     */
    public String getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * @return the iClose
     */
    public IndividualClose getiClose() {
        return iClose;
    }

    /**
     * @param iClose the iClose to set
     */
    public void setiClose(IndividualClose iClose) {
        this.iClose = iClose;
    }

    /**
     * @return the topCap
     */
    public int getTopCap() {
        return topCap;
    }

    /**
     * @param topCap the topCap to set
     */
    public void setTopCap(int topCap) {
        this.topCap = topCap;
    }

    /**
     * @return the result
     */
    public int getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(int result) {
        this.result = result;
    }

    /**
     * @return the sector
     */
    public int getSector() {
        return sector;
    }

    /**
     * @param sector the sector to set
     */
    public void setSector(int sector) {
        this.sector = sector;
    }

    /**
     * @return the shortDaysRank
     */
    public int getShortDaysRank() {
        return shortDaysRank;
    }

    /**
     * @param shortDaysRank the shortDaysRank to set
     */
    public void setShortDaysRank(int shortDaysRank) {
        this.shortDaysRank = shortDaysRank;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the shortDays
     */
    public int getShortDays() {
        return shortDays;
    }

    /**
     * @param shortDays the shortDays to set
     */
    public void setShortDays(int shortDays) {
        this.shortDays = shortDays;
    }

    /**
     * @return the shareSkipCount
     */
    public int getShareSkipCount() {
        return shareSkipCount;
    }

    /**
     * @param shareSkipCount the shareSkipCount to set
     */
    public void setShareSkipCount(int shareSkipCount) {
        this.shareSkipCount = shareSkipCount;
    }

    /**
     * @return the indPeriod
     */
    public int getIndPeriod() {
        return indPeriod;
    }

    /**
     * @param indPeriod the indPeriod to set
     */
    public void setIndPeriod(int indPeriod) {
        this.indPeriod = indPeriod;
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
     * @return the indValueHigh
     */
    public double getIndValueHigh() {
        return indValueHigh;
    }

    /**
     * @param indValueHigh the indValueHigh to set
     */
    public void setIndValueHigh(double indValueHigh) {
        this.indValueHigh = indValueHigh;
    }

    /**
     * @return the indDays
     */
    public int getIndDays() {
        return indDays;
    }

    /**
     * @param indDays the indDays to set
     */
    public void setIndDays(int indDays) {
        this.indDays = indDays;
    }
}
