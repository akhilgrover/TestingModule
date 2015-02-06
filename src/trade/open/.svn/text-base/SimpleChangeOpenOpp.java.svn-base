/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.open;

import Share.ShareData;
import Share.ShareList;
import indicator.IndicatorField;
import indicator.IndicatorList;
import java.util.HashMap;
import trade.Trade;
import trade.TradeList;
import trade.filter.Filters;

/**
 *
 * @author Admin
 */
public class SimpleChangeOpenOpp extends  AbstractOpen
{

    public SimpleChangeOpenOpp()
    {
        this.paramCount=1;
    }

    public SimpleChangeOpenOpp(HashMap param, int buy)
    {
       this.buy=buy;
       this.paramCount=1;
       this.params=param;
    }

    public TradeList fillOpen(IndicatorList indList, ShareList sl,
            Filters filter)
    {
        TradeList trdList=new TradeList();
        Trade trd;
        double pct=0;
        if(params.get(1) instanceof Double)
            pct=(Double)params.get(1);
        else
            pct=((Integer)params.get(1))*1.0;
        double high=indList.getSharedata(2).getValue();
        double low=indList.getSharedata(2).getValue();
        boolean trdStart=true;
        for(int i=2;i<indList.getSize();i++)
        {
            IndicatorField indf2l=indList.getSharedata(i-2);
            IndicatorField indfl=indList.getSharedata(i-1);
            IndicatorField indf=indList.getSharedata(i);
            if(indf.getValue()>indfl.getValue() && indfl.getValue()<indf2l.getValue() && !trdStart)
            {
                high=indf.getValue();
                trdStart=true;
            }
            if(indf.getValue()<indfl.getValue() && indfl.getValue()>indf2l.getValue() && !trdStart)
            {
                low=indf.getValue();
                trdStart=true;
            }
            //Date dd=indf.getDDate();
            if(pct==0)
            {
                if((buy==0 && indf.getValue()>indfl.getValue() && indfl.getValue()<=indf2l.getValue()) ||
                        (buy==1 && indf.getValue()<indfl.getValue() && indfl.getValue()>=indf2l.getValue()))
                {
                    int start=sl.isDatePresent(indf.getDDate());
                    if(start==-1)
                        start=sl.isLowerDatePresent(indf.getDDate());
                    ShareData sd=sl.getSharedata(start);
                    trd=new Trade(indf.getDDate(),sd.getClosePrice(),null,0);
                    if(filter==null)
                        trdList.addTrade(trd);
                    else if(filter.filterTrade(start,buy,sl))
                        trdList.addTrade(trd);

                }
            }
            else
            {
                if(((buy==0 && indf.getValue()>=low+(low*pct*0.01) ) ||
                        (buy==1 && indf.getValue()<=high-(high*pct*0.01))) && trdStart)
                {
                    int start=sl.isDatePresent(indf.getDDate());
                    if(start==-1)
                        start=sl.isLowerDatePresent(indf.getDDate());
                    ShareData sd=sl.getSharedata(start);
                    trd=new Trade(indf.getDDate(),sd.getClosePrice(),null,0);
                    if(filter==null)
                        trdList.addTrade(trd);
                    else if(filter.filterTrade(start,buy,sl))
                        trdList.addTrade(trd);
                    trdStart=false;

                }
            }


            if(indf.getValue()>high)
                high=indf.getValue();
            if(indf.getValue()<low)
                low=indf.getValue();
        }
        return trdList;

    }

    @Override
	public String toString()
	{
            StringBuilder buffer = new StringBuilder();
            double pct=0;
            if(params.get(1) instanceof Double)
                pct=(Double)params.get(1);
            else
                pct=((Integer)params.get(1))*1.0;
            if(pct>0)
            {
                buffer.append(pct).append("% ");
            }
            //buffer.append("Simple Change Open Opposite ");
            buffer.append("Open9 ");
            return buffer.toString();
	}

//    public int compareTo(AbstractOpen o) {
//        if(!(o instanceof SimpleChangeOpenOpp) || o==null)
//            return -1;
//        else
//        {
//            return 0;
//        }
//    }

}
