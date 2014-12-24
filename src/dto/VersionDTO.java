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
package dto;

import java.util.Date;
import util.ParametersEnum;

public class VersionDTO extends BaseDTO {

    private Integer id;
    private Integer step;
    private ParametersEnum step_type;
    private Date dateFrom, dateTo;
    
    public VersionDTO() {
        id = -1;
        dateFrom = dateTo = null;
    }

    public VersionDTO(Integer id, Date dateFrom, Date dateTo) {
        this.id = id;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public Integer getId() {
        return id;
    }
    
    public void setStep(Integer step) {
        this.step = step;
    }

    public void setStepType(ParametersEnum step_type) {
        this.step_type = step_type;
    }

    public Integer getStep() {
        return step;
    }

    public ParametersEnum getStepType() {
        return step_type;
    }

    public Date getDateFrom() {
        return dateFrom; 
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }
}
