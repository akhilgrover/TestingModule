package indicator;

import java.util.*;
import Share.*;

public class IndicatorGroup {
	
	
	/**
	 * Method IndicatorGroup
	 *
	 *
	 */
	public IndicatorGroup() {
	
	}
		
	public HashMap computeGroup(HashMap slist,BaseIndicator ind)
	{
		Iterator set=slist.entrySet().iterator();
		HashMap	indList=new HashMap();
		while(set.hasNext())
		{
			Map.Entry val=(Map.Entry)set.next();
			IndicatorList il=ind.buildIndicator((ShareList)val.getValue());
			indList.put(val.getKey(),il);
		}		
		return indList;
	}
}
