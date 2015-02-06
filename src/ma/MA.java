package ma;

import java.io.Serializable;
import java.util.*;
public class MA implements  Serializable,Cloneable {

	private int period;
	transient private ArrayList<Double> data;
	static public int Simple=0;
	static public int Exponential=1;
        static public int Weighted=2;
	private int type;
	private double last;
        private double total;
        private double num;
        private int den;
        transient private String name;
        private static final long serialVersionUID = 7526472295622776147L;

        public MA() {
    }



	/**
	 * Method MA
	 *
	 *
         *
         * @param prd  Moving Average Period
         * @param type Moving Average Type
         */
	public MA(int prd,int type)
	{
            this.period=prd;
            data=new ArrayList(period+1);
            this.type=type;
            this.last=-1.0;
            if(type==MA.Weighted)
            {
                den=0;
                for(int i=1;i<=period;i++)
                    den+=i;
            }
	}
	/*
	 * Calculate next Moving Avg value
	 *
	 * @npoint: moving avg point
	 */
	public double next(double npoint)
	{
            double ret=0.0;
//            data.add(npoint);
//            double del=0;
//            if(data.size()>getPeriod())
//                del=(Double)data.remove(0);

            if(type==MA.Simple)
            {
                ret=nextSMA(npoint);
            }
            else if(type==MA.Exponential)
            {
                ret=nextEMA(npoint);
            }
            else if(type==MA.Weighted)
            {
                ret=nextWMA(npoint);
            }
            this.last=ret;
            return ret;
	}

        /*
	 * Calculate next Moving Avg value to check
	 *
	 * @npoint: moving avg point
	 */
	public double nextCheck(double npoint)
	{
            double ret=0;
//            data.add(npoint);
//            double del=0;
//            if(data.size()>getPeriod())
//                del=(Double)data.remove(0);

            if(type==MA.Simple)
            {
                ret=nextCSMA(npoint);
            }
            else if(type==MA.Exponential)
            {
                ret=nextEMA(npoint);
            }
            else if(type==MA.Weighted)
            {
                ret=nextCWMA(npoint);
            }
//            data.add(0, del);
//            if(data.size()>period)
//                data.remove(data.size()-1);
            //this.last=ret;
            return ret;
	}

	private double nextEMA(double npoint)
	{
            if(last==-1)
                last=npoint;
            double em=2.0;
            return (last+(em/(period+1))*(npoint-last));
	}

	private double nextSMA(double npoint)
	{
            double sum=0;
            data.add(npoint);
            double del=0;
            if(data.size()>getPeriod())
                del=data.remove(0);
            for(int i=0;i<data.size();i++)
            {
                sum+=data.get(i).doubleValue();
            }
            return sum/period;
            //return last-(del/period)+(npoint/period);
	}

        private double nextCSMA(double npoint)
	{
            double sum=0;
            data.add(npoint);
            double del=0;
            if(data.size()>getPeriod())
                del=data.remove(0);
            for(int i=0;i<data.size();i++)
            {
                sum+=data.get(i);
            }
            data.add(0, del);
            if(data.size()>period)
                data.remove(data.size()-1);
            return sum/period;
            //return last-(del/period)+(npoint/period);
	}

        private Double nextWMA(double npoint)
        {
            data.add(npoint);
            double del=0;
            if(data.size()>getPeriod())
                del=data.remove(0);
            num=num+(getPeriod()*npoint)-total;
            total=total+npoint-del;
            return num/den;
        }

        private Double nextCWMA(double npoint)
        {
            data.add(npoint);
            double del=0;
            if(data.size()>getPeriod())
                del=data.remove(0);
            num=num+(getPeriod()*npoint)-total;
            total=total+npoint-del;
            return num/den;
        }

        public int getType()
        {
            return type;
        }

    @Override
        public String toString()
        {
            if(name==null){
            StringBuilder ret=new StringBuilder();
            ret.append(getPeriod());
            ret.append(" day ");
            if(type==MA.Exponential)
                ret.append("E");
            else if(type==MA.Weighted)
                ret.append("W");
            else if(type==MA.Simple)
                ret.append("S");
            ret.append("MA ");
            name=ret.toString();
            }
            return name;
        }

    @Override
        public Object clone(){
        try{
            MA cloned=(MA) super.clone();
            cloned.data=(ArrayList) data.clone();
          return cloned;
        }
        catch(CloneNotSupportedException e){
          System.out.println(e);
          return null;
        }
  }

    /**
     * @return the period
     */
    public int getPeriod() {
        return period;
    }

    public synchronized void refresh(){
        data=new ArrayList<Double>(period+1);
        this.last=-1.0;
    }
}
