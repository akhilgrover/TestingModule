/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Summary.SummaryFilter;

import java.util.HashMap;
import report.Summary;

/**
 *
 * @author admin
 */
public class DataSourceFilter extends BasicFilter {

    public DataSourceFilter() {
        super();
        params.put(1, 0);
        params.put(2, 999);
    }
    
    public DataSourceFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        int dayStart=(Integer)params.get(1);
        int dayEnd=(Integer)params.get(2);
        String share=sum.getShare();
        if(share.contains(" "))
        {
            //String shares[]=share.split(" ");
            //System.out.println(share.substring(10, share.lastIndexOf(" ")));
            String shares=share.substring(10,share.indexOf(" ", 10));
            
            try{
                int days=Integer.parseInt(shares);//[3]);
                boolean b=(days>=dayStart && days<=dayEnd);
                return b;
            }
            catch(Exception ex)
            {
                int days=Integer.parseInt(shares);//[2]);
                boolean b=(days>=dayStart && days<=dayEnd);
                return b;
            }
        }
        else
            return true;
    }

}
