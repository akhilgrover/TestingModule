
package report;

import trade.TradeList;

/**
 *
 * @author Admin
 */
public class Grp 
{
    private String fName;
    private int buy;
    private Summary summ;
    private TradeList tl;
    
    public Grp(String name,int buy,Summary sum,TradeList tl)
    {
        this.fName=name;
        this.buy=buy;
        this.summ=sum;
        this.tl=tl;
    }
    
    public String getFName()
    {
        return fName;
    }
    
    public int getBuy()
    {
        return buy;
    }
    
    public Summary getSumm()
    {
        return summ;
    }
    
    public TradeList getTL()
    {
        return tl;
    }
    

}
