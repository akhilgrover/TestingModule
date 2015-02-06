/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indicator;

/**
 *
 * @author gni
 */
import Share.ShareList;
import Share.ShareData;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

public class CorrelationIndicator extends AbstractIndicator {

    /**
     * Method init
     *
     *
     */
    public void init() {
        this.params = new HashMap(1);
        params.put(1, 85);   //period
        this.name = "Correlation";
        this.paramCount = 1;
    }

    public void init(HashMap param) {
        this.params = param;
        this.name = "Correlation";
        this.paramCount = 1;
    }

    /**
     * Method buildIndicator
     *
     *
     * @return: Indicator result
     *
     */
    @Override
    public IndicatorList buildIndicator(ShareList sl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IndicatorList buildIndicator(ShareList sl1, ShareList sl2) {
        int variance = (Integer) params.get(1);
        //int type=(Integer)params.get(2);
        //Date fromDt=G.myformat.format(params.get(3));
        //Date toDt=G.myformat.format(params.get(4));
        double s1close[] = new double[variance];
        double s2close[] = new double[variance];
        double corr_value = 0.0;
        IndicatorList idl = new IndicatorList(sl1.getSize());
        IndicatorField idf = null;
        PearsonsCorrelation pcor = new PearsonsCorrelation();
        ArrayList<ShareData> al1 = new ArrayList<ShareData>();
        ArrayList<ShareData> al2 = new ArrayList<ShareData>();

//        int currIndx = 0;
//        int backIndx = 0; //adding one because we need e.g. 20 records only instead of 21(like in volatility)
//        if(currIndx < variance){
//            currIndx = variance - 1;
//            backIndx = 0;
//        }

        int shlEndIdx = sl1.getSize() - 1;  //e.g. size 3522 [so i=0 to 3521] hence shlEndIdx=3522-2=3520 to avoid today's date
        for (int k = 0; k <= shlEndIdx; k++) {
            ShareData sd1 = sl1.getSharedata(k);
            if(sl2.isDatePresent(sd1.getDate())==-1)
                continue;
            ShareData sd2 = sl2.getSharedata(sl2.isDatePresent(sd1.getDate()));
            //debug.sopWC("k = " + k + " = " + G.myformat.format(sd1.getDate()));
            if (k < (variance - 1)) {
                al1.add(sd1);
                al2.add(sd2);
            } else {
                if (k > (variance - 1)) {
                    al1.remove(0);
                    al2.remove(0);
                }
                al1.add(sd1);
                al2.add(sd2);
                for (int j = 0; j < variance; j++) {
                    s1close[j] = al1.get(j).getClosePrice();
                    s2close[j] = al2.get(j).getClosePrice();
                    //debug.sopWC("******   " + al1.get(j).toString()+ "   " + G.myformat.format(sd1.getDate()) + "   " + s1close[j]);
                    //debug.sopWC("\t   " + al2.get(j).toString()+ "   " + G.myformat.format(sd2.getDate()) + "   " + s2close[j]);
                }
                corr_value = pcor.correlation(s1close, s2close);
                if (Double.isNaN(corr_value)) {
                    corr_value = 0.00;
                }
                idf = new IndicatorField(sd1.getDate(), corr_value, 0);
                idl.addIndField(idf);
            }
        }
        idf = null;
        return idl;
    }

    //to add todays indicator in the list, which is missing in database.
    //if shlEndDate passed null return IDL only for the idlEndDt day.
//    public IndicatorList buildIndicator(ShareList sl1,ShareList sl2, Date idlEndDt, Date shlEndDate) {
//        int variance=(Integer)params.get(1);
//        //int type=(Integer)params.get(2);
//        //Date fromDt=G.myformat.format(params.get(3));
//        //Date toDt=G.myformat.format(params.get(4));
//        double s1close[] = new double[variance];
//        double s2close[] = new double[variance];
//        double corr_value=0.0;
//        IndicatorList idl = new IndicatorList(sl1.getSize());
//        IndicatorField idf = null;
//        PearsonsCorrelation pcor = new PearsonsCorrelation();
//        ArrayList<ShareData> al1=new ArrayList<ShareData>();
//        ArrayList<ShareData> al2=new ArrayList<ShareData>();
//
//        int currIndx = sl1.isDatePresent(idlEndDt);
//        if(currIndx==-1){
//            currIndx = sl1.isHigherDatePresent(idlEndDt);
//        } else {
//            if(shlEndDate!=null){
//                if(idlEndDt.compareTo(shlEndDate)==0 && idlEndDt.compareTo(G.today)==0)
//                    currIndx = currIndx;
//                else
//                    currIndx = currIndx + 1; //idlEndDt is already present, so need to start from next date
//            }
//        }
//        int backIndx = currIndx - variance + 1; //adding one because we need e.g. 20 records only instead of 21(like in volatility)
//        if(backIndx < 0){
//            return idl;
//        }
//
//        int shlEndIdx = -1;
//        if(shlEndDate!=null)
//            shlEndIdx = sl1.isDatePresent(shlEndDate);
//        else
//            shlEndIdx = currIndx;
//
//        for (int k = backIndx; k <= shlEndIdx; k++)
//        {
//            ShareData sd1=sl1.getSharedata(k);
//            ShareData sd2=sl2.getSharedata(k);
//            //debug.sopWC("k = " + k + " = " + G.myformat.format(sd1.getDate()));
//            if(k<(backIndx+variance-1)){
//                al1.add(sd1);
//                al2.add(sd2);
//            } else {
//                if(k>(backIndx+variance-1)){
//                    al1.remove(0);
//                    al2.remove(0);
//                }
//                al1.add(sd1);
//                al2.add(sd2);
//                for (int j = 0; j < variance; j++){
//                    s1close[j] = ((ShareData) (al1.get(j))).getClosePrice();
//                    s2close[j] = ((ShareData) (al2.get(j))).getClosePrice();
//                    //debug.sopWC("******   " + al1.get(j).toString()+ "   " + G.myformat.format(sd1.getDate()) + "   " + s1close[j]);
//                    //debug.sopWC("\t   " + al2.get(j).toString()+ "   " + G.myformat.format(sd2.getDate()) + "   " + s2close[j]);
//                }
//                corr_value = pcor.correlation(s1close, s2close);
//                if(Double.isNaN(corr_value))
//                    corr_value = 0.00;
//                idf = new IndicatorField(sd1.getDate(), corr_value);
//                idl.addIndField(idf);
//            }
//        }
//        idf = null;
//        return idl;
//    }
//
//
//
//    public IndicatorList buildIndicator(TreeMap<Date, Double> tm1, TreeMap<Date, Double> tm2) {
//        int variance=(Integer)params.get(1);
//        double s1close[] = new double[variance];
//        double s2close[] = new double[variance];
//        double corr_value=0.0;
//        IndicatorList idl = new IndicatorList(tm1.size());
//        IndicatorField idf = null;
//        PearsonsCorrelation pcor = new PearsonsCorrelation();
//        ArrayList<Double> al1=new ArrayList<Double>();
//        ArrayList<Double> al2=new ArrayList<Double>();
//
//        int k=0;
//        for (Date dt : tm1.keySet()){
//            k++;
//            if(!tm2.containsKey(dt))
//                continue;
//            Double d1=tm1.get(dt);
//            Double d2=tm2.get(dt);
//            if(k<variance){
//                al1.add(d1);
//                al2.add(d2);
//            } else {
//                if(k>variance){
//                    al1.remove(0);
//                    al2.remove(0);
//                }
//                al1.add(d1);
//                al2.add(d2);
//                for(int j=0; j<variance; j++){
//                    s1close[j] = al1.get(j);
//                    s2close[j] = al2.get(j);
//                    //debug.sopWC("******   " + al1.get(j).toString()+ "   " + G.myformat.format(sd1.getDate()) + "   " + s1close[j]);
//                    //debug.sopWC("\t   " + al2.get(j).toString()+ "   " + G.myformat.format(sd2.getDate()) + "   " + s2close[j]);
//                }
//                corr_value = pcor.correlation(s1close, s2close);
//                if(Double.isNaN(corr_value))
//                    corr_value = 0.00;
//                idf = new IndicatorField(dt, corr_value);
//                idl.addIndField(idf);
//            }
//        }
//        idf = null;
//        return idl;
//    }
    @Override
    public String toString() {
        int param1 = (Integer) params.get(1);
        StringBuilder buffer = new StringBuilder();
        buffer.append(name);
        buffer.append(" ");
        buffer.append(param1);
        buffer.append(" Period ");
        buffer.append(" ");
        return buffer.toString();
    }
}