package indicator;

import Share.*;
import Utils.Fibonacci;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import ma.MA;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

public class PinBarIndicator extends AbstractIndicator {

    /**
     * Method init
     *
     *
     */
    @Override
    public void init() {
        this.params = new HashMap();
        params.put(1, 9);
        params.put(2, 5);
        params.put(3, 1.5);
        params.put(4, 3);
        this.name = "P";
        this.paramCount = 4;
    }

    @Override
    public void init(HashMap param) {
        this.params = param;
        this.name = "P";
        this.paramCount = 4;
    }

    /**
     * Method buildIndicator
     *
     *
     * @param sl
     * @return: Indicator result
     *
     */
    @Override
    public IndicatorList buildIndicator(ShareList sl) {
        double param1 = Double.parseDouble(params.get(1).toString());
        double param2 = Double.parseDouble(params.get(2).toString());
        double param3 = Double.parseDouble(params.get(3).toString());
        double param4 = Double.parseDouble(params.get(4).toString());
        IndicatorList Pin = new IndicatorList(sl.getSize());
        IndicatorField indf;
        double hlavg=0.0;
        double ema=0.0;
        MA avgPer=new MA((int)param1, MA.Simple);
        MA avgTrend=new MA((int)param2, MA.Simple);
        ArrayList<ShareData> nsl=new ArrayList<>();
        for (int k = 1; k < sl.getSize(); k++) {
            double val=0.0;
            ShareData sd = sl.getSharedata(k);
            nsl.add(sd);
            double hl = (sd.getHigh() - sd.getLow()) * 100 / sd.getLow();
            if (sd.getClosePrice() < sd.getOpenPrice()) {
                //-1
                if (hl >= hlavg * param3) {
                    if (ema < sd.getClosePrice()) {
                        double diff = (sd.getHigh() - sd.getLow()) / param4;
                        if (sd.getOpenPrice() <= sd.getLow() + diff) {
                            //-1
                            val = -1;                            
                        }
                    }
                }
            } else if (sd.getClosePrice() > sd.getOpenPrice()) {
                /*if (hl >= hlavg * param3)*/ {
                    if (ema > sd.getClosePrice()) {
                        double diff = (sd.getHigh() - sd.getLow()) / param4;
                        if (sd.getOpenPrice() >= sd.getHigh() - diff) {
                            //+1
                            val = 1;

                        }
                    }
                }
            }
            hlavg = avgPer.next(hl);
            ema = avgTrend.next(sd.getClosePrice());
            indf = new IndicatorField(sd.getDate(), val, 0);
            Pin.addIndField(indf);
        }
        return Pin;
    }

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
    
    private double[] getMaxMin(ArrayList<ShareData> nsl) {
        double[] ret = new double[2];
        ret[0] = nsl.get(0).getLow();
        ret[1] = nsl.get(0).getHigh();
        for (ShareData sd : nsl) {
            if (sd.getLow() < ret[0]) {
                ret[0] = sd.getLow();
            }
            if (sd.getHigh() > ret[1]) {
                ret[1] = sd.getHigh();
            }
        }
        return ret;
    }
    
}