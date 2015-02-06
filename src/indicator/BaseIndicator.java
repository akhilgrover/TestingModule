package indicator;

import Share.*;
import java.util.HashMap;

public interface BaseIndicator {


	public void init();

        public void init(HashMap params);

	//public abstract void init(int period);

	public IndicatorList buildIndicator() throws Exception;

	public IndicatorList buildIndicator(ShareList sl);

        public IndicatorList buildIndicator(ShareList sl, ShareList sl1);

	public String getName();

    @Override
	public String toString();

        public int getParamCount();
}
