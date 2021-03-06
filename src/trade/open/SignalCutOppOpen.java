/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.open;

import Share.ShareData;
import Share.ShareList;
import indicator.*;
import trade.*;
import trade.filter.Filters;

/**
 *
 * @author Admin
 */
public class SignalCutOppOpen extends AbstractOpen 
{
    public SignalCutOppOpen()
    {
        this.paramCount=0;
    }
    
    public SignalCutOppOpen(int buy)
    {
        this.buy=buy;
        this.paramCount=0;
    }

    public TradeList fillOpen(IndicatorList indList, ShareList sl, Filters filter) 
    {
        TradeList trdList=new TradeList();
        Trade trd = null;
        for(int i=1;i<indList.getSize();i++)
        {
            IndicatorField indf=indList.getSharedata(i);
            IndicatorField indfl=indList.getSharedata(i-1);
            if((buy==0 && indf.getValue()>indf.getSignal() && indfl.getValue()<=indfl.getSignal()) ||
                    (buy==1 && indf.getValue()<indf.getSignal() && indfl.getValue()>=indfl.getSignal()))
            {
                int start=sl.isDatePresent(indf.getDDate());
                if(start==-1)
                    start=sl.isLowerDatePresent(indf.getDDate());
                ShareData sd=null;
                sd=sl.getSharedata(start);
                trd=new Trade(indf.getDDate(),sd.getClosePrice(),null,0);
                if(filter==null)
                    trdList.addTrade(trd);
                else if(filter.filterTrade(start,buy,sl))
                    trdList.addTrade(trd);
            }
        }
        return trdList;
    }
    
    @Override
	public String toString()
	{
            //StringBuilder buffer = new StringBuilder();
            //buffer.append("SignalCut Opp Open ");
            return "Open4 ";
	}

//    public int compareTo(AbstractOpen o) {
//        if(!(o instanceof SignalCutOpen) || o==null)
//            return -1;
//        else
//        {
//            return 0;
//        }
//    }

}
