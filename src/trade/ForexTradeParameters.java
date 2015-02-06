/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trade;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author akhil
 */
public class ForexTradeParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String buy;
    private final String baseCurrency;
    private final String openIndicator;
    private final String open;
    private final ArrayList<String> filters;
    private final ArrayList<String> filterValue;
    private final ArrayList<String> filterTime;
    private final String closeCurrency;
    private final String closeIndicator;
    private final String close;
    private final double stopLoss;
    private final double profitTarget;
    private final int trdCount;
    private final int systemCount;
    private final ArrayList<String> filtersAfter;
    private final ArrayList<String> filterAfterValue;
    private final ArrayList<String> filterAfterTime;
    private transient String id;
    private transient String openid;
    private transient String closeid;

    public ForexTradeParameters() {
        filters = new ArrayList<>();
        filterValue = new ArrayList<>();
        filterTime = new ArrayList<>();

        filtersAfter = new ArrayList<>();
        filterAfterValue = new ArrayList<>();
        filterAfterTime = new ArrayList<>();

        this.buy = "";
        this.baseCurrency = "";
        this.openIndicator = "";
        this.open = "";
        this.closeCurrency = "";
        this.closeIndicator = "";
        this.close = "";
        this.stopLoss = 100;
        this.profitTarget = 100;
        this.trdCount = 0;
        this.systemCount = 0;
    }

    /**
     *
     * @param Buy
     * @param baseCurrency
     * @param openIndicator
     * @param open
     * @param filters
     * @param filterValue
     * @param filterTime
     * @param closeCurrency
     * @param closeIndicator
     * @param close
     * @param stopLoss
     * @param profitTarget
     * @param trdCount
     * @param systemCount
     * @param filtersAfter
     * @param filterAfterValue
     * @param filterAfterTime
     */
    public ForexTradeParameters(String Buy, String baseCurrency, String openIndicator, String open, ArrayList<String> filters, ArrayList<String> filterValue, ArrayList<String> filterTime, String closeCurrency, String closeIndicator, String close, double stopLoss, double profitTarget, int trdCount, int systemCount, ArrayList<String> filtersAfter, ArrayList<String> filterAfterValue, ArrayList<String> filterAfterTime) {
        this.buy = Buy;
        this.baseCurrency = baseCurrency;
        this.openIndicator = openIndicator;
        this.open = open;
        this.filters = filters;
        this.filterValue = filterValue;
        this.filterTime = filterTime;
        this.closeCurrency = closeCurrency;
        this.closeIndicator = closeIndicator;
        this.close = close;
        this.stopLoss = stopLoss;
        this.profitTarget = profitTarget;
        this.trdCount = trdCount;
        this.systemCount = systemCount;
        this.filtersAfter = filtersAfter;
        this.filterAfterValue = filterAfterValue;
        this.filterAfterTime = filterAfterTime;
    }
    
    public static ForexTradeParameters buildInstanceFromID(String fullId){
        
        String[] split=fullId.split(";");
        String buy=split[0];
        String base=split[1];
        String openInd=split[2];
        String open=split[3];
        String filters=split[4];
        ArrayList<String> f=new ArrayList<>();
        ArrayList<String> fv=new ArrayList<>();
        ArrayList<String> ft=new ArrayList<>();
        String filtersAfter=split[5];
        ArrayList<String> fa=new ArrayList<>();
        ArrayList<String> fav=new ArrayList<>();
        ArrayList<String> fat=new ArrayList<>();
        String clsCur=split[6];
        String clsInd=split[7];
        String close=split[8];
        double sl=Double.parseDouble(split[9]);
        double pt=Double.parseDouble(split[10]);
        int trdcnt=Integer.parseInt(split[11]);
        int syscnt=Integer.parseInt(split[12]);
        
//            for(int i=0;i<filters.size();i++){
//                ret.append(filters.get(i)).append(":").append(filterValue.get(i)).append(":").append(filterTime.get(i));
//                if(i<filters.size()-1)
//                    ret.append("|");
//            }
//            for(int i=0;i<filtersAfter.size();i++){
//                ret.append(filtersAfter.get(i)).append(":").append(filterAfterValue.get(i)).append(":").append(filterAfterTime.get(i));
//                if(i<filters.size())
//                    ret.append("|");
//            }
        ForexTradeParameters frt=new ForexTradeParameters(buy, base, openInd, open, f, fv, ft, clsCur, clsInd, close, sl, pt, trdcnt, syscnt, fa, fav, fat);
        if(!frt.getId().equals(fullId))
            System.out.println("Invalid Params");
        return frt;
    }

    /**
     * @return the buy
     */
    public String getBuy() {
        return buy;
    }
    
    /**
     * @return the buy
     */
    public int getBuyInt() {
        return buy.equals("Buy")?1:0;
    }
    
    /**
     * @return the buy
     */
    public boolean getBuyBool() {
        return buy.equals("Buy");
    }

    /**
     * @return the baseTime
     */
    public String getBaseCurrency() {
        return baseCurrency;
    }
    
    /**
     * @return the baseTime
     */
    public String getBaseTime() {
        return baseCurrency.split(",")[1];
    }

    /**
     * @return the openIndicator
     */
    public String getOpenIndicator() {
        return openIndicator;
    }

    /**
     * @return the open
     */
    public String getOpen() {
        return open;
    }

    /**
     * @return the filters
     */
    public ArrayList<String> getFilters() {
        return filters;
    }

    /**
     * @return the filterValue
     */
    public ArrayList<String> getFilterValue() {
        return filterValue;
    }

    /**
     * @return the filterTime
     */
    public ArrayList<String> getFilterTime() {
        return filterTime;
    }

    /**
     * @return the closeCurrency
     */
    public String getCloseCurrency() {
        return closeCurrency;
    }
    
    /**
     * @return the closeCurrency
     */
    public String getCloseCurrencyTime() {
        return closeCurrency.split(",")[1];
    }

    /**
     * @return the closeIndicator
     */
    public String getCloseIndicator() {
        return closeIndicator;
    }

    /**
     * @return the close
     */
    public String getClose() {
        return close;
    }

    /**
     * @return the stopLoss
     */
    public double getStopLoss() {
        return stopLoss;
    }

    /**
     * @return the profitTarget
     */
    public double getProfitTarget() {
        return profitTarget;
    }

    /**
     * @return the trdCount
     */
    public int getTrdCount() {
        return trdCount;
    }

    /**
     * @return the systemCount
     */
    public int getSystemCount() {
        return systemCount;
    }

    /**
     * @return the filtersAfter
     */
    public ArrayList<String> getFiltersAfter() {
        return filtersAfter;
    }

    /**
     * @return the filterAfterValue
     */
    public ArrayList<String> getFilterAfterValue() {
        return filterAfterValue;
    }

    /**
     * @return the filterAfterTime
     */
    public ArrayList<String> getFilterAfterTime() {
        return filterAfterTime;
    }

    @Override
    public String toString() {
        return getId();
    }

    /**
     * @return the id
     */
    public String getId() {
        if (id == null) {
            StringBuilder ret = new StringBuilder(buy);
            ret.append(";").append(baseCurrency.split(",")[1]).append(";").append(openIndicator).append(";").append(open);
            openid=ret.toString();
            ret.append(";");
            for(int i=0;i<filters.size();i++){
                ret.append(filters.get(i)).append(":").append(filterValue.get(i)).append(":").append(filterTime.get(i));
                if(i<filters.size()-1)
                    ret.append("|");
            }
            ret.append(";");
            for(int i=0;i<filtersAfter.size();i++){
                ret.append(filtersAfter.get(i)).append(":").append(filterAfterValue.get(i)).append(":").append(filterAfterTime.get(i));
                if(i<filters.size())
                    ret.append("|");
            }
            ret.append(";");
            StringBuilder ret1=new StringBuilder();
            ret1.append(closeCurrency.split(",")[1]).append(";").append(closeIndicator).append(";").append(close).append(";").append(stopLoss).append(";").append(profitTarget);
            closeid=ret1.toString();
            ret.append(ret1.toString());
            ret.append(";").append(trdCount);
            ret.append(";").append(systemCount);
            id=ret.toString();
        }
        return id;
    }

    /**
     * @return the openid
     */
    public String getOpenid() {
        if(openid==null)
            getId();
        return openid;
    }

    /**
     * @return the closeid
     */
    public String getCloseid() {
        if(closeid==null)
            getId();
        return closeid;
    }

}
