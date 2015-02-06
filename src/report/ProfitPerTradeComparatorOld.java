package report;

import java.util.Comparator;

public class ProfitPerTradeComparatorOld implements Comparator
{
    protected boolean asc=false;

    public ProfitPerTradeComparatorOld(boolean asc)
    {
        this.asc=asc;
    }
	
    public int compare(Object parm1, Object parm2)
    {
        if (parm1 == null && parm2 == null) return 0;
    	if (parm1 != null && parm2 == null) return -1;
    	if (parm1 == null && parm2 != null) return 1;
    	if (!(parm1 instanceof Summary) || !(parm2 instanceof Summary)) {
      		throw new IllegalArgumentException ("Should be Summary");
    	}
    	
    	Summary s1=(Summary) parm1;
    	Summary s2=(Summary) parm2;
    	Double ret=(s2.getTProfit()/s2.getTCount())-(s1.getTProfit()/s1.getTCount());
        if(asc)
            ret=-ret;
    	return ret.intValue();
    }

		
}
