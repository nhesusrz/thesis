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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.xml.sax.Attributes;

public class BugDTO extends BaseDTO {

    private Integer id;
    private String title;
    private String status;
    private String owner;
    private boolean closedON;
    private Date closedDate;
    private String type;
    private String priority;
    private String component;
    private Integer starts;
    private String reportedBY;
    private Date openedDate;
    private String descripcion;
    private List<CommentDTO> comments;

    public BugDTO() {
        comments = new ArrayList<CommentDTO>();
    }

    public BugDTO(Integer id, String title, String status, String owner,
            boolean closedON, Date clsosedDate, String type, String priority, String component,
            Integer starts, String reportedBY, Date openedDate, String description,
            List<CommentDTO> comments) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.owner = owner;
        this.closedON = closedON;
        if (closedON) {
            this.closedDate = clsosedDate;
        } else {
            this.closedDate = null;
        }
        this.type = type;
        this.priority = priority;
        this.component = component;
        this.starts = starts;
        this.reportedBY = reportedBY;
        this.openedDate = openedDate;
        this.descripcion = description;
        this.comments = comments;
    }

    @Override
    public String toString() {
        return ("Title: " + title + "Description: " + descripcion);
    }

    /**
     * Returns an HTML string representation.
     *
     * @return String.
     */
    public String getFullInfoString() {
        String result = "<bold>Title</bold>"
                + title
                + "\nDescription\n " + descripcion;
        return result;
    }

    public Integer getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getOwner() {
        return owner;
    }

    public boolean getClosedON() {
        return closedON;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public String getType() {
        return type;
    }

    public String getPriority() {
        return priority;
    }

    public String getComponent() {
        return component;
    }

    public Integer getStars() {
        return starts;
    }

    public String getReportedBY() {
        return reportedBY;
    }

    public Date getOpenedDate() {
        return openedDate;
    }

    public String getDescription() {
        return descripcion;
    }

    public List<CommentDTO> getComments() {
        return comments;
    }

    public void setID(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setClosedON(boolean closedON) {
        this.closedON = closedON;
    }

    public void setClosedDate(Date closedDate) {
        this.closedDate = closedDate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public void setStars(Integer starts) {
        this.starts = starts;
    }

    public void setReportedBY(String reportedBY) {
        this.reportedBY = reportedBY;
    }

    public void setOpenedDate(Date openedDate) {
        this.openedDate = openedDate;
    }

    public void setDescription(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
    }

    public void addComment(CommentDTO comment) {
        comments.add(comment);
    }
}
