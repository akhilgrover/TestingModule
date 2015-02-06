package Summary.SummaryCompare;

import java.util.ArrayList;
import java.util.HashMap;
import report.Summary;
import report.Summary;
import java.util.Comparator;
import javolution.util.FastMap;
import report.SystemGrade;

/**
 *
 */
public class GradeComparator implements Comparator
{
    protected boolean asc=false;
    //HashMap<String, SystemGrade> grades;

    public GradeComparator(boolean asc)
    {
        this.asc=asc;
    }

    //public GradeComparator(HashMap<String, SystemGrade> grades, boolean asc) {
    //    this.asc=asc;
    //    this.grades=grades;
    //}

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
        int ret=0;
        //if(grades.get(s1.getName()) instanceof SystemGrade)
        {
            //SystemGrade sg1=(SystemGrade) grades.get(s1.getName());
            //SystemGrade sg2=(SystemGrade) grades.get(s2.getName());
            SystemGrade sg1=s1.getSg();
            SystemGrade sg2=s2.getSg();
            if (sg1 == null && sg2 == null) return 0;
            if (sg1 != null && sg2 == null) return -1;
            if (sg1 == null && sg2 != null) return 1;
            ret=sg2.compareTo(sg1);
        }
        //else
        //{
        //    ret=Double.compare((Double)grades.get(s2.getName()), (Double)grades.get(s1.getName()));
        //}
        if(asc)
            ret=-ret;
        return ret;

    }
}
