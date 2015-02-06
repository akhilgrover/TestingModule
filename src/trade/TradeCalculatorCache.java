/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trade;

/**
 *
 * @author gnisoft
 */
public class TradeCalculatorCache {

    private TradeCalculatorCache() {
    }

    public static TradeCalculatorCache getInstance() {
        return TradeCalculatorCacheHolder.INSTANCE;
    }

    private static class TradeCalculatorCacheHolder {

        private static final TradeCalculatorCache INSTANCE = new TradeCalculatorCache();
    }
}
