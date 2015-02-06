package indicator;

import Share.*;
import Utils.Fibonacci;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class FibIndicator extends AbstractIndicator {

    /**
     * Method init
     *
     *
     */
    @Override
    public void init() {
        this.params = new HashMap();
        params.put(1, 30);
        this.name = "Fib";
        this.paramCount = 1;
    }

    @Override
    public void init(HashMap param) {
        this.params = param;
        this.name = "Fib";
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
        ArrayList<ShareData> nsl=new ArrayList<>();
        for (int k = 0; k < sl.getSize(); k++) {
            ShareData sd = sl.getSharedata(k);
            nsl.add(sd);
            if (k >= param1) {
                double[] mm=getMaxMin(nsl);
                TreeSet<Double> fibLevels=Fibonacci.fibonacciRetracement(mm[0], mm[1]);
                indf = new IndicatorField(sd.getDate(), fibLevels.first(), fibLevels.last());
                atr.addIndField(indf);
            }
            if(k>=param1-1){
                nsl.remove(0);
            } else if(k>0) {
                indf = new IndicatorField(sd.getDate(), sd.getLow(), sd.getHigh());
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

    private double[] getMaxMin(ArrayList<ShareData> nsl) {
        double[] ret=new double[2];
        ret[0]=nsl.get(0).getLow();
        ret[1]=nsl.get(0).getHigh();
        for(ShareData sd:nsl){
            if(sd.getLow()<ret[0])
                ret[0]=sd.getLow();
            if(sd.getHigh()>ret[1])
                ret[1]=sd.getHigh();
        }
        return ret;
    }
}