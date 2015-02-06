package trade.open;

import indicator.*;
import Share.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.HashMap;
import trade.*;
import trade.filter.*;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface Open
{
    public TradeList fillOpen(IndicatorList indList,ShareList sl,Filters filter);

    @Override
	public String toString();

    public int getParamCount();

    public HashMap getParams();

    public int getBuy();

    public void setParams(HashMap param);

    public void setBuy(int buy);

}
