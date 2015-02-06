package Summary.SummaryCompare;

import java.util.Arrays;
import java.util.Collections;
import report.Summary;
import report.Summary;
import java.util.Comparator;
import org.apache.commons.lang3.ArrayUtils;

public class SummaryComparator<T> implements Comparator
{
    protected boolean asc=false;
    private String field;

    public static final String PROFIT="TProfit";
    public static final String TRADE_COUNT="TCount";
    public static final String PPTE="PPTE";
    public static final String RATIO_COUNT="RCount";
    public static final String RATIO_PROFIT="RProfit";
    public static final String DAY_PERCENT="DPercent";
    public static final String DAY_LENGTH="DLen";
    public static final String MG="MG%";
    public static final String ML="ML%";
    public static final String GRAPH_GRADE="GG";
    public static final String GRAPH_GRADE_P="GG%";
    public static final String GRAPH_GRADE_PL="GGL%";
    public static final String GRAPH_GRADE_N="GGN";
    public static final String GRAPH_GRADE_NL="GGNL";
    public static final String DRAWDOWN="DD";
    public static final String FLAT_GRADE="FG";
    public static final String POSITIVE_GRADE="PG";
    public static final String FLAT_PROFIT="FP";
    public static final String BEST="BEST";
    public static final String MAX_LOW_YEAR="MLY";

    public SummaryComparator(boolean asc)
    {
        this.asc=asc;
        field="TProfit";
    }

