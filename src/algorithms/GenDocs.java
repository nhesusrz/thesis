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
package algorithms;

import dao.DAOManager;
import dao.DAONameEnum;
import dao.DocumentDAO;
import java.util.Observable;

public class GenDocs extends Observable implements Runnable {

    private static GenDocs instance = null;

    public GenDocs() {
    }

    public static GenDocs getInstance() {
        if (instance == null) {
            instance = new GenDocs();
        }
        return instance;
    }

    @Override
    public void run() {
        ((DocumentDAO) DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).generateDocsForLDA();
    }

}
