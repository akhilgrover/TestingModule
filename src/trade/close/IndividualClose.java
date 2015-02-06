/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.close;

import java.util.HashMap;

/**
 *
 * @author admin
 */
public interface IndividualClose {
    

    /**
     * @return the params
     */
    HashMap<Integer, Number> getParams();

    /**
     * @param params the params to set
     */
    void setParams(HashMap<Integer, Number> params);

}
