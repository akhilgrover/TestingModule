package indicator;

import Share.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.HashMap;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class AbstractIndicator implements BaseIndicator, Serializable//, Comparable<AbstractIndicator>
{

    protected String name;
    protected HashMap params;
    protected int paramCount;
    protected transient ShareList sList;
    protected transient String string;
    private static final long serialVersionUID = 7526472295622776147L;

    @Override
    public abstract void init();

    @Override
    public abstract void init(HashMap params);

    /**
     *
     * @return @throws java.lang.Exception
     */
    @Override
    public IndicatorList buildIndicator() throws Exception {
        if (sList != null) {
            IndicatorList indl;
            IndicatorDB indDB = new IndicatorDB();
            if (indDB.isPresent(sList.getShare(), name, params)) {
                indl = indDB.getDBIndicator(sList.getShare(), name, params);
            } else {
                indl = buildIndicator(sList);
                indDB.setDBIndicator(sList.getShare(), name, params, indl);
            }
            return indl;
        } else {
            return null;
        }
    }

    /**
     *
     * @param sl
     * @return
     */
    @Override
    public abstract IndicatorList buildIndicator(ShareList sl);

    @Override
    public IndicatorList buildIndicator(ShareList sl, ShareList sl1) {
        throw new UnsupportedOperationException("Not supported For Abstract");
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    public HashMap getParams() {
        return params;
    }

    public String getShortName() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(name);
        buffer.append(",");
        for (int i = 1; i <= params.size(); i++) {
            buffer.append(params.get(i));
            if (i < params.size()) {
                buffer.append(",");
            }
        }

        return buffer.toString();
    }

    public void setSList(ShareList sList) {
        this.sList = sList;

    }

    public ShareList getSList() {
        return (this.sList);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(name);
        buffer.append(" ");

        return buffer.toString();
    }

    @Override
    public int getParamCount() {
        return paramCount;
    }

    public AbstractIndicator() {
    }

    /*public int compareTo(AbstractIndicator o)
     {
     if(paramCount!=o.paramCount)
     return -1;
     else
     {
     if(paramCount==0)
     {
     return 0;
     }
     else if(paramCount==1)
     {
     int param1O=(Integer)o.params.get(1);
     int param1=(Integer)params.get(1);
     int ret=(param1-param1O);
     return ret;
     }
     else if(paramCount==2)
     {
     int param1O=(Integer)o.params.get(1);
     int param2O=(Integer)o.params.get(2);
     int param1=(Integer)params.get(1);
     int param2=(Integer)params.get(2);
     int ret=(param1-param1O)*10+(param2-param2O);
     return ret;
     }
     else
     {
     int param1O=(Integer)o.params.get(1);
     int param2O=(Integer)o.params.get(2);
     int param3O=(Integer)o.params.get(3);
     int param1=(Integer)params.get(1);
     int param2=(Integer)params.get(2);
     int param3=(Integer)params.get(3);
     int ret=(param1-param1O)*100+(param2-param2O)*10+(param3-param3O);
     return ret;
     }
     }
     }*/
}
