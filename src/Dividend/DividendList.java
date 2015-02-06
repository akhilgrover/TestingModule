package Dividend;



import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import javolution.util.*;

public class DividendList implements Serializable
{

    //private ArrayList<DividendData> divd;
    private FastTable<DividendData> divd;
    private TreeMap<Date,Integer> dDate;
    private char[] Share;
    //private HashMap<String,Double> dRange;
    //private static final SimpleDateFormat sdf=new SimpleDateFormat("ddmmyy") ;

    /**
     * Method DividendList
     *
     */
    public DividendList() {
        //divd=new ArrayList();
        divd=new FastTable<DividendData>();
        dDate=new TreeMap<Date,Integer>();
        //dRange=new HashMap<String, Double>();//.shared().setKeyComparator(FastComparator.STRING);
    }

    public DividendList(int count,String share) {
        //shd=new ArrayList(count);
        divd=new FastTable<DividendData>(count);
        dDate=new TreeMap<Date, Integer>();
        //dRange=new HashMap<String, Double>();//.shared().setKeyComparator(FastComparator.STRING);
        this.Share=share.toCharArray();
    }

    public void addDividendData(DividendData dd)
    {
        divd.add(dd);
        dDate.put(dd.getDate(), divd.size()-1);
    }

    public void removeDividendData(DividendData dd)
    {
        divd.remove(dd);
        dDate.remove(dd.getDate());
    }

    public void removeDividendDataAll()
    {
        divd.removeAll(divd);
        dDate.clear();
    }

    public DividendData getDividendData(int i)
    {
        return divd.get(i);
    }

    public int getSize()
    {
        return divd.size();
    }

    public String getShare()
    {
        return new String(Share);
    }

    public int isDatePresent(Date dd)
    {
        int ret=-1;
        Object d=dDate.get(dd);
        if(d!=null)
            ret=(Integer)d;
        return ret;
    }

    public int isLowerDatePresent(Date dd)
    {
        int ret=-1;
        Date d=dDate.lowerKey(dd);
        if(d!=null)
            ret=(Integer)dDate.get(d);
        return ret;
    }

    public int isHigherDatePresent(Date dd)
    {
        int ret=-1;
        Date d=dDate.higherKey(dd);
        if(d!=null)
            ret=(Integer)dDate.get(d);
        return ret;
    }

    public double getDividendDataRangeAlt(Date dt1, Date dt2)
    {
        int startIndex=-1,endIndex=-1;
        //String sb=sdf.format(dt1).concat(sdf.format(dt2));
        double sum=0.0;//dRange.get(sb);
        //if(sum==0)
        {
            boolean found=false;
            //DividendList dl = null;
            Date d1=dDate.higherKey(dt1);
            if(d1!=null){
                startIndex=dDate.get(d1);

                Object ob=dDate.get(dt2);
                if(ob!=null){
                    endIndex=(Integer)ob;
                    found=true;
                } else {
                    Date d2=dDate.lowerKey(dt2);
                    if(d2!=null) {
                        endIndex=dDate.get(d2);
                        found = true;
                    } else {
                        found = false;
                    }
                }
            }
            sum=0.0;
            if(found){

                //dl = new DividendList((endIndex-startIndex+1), this.getShare());
                for(int i=startIndex; i<=endIndex; i++){
                    sum+=this.divd.get(i).getAmount();
                    //dl.addDividendData(this.getDividendData(i));
                }
            }
            //dRange.put(sb, sum);
        }
        return sum;
    }

    public double getDividendDataRangeOld(Date dt1, Date dt2)
    {
        double sum=0.0;
        for(DividendData d:divd)
        {
            Date dd=d.getDate();
            if(dd.after(dt2))
                break;
            if(dd.after(dt1))
                sum+=d.getAmount();

        }

        return sum;
    }

    public double getDividendDataRange(Date dt1, Date dt2)
    {
        double sum=0.0;
        if(dt1.after(dt2))
            return sum;
        SortedMap<Date,Integer> dts=dDate.subMap(dt1, false, dt2, true);
        //for(DividendData d:divd)
        for(Date dd:dts.keySet())
        {
            //Date dd=d.getDate();
            DividendData d=divd.get(dts.get(dd));
            //if(dd.after(dt2))
            //    break;
            //if(dd.after(dt1))
                sum+=d.getAmount();

        }

        return sum;
    }

    public ListIterator getIterator()
    {
        return divd.listIterator();
    }

}
