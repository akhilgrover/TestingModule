package trade.filter;

import Share.ShareList;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.HashMap;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface Filters
{
    public boolean filterTrade(int shareIndex, int buy, ShareList sl);

    public void buildFilter(int buy, ShareList sl);

    @Override
	public String toString();

    public int getParamCount();

    public HashMap getParams();

    public void setParams(HashMap param);

    public void clear();
}
