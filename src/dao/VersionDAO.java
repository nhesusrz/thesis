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

import java.util.Date;
import util.ParametersEnum;

public interface VersionDAO extends BaseDAO {

    /**
     * Sets the parameters for versioning process.
     *
     * @param type Type of version.
     * @param step Amount of type.
     * @param dateFrom The initial date.
     * @param dateTo The final date.
     */
    public void setParametersForGen(ParametersEnum type, Integer step, Date dateFrom, Date dateTo);

    /**
     * Executes the process for generate all versions.
     *
     * @return True if the process successfully concluded. False, if not.
     */
    public boolean generateVersions();

    /**
     * Returns the type of version choosed.
     *
     * @return A parameter enumeration.
     */
    public ParametersEnum getTypeVersion();

    /**
     * Returns the amount choosed for the type of version.
     *
     * @return Amount
     */
    public Integer getStepVersion();

    /**
     * Returns the initial date of the main period.
     *
     * @return Date value.
     */
    public Date getDateFromVersion();

    /**
     * Returns the fnal date of the main period.
     *
     * @return Date value.
     */
    public Date getDateToVersion();

}
