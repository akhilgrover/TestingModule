package trade.close;


import trade.*;
import indicator.*;
import Share.*;
import trade.filter.*;
import java.util.*;

public class DayCutClose extends AbstractClose
{
	/*private int days;
	private int buy;
        private int params=1;
	*/
    
        public DayCutClose()
	{
		this.paramCount=1;
	}
        
	public DayCutClose(HashMap param,int buy)
	{
		this.params=param;
		this.buy=buy;
                this.paramCount=1;
	}

	public TradeList fillClose(TradeList trdList, IndicatorList indList, 
					ShareList sl, Filters filter)
	{
            int days=0;
            if(params.get(1) instanceof Double)
                days=((Double)params.get(1)).intValue();
            else
                days=(Integer)params.get(1);
            TradeList trdListNew=new TradeList();
            Trade trd=null,trdOld;
            for(int i=0;i<trdList.getSize();i++)
            {
                trdOld=trdList.getTrade(i);
                int startIndex=sl.isDatePresent(trdOld.getStartDate());
                int endIndex=sl.getSize()-1;
                if(startIndex+days<sl.getSize())
                    endIndex=startIndex+days;
                else
                    continue;
                Date endDate=sl.getSharedata(endIndex).getDate();
                double sharePrice=sl.getSharedata(endIndex).getClosePrice();
                trd=new Trade(trdOld.getStartDate(),trdOld.getStartPrice(),endDate,sharePrice);
                if(filter==null || filter.filterTrade(endIndex,buy,sl))
                    trdListNew.addTrade(trd);
            }
            return trdListNew;
	}
        
        
    @Override
	public String toString()
	{
            //String sep = System.getProperty("line.separator");
            int days=0;
            if(params.get(1) instanceof Double)
                days=((Double)params.get(1)).intValue();
            else
                days=(Integer)params.get(1);
            StringBuilder buffer = new StringBuilder();
            buffer.append(days);
            buffer.append(" d Close1 ");


            return buffer.toString();
	}

    /*public int compareTo(AbstractClose o) {
        if(!(o instanceof DayCutClose) || o==null)
            return -1;
        else
        {
            int daysO=(Integer)o.params.get(1);
            int days=(Integer)params.get(1);
            return days-daysO;
        }
    }*/
	
}
