/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package report.ReportWriter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jxl.write.WriteException;
import report.Summary;

/**
 *
 * @author gnisoft
 */
public interface ReportWriter {

    void close() throws InterruptedException, SQLException;

    void refresh();

    void open(String file) throws Exception;

    String writeSummFile(String file, List<Summary> summ) throws IOException, WriteException, SQLException, ClassNotFoundException;

}
