/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.close;

import java.io.Serializable;
import java.util.HashMap;
import trade.Trade;

/**
 *
 * @author admin
 */
public class AbstractIndividualClose implements IndividualClose,Serializable {
    private static final long serialVersionUID = 123456789L;
    
    private HashMap<Integer, Number> params;

    public AbstractIndividualClose(HashMap<Integer, Number> params) {
        this.params = params;
    }
    
    /**
     * @return the params
     */
    public HashMap<Integer, Number> getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(HashMap<Integer, Number> params) {
        this.params = params;
    }
    
    //public abstract Trade closeTrade(Trade trd,boolean sell);

}
