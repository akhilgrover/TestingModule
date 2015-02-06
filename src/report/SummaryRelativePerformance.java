/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package report;

import java.io.Serializable;
import java.util.HashMap;
import trade.Trade;
import trade.TradeList;

/**
 *
 * @author Admin
 */
public class SummaryRelativePerformance implements Serializable
{
    //serial version
    private static final long serialVersionUID = 7526472295622776147L;
    
    //Group Share name ukx,gspc etc
    private String share;
    //Forward period for blocks
    private int frwdPeriod;
    //backward period
    private int backPeriod;
    //Function Name
    private String Name;
    //total profit
    private double Profit;
    //Total Trade count
    private int trdCount;
    //total Block Count
    private int blockCount;
    //profit / block
    private double Avg;
    //profit / trade
    private double prftTrade;
    //days open/max possible open days
    private double pctOpenDays;
    //associated trade list
    private TradeList tl;
    //associated trade list
    private TradeList indtl;
    //rollover prd
    private int rollover;
    //share count
    private int shareCount;
    //relative performance
    private double relPrformance;
    //Win performance
    private double winPrformance;
    //adte wise share list
    private HashMap dateShare;
    //total share change count
    private int totalShareChange;

    public SummaryRelativePerformance(String sh,int scount,String Name,int frwd,int back, double Profit, int trdCount, int blockCount, double Avg, double prftTrade, double pctOpenDays, TradeList tl, TradeList indtl, int roll,double relPerf, double winPerf,int totchange) {
        this.share=sh;
        this.Name = Name;
        this.frwdPeriod=frwd;
        this.backPeriod=back;
        this.Profit = Profit;
        this.trdCount = trdCount;
        this.blockCount = blockCount;
        this.Avg = Avg;
        this.prftTrade = prftTrade;
        this.pctOpenDays = pctOpenDays;
        this.indtl=indtl;
        this.rollover=roll;
        this.tl = tl;
        this.shareCount=scount;
        this.winPrformance=winPerf;
        this.relPrformance=relPerf;
        this.dateShare=new HashMap();
        this.totalShareChange=totchange;
    }
    
    public void calcSummary()
    {
        Profit=0;
        trdCount=0;
        double blkPrft=0;
        int lastl=0;
        int posBlk=0,negBlk=0,pBlk=0,nBlk=0;
        for(int i=1;i<=this.tl.getSize();i++)
        {
            Trade trd=tl.getTrade(i-1);
            Trade indTrd=null;
            for(int k=lastl;k<indtl.getSize();k++)
            {
                indTrd=indtl.getTrade(k);
                if(indTrd.getStartDate().equals(trd.getStartDate()))
                {
                    lastl=k;
                    break;
                }
            }
            Profit+=trd.getProfit();
            blkPrft+=trd.getProfit();
            trdCount++;
            if(i%shareCount==0 && i>0)
            {
                blockCount++;
                if((blkPrft/shareCount)-indTrd.getProfit()>0)
                {
                    posBlk++;
                }
                else if((blkPrft/shareCount)-indTrd.getProfit()<0)
                {
                    negBlk++;
                }
                if(blkPrft>0)
                    pBlk++;
                else if(blkPrft<0)
                    nBlk++;
                blkPrft=0;
            }
        }
        Avg=Profit/blockCount;
        prftTrade=Profit/trdCount;
        relPrformance=((posBlk*1.0)/(posBlk+negBlk))*100;
        setWinPrformance(((pBlk * 1.0) / (pBlk + nBlk)) * 100);
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public double getProfit() {
        return Profit;
    }

    public void setProfit(double Profit) {
        this.Profit = Profit;
    }

    public int getTrdCount() {
        return trdCount;
    }

    public void setTrdCount(int trdCount) {
        this.trdCount = trdCount;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    public double getAvg() {
        return Avg;
    }

    public void setAvg(double Avg) {
        this.Avg = Avg;
    }

    public double getPrftTrade() {
        return prftTrade;
    }

    public void setPrftTrade(double prftTrade) {
        this.prftTrade = prftTrade;
    }

    public double getPctOpenDays() {
        return pctOpenDays;
    }

    public void setPctOpenDays(double pctOpenDays) {
        this.pctOpenDays = pctOpenDays;
    }

    public TradeList getTl() {
        return tl;
    }

    public void setTl(TradeList tl) {
        this.tl = tl;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public TradeList getIndtl() {
        return indtl;
    }

    public void setIndtl(TradeList indtl) {
        this.indtl = indtl;
    }

    public int getFrwdPeriod() {
        return frwdPeriod;
    }

    public void setFrwdPeriod(int frwdPeriod) {
        this.frwdPeriod = frwdPeriod;
    }

    public int getRollover() {
        return rollover;
    }

    public void setRollover(int rollover) {
        this.rollover = rollover;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public int getBackPeriod() {
        return backPeriod;
    }

    public void setBackPeriod(int backPeriod) {
        this.backPeriod = backPeriod;
    }

    public double getRelPrformance() {
        return relPrformance;
    }

    public void setRelPrformance(double relPrformance) {
        this.relPrformance = relPrformance;
    }

    public double getWinPrformance() {
        return winPrformance;
    }

    public void setWinPrformance(double winPrformance) {
        this.winPrformance = winPrformance;
    }

    /**
     * @return the dateShare
     */
    public HashMap getDateShare() {
        return dateShare;
    }

    /**
     * @param dateShare the dateShare to set
     */
    public void setDateShare(HashMap dateShare) {
        this.dateShare = dateShare;
    }

    /**
     * @return the totalShareChange
     */
    public int getTotalShareChange() {
        return totalShareChange;
    }

    /**
     * @param totalShareChange the totalShareChange to set
     */
    public void setTotalShareChange(int totalShareChange) {
        this.totalShareChange = totalShareChange;
    }

}
