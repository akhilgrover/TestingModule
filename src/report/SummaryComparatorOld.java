package report;

import java.util.Comparator;

public class SummaryComparatorOld implements Comparator
{
    protected boolean asc=false;
    private String field;

    public SummaryComparatorOld(boolean asc)
    {
        this.asc=asc;
        field="TProfit";
    }

    public SummaryComparatorOld(String field,boolean asc)
    {
        this.asc=asc;
        this.field=field;
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
        Double ret=0.0;
        if(field.equals("TProfit"))
            ret=s2.getTProfit()-s1.getTProfit();
        else if(field.equals("TCount"))
            ret=(s2.getTCount()-s1.getTCount())/1.0;
        if(asc)
            ret=-ret;
        return ret.intValue();
    }
}
