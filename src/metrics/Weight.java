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
package metrics;

import dao.DAOManager;
import dao.DAONameEnum;
import dto.BaseDTO;
import dto.DocumentDTO;
import dto.TopicDTO;
import dto.VersionDTO;
import java.math.BigDecimal;
import java.util.List;

public class Weight extends Metric {

    public Weight() {
        super(MetricNameEnum.WEIGHT);
    }

    @Override
    public BigDecimal getResult(VersionDTO version, TopicDTO topic) {
        BigDecimal result = new BigDecimal(0.0);
        List<BaseDTO> docsInVersion = (DAOManager.getDAO(DAONameEnum.DOCUMENT_DAO.getName())).getBeetwDates(new java.sql.Date(version.getDateFrom().getTime()), new java.sql.Date(version.getDateTo().getTime()));
        for (BaseDTO baseDto : docsInVersion) {
            DocumentDTO doc = (DocumentDTO) baseDto;
            if (topic.getDocProb(doc.getId()).signum() == 1) {
                result = result.add(topic.getDocProb(doc.getId()).multiply(new BigDecimal(doc.size())));
            }
        }
        return result;
    }
}
