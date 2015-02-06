package indicator;

import Share.*;
import java.util.ArrayList;
import java.util.HashMap;
import ma.MA;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

public class VolatilityIndicator extends AbstractIndicator {

    /**
     * Method init
     *
     *
     */
    public void init() {
        this.params = new HashMap();
        params.put(1, 20);
        this.name = "Volatility";
        this.paramCount = 1;
    }

    public void init(HashMap param) {
        this.params = param;
        this.name = "Volatility";
        this.paramCount = 1;
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
        int param1 = (Integer) params.get(1);
        int param2 = 0;
        ma.MA ma = null;
        if (params.get(2) != null) {
            param2 = (Integer) params.get(2);
            ma=new MA(param2, MA.Exponential);
        }
        double s1closeLog[] = new double[param1];
        double volt_value1 = 0;
        IndicatorList vol = new IndicatorList(sl.getSize());
        IndicatorField indf;
        StandardDeviation stdDev = new StandardDeviation();
        ArrayList<ShareData> al1 = new ArrayList<ShareData>();
        for (int k = 0; k < sl.getSize(); k++) {
            ShareData sd = sl.getSharedata(k);
            if (k < param1) {
                al1.add(sd);
            } else {
                if (k > param1) {
                    al1.remove(0);
                }
                al1.add(sd);
                for (int j = 0; j < param1; j++) {
                    s1closeLog[j] = Math.log(al1.get(j + 1).getClosePrice() / al1.get(j).getClosePrice());
                }
                volt_value1 = stdDev.evaluate(s1closeLog) * Math.sqrt(1) * 100;
                if (Double.isNaN(volt_value1)) {
                    volt_value1 = 0;
                } else if (volt_value1 >= 100) {
                    volt_value1 = 99.99;
                }
                if(ma==null)
                    indf = new IndicatorField(sd.getDate(), volt_value1, volt_value1);
                else
                    indf = new IndicatorField(sd.getDate(), volt_value1, ma.next(volt_value1));
                vol.addIndField(indf);
            }
        }
        return vol;
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