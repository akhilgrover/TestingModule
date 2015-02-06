/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author admin
 */
public class SystemGrade implements Comparable<SystemGrade>, Serializable {

    private static final long serialVersionUID = 860043317876954322L;
    private Integer[] oShare;
    private Integer[] cShare;
    private Integer[] oInd;
    private Integer[] cInd;
    private ArrayList<Integer[]> opn;
    private ArrayList<Integer[]> cls;
    private double avgPercent;
    private double avgLength;
    private transient String str;

    /**
     * @return the oShare
     */
    public Integer[] getoShare() {
        return oShare;
    }

    /**
     * @param oShare the oShare to set
     */
    public void setoShare(Integer[] oShare) {
        this.oShare = oShare;
    }

    /**
     * @return the cShare
     */
    public Integer[] getcShare() {
        return cShare;
    }

    /**
     * @param cShare the cShare to set
     */
    public void setcShare(Integer[] cShare) {
        this.cShare = cShare;
    }

    /**
     * @return the oInd
     */
    public Integer[] getoInd() {
        return oInd;
    }

    /**
     * @param oInd the oInd to set
     */
    public void setoInd(Integer[] oInd) {
        this.oInd = oInd;
    }

    /**
     * @return the cInd
     */
    public Integer[] getcInd() {
        return cInd;
    }

    /**
     * @param cInd the cInd to set
     */
    public void setcInd(Integer[] cInd) {
        this.cInd = cInd;
    }

    /**
     * @param ser
     * @return the opn
     */
    public Integer[] getOpn(int ser) {
        return opn.get(ser);
    }

    /**
     * @param ser
     * @param opn the opn to set
     */
    public void setOpn(int ser, Integer[] opn) {
        if (this.opn == null) {
            this.opn = new ArrayList<Integer[]>();
        }
        this.opn.add(ser, opn);
    }

    /**
     * @param ser
     * @return the cls
     */
    public Integer[] getCls(int ser) {
        return cls.get(ser);
    }

    /**
     * @param ser
     * @param cls the cls to set
     */
    public void setCls(int ser, Integer[] cls) {
        if (this.cls == null) {
            this.cls = new ArrayList<Integer[]>();
        }
        this.cls.add(cls);
    }

    @Override
    public String toString() {
        if (str == null) {
            StringBuilder sb = new StringBuilder();
            String sep = ",";
            String line = "\n ";
            if (oShare != null) {
                sb.append("Open Share");
                sb.append(sep);
                sb.append(oShare[0]);
                sb.append(sep);
                sb.append(oShare[1]);
                sb.append(sep);
                sb.append(oShare[2]);
                sb.append(line);
            }
            if (cShare != null) {
                sb.append("Close Share");
                sb.append(sep);
                sb.append(cShare[0]);
                sb.append(sep);
                sb.append(cShare[1]);
                sb.append(sep);
                sb.append(cShare[2]);
                sb.append(line);
            }
            if (oInd != null) {
                sb.append("Open Indicator");
                sb.append(sep);
                sb.append(oInd[0]);
                sb.append(sep);
                sb.append(oInd[1]);
                sb.append(sep);
                sb.append(oInd[2]);
                sb.append(line);
            }
            if (cInd != null) {
                sb.append("Close Indicator");
                sb.append(sep);
                sb.append(cInd[0]);
                sb.append(sep);
                sb.append(cInd[1]);
                sb.append(sep);
                sb.append(cInd[2]);
                sb.append(line);
            }
            if (opn != null) {
                for (int i = 0; i < opn.size(); i++) {
                    if (opn != null) {
                        sb.append("Open").append(i + 1).append("-");
                        sb.append(opn.get(i)[0]);
                        sb.append(sep);
                        sb.append(opn.get(i)[1]);
                        sb.append(sep);
                        sb.append(opn.get(i)[2]);
                        sb.append(line);
                    }
                }
            }
            if (cls != null) {
                for (int i = 0; i < cls.size(); i++) {
                    if (cls != null) {
                        sb.append("Close").append(i + 1).append("-");
                        sb.append(cls.get(i)[0]);
                        sb.append(sep);
                        sb.append(cls.get(i)[1]);
                        sb.append(sep);
                        sb.append(cls.get(i)[2]);
                        sb.append(line);
                    }
                }
            }
            DecimalFormat df = new DecimalFormat("##.#");
            if (avgPercent > 0) {

                sb.append(df.format(avgPercent));
                sb.append(sep);
                sb.append(df.format(avgLength));
                sb.append(line);
            }
            sb.append("|").append(df.format(getRatio()));
            sb.append(line);
            str = sb.toString();
        }
        return str;
    }

