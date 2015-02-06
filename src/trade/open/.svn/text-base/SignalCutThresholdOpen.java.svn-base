/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.open;

import Share.ShareData;
import Share.ShareList;
import indicator.*;
import java.util.HashMap;
import trade.*;
import trade.filter.Filters;

/**
 *
 * @author Admin
 */
public class SignalCutThresholdOpen extends AbstractOpen
{
    public SignalCutThresholdOpen()
    {
        this.paramCount=2;
    }

    public SignalCutThresholdOpen(HashMap params,int buy)
    {
        this.buy=buy;
        this.params=params;
        this.paramCount=2;
    }

    public TradeList fillOpen(IndicatorList indList, ShareList sl, Filters filter)
    {
        TradeList trdList=new TradeList();
        Trade trd;
        double thresh=(Double)params.get(1);
        int cond =((Double)params.get(2)).intValue();
        for(int i=1;i<indList.getSize();i++)
        {
            IndicatorField indf=indList.getSharedata(i);
            IndicatorField indfl=indList.getSharedata(i-1);
            boolean valid=true;
            switch(cond)
            {
                case 1:
                    if(indf.getValue()>thresh)
                        valid=true;
                    else
                        valid=false;
                    break;
                case -1:
                    if(indf.getValue()<thresh)
                        valid=true;
                    else
                        valid=false;
                    break;
            }
            if(valid && ((buy==1 && indf.getValue()>indf.getSignal() && indfl.getValue()<=indfl.getSignal()) ||
                    (buy==0 && indf.getValue()<indf.getSignal() && indfl.getValue()>=indfl.getSignal())))
            {
                int start=sl.isDatePresent(indf.getDDate());
                ShareData sd=sl.getSharedata(start);
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
            StringBuilder buffer = new StringBuilder();
            buffer.append("SignalCut ");
            int cond =((Double)params.get(2)).intValue();
            switch(cond)
            {
                case 1:
                    buffer.append("Above ");
                    break;
                case -1:
                    buffer.append("Below ");
                    break;
            }
            buffer.append(params.get(1));
            buffer.append(" Open5 ");
            //buffer.append(" Thrshold Open ");
            return buffer.toString();
	}

//    public int compareTo(AbstractOpen o) {
//        if(!(o instanceof SignalCutThresholdOpen) || o==null)
//            return -1;
//        else
//        {
//            double thresholdo=(Double) o.params.get(1);
//            double threshold=(Double) params.get(1);
//            return Double.valueOf(threshold-thresholdo).intValue();
//        }
//    }

}
