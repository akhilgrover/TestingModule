/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trade.filter;


import Share.ShareList;
import java.io.Serializable;
import java.util.HashMap;



/**
 *
 * @author Admin
 */
public abstract class AbstractFilter implements Filters, Serializable //,Comparable<AbstractFilter>
{

    int paramCount;
    HashMap params;
    protected transient String name;
    private static final long serialVersionUID = 7526472295622776147L;

    public abstract boolean filterTrade(int shareIndex, int buy, ShareList sl) ;

    public int getParamCount(){
        return paramCount;
    }

    public HashMap getParams(){
        return params;
    }

    public void setParams(HashMap param)
    {
        this.params=param;
    }

    @Override
    public void clear() {
        return;
    }



}
