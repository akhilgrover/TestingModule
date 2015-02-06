package trade.close;

import indicator.*;
import Share.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.HashMap;
import trade.*;
import trade.filter.*;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface Close
{

    /**
     * Fill Close Calculates the closes of the trades. it uses the open dates
     * from the trade list passed to it and calculates the closes.
     *
     * @param trdList
     *  Trade list with open trades only to fill closes
     * @param indList
     *  Indicator List to calculate the closes
     * @param sl
     *  Sharelist for rference and to get the close price
     * @param filter
     *  filter the close or can be null for no filter
     * @return
     *  returns the complete trade list with open and closed trades.
     */
    public TradeList fillClose(TradeList trdList,IndicatorList indList,ShareList sl,Filters filter);

    /**
     *
     * @return
     *  returns the String with Description of close and its parameters
     */
    @Override
	public String toString();

    /**
     *
     * @return
     *  returns the parameter count used by the specific close type
     */
    public int getParamCount();

    /**
     *
     * @return
     *   returns the parameters hashmap.
     */
    public HashMap getParams();

    public int getBuy();

    public void setParams(HashMap param);

    public void setBuy(int buy);

}
