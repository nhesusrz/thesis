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
package dao;

import dto.CommentDTO;
import java.util.List;

public interface CommentDAO extends BaseDAO {

    /**
     * Returns the numbers of comments for a bug.
     *
     * @param bugID Bug identificator.
     * @return Int value.
     */
    public int getCount(int bugID);

    /**
     * Returns a list of comments from a bug.
     *
     * @param bugID Bug indentificator.
     * @return List of commentDTO.
     */
    public List<CommentDTO> getComments(int bugID);
}
