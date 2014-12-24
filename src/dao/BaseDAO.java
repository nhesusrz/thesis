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

import dto.BaseDTO;
import java.sql.Date;
import java.util.List;

public interface BaseDAO {

    /**
     * Returns the numbers of rows.
     *
     * @return Int value.
     */
    public int getCount();

    /**
     * Returns the numbers of row beetween dateFrom and dateTo.
     *
     * @param dateFrom The initial date.
     * @param dateTo The final date.
     * @return Int value.
     */
    public int getCountBeetwDates(Date dateFrom, Date dateTo);

    /**
     * Inserts a DTO into a table.
     *
     * @param dto The element to insert.
     * @return If exists an error.
     */
    public boolean insert(BaseDTO dto);

    /**
     * Returns a DTO with an especific id.
     *
     * @param id Id DTO.
     * @return The DTO.
     */
    public BaseDTO get(int id);

    /**
     * Checks if exists a dto with that id.
     *
     * @param id Id to find.
     * @return True if eixts. False, if not.
     */
    public boolean exist(int id);

    /**
     * Returns a list of DTO with each DTO's date is beetween dateFrom and
     * dateTo.
     *
     * @param dateFrom The initial date.
     * @param dateTo The final date.
     * @return List of DTOs.
     */
    public List<BaseDTO> getBeetwDates(Date dateFrom, Date dateTo);

    /**
     * Deletes all DTO occurrences .
     *
     * @return True if completes with success.
     */
    public boolean delete();
}