    public boolean isGood() {
        return isGood(3);
    }

    public boolean isGood(int val) {
        boolean ret = true;

        int sum = 0;
        int cnt = 0;
        if(oShare!=null)
            sum += oShare[2];
        //sum+=oInd[2];
        cnt = 1;
        if (cShare != null) {
            sum += cShare[2];
            cnt++;
            if (cShare[0] >= val && cShare[1] >= val) {
                ret = true;
            } else {
                ret = false;
            }
        }
        if (cInd != null && ret) {
            //sum+=cInd[2];
            //cnt++;
            if (cInd[0] >= val) {
                ret = true;
            } else {
                ret = false;
            }
        }

        if (opn != null && !opn.isEmpty() && ret) {
            if (opn.get(0)[0] >= val) {
                ret = true;
            } else {
                ret = false;
            }
        } else if (opn == null) {
            ret = false;
        }

        if (cls != null && !cls.isEmpty() && ret) {
            if (cls.get(0)[0] >= val) {
                ret = true;
            } else {
                ret = false;
            }
        } else if (cls == null) {
            ret = false;
        }


//        if((sum*100)/(cnt*14)>=60 && ret)
//            ret=true;
//        else
//            ret=false;
        //System.out.println(sum);
        return oShare[0] >= val && (oInd == null || oInd[0] >= val) && oShare[1] >= val && ret;
    }

    public boolean isGoodStrict(int val) {
        boolean ret = true;

        int sum = 0;
        int cnt = 0;
        sum += oShare[2];
        //sum+=oInd[2];
        cnt = 1;
        if (cShare != null) {
            sum += cShare[2];
            cnt++;
            if (cShare[0] >= val && cShare[1] >= val) {
                ret = true;
            } else {
                ret = false;
            }
        }
        if (cInd != null && ret) {
            //sum+=cInd[2];
            //cnt++;
            if (cInd[0] >= val && cInd[1] >= val) {
                ret = true;
            } else {
                ret = false;
            }
        }

        if (opn != null && !opn.isEmpty() && ret) {
            if (opn.get(0)[0] >= val && opn.get(0)[1] >= val) {
                ret = true;
            } else {
                ret = false;
            }
        } else if (opn == null) {
            ret = false;
        }

        if (cls != null && !cls.isEmpty() && ret) {
            for (int i = 0; i < cls.size(); i++) {
                if (cls.get(0)[0] >= val && cls.get(0)[1] >= val) {
                    ret = true;
                } else {
                    ret = false;
                    break;
                }
            }
        } else if (cls == null) {
            ret = false;
        }


//        if((sum*100)/(cnt*14)>=60 && ret)
//            ret=true;
//        else
//            ret=false;
        //System.out.println(sum);
        return oShare[0] >= val && (oInd == null || (oInd[0] >= val && oInd[1] >= val)) && oShare[1] >= val && ret;
    }

