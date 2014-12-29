/**
 * Copyright (C) 2014 Martín Pacheco.
 *
 * This file is part of my Thesis aplication called "Extraction and Analysis
 * System of Topics for Software History Reports". Faculty of Exact Sciences of
 * the UNICEN University. Tandil, Argentine. http://www.exa.unicen.edu.ar/
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Email: mpacheco@alumnos.exa.unicen.edu.ar
 *
 * @author Martín Pacheco
 */
package logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import util.ParametersEnum;

public class ThesisLogger {

    private static ThesisLogger instance;
    private final Logger logger;

    public ThesisLogger() {
        logger = Logger.getLogger(this.getClass());
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(ParametersEnum.LOG4J_PROPERTIE_FILE_DEFAULT_PATH.getValue()));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ThesisLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        PropertyConfigurator.configure(props);
    }

    public static Logger get() {
        if (instance == null) {
            instance = new ThesisLogger();
        }
        return instance.logger;
    }

}
