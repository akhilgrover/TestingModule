package report;

import java.util.Comparator;

/**
 *
 */
public class PPTPDComparatorOld implements Comparator
{
    protected boolean asc=false;

    public PPTPDComparatorOld(boolean asc)
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
        Double ret=(s2.getPPTPD())-(s1.getPPTPD());
        if(asc)
            ret=-ret;
        return ret.intValue();
    }
}
