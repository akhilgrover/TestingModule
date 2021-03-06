package indicator;

import Share.*;
import java.util.HashMap;

public class BetaIndicator extends AbstractIndicator {

    VolatilityIndicator vol;
    CorrelationSIndicator corr;

    /**
     * Method init
     *
     *
     */
    public void init() {
        this.params = new HashMap();
        params.put(1, 20);
        this.name = "Beta";
        this.paramCount = 1;
        vol=new VolatilityIndicator();
        vol.init(params);
        corr=new CorrelationSIndicator();
        corr.init(params);
    }

    public void init(HashMap param) {
        this.params = param;
        this.name = "Beta";
        this.paramCount = 1;
        vol=new VolatilityIndicator();
        vol.init(params);
        corr=new CorrelationSIndicator();
        corr.init(params);
    }

    /**
     * Method buildIndicator
     *
     *
     * @return: Indicator result
     *
     */
    @Override
    public IndicatorList buildIndicator(ShareList sl, ShareList slIndex) {
        int param1 = (Integer) params.get(1);
        IndicatorList beta = new IndicatorList(sl.getSize());
        IndicatorList ilVolSL=vol.buildIndicator(sl);
        IndicatorList ilVolInd=vol.buildIndicator(slIndex);
        IndicatorList ilCorr=corr.buildIndicator(sl, slIndex);
        for(int i=0;i<ilVolSL.getSize();i++)
        {
            IndicatorField indfSL=ilVolSL.getSharedata(i);
            if(ilVolInd.isDatePresent(indfSL.getDDate())>-1)
            {
                IndicatorField indfInd=ilVolInd.getSharedata(ilVolInd.isDatePresent(indfSL.getDDate()));
                if(ilCorr.isDatePresent(indfSL.getDDate())>-1)
                {
                    IndicatorField indfCorr=ilCorr.getSharedata(ilCorr.isDatePresent(indfSL.getDDate()));
                    double d=0;
                    d=(indfSL.getValue()*indfCorr.getValue())/indfInd.getValue();
                    //System.out.println(indfSL.getDDate()+","+indfSL.getValue()+","+indfCorr.getValue()+","+indfInd.getValue());
                    IndicatorField inf=new IndicatorField(indfSL.getDDate(), d, 0);
                    beta.addIndField(inf);
                }
            }
        }
        return beta;
    }

    @Override
    public String toString() {
        int param1 = (Integer) params.get(1);
        StringBuilder buffer = new StringBuilder();
        //buffer.append(name);
        //buffer.append(" ");
        buffer.append(param1);
        buffer.append("d Beta");
        return buffer.toString();
    }

    @Override
    public IndicatorList buildIndicator(ShareList sl) {
        throw new UnsupportedOperationException("Not supported For Beta.");
    }
}