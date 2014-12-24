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
package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesApp {

    public static PropertiesApp instance;

    private final Properties propertie;
    private InputStream inputStream;

    public PropertiesApp() {
        instance = null;
        propertie = new Properties();
        inputStream = null;
    }

    public static PropertiesApp getInstance() {
        if (instance == null) {
            instance = new PropertiesApp();
        }
        return instance;
    }

    public void fileLoad(String path) {
        try {
            inputStream = new FileInputStream(path);
            propertie.load(inputStream);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public String getPropertie(String propertieName) {
        if (!propertie.isEmpty()) {
            return propertie.getProperty(propertieName);
        }
        return null;
    }

}
