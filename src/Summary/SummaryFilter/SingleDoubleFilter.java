/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Summary.SummaryFilter;

import indicator.BaseIndicator;
import indicator.MaIndicator;
import indicator.RsiIndicator;
import java.util.HashMap;
import report.Summary;
import trade.TradeParameters;

/**
 *
 * @author admin
 */
public class SingleDoubleFilter extends BasicFilter {

    public SingleDoubleFilter() {
        super();
        params.put(1, 1);
    }

    public SingleDoubleFilter(HashMap params) {
        super(params);
    }


    @Override
    boolean isValid(Summary sum) {
        int sysType=(Integer)params.get(1);
        boolean single=true;
        TradeParameters pos = sum.getTradeParams();
        BaseIndicator indOpen = pos.getIndList();
        BaseIndicator indClose = pos.getIndCloseList();
        String open = pos.getSl().getShare();
        String close = pos.getCloseSL().getShare();

        if (indClose == null) {
            single = true;
        } else if (indOpen.toString().equals(indClose.toString())) {
            if (open.equals(close) || (open.length() < close.length() && open.equals(close.substring(0, open.length())))) {
                single = true;
            } else {
                single = false;
            }
        } else if (indOpen instanceof MaIndicator && (open.equals(close) || (open.length() < close.length() && open.equals(close.substring(0, open.length()))))) {
            single = true;
        } else {
            single = false;
        }
        if (sysType == 2) {
            single = !single;
        }
        else if (sysType == 3) {
            if (single) {
                single = false;
                if (pos.getClose().toString().contains("Close10")) {
                    if (pos.isBuy()) {
                        if (open.contains("Sys A") || open.contains("Sys C") || open.contains("Sys E") || open.contains("Sys G") || open.contains("Sys I") || open.contains("Sys K")) {
                            if (Double.parseDouble(pos.getOpen().getParams().get(1).toString()) >= Double.parseDouble(pos.getClose().getParams().get(1).toString())) {
                                single = true;
                            }
                        }
                    } else {
                        if (open.contains("Sys A") || open.contains("Sys C") || open.contains("Sys E") || open.contains("Sys G") || open.contains("Sys I") || open.contains("Sys K")) {
                            if (Double.parseDouble(pos.getOpen().getParams().get(1).toString()) <= Double.parseDouble(pos.getClose().getParams().get(1).toString())) {
                                single = true;
                            }
                        }
                    }

                } else if (pos.getClose().toString().contains("Close27")) {
                    if (pos.isBuy()) {
                        if (open.contains("Sys B") || open.contains("Sys D") || open.contains("Sys F") || open.contains("Sys H") || open.contains("Sys J") || open.contains("Sys L")) {
                            if (Double.parseDouble(pos.getOpen().getParams().get(1).toString()) <= Double.parseDouble(pos.getClose().getParams().get(1).toString())) {
                                single = true;
                            }
                        }
                    } else {
                        if (open.contains("Sys B") || open.contains("Sys D") || open.contains("Sys F") || open.contains("Sys H") || open.contains("Sys J") || open.contains("Sys L")) {
                            if (Double.parseDouble(pos.getOpen().getParams().get(1).toString()) >= Double.parseDouble(pos.getClose().getParams().get(1).toString())) {
                                single = true;
                            }
                        }
                    }
                }


            } else {
                if (pos.getClose().toString().contains("Close10")) {
                    RsiIndicator oInd = (RsiIndicator) pos.getIndList();
                    RsiIndicator cInd = (RsiIndicator) pos.getIndCloseList();
                    if (pos.isBuy()) {
                        if (open.contains("Sys A") || open.contains("Sys C") || open.contains("Sys E") || open.contains("Sys G") || open.contains("Sys I") || open.contains("Sys K")) {
                            if ((Double.parseDouble(pos.getOpen().getParams().get(1).toString()) >= Double.parseDouble(pos.getClose().getParams().get(1).toString())) && Double.parseDouble(oInd.getParams().get(1).toString()) >= Double.parseDouble(cInd.getParams().get(1).toString())) {
                                single = true;
                            }
                        }
                    } else {
                        if (open.contains("Sys A") || open.contains("Sys C") || open.contains("Sys E") || open.contains("Sys G") || open.contains("Sys I") || open.contains("Sys K")) {
                            if ((Double.parseDouble(pos.getOpen().getParams().get(1).toString()) <= Double.parseDouble(pos.getClose().getParams().get(1).toString())) && Double.parseDouble(oInd.getParams().get(1).toString()) >= Double.parseDouble(cInd.getParams().get(1).toString())) {
                                single = true;
                            }
                        }
                    }
                } else if (pos.getClose().toString().contains("Close27")) {
                    RsiIndicator oInd = (RsiIndicator) pos.getIndList();
                    RsiIndicator cInd = (RsiIndicator) pos.getIndCloseList();
                    if (pos.isBuy()) {
                        if (open.contains("Sys B") || open.contains("Sys D") || open.contains("Sys F") || open.contains("Sys H") || open.contains("Sys J") || open.contains("Sys L")) {
                            if ((Double.parseDouble(pos.getOpen().getParams().get(1).toString()) <= Double.parseDouble(pos.getClose().getParams().get(1).toString())) && Double.parseDouble(oInd.getParams().get(1).toString()) >= Double.parseDouble(cInd.getParams().get(1).toString())) {
                                single = true;
                            }
                        }
                    } else {
                        if (open.contains("Sys B") || open.contains("Sys D") || open.contains("Sys F") || open.contains("Sys H") || open.contains("Sys J") || open.contains("Sys L")) {
                            if ((Double.parseDouble(pos.getOpen().getParams().get(1).toString()) >= Double.parseDouble(pos.getClose().getParams().get(1).toString())) && Double.parseDouble(oInd.getParams().get(1).toString()) >= Double.parseDouble(cInd.getParams().get(1).toString())) {
                                single = true;
                            }
                        }
                    }
                }
            }
        }

        return single;
    }
}