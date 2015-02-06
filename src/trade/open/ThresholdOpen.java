package trade.open;

import trade.*;
import trade.filter.*;
import indicator.*;
import Share.*;
import java.util.HashMap;

public class ThresholdOpen extends AbstractOpen
{

        public ThresholdOpen()
        {
            this.paramCount=1;
        }

	public ThresholdOpen(HashMap param,int buy)
	{
            this.params=param;
            this.buy=buy;
            this.paramCount=1;
	}

	public TradeList fillOpen(IndicatorList indList, ShareList sl,
					Filters filter)
	{
            double threshold=(Double) params.get(1);
            TradeList trdList=new TradeList();
            Trade trd;
            for(int i=1;i<indList.getSize();i++)
            {
                IndicatorField indf=indList.getSharedata(i);
                IndicatorField indfl=indList.getSharedata(i-1);
                if((buy==1 && indf.getValue()>threshold && indfl.getValue()<=threshold) ||
                        ((buy==0 && indf.getValue()<threshold && indfl.getValue()>=threshold)))
                {
                    int start=sl.isDatePresent(indf.getDDate());
                    if(start==-1)
                    {
                        //System.out.print(indf.getDDate());
                        start=sl.isLowerDatePresent(indf.getDDate())+1;
                        if(start>=sl.getSize())
                            start=sl.getSize()-1;
                    }
                    if(start!=-1)
                    {
                        ShareData sd = sl.getSharedata(start);
                        trd=new Trade(indf.getDDate(),sd.getClosePrice(),null,0);
                        if(filter==null)
                            trdList.addTrade(trd);
                        else if(filter.filterTrade(start,buy,sl))
                            trdList.addTrade(trd);
                    }
                }
            }
            return trdList;
	}

    @Override
	public String toString()
	{
            if(name==null){
            //String sep = System.getProperty("line.separator");
            double threshold=(Double) params.get(1);
            StringBuilder buffer = new StringBuilder();
            buffer.append(threshold);
            //buffer.append(" Thrshold Open ");
            buffer.append(" Open1 ");
            name= buffer.toString();
            }
            return name;

	}

//    public int compareTo(AbstractOpen o) {
//        if(!(o instanceof ThresholdOpen) || o==null)
//            return -1;
//        else
//        {
//            double thresholdo=(Double) o.params.get(1);
//            double threshold=(Double) params.get(1);
//            return Double.valueOf(threshold-thresholdo).intValue();
//
//        }
//    }
}
