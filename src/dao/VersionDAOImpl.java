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
import dto.VersionDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Observer;
import java.util.ResourceBundle;
import logger.ThesisLogger;
import util.Duration;
import util.ParametersEnum;

public class VersionDAOImpl extends DAOManager implements VersionDAO {

    private int versionCount;
    private Integer stepVersion;
    private ParametersEnum typeVersion;
    private Date dateFromVersion, dateToVersion;

    public VersionDAOImpl(Observer observer) {
        if (observer != null) {
            this.addObserver(observer);
        }
        if (connectionActive) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(*) FROM VERSION");
                if (rs != null) {
                    rs.next();
                    versionCount = ((Long) rs.getObject(1)).intValue();
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("VersionDAOImpl.executeQuery: " + ex.toString());
            }
        } else {
            versionCount = 0;            
        }
        if(versionCount>0){
            setTypePeriod();            
            setPeriodDates();
        }
    }
    
    private void setTypePeriod(){
        try {
            ResultSet rs = executeQuery("SELECT * FROM VERSION WHERE COUNTER_ID='1'");
            if (rs != null) {
                rs.next();     
                if(((String) rs.getObject(2)).equals(ResourceBundle.getBundle("view/Bundle").getString("MainView.jCombox6.version.day"))) {
                        typeVersion = ParametersEnum.VERSION_STEP_DAY;
                    } else if(((String) rs.getObject(2)).equals(ResourceBundle.getBundle("view/Bundle").getString("MainView.jCombox6.version.month"))) {
                        typeVersion = ParametersEnum.VERSION_STEP_MONTH;
                    } else {
                        typeVersion = ParametersEnum.VERSION_STEP_YEAR;
                    }
                stepVersion = ((Integer) rs.getObject(3));
            }
        } catch (SQLException ex) {
            ThesisLogger.get().error("VersionDAOImpl.executeQuery: " + ex.toString());
        }
    }
    
    private void setPeriodDates() {
        VersionDTO version = (VersionDTO) get(1);
        if(version != null)
            dateFromVersion = version.getDateFrom();
        version = (VersionDTO) get(versionCount); 
        if(version != null)
            dateToVersion = version.getDateTo();
    }

    @Override
    public int getCount() {
        return versionCount;
    }

    @Override
    public int getCountBeetwDates(java.sql.Date dateFrom, java.sql.Date dateTo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean insert(BaseDTO dto) {
        boolean error = false;
        if (connectionActive) {
            error = executeStatement("INSERT INTO VERSION VALUES (null, '"
                    + ((VersionDTO) dto).getStepType().getValue() + "' , '"
                    + ((VersionDTO) dto).getStep().toString() + "' , '"
                    + new java.sql.Date(((VersionDTO) dto).getDateFrom().getTime()).toString() + "' , '"
                    + new java.sql.Date(((VersionDTO) dto).getDateTo().getTime()).toString() + "');");
            if (!error) {
                versionCount++;
            }
        }
        return error;
    }

    @Override
    public BaseDTO get(int id) {
        if (connectionActive) {
            ResultSet rs = executeQuery("SELECT * FROM VERSION WHERE COUNTER_ID='" + id + "'");
            try {
                if (rs != null && rs.next()) {
                    VersionDTO version = new VersionDTO();
                    version.setId(id);
                    java.util.Date javaDate = new java.util.Date(((Timestamp) rs.getObject(4)).getTime());
                    version.setDateFrom(javaDate);
                    javaDate = new java.util.Date(((Timestamp) rs.getObject(5)).getTime());
                    version.setDateTo(javaDate);
                    rs.close();
                    javaDate = null;
                    return version;
                }
            } catch (SQLException ex) {
                ThesisLogger.get().error("VersionDAOImpl.get: " + ex.toString());
            }
        }
        return null;
    }

    @Override
    public boolean exist(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<BaseDTO> getBeetwDates(java.sql.Date dateFrom, java.sql.Date dateTo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete() {
        if (connectionActive) {
            versionCount = 0;
            executeStatement("DELETE FROM VERSION;");
            executeStatement("ALTER TABLE VERSION ALTER COLUMN COUNTER_ID RESTART WITH 1;");
            return true;
        }
        return false;
    }

   

    @Override
    public void setParametersForGen(ParametersEnum type, Integer step, Date dateFrom, Date dateTo) {
        typeVersion = type;
        stepVersion = step;
        dateFromVersion = dateFrom;
        dateToVersion = dateTo;
    }

    @Override
    public boolean generateVersions() {
        boolean error = false;
        if (connectionActive) {
            if (typeVersion != null && stepVersion >= 0 && dateFromVersion != null && dateToVersion != null) {
                if (dateFromVersion.before(dateToVersion)) {
                    delete();
                    setChanged();
                    notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Action.Inic"));
                    long start = System.currentTimeMillis();
                    setChanged();
                    notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Action.Run"));
                    java.util.Date dini = new java.util.Date(dateFromVersion.getTime());
                    java.util.Date dtemp = new java.util.Date(dateFromVersion.getTime());
                    if(typeVersion.toString().equals(ResourceBundle.getBundle("view/Bundle").getString("MainView.jCombox6.version.day"))) {
                        dtemp.setDate(dtemp.getDate() + stepVersion);
                    } else if(typeVersion.toString().equals(ResourceBundle.getBundle("view/Bundle").getString("MainView.jCombox6.version.month"))) {
                        dtemp.setMonth(dtemp.getMonth() + stepVersion);
                    } else {
                        dtemp.setYear(dtemp.getYear() + stepVersion);
                    }         
                    java.util.Date dend = new java.util.Date(dateToVersion.getTime());
                    VersionDTO versionDto = new VersionDTO();
                    versionDto.setDateFrom(dini);
                    versionDto.setDateTo(dtemp);
                    versionDto.setStep(stepVersion);
                    versionDto.setStepType(typeVersion);             
                    while (dend.after(dtemp)) {
                        error = this.insert(versionDto);
                        if (!error) {                            
                            dini = new java.util.Date(dtemp.getTime());
                            versionDto.setDateFrom(dini);
                            if(typeVersion.toString().equals(ResourceBundle.getBundle("view/Bundle").getString("MainView.jCombox6.version.day"))) {
                                dtemp.setDate(dtemp.getDate() + stepVersion);
                            } else if(typeVersion.toString().equals(ResourceBundle.getBundle("view/Bundle").getString("MainView.jCombox6.version.month"))) {
                                dtemp.setMonth(dtemp.getMonth() + stepVersion);
                            } else {
                                dtemp.setYear(dtemp.getYear() + stepVersion);
                            }                            
                            versionDto.setDateTo(dtemp);
                        }
                    }
                    if (dend.after(dini) && !error) {                        
                        versionDto.setDateFrom(dini);
                        versionDto.setDateTo(dtemp);
                        insert(versionDto);
                    }
                    closeStatement();
                    long end = System.currentTimeMillis() - start;
                    setChanged();
                    notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Action.End"));
                    if (!error) {
                        setChanged();
                        notifyObservers(MessageFormat.format(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Mensage1"), versionCount, Duration.getDurationBreakdown(end)));
                    }
                } else {
                    setChanged();
                    notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Error2"));
                }
            } else {
                setChanged();
                notifyObservers(ResourceBundle.getBundle("view/Bundle").getString("VersionGen.Error1"));
            }
        }

        return error;
    }

    @Override
    public ParametersEnum getTypeVersion() {
        return typeVersion;
    }

    @Override
    public Integer getStepVersion() {
        return stepVersion;
    }

    @Override
    public Date getDateFromVersion() {
        return dateFromVersion;
    }

    @Override
    public Date getDateToVersion() {
        return dateToVersion;
    }

}