    /*
     * Open Share
    Open Indicator
    Open
    Close Share
    Close Indicator
    Close
     */
    public double getStableValue(String ignore) {
        double total = 0.0;
        int cnt = 0;
        if (oShare != null && !ignore.equals("Open Share")) {
            total += oShare[0];
            total += oShare[1];
            cnt += 2;
        }
        if (cShare != null && !ignore.equals("Close Share")) {
            total += cShare[0];
            total += cShare[1];
            cnt += 2;
        }
        if (oInd != null && !ignore.equals("Open Indicator")) {
            total += oInd[0];
            total += oInd[1];
            cnt += 2;
        }
        if (cInd != null && !ignore.equals("Close Indicator")) {
            total += cInd[0];
            total += cInd[1];
            cnt += 2;
        }
        if (opn != null) {
            for (int i = 0; i < opn.size(); i++) {
                if (opn != null && !ignore.equals("Open")) {
                    total += opn.get(i)[0];
                    total += opn.get(i)[1];
                    cnt += 2;
                }
            }
        }
        if (cls != null) {
            for (int i = 0; i < cls.size(); i++) {
                if (cls != null && !ignore.equals("Close")) {
                    total += cls.get(i)[0];
                    total += cls.get(i)[1];
                    cnt += 2;
                }
            }
        }
        return total / cnt;
    }

    @Override
    public int compareTo(SystemGrade sys) {
//        if(oShare[0].equals(sys.oShare[0]))
//        {
//            if(oShare[1].equals(sys.oShare[1]))
//            {
//                if((oInd!=null && (oInd[0].equals(sys.oInd[0]))) || oInd==null)
//                {
//                    if(cShare!=null)
//                    {
//                        if(cShare[0].equals(sys.cShare[0]))
//                        {
//                            if(cShare[1].equals(sys.cShare[1]))
//                            {
//                                if(oInd!=null)
//                                {
//                                    if(oInd[1].equals(sys.oInd[1]))
//                                    {
//                                        return 0;
//                                    }
//                                    else
//                                        return oInd[1].compareTo(sys.oInd[1]);
//                                }
//                                else
//                                    return 0;
//                            }
//                            else
//                                return cShare[1].compareTo(sys.cShare[1]);
//                        }
//                        else
//                            return cShare[0].compareTo(sys.cShare[0]);
//                    }
//                    else
//                    {
//                        if(oInd!=null)
//                        {
//                            if(oInd[1].equals(sys.oInd[1]))
//                            {
//                                return 0;
//                            }
//                            else
//                                return oInd[1].compareTo(sys.oInd[1]);
//                        }
//                        else
//                            return 0;
//                    }
//
//                }
//                else
//                    return oInd[0].compareTo(sys.oInd[0]);
//            }
//            else
//                return oShare[1].compareTo(sys.oShare[1]);
//        }
//        else
//            return oShare[0].compareTo(sys.oShare[0]);

        double r1 = getRatio();
        double r2 = sys.getRatio();
        return Double.compare(r1, r2);

    }

    /**
     * @return the avgPercent
     */
    public double getAvgPercent() {
        return avgPercent;
    }

    /**
     * @param avgPercent the avgPercent to set
     */
    public void setAvgPercent(double avgPercent) {
        this.avgPercent = avgPercent;
    }

    /**
     * @return the avgLength
     */
    public double getAvgLength() {
        return avgLength;
    }

    /**
     * @param avgLength the avgLength to set
     */
    public void setAvgLength(double avgLength) {
        this.avgLength = avgLength;
    }

    public double getRatio() {
        double sum = 0.0, total = 0.0;

        if (oShare != null) {
            sum += oShare[0] * 1.4;
            sum += oShare[1] * 1.2;
            total += 13;
        }
        if (cShare != null) {
            sum += cShare[0] * 1.4;
            sum += cShare[1] * 1.2;
            total += 13;
        }
        if (oInd != null) {
            sum += oInd[0] * 1.4;
            sum += oInd[1] * 1.2;
            total += 13;
        }
        if (cInd != null) {
            sum += cInd[0] * 1.4;
            sum += cInd[1] * 1.2;
            total += 13;
        }
        if (opn != null) {
            for (int i = 0; i < opn.size(); i++) {
                if (opn != null) {
                    sum += opn.get(i)[0] * 1.2;
                    sum += opn.get(i)[1] * 1.2;
                    total += 12;
                }
            }
        }
        if (cls != null) {
            for (int i = 0; i < cls.size(); i++) {
                if (cls != null) {
                    sum += cls.get(i)[0];
                    sum += cls.get(i)[1];
                    total += 12;
                }
            }
        }
        return (sum / total) * 100;
    }
}