    public SummaryComparator(String field,boolean asc)
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
        int ret=0;
        if(field.equals("TProfit"))
            ret=Double.compare(s2.getTProfit(), s1.getTProfit());
        else if(field.equals("TCount"))
            ret=(s2.getTCount()-s1.getTCount());
        else if(field.equals("PPTE"))
        {
            double val1=s1.getPPTExec()*100.0/(s1.getTProfit()/s1.getTCount());
            double val2=s2.getPPTExec()*100.0/(s2.getTProfit()/s2.getTCount());
            if(val1>100)
                val1=0;
            if(val2>100)
                val2=0;
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("RCount"))
        {
            double val1=(s1.getLossTradeCount()>0)?s1.getGainTrades()*1.0/s1.getLossTradeCount():s1.getGainTrades()*1.0;
            double val2=(s2.getLossTradeCount()>0)?s2.getGainTrades()*1.0/s2.getLossTradeCount():s2.getGainTrades()*1.0;
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("RProfit"))
        {
            double val1=(s1.getLossAvgTrd()!=0)?s1.getGainAvgTrd()/(s1.getLossAvgTrd()*-1):s1.getGainAvgTrd();
            double val2=(s2.getLossAvgTrd()!=0)?s2.getGainAvgTrd()/(s2.getLossAvgTrd()*-1):s2.getGainAvgTrd();
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("DPercent"))
        {
            ret=Double.compare(s2.getDayPercentage(), s1.getDayPercentage());
        }
        else if(field.equals("DLen"))
        {
            ret=Double.compare(s2.getAvgTradeLength(), s1.getAvgTradeLength());
        }
        else if(field.equals("MG%"))
        {
            double val1=s1.getGainHighPercent();
            double val2=s2.getGainHighPercent();
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("ML%"))
        {
            double val1=s1.getLossHighPercent();
            double val2=s2.getLossHighPercent();
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("GG"))
        {
            int val1=s1.getYearlyGrade();
            int val2=s2.getYearlyGrade();
            ret=val2-val1;
            if(ret==0)
            {
                ret=Double.compare(s2.getTProfit(), s1.getTProfit());
            }
        }
        else if(field.equals("GG%"))
        {
            double val1=s1.getYearlyGradePerc();
            double val2=s2.getYearlyGradePerc();
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("GGL%"))
        {
            double val1=s1.getLimitYearlyGradePerc();
            double val2=s2.getLimitYearlyGradePerc();
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("GGN"))
        {
            int val1=s1.getPosGradePerc();
            int val2=s2.getPosGradePerc();
            ret=val2-val1;
            if(ret==0)
            {
                ret=Double.compare(s2.getTProfit(), s1.getTProfit());
            }
        }
        else if(field.equals("GGNL"))
        {
            int val1=s1.getLimitGradePerc();
            int val2=s2.getLimitGradePerc();
            ret=val2-val1;
            if(ret==0)
            {
                ret=Double.compare(s2.getTProfit(), s1.getTProfit());
            }
        }
        else if(field.equals("DD"))
        {
            double val1=Math.min(Collections.min(Arrays.asList(ArrayUtils.toObject(s1.getDrawDown().toArray()))), Collections.min(Arrays.asList(ArrayUtils.toObject(s1.getDrawDownTrades().toArray()))));
            double val2=Math.min(Collections.min(Arrays.asList(ArrayUtils.toObject(s2.getDrawDown().toArray()))), Collections.min(Arrays.asList(ArrayUtils.toObject(s2.getDrawDownTrades().toArray()))));
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("FP"))
        {
            double val1=s1.getFlatProfit();
            double val2=s2.getFlatProfit();
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("FG"))
        {
            int val1=s1.getFlatGrade();
            int val2=s2.getFlatGrade();
            ret=val2-val1;
        }
        else if(field.equals("PG"))
        {
            int val1=s1.getPositiveGrade();
            int val2=s2.getPositiveGrade();
            ret=val2-val1;
        }
        else if(field.equals("BEST"))
        {
            int val1=s1.getGoodScore();
            int val2=s2.getGoodScore();
            ret=val2-val1;
            if(ret==0)
                ret=Double.compare(s2.getTProfit(), s1.getTProfit());
        }
        else if(field.equals("MLY"))
        {
            double val1=s1.getNewHighDays();
            double val2=s2.getNewHighDays();
            ret=Double.compare(val2,val1);
        } else if(field.equals("Filter"))
        {
            String[] names=s1.getName().split(" ");
            int ind=Arrays.asList(names).indexOf("Filter1");
            String fValue = "0";
            if (ind > 0) {
                fValue = names[ind - 1];
                fValue = fValue.substring(0, fValue.length() - 1);
            }
            Integer val1=Integer.parseInt(fValue);

            names=s2.getName().split(" ");
            ind=Arrays.asList(names).indexOf("Filter1");
            fValue = "0";
            if (ind > 0) {
                fValue = names[ind - 1];
                fValue = fValue.substring(0, fValue.length() - 1);
            }
            Integer val2=Integer.parseInt(fValue);
            ret=val2.compareTo(val1);
        } else if(field.equals("250d")){

            double val1=s1.getEMAFilterProfit();
            double val2=s2.getEMAFilterProfit();
            ret=Double.compare(val2,val1);
        } else if(field.equals("250dA")){

            double val1=s1.getEMAFilterAboveWL();
            double val2=s2.getEMAFilterAboveWL();
            ret=Double.compare(val2,val1);
        } else if(field.equals("250dB")){

            double val1=s1.getEMAFilterBelowWL();
            double val2=s2.getEMAFilterBelowWL();
            ret=Double.compare(val2,val1);
        }
        else if(field.equals("250dAT")){

            Integer val1=s1.getEMAFilterAboveTrds();
            Integer val2=s2.getEMAFilterAboveTrds();
            ret=val2.compareTo(val1);
            //ret=Integer.compare(val2,val1);
        } else if(field.equals("250dBT")){

            Integer val1=s1.getEMAFilterBelowTrds();
            Integer val2=s2.getEMAFilterBelowTrds();
            //ret=Integer.compare(val2,val1);
            ret=val2.compareTo(val1);
        } else if(field.equals("FTPts")){

            double val1=s1.getHighLowPoints()[0];
            double val2=s2.getHighLowPoints()[0];
            ret=Double.compare(val2,val1);
        }
        if(asc)
            ret=-ret;
        return ret;
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setField(String field) {
        this.field = field;
    }
}
