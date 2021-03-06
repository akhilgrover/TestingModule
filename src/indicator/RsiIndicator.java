package indicator;

import Share.*;
import java.util.HashMap;
import java.util.Iterator;
import ma.MA;

public class RsiIndicator extends AbstractIndicator {

    /**
     * Method init
     *
     *
     */
    @Override
    public void init() {
        this.params = new HashMap();
        params.put(1, 14);
        params.put(2, 1);
        this.name = "RSI";
        this.paramCount = 2;
    }

    @Override
    public void init(HashMap param) {
        this.params = param;
        this.name = "RSI";
        this.paramCount = 2;
    }

    /*
    public void init(int periods,int signal)
    {
    this.period=periods;
    this.signal=signal;
    this.name="RSI";
    this.params=2;
    }*/
    /**
     * Method buildIndicator
     *
     *
     * @return: Indicator result
     *
     *
     * @param sl
     * @return
     */
    @Override
    public IndicatorList buildIndicator(ShareList sl) {
        int param1 = (Integer) params.get(1);
        int param2 = (Integer) params.get(2);
        IndicatorList rsi = new IndicatorList(sl.getSize());
        IndicatorField indf;
        //double dw[] = new double[sl.getSize()];
        //double uw[] = new double[sl.getSize()];
        double dw = 0;
        double uw = 0;
        double dwLst = 0;
        double uwLst = 0;
        //dw[0] = 0;
        //uw[0] = 0;
        double d = 0;
        double u = 0;
        Iterator itr = sl.getIterator();
        MA sma = new MA(param2, MA.Exponential);
        ShareData sdYest = (ShareData) itr.next();
        for (int i = 1; i < sl.getSize(); i++) {
            ShareData sdToday = (ShareData) itr.next();
            d = 0;
            u = 0;
            if (sdToday.getClosePrice() - sdYest.getClosePrice() > 0) {
                u = sdToday.getClosePrice() - sdYest.getClosePrice();
            }
            if (sdToday.getClosePrice() - sdYest.getClosePrice() < 0) {
                d = sdYest.getClosePrice() - sdToday.getClosePrice();
            }
            dw = dwLst + (1.0 / param1) * (d - dwLst);
            uw = uwLst + (1.0 / param1) * (u - uwLst);
            dwLst = dw;
            uwLst = uw;
            double rs = 100 - (100 / (1 + (uw / dw)));
            double sig = sma.next(rs);
            indf = new IndicatorField(sdToday.getDate(), rs, sig);
            rsi.addIndField(indf);
            sdYest = sdToday;
        }
        return rsi;
    }

    @Override
    public String toString() {
        if (string == null) {
            int param1 = (Integer) params.get(1);
            int param2 = (Integer) params.get(2);
            StringBuilder buffer = new StringBuilder();
            //buffer.append(name);
            //buffer.append(" ");
            buffer.append(param1);
            buffer.append("d ");
            buffer.append(param2);
            buffer.append("dSig Indicator1 ");

            string = buffer.toString();
        }
        return string;
    }
}