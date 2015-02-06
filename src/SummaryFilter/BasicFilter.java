/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SummaryFilter;

import java.io.Serializable;
import java.util.HashMap;
import report.Summary;

/**
 *
 * @author admin
 */
public abstract class BasicFilter implements Serializable
{
    HashMap params;

    public BasicFilter(HashMap params) {
        this.params = params;
    }

    public BasicFilter() {
        params=new HashMap();
    }

    /**
     * @return the params
     */
    public HashMap getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(HashMap params) {
        this.params = params;
    }

    public int getParamCount()
    {
        return params.size();
    }

    abstract boolean isValid(Summary sum);

}
