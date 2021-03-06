package trade.open;

import trade.*;
import trade.filter.*;
import indicator.*;
import Share.*;
import java.util.HashMap;

public class DaysThresholdOpen extends AbstractOpen
{
	
        public DaysThresholdOpen()
	{
            this.paramCount=2;
        }
        
	public DaysThresholdOpen(HashMap param,int buy)
	{
		this.params=param;
		this.buy=buy;
                this.paramCount=2;
	}

	public TradeList fillOpen(IndicatorList indList, ShareList sl, 
					Filters filter)
	{
                int days=0;
                if(params.get(1) instanceof Double)
                    days=((Double)params.get(1)).intValue();
                else 
                    days=(Integer)params.get(1);
                double threshold=(Double)params.get(2);
		TradeList trdList=new TradeList();
		Trade trd;
		for(int i=1;i<indList.getSize();i++)
		{
			boolean invalid=false;
			if(i>days-1)
			{
				for(int k=0;k<days-1;k++)
				{
					IndicatorField	indTemp=indList.getSharedata(i-(k+2));
					if((buy==0 && indTemp.getValue()>threshold) || (buy==1 && indTemp.getValue()<threshold))
					{
						invalid=true;
					}
				}
			}
			IndicatorField	indf=indList.getSharedata(i);
			IndicatorField	indfl=indList.getSharedata(i-1);
			if(((buy==1 && indf.getValue()>threshold && indfl.getValue()<=threshold) || 
				((buy==0 && indf.getValue()<threshold && indfl.getValue()>=threshold))) && !invalid)
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
            int days=0;
            if(params.get(1) instanceof Double)
                days=((Double)params.get(1)).intValue();
            else
                days=(Integer)params.get(1);
            double threshold=(Double)params.get(2);
            StringBuilder buffer = new StringBuilder();
            buffer.append(days);
            buffer.append(" d ");
            buffer.append(threshold);
            buffer.append(" Open2 ");
            return buffer.toString();
	}

//    public int compareTo(AbstractOpen o) {
//        if(!(o instanceof DaysThresholdOpen) || o==null)
//            return -1;
//        else
//        {
//            if(o.paramCount!=this.paramCount)
//                return -1;
//            else
//            {
//                int periodO=(Integer)o.params.get(1);
//                int typeO=(Integer)o.params.get(2);
//                int period=(Integer)params.get(1);
//                int type=(Integer)params.get(2);
//                if(typeO!=type)
//                    return -1;
//                else
//                    return period-periodO;
//            }
//        }
//    }


}
