package indicator;

import Share.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

public class AtrIndicator extends AbstractIndicator {

    /**
     * Method init
     *
     *
     */
    @Override
    public void init() {
        this.params = new HashMap();
        params.put(1, 14);
        this.name = "ATR";
        this.paramCount = 1;
    }

    @Override
    public void init(HashMap param) {
        this.params = param;
        this.name = "Atr";
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
        int param1 = (Integer) params.get(1);
        IndicatorList atr = new IndicatorList(sl.getSize());
        IndicatorField indf;
        double maxc = 0.0;
        double tr = 0.0;
        double atrv = 0.0;
        for (int k = 1; k < sl.getSize(); k++) {
            ShareData sd = sl.getSharedata(k);
            ShareData sdL = sl.getSharedata(k-1);
            double hl=sd.getHigh()-sd.getLow();
            double hc=sd.getHigh()-sdL.getClosePrice();
            double lc=sdL.getClosePrice()-sd.getLow();
            maxc=Double.max(hl,Double.max(hc, lc));
            if (k < param1) {
                atrv+=(maxc/param1);
                
                indf = new IndicatorField(sd.getDate(), hl, hl);
                atr.addIndField(indf);
            } else {
                tr=maxc-atrv;
                atrv+=(tr/param1);
                indf = new IndicatorField(sd.getDate(), atrv, atrv);
                atr.addIndField(indf);
            }
        }
        return atr;
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
}