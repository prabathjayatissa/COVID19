/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.ejb.AnalysisBean;
import lk.gov.health.phsp.ejb.CovidDataHolder;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Numbers;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.NumbersFacade;
import lk.gov.health.phsp.pojcs.CovidData;
import lk.gov.health.phsp.pojcs.InstitutionCount;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class DashboardApplicationController {

    @EJB
    ClientFacade clientFacade;
    @EJB
    EncounterFacade encounterFacade;
    @EJB
    ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;
    @EJB
    NumbersFacade numbersFacade;

    @Inject
    ItemController itemController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    AreaApplicationController areaApplicationController;
    @Inject
    CovidDataHolder covidDataHolder;

   

    /**
     * Creates a new instance of DashboardController
     */
    public DashboardApplicationController() {
    }

    @PostConstruct
    public void init() {
        List<Institution> mohs;
        List<Institution> hospitals;
        List<Institution> labs;
        List<Area> areas;
        mohs = institutionApplicationController.findInstitutions(InstitutionType.MOH_Office);
        labs = institutionApplicationController.findInstitutions(InstitutionType.Lab);
        hospitals = institutionApplicationController.findInstitutions(institutionApplicationController.getHospitalTypes());
        areas = areaApplicationController.getAllAreas(areaApplicationController.getCovidMonitoringAreaTypes());
        Item testType = itemController.findItemByCode("test_type");
        Item orderingCat = itemController.findItemByCode("covid_19_test_ordering_context_category");
        Item pcr = itemController.findItemByCode("covid19_pcr_test");
        Item rat = itemController.findItemByCode("covid19_rat");
        covidDataHolder.generateCovidCountsAsync(pcr, rat, testType, orderingCat, mohs, hospitals, labs, areas);
    }

    public void calculateNumbers() {
        calculateNumbers(new Date());
    }

    public void calculateNumbers(Date date) {
        calculateCasesAndTestNumbers(date, date);
    }

    public void calculateNumbers(Date fromDate, Date toDate) {
        calculateNumbersByOrderedTest(fromDate, toDate);
        calculateCasesAndTestNumbers(fromDate, toDate);
    }

    public void calculateNumbersByOrderedTest(Date fromDate, Date toDate) {
        ClientEncounterComponentItem i = new ClientEncounterComponentItem();
        i.getItemEncounter();
        i.getItem();
        i.getItemValue();
        List<Item> items = new ArrayList<>();
        items.add(itemController.findItemByCode("test_type"));
        items.add(itemController.findItemByCode("covid_19_test_ordering_context_category"));
        Item pcr = itemController.findItemByCode("covid19_pcr_test");
        Item rat = itemController.findItemByCode("covid19_rat");

        String j = "select  new lk.gov.health.phsp.pojcs.InstitutionCount(count(e), e.institution, e.encounterDate, e.encounterType, i.item, i.itemValue)  "
                + " from  ClientEncounterComponentItem i join i.itemEncounter e"
                + " where e.retired=false "
                + " and e.encounterDate between :fd and :td "
                + " and i.item in :items "
                + " and e.encounterType=:test"
                + " group by e.encounterDate, e.institution, e.encounterType, i.item, i.itemValue";
        Map m = new HashMap();
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("items", items);
        m.put("test", EncounterType.Test_Enrollment);
        List<Object> os = encounterFacade.findObjectByJpql(j, m, TemporalType.DATE);
        for (Object o : os) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                String name = "";

                if (ic.getEncounerType() == null) {
                    continue;
                }
                switch (ic.getEncounerType()) {
                    case Test_Enrollment:
                        if (ic.getItemValue() == null) {
                            continue;
                        }
                        if (ic.getItemValue().equals(rat)) {
                            name = "rat orders";
                        } else if (ic.getItemValue().equals(pcr)) {
                            name = "pcr orders";
                        } else {
                            name = "";
                        }
                        break;
                    default:
                        name = "";
                }

                if (!name.trim().equals("")) {
                    Numbers n = new Numbers();
                    n.setInstitution(ic.getInstitution());
                    n.setNumberDate(ic.getDate());
                    n.setName(name);
                    n.setCount(ic.getCount());
                    Numbers nn = save(n);
                }
            }
        }
    }

    public void calculateCasesAndTestNumbers(Date fromDate, Date toDate) {
        String j = "select  new lk.gov.health.phsp.pojcs.InstitutionCount(count(e), e.institution, e.encounterDate, e.encounterType)  "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.encounterDate between :fd and :td "
                + " group by e.encounterDate, e.institution, e.encounterType";
        Map m = new HashMap();
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Object> os = encounterFacade.findObjectByJpql(j, m, TemporalType.DATE);
        for (Object o : os) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                String name = "";
                switch (ic.getEncounerType()) {
                    case Case_Enrollment:
                        name = "cases";
                        break;
                    case Death:
                        name = "deaths";
                        break;
                    case Test_Enrollment:
                        name = "test orders";
                        break;
                    default:
                        name = "";
                }

                if (!name.trim().equals("")) {
                    Numbers n = new Numbers();
                    n.setInstitution(ic.getInstitution());
                    n.setNumberDate(ic.getDate());
                    n.setName(name);
                    n.setCount(ic.getCount());
                    Numbers nn = save(n);
                }
            }
        }
    }

    public Numbers save(Numbers numbers) {
        if (numbers == null) {
            return null;
        }
        if (numbers.getId() != null) {
            numbersFacade.edit(numbers);
            return numbers;
        }
        Numbers databaseNumbers;
        Map m = new HashMap();
        String j = "select n "
                + " from Numbers n "
                + " where n.numberDate=:d "
                + " and n.name=:name ";
        m.put("d", numbers.getNumberDate());
        m.put("name", numbers.getName());
        if (numbers.getInstitution() != null) {
            j += " and n.institution=:ins ";
            m.put("ins", numbers.getInstitution());
        } else if (numbers.getArea() != null) {
            j += " and n.area=:area ";
            m.put("area", numbers.getArea());
        } else {
            return null;
        }
        j += " order by n.id desc";
        databaseNumbers = numbersFacade.findFirstByJpql(j, m);
        if (databaseNumbers == null) {
            numbersFacade.create(numbers);
            return numbers;
        } else {
            databaseNumbers.setCount(numbers.getCount());
            numbersFacade.edit(databaseNumbers);
            return databaseNumbers;
        }
    }

    
}
