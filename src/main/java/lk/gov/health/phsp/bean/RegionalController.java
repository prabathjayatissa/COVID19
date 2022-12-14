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

// <editor-fold defaultstate="collapsed" desc="Import">
import java.io.Serializable;
import java.text.DecimalFormat;

import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.facade.ClientFacade;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.SmsFacade;
import javax.inject.Named;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.PersonFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.InstitutionPeformance;
// </editor-fold>

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class RegionalController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">

    @EJB
    private ClientFacade clientFacade;
    @EJB
    private EncounterFacade encounterFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    ClientEncounterComponentItemFacade ceciFacade;
    @EJB
    private SmsFacade smsFacade;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    ClientApplicationController clientApplicationController;
    @Inject
    ApplicationController applicationController;
    @Inject
    ClientController clientController;
    @Inject
    private AreaApplicationController areaApplicationController;
    @Inject
    SessionController sessionController;
    @Inject
    private InstitutionApplicationController institutionApplicationController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private EncounterController encounterController;
    @Inject
    private ItemController itemController;
    @Inject
    private ItemApplicationController itemApplicationController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    private CommonController commonController;
    @Inject
    private AreaController areaController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    private DashboardApplicationController dashboardApplicationController;
    @Inject
    private PreferenceController preferenceController;
    @Inject
    MenuController menuController;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Variables">
    private Boolean nicExistsForPcr;
    private Boolean nicExistsForRat;
    private Encounter rat;
    private Encounter pcr;
    private Encounter covidCase;
    private Encounter test;
    private Encounter deleting;

    private WebUser assignee;

    private List<Encounter> tests;
    private List<ClientEncounterComponentItem> cecItems;
    private List<ClientEncounterComponentItem> selectedCecis;
    private List<Encounter> selectedToAssign;
    private Date fromDate;
    private Date toDate;

    private Item orderingCategory;
    private Item result;
    private Item testType;
    private Item managementType;
    private Institution lab;
    private Institution mohOrHospital;

    private List<Institution> regionalMohsAndHospitals;
    private List<InstitutionCount> institutionCounts;
    private List<InstitutionCount> awaitingDispatch;
    private List<InstitutionCount> awaitingReceipt;
    private List<InstitutionCount> awaitingResults;
    private List<InstitutionCount> resultsAvailable;

    private Institution institution;
    private Institution referingInstitution;
    private Institution dispatchingLab;
    private Institution divertingLab;
    private Institution assigningInstitution;

    private List<Encounter> listedToDispatch;
    private List<Encounter> listedToDivert;
    private List<Encounter> selectedToDivert;
    private List<Encounter> selectedToDispatch;
    private List<Encounter> selectedToChangeInstitution;

    private List<InstitutionCount> labSummariesToReceive;
    private List<InstitutionCount> labSummariesReceived;
    private List<InstitutionCount> labSummariesEntered;
    private List<InstitutionCount> labSummariesReviewed;
    private List<InstitutionCount> labSummariesConfirmed;
    private List<InstitutionCount> labSummariesPositive;

    private List<InstitutionPeformance> institutionPeformances;
    private List<InstitutionPeformance> institutionPeformancesFiltered;
    private InstitutionPeformance institutionPeformancesSummery;

    private Item vaccinationStatus;

    private Area district;
    private Area mohArea;
    private Area rdhs;

    private String filter;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Constructors">
    public RegionalController() {
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Functions">
    public String toSummaryByOrderedInstitutionVsLabToReceive() {
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, c.referalInstitution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type ";
        j += " and c.encounterDate between :fd and :td "
                + " and (c.receivedAtLab is null or c.receivedAtLab=:rl ) "
                + " and c.institution.rdhsArea=:rd ";
        j += " and c.sentToLab=:sl ";
        j += " group by c.institution, c.referalInstitution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("rl", false);
        m.put("sl", true);
        m.put("rd", webUserController.getLoggedInstitution().getRdhsArea());
        labSummariesToReceive = new ArrayList<>();
        List<Object> obs = encounterFacade.findObjectByJpql(j, m, TemporalType.DATE);
        // // System.out.println("obs = " + obs.size());
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesToReceive.add((InstitutionCount) o);
            }
        }
        return "/regional/summary_lab_vs_ordered_to_receive";
    }

    public void processSummaryReceivedAtLab() {
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, c.referalInstitution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.receivedAtLab=:rl ";

        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("rl", true);

        j += " and (c.institution.rdhsArea=:rd or c.institution.district=:dis) ";
        m.put("rd", webUserController.getLoggedInstitution().getRdhsArea());
        m.put("dis", webUserController.getLoggedInstitution().getDistrict());

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }

        j += " group by c.institution, c.referalInstitution";
        institutionCounts = new ArrayList<>();

        List<Object> obs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        Long c = 1l;
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                ic.setId(c);
                c++;
                institutionCounts.add(ic);
            }
        }
    }

    public String toSummaryByOrderedInstitutionVsLabToReceive1() {
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, c.referalInstitution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution.rdhsArea=:rd "
                + " and (c.receivedAtLab is null or c.receivedAtLab=:ral) "
                + " and c.sentToLab=:sl "
                + " group by c.institution, c.referalInstitution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ral", false);
        m.put("sl", true);
        m.put("rd", webUserController.getLoggedInstitution().getRdhsArea());
        labSummariesToReceive = new ArrayList<>();
        List<Object> obs = encounterFacade.findObjectByJpql(j, m, TemporalType.DATE);
        // // System.out.println("obs = " + obs.size());
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesToReceive.add((InstitutionCount) o);
            }
        }
        return "/regional/summary_lab_vs_ordered_to_receive";
    }

    public void prepareDispatchSummery() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from Encounter c "
                + " where (c.retired=:ret or c.retired is null) "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution.rdhsArea=:rdhs ";
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        j += " and (c.sentToLab is null or c.sentToLab=:sl or c.referalInstitution is null) "
                + " group by c.institution "
                + " order by c.institution.name";
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("sl", false);
        m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
        List<Object> os = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        awaitingDispatch = new ArrayList<>();
        Long c = 0l;
        for (Object o : os) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                ic.setId(c);
                c++;
                awaitingDispatch.add(ic);
            }
        }
    }

    public String toDispatchSamples() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins ";
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        j += " and (c.sentToLab is null or c.sentToLab=:sl or c.referalInstitution is null) "
                + " order by c.id";

        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("sl", false);
        listedToDispatch = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/dispatch_samples";
    }

    public String toDivertSamples() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins ";
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        j += " and c.resultEntered is null "
                + " order by c.id";
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        listedToDivert = encounterFacade.findByJpql(j, m, TemporalType.DATE);
        return "/regional/divert_samples";
    }

    public String toDispatchSamplesWithReferringLab() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins ";
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        j += " and c.sentToLab is null "
                + " order by c.id";
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        listedToDispatch = encounterFacade.findByJpql(j, m, TemporalType.DATE);
        return "/regional/dispatch_samples";
    }

    public String divertSelectedSamples() {
        if (divertingLab == null) {
            JsfUtil.addErrorMessage("Please select a lab to divert samples");
            return "";
        }
        for (Encounter e : selectedToDivert) {
            e.setSentToLab(true);
            e.setSentToLabAt(new Date());
            e.setSentToLabBy(webUserController.getLoggedUser());
            e.setReferalInstitution(divertingLab);
            encounterFacade.edit(e);
        }
        selectedToDivert = null;
        return menuController.toDivertSummary();
    }

    public String listToSelectDivertingSamples() {
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins "
                + " and c.resultEntered is null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        listedToDivert = encounterFacade.findByJpql(j, m, TemporalType.DATE);
        return menuController.toDivertSummary();
    }

    public String assignMohAreaToContactScreeningAtRegionalLevel() {
        if (selectedCecis == null || selectedCecis.isEmpty()) {
            JsfUtil.addErrorMessage("Please select contacts");
            return "";
        }
        if (mohArea == null) {
            JsfUtil.addErrorMessage("Please select an MOH Area");
            return "";
        }
        for (ClientEncounterComponentItem i : selectedCecis) {
            i.setAreaValue(mohArea);
            i.setLastEditBy(webUserController.getLoggedUser());
            i.setLastEditeAt(new Date());
            ceciFacade.edit(i);
        }
        selectedCecis = null;
        mohArea = null;
        JsfUtil.addSuccessMessage("MOH Areas added");
        return toListOfFirstContactsWithoutMohForRegionalLevel();
    }

    public String assignMohAreaToSelectedEncounters() {
        if (selectedToAssign == null || selectedToAssign.isEmpty()) {
            JsfUtil.addErrorMessage("Please select persons");
            return "";
        }
        if (mohArea == null) {
            JsfUtil.addErrorMessage("Please select an MOH Area");
            return "";
        }
        for (Encounter i : selectedToAssign) {
            if (i.getClient() != null || i.getClient().getPerson() != null) {
                i.getClient().getPerson().setMohArea(mohArea);
                personFacade.edit(i.getClient().getPerson());
                clientFacade.edit(i.getClient());
                encounterFacade.edit(i);
            }
        }
        selectedToAssign = null;
        mohArea = null;
        JsfUtil.addSuccessMessage("MOH Areas added");
        return toListOfTestsWithoutMoh();
    }

    public String assignDistrictToSelectedEncounters() {
        if (selectedToAssign == null || selectedToAssign.isEmpty()) {
            JsfUtil.addErrorMessage("Please select persons");
            return "";
        }
        if (district == null) {
            JsfUtil.addErrorMessage("Please select District");
            return "";
        }
        for (Encounter i : selectedToAssign) {
            if (i.getClient() != null || i.getClient().getPerson() != null) {
                i.getClient().getPerson().setDistrict(district);
                personFacade.edit(i.getClient().getPerson());
                clientFacade.edit(i.getClient());
                encounterFacade.edit(i);
            }
        }
        selectedToAssign = null;
        district = null;
        JsfUtil.addSuccessMessage("Districts added");
        return toListOfTestsWithoutMoh();
    }

    // Get positivity rate by MOH area
    // Rukshan
    public String toTestPositivtyRateByMoh() {
        DecimalFormat df = new DecimalFormat("0.0");

        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.mohArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (this.filter == null) {
            this.filter = "createdat";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            default:
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        j += " and c.institution.rdhsArea=:rd ";
        m.put("rd", webUserController.getLoggedInstitution().getRdhsArea());

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }

        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        j += " group by c.institution.mohArea "
                + " order by count(c) desc ";

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);

        m = new HashMap();

        j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.mohArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":

                j += " and (c.createdAt > :fd and c.createdAt < :td) ";
                break;
            case "SAMPLEDAT":
                j += " and (c.sampledAt > :fd and c.sampledAt < :td) ";
                break;
            case "RESULTSAT":
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
            default:
                j += " and (c.resultConfirmedAt > :fd and c.resultConfirmedAt < :td) ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        j += " and c.institution.rdhsArea=:rd";
        m.put("rd", webUserController.getLoggedInstitution().getRdhsArea());

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }

        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }

        j += " and c.pcrResult=:result ";
        m.put("result", itemApplicationController.getPcrPositive());

        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        j += " group by c.institution.mohArea "
                + " order by count(c) desc ";

        List<Object> objPositives = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        institutionCounts = new ArrayList<>();

        if (objCounts == null || objCounts.isEmpty()) {
            return "/regional/positivity_rate_by_moh";
        }

        for (int index = 0; index <= objPositives.size() - 1; index++) {
            InstitutionCount incPositive = (InstitutionCount) objPositives.get(index);
            InstitutionCount incCounts = (InstitutionCount) objCounts.get(index);
            double tempPositiveRate = ((double) incPositive.getCount() / incCounts.getCount() * 100);
            String tempRate = df.format(tempPositiveRate) + "%";
            InstitutionCount rateCount = new InstitutionCount();
            rateCount.setArea(incPositive.getArea());
            rateCount.setPositiveRate(tempRate);
            rateCount.setCount(incCounts.getCount());
            institutionCounts.add(rateCount);
            System.out.println(tempRate);
        }

        return "/regional/positivity_rate_by_moh";
    }

    public List<InstitutionPeformance> getInstitutionPeformances() {
        if (institutionPeformances == null) {
            institutionPeformances = new ArrayList<>();
        }
        return institutionPeformances;
    }

    public String toInstitutionvicePeformanceReport() {
        return "/regional/institution_vice_peformance_report";
    }

    public String toMohAreaResultList() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_results_moh";
    }

    public String toPcrPositiveByDistrict() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.district, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        j += " group by c.client.person.district";
        List<Object> objs = new ArrayList<>();
        try {
            objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        } catch (Exception e) {

        }
        institutionCounts = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/national/pcr_positive_counts_by_district";
    }

    public String dispatchSelectedSamples() {
        if (dispatchingLab == null) {
            JsfUtil.addErrorMessage("Please select a lab to send samples");
            return "";
        }
        for (Encounter e : selectedToDispatch) {
            e.setSentToLab(true);
            e.setSentToLabAt(new Date());
            e.setSentToLabBy(webUserController.getLoggedUser());
            e.setReferalInstitution(dispatchingLab);
            encounterFacade.edit(e);
        }
        selectedToDispatch = null;
        return toDispatchSamples();
    }

    public String toPcrPositiveByInstitutionDistrict() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution.district, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        j += " group by c.institution.district";
        List<Object> objs = new ArrayList<>();
        try {
            objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        } catch (Exception e) {

        }
        institutionCounts = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/national/pcr_positive_counts_by_institution_district";
    }

    public String toPcrPositiveByOrderedInstitute() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        j += " group by c.institution";
        List<Object> objs = new ArrayList<>();
        try {
            objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        } catch (Exception e) {

        }
        institutionCounts = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/national/pcr_positive_counts_by_ordered_institution";
    }

    public String toPcrPositiveByLab() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.referalInstitution, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        j += " and c.pcrTestType=:tt ";
        m.put("tt", testType);
        j += " and c.pcrResult=:result ";
        m.put("result", result);
        j += " group by c.referalInstitution";
        List<Object> objs = new ArrayList<>();
        try {
            objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        } catch (Exception e) {

        }
        institutionCounts = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }
        return "/national/pcr_positive_counts_by_lab";
    }

    public void deleteTest() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Nothing to delete");
            return;
        }
        if (deleting.getInstitution() == null) {
            JsfUtil.addErrorMessage("No Institution");
            return;
        }
        if (deleting.getReceivedAtLab() != null && deleting.getReferalInstitution() != null) {
            if (deleting.getReceivedAtLab() != null && deleting.getReferalInstitution() != webUserController.getLoggedInstitution()) {
                JsfUtil.addErrorMessage("Already receievd by the Lab. Can't delete.");
                return;
            }
        }
        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetiredBy(webUserController.getLoggedUser());
        JsfUtil.addSuccessMessage("Removed");
        encounterFacade.edit(deleting);
    }

    private void fillRegionalMohsAndHospitals() {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.Hospital);
        its.add(InstitutionType.MOH_Office);
        Area rdhs;
        if (webUserController.getLoggedInstitution() != null && webUserController.getLoggedInstitution().getRdhsArea() != null) {
            rdhs = webUserController.getLoggedInstitution().getRdhsArea();
            regionalMohsAndHospitals = institutionApplicationController.findRegionalInstitutions(its, rdhs);
        } else {
            regionalMohsAndHospitals = new ArrayList<>();
        }

    }

    public String toListOfTests() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and (c.institution.rdhsArea=:rdhs or c.institution.district=:district ) ";
        m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        if (this.filter == null) {
            this.filter = "createdat";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.createdAt between :fd and :td ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        return "/regional/list_of_tests";
    }

    // This function will return the request counts by MOH area fora given RE
    public String toCountOfTestsByMohArea() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.mohArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and (c.client.person.district=:district) ";
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        if (this.filter == null) {
            this.filter = "createdat";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;

            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.createdAt between :fd and :td ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        j += " group by c.client.person.mohArea "
                + " order by count(c) desc";
        institutionCounts = new ArrayList<>();
        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/regional/count_of_requests_by_moh_area";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/regional/count_of_requests_by_moh_area";
    }

    public String toCountOfTestsByOrderedInstitution() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and (c.institution.rdhsArea=:rdhs or c.institution.district=:district ) ";
        m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        if (this.filter == null) {
            this.filter = "createdAt";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;

            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;

            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;

            default:
                j += " and c.createdAt between :fd and :td ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        j += " group by c.institution"
                + " order by count(c) desc ";
        institutionCounts = new ArrayList<>();
        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/regional/count_of_tests_by_ordered_institution";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/regional/count_of_tests_by_ordered_institution";
    }

    public String toCountOfResultsByOrderedInstitution() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and (c.institution.rdhsArea=:rdhs or c.institution.district=:district ) ";
        m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        if (this.filter == null) {
            this.filter = "createdAt";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (this.vaccinationStatus != null) {
            j += " and c.vaccinationStatus=:vaccinationStatus";
            m.put("vaccinationStatus", this.vaccinationStatus);
        }

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        j += " group by c.institution"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/regional/count_of_results_by_ordered_institution";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/regional/count_of_results_by_ordered_institution";
    }

    public String toCountOfResultsByLab() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.referalInstitution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and (c.institution.rdhsArea=:rdhs or c.institution.district=:district) ";
        m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        if (this.filter == null) {
            this.filter = "createdAt";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        j += " group by c.referalInstitution"
                + " order by c.referalInstitution.name ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/regional/count_of_results_by_lab";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/regional/count_of_results_by_lab";
    }

    public String toCountOfResultsByGnArea() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.gnArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and (c.client.person.district=:dis) ";
        m.put("dis", webUserController.getLoggedInstitution().getDistrict());

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        } else {
            j += " and c.pcrResult in :results ";
            m.put("results", itemApplicationController.getPcrResults());
        }
        j += " group by c.client.person.gnArea, c.institution"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);

        if (objCounts == null || objCounts.isEmpty()) {
            return "/regional/count_of_results_by_gn";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/regional/count_of_results_by_gn";
    }

    public String toCountOfResultsByMoh() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.mohArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and (c.client.person.district=:district) ";
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        if (this.filter == null) {
            this.filter = "createdAt";
        }

        switch (this.filter = "createdAt") {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (this.vaccinationStatus != null) {
            j += " and c.vaccinationStatus=:vaccinationStatus";
            m.put("vaccinationStatus", this.vaccinationStatus);
        }

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        j += " group by c.client.person.mohArea "
                + " order by count(c) desc";
        institutionCounts = new ArrayList<>();
        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/regional/count_of_results_by_moh";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/regional/count_of_results_by_moh";
    }

    public String toListOfTestsWithoutMoh() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.client.person.mohArea is null ";
        j += " and c.client.person.district=:district ";
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        if (this.filter == null) {
            this.filter = "createdAt";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.createdAt between :fd and :td ";
                break;
        }

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_tests_without_moh";
    }

    public String toListOfFirstContactsWithoutMohForRegionalLevel() {
        Map m = new HashMap();
        String j = "select ci "
                + " from ClientEncounterComponentItem ci"
                + " join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
//        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();
//        ci.getItem().getCode();
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        j += " and ci.areaValue is null ";

        j += " and ci.item.code=:code ";
        m.put("code", "first_contacts");

        j += " and ci.areaValue2=:district ";
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        cecItems = ceciFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_first_contacts_without_moh";
    }

    public String toOrderTestsForFirstContactsForMoh() {
        Map m = new HashMap();
        String j = "select ci "
                + " from ClientEncounterComponentItem ci"
                + " join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
//        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();
//        ci.getItem().getCode();
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        j += " and ci.areaValue=:mymoh ";
        m.put("mymoh", webUserController.getLoggedInstitution().getMohArea());

        j += " and ci.item.code=:code ";
        m.put("code", "first_contacts");

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        cecItems = ceciFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/order_tests_for_moh";
    }

    public String toListOfFirstContactsForRegionalLevel() {
        Map m = new HashMap();
        String j = "select ci "
                + " from ClientEncounterComponentItem ci"
                + " join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
//        ClientEncounterComponentItem ci = new ClientEncounterComponentItem();
//        ci.getItem().getCode();
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        j += " and ci.item.code=:code ";
        m.put("code", "first_contacts");

        j += " and ci.areaValue2=:district ";
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        cecItems = ceciFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_first_contacts";
    }

    public String toListOfInvestigatedCasesForMoh() {
        return "/moh/investigated_list";
    }

    public void toDeleteTestFromLastPcrList() {
        deleteTest();
        sessionController.getPcrs().remove(deleting.getId());
    }

    public void toDeleteTestFromLastRatList() {
        deleteTest();
        sessionController.getRats().remove(deleting.getId());
    }

    public String toDeleteTestFromTestList() {
        deleteTest();
        return toListResults();
    }

    public String toRatView() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (rat.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/rat_view";
    }

    public String toRatOrderView() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (rat.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/rat_order_view";
    }

    public String toRatResultView() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (rat.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/rat_result_view";
    }

    public String toPcrResultView() {
        if (pcr == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (pcr.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/pcr_result_view";
    }

    public String toPcrView() {
        if (pcr == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (pcr.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        return "/moh/pcr_view";
    }

    public String toEditTest() {
        if (test == null) {
            JsfUtil.addErrorMessage("No Test");
            return "";
        }
        if (test.getPcrTestType().equals(itemApplicationController.getRat())) {
            rat = test;
            return toRatOrderEdit();
        } else if (test.getPcrTestType().equals(itemApplicationController.getPcr())) {
            pcr = test;
            return toPcrEdit();
        } else {
            //TODO: add to edit test when test type not given
            JsfUtil.addErrorMessage("Not a RAT or PCR.");
            return "";
        }
    }

    public String toViewTest() {
        if (test == null) {
            JsfUtil.addErrorMessage("No Test");
            return "";
        }
        if (test.getPcrTestType().equals(itemApplicationController.getRat())) {
            rat = test;
            return toRatOrderView();
        } else if (test.getPcrTestType().equals(itemApplicationController.getPcr())) {
            pcr = test;
            return toPcrView();
        } else {
            //TODO: add to edit test when test type not given
            JsfUtil.addErrorMessage("Not a RAT or PCR.");
            return "";
        }
    }

    public String toViewResult() {
        if (test == null) {
            JsfUtil.addErrorMessage("No Test");
            return "";
        }
        if (test.getPcrTestType().equals(itemApplicationController.getRat())) {
            rat = test;
            return toRatResultView();
        } else if (test.getPcrTestType().equals(itemApplicationController.getPcr())) {
            pcr = test;
            return toPcrResultView();
        } else {
            //TODO: add to edit test when test type not given
            JsfUtil.addErrorMessage("Not a RAT or PCR.");
            return "";
        }
    }

    public String toRatEdit() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (!rat.getPcrTestType().equals(itemApplicationController.getRat())) {
            JsfUtil.addErrorMessage("Not a RAT");
            return "";
        }
        return "/moh/rat";
    }

    public String toRatOrderEdit() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT");
            return "";
        }
        if (!rat.getPcrTestType().equals(itemApplicationController.getRat())) {
            JsfUtil.addErrorMessage("Not a RAT");
            return "";
        }
        return "/moh/rat_order";
    }

    public String toPcrEdit() {
        if (pcr == null) {
            JsfUtil.addErrorMessage("No PCR");
            return "";
        }
        if (!pcr.getPcrTestType().equals(itemApplicationController.getPcr())) {
            JsfUtil.addErrorMessage("Not a PCR");
            return "";
        }
        return "/moh/pcr";
    }

    public List<Area> completeDistricts(String qry) {
        return areaController.completeDistricts(qry);
    }

    public List<Area> completeMohAreas(String qry) {
        return areaController.completeMoh(qry);
    }

    public List<Item> getCitizenships() {
        return itemApplicationController.getCitizenships();
    }

    public List<Item> getSexes() {
        return itemApplicationController.getSexes();
    }

    public String toListResults() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and (c.institution.rdhsArea=:rdhs or c.institution.district=:district) ";
        m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        if (this.filter == null) {
            this.filter = "resultsat";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_results";
    }
    
    
    public String toListToAssignInstitution() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and (c.institution.rdhsArea=:rdhs or c.institution.district=:district) ";
        m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
        m.put("district", webUserController.getLoggedInstitution().getDistrict());

        if (this.filter == null) {
            this.filter = "resultsat";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
            default:
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        if (institution != null) {
            j += " and c.institution=:oi ";
            m.put("oi", institution);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/admin/change_institution";
    }
    
    public void assignInstitution(){
        if(assigningInstitution==null){
            JsfUtil.addErrorMessage("No Institution");
            return;
        }
        if(selectedToAssign==null||selectedToAssign.isEmpty()){
            JsfUtil.addErrorMessage("No Tests Selected");
            return;
        }
        int c=0;
        for(Encounter e:selectedToAssign){
            e.setInstitution(assigningInstitution);
            e.setLastEditBy(webUserController.getLoggedUser());
            e.setLastEditeAt(new Date());
            encounterFacade.edit(e);
            c++;
        }
        JsfUtil.addSuccessMessage("New Institution assigned to " + c + " institutions.");
    }

    public String toListResultsByResidentMohArea() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and (c.client.person.district=:district) ";
//        m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
        m.put("district", webUserController.getLoggedInstitution().getDistrict());
        if (this.filter == null) {
            this.filter = "createdAt";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;

            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;

            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
                break;

            default:
                j += " and c.createdAt between :fd and :td ";
                break;
        }
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_results_moh";
    }

    public String toDistrictViceTestListForOrderingCategories() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.district, c.pcrOrderingCategory, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        j += " group by c.pcrOrderingCategory, c.client.person.district";
        List<Object> objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        List<InstitutionCount> tics = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                tics.add(ic);
            }
        }
        institutionCounts = tics;
        return "/national/ordering_category_district";
    }

    public String toMohViceTestListForOrderingCategories() {
        Map m = new HashMap();

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.mohArea, c.pcrOrderingCategory, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        if (district != null) {
            j += " and c.client.person.district=:dis ";
            m.put("dis", district);
        }
        j += " group by c.pcrOrderingCategory, c.client.person.mohArea";
        List<Object> objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        List<InstitutionCount> tics = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                tics.add(ic);
            }
        }
        institutionCounts = tics;
        return "/moh/ordering_category_moh";
    }

    public String toTestRequestDistrictCounts() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_tests";
    }

    public String toTestListWithoutResults() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.institution=:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_tests_without_results";
    }

    public String toListOfTestsRegional() {
        System.out.println("toTestList");
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.client.person.district=:district ";
        m.put("district", webUserController.getLoggedUser().getArea());

        if (mohOrHospital != null) {
            j += " and c.institution=:ins ";
            m.put("ins", mohOrHospital);
        } else {
            j += " and (c.institution.rdhsArea=:rdhs or c.client.person.district=:district) ";
            m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
            m.put("district", webUserController.getLoggedInstitution().getDistrict());
        }

        if (mohArea != null) {
            j += " and c.client.person.mohArea=:moh ";
            m.put("moh", mohArea);
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/regional/list_of_tests";
    }

    public String toListCasesByManagement() {
        Map m = new HashMap();
        String j = "select distinct(c) "
                + " from ClientEncounterComponentItem ci join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

//        ClientEncounterComponentItem ci;
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        if (mohOrHospital != null) {
            j += " and c.institution=:ins ";
            m.put("ins", mohOrHospital);
        } else {
            j += " and (c.institution.rdhsArea=:rdhs or c.client.person.district=:district) ";
            m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
            m.put("district", webUserController.getLoggedInstitution().getDistrict());
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (managementType != null) {
            j += " and (ci.item.code=:mxplan and ci.itemValue.code=:planType) ";
            m.put("mxplan", "placement_of_diagnosed_patient");
            m.put("planType", managementType.getCode());
        } else {
            j += " and ci.item.code=:mxplan ";
            m.put("mxplan", "placement_of_diagnosed_patient");
        }

        j += " group by c";

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        return "/regional/list_of_cases_by_management_plan";
    }

    public String toListCasesByManagementForNationalLevel() {
        Map m = new HashMap();
        String j = "select distinct(c) "
                + " from ClientEncounterComponentItem ci join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

//        ClientEncounterComponentItem ci;
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (managementType != null) {
            j += " and (ci.item.code=:mxplan and ci.itemValue.code=:planType) ";
            m.put("mxplan", "placement_of_diagnosed_patient");
            m.put("planType", managementType.getCode());
        } else {
            j += " and ci.item.code=:mxplan ";
            m.put("mxplan", "placement_of_diagnosed_patient");
        }

        j += " group by c";

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        return "/national/list_of_cases_by_management_plan";
    }

    public List<InstitutionPeformance> getInstitutionPeformancesFiltered() {
        if (institutionPeformancesFiltered == null) {
            institutionPeformancesFiltered = new ArrayList<>();
        }
        return institutionPeformancesFiltered;
    }

    public void generateFilteredInstitutionPeformanceSummery() {
        Long p = 0l;
        Long r = 0l;
        Long pp = 0l;
        Long rp = 0l;
        if (getInstitutionPeformancesFiltered() != null && !getInstitutionPeformancesFiltered().isEmpty()) {
            for (InstitutionPeformance ip : getInstitutionPeformancesFiltered()) {
                if (ip.getPcrPositives() != null) {
                    pp += ip.getPcrPositives();
                }
                if (ip.getRatPositives() != null) {
                    rp += ip.getRatPositives();
                }
                if (ip.getPcrs() != null) {
                    p += ip.getPcrs();
                }
                if (ip.getRats() != null) {
                    r += ip.getRats();
                }
            }
        }
        getInstitutionPeformancesSummery().setId(0l);
        getInstitutionPeformancesSummery().setPcrPositives(pp);
        getInstitutionPeformancesSummery().setPcrs(p);
        getInstitutionPeformancesSummery().setRats(r);
        getInstitutionPeformancesSummery().setRatPositives(rp);

    }

    public void processInstitutionVicePeformanceReport() {
        institutionPeformances = new ArrayList<>();
        List<InstitutionType> types = new ArrayList<>();
        types.add(InstitutionType.Base_Hospital);
        types.add(InstitutionType.District_General_Hospital);
        types.add(InstitutionType.Divisional_Hospital);
        types.add(InstitutionType.Hospital);
        types.add(InstitutionType.Intermediate_Care_Centre);
        types.add(InstitutionType.Lab);
        types.add(InstitutionType.MOH_Office);
        types.add(InstitutionType.Mobile_Lab);
        types.add(InstitutionType.National_Hospital);
        types.add(InstitutionType.Private_Sector_Institute);
        types.add(InstitutionType.Primary_Medical_Care_Unit);
        types.add(InstitutionType.Private_Sector_Labatory);
        types.add(InstitutionType.Provincial_General_Hospital);
        types.add(InstitutionType.Teaching_Hospital);

        rdhs = webUserController.getLoggedUser().getInstitution().getRdhsArea();

        List<Institution> inss = institutionApplicationController.findRegionalInstitutions(types, rdhs);
        fromDate = CommonController.startOfTheDate(fromDate);
        toDate = CommonController.endOfTheDate(toDate);
        for (Institution ins : inss) {
            InstitutionPeformance ip = new InstitutionPeformance();
            ip.setInstitution(ins);
            ip.setPcrs(dashboardApplicationController.getOrderCount(ins, fromDate, toDate,
                    itemApplicationController.getPcr(), orderingCategory, null, null));
            ip.setRats(dashboardApplicationController.getOrderCount(ins, fromDate, toDate,
                    itemApplicationController.getRat(), orderingCategory, null, null));
            ip.setPcrPositives(dashboardApplicationController.getOrderCount(ins, fromDate, toDate,
                    itemApplicationController.getPcr(), orderingCategory, itemApplicationController.getPcrPositive(), null));
            ip.setRatPositives(dashboardApplicationController.getOrderCount(ins, fromDate, toDate,
                    itemApplicationController.getRat(), orderingCategory, itemApplicationController.getPcrPositive(), null));
            institutionPeformances.add(ip);
        }
        generateInstitutionPeformanceSummery();
    }

    public void generateInstitutionPeformanceSummery() {
        Long p = 0l;
        Long r = 0l;
        Long pp = 0l;
        Long rp = 0l;
        if (getInstitutionPeformances() != null && !getInstitutionPeformances().isEmpty()) {
            for (InstitutionPeformance ip : getInstitutionPeformances()) {
                if (ip.getPcrPositives() != null) {
                    pp += ip.getPcrPositives();
                }
                if (ip.getRatPositives() != null) {
                    rp += ip.getRatPositives();
                }
                if (ip.getPcrs() != null) {
                    p += ip.getPcrs();
                }
                if (ip.getRats() != null) {
                    r += ip.getRats();
                }
            }
        }
        getInstitutionPeformancesSummery().setId(0l);
        getInstitutionPeformancesSummery().setPcrPositives(pp);
        getInstitutionPeformancesSummery().setPcrs(p);
        getInstitutionPeformancesSummery().setRats(r);
        getInstitutionPeformancesSummery().setRatPositives(rp);
    }

    public InstitutionPeformance getInstitutionPeformancesSummery() {
        if (institutionPeformancesSummery == null) {
            institutionPeformancesSummery = new InstitutionPeformance();
        }
        return institutionPeformancesSummery;
    }

    public String toEnterResults() {
        System.out.println("toTestList");
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.institution=:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/enter_results";
    }

    public List<Item> getCovidTestOrderingCategories() {
        return itemApplicationController.getCovidTestOrderingCategories();
    }

    public List<Item> getCovidTestTypes() {
        return itemApplicationController.getCovidTestTypes();
    }

    public List<Item> getResultTypes() {
        return itemApplicationController.getPcrResults();
    }

    public List<Item> getManagementTypes() {
        return itemApplicationController.getManagementTypes();
    }

    public List<Institution> completeLab(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.Lab);
        return institutionController.fillInstitutions(its, qry, null);
    }

    public List<Institution> completeRegionalInstitutions(String nameQry) {
        List<Institution> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        List<InstitutionType> types = new ArrayList<>();
        types.add(InstitutionType.Hospital);
        types.add(InstitutionType.Base_Hospital);
        types.add(InstitutionType.Clinic);
        types.add(InstitutionType.District_General_Hospital);
        types.add(InstitutionType.Divisional_Hospital);
        types.add(InstitutionType.Intermediate_Care_Centre);
        types.add(InstitutionType.MOH_Office);
        types.add(InstitutionType.Mobile_Lab);
        types.add(InstitutionType.OPD);
        types.add(InstitutionType.Primary_Medical_Care_Unit);
        types.add(InstitutionType.Teaching_Hospital);
        types.add(InstitutionType.Regional_Department_of_Health_Department);
        types.add(InstitutionType.Provincial_General_Hospital);
        types.add(InstitutionType.Private_Sector_Labatory);
        types.add(InstitutionType.Private_Sector_Institute);
        Area tRdhs= webUserController.getLoggedInstitution().getRdhsArea();
        for (Institution i : allIns) {
            boolean canInclude = true;
            if (tRdhs != null) {
                if (i.getRdhsArea()== null) {
                    canInclude = false;
                } else {
                    if (!i.getRdhsArea().equals(tRdhs)) {
                        canInclude = false;
                    }
                }
            }
            boolean typeFound = false;
            for (InstitutionType type : types) {
                if (type != null) {
                    if (i.getInstitutionType() != null && i.getInstitutionType().equals(type)) {
                        typeFound = true;
                    }
                }
            }
            if (!typeFound) {
                canInclude = false;
            }
            if (i.getName() == null || i.getName().trim().equals("")) {
                canInclude = false;
            } else {
                if (!i.getName().toLowerCase().contains(nameQry.trim().toLowerCase())) {
                    canInclude = false;
                }
            }
            if (canInclude) {
                resIns.add(i);
            }
        }
        return resIns;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Getters & Setters">
// </editor-fold>
    public Encounter getRat() {
        return rat;
    }

    public void setRat(Encounter rat) {
        this.rat = rat;
    }

    public Encounter getPcr() {
        return pcr;
    }

    public void setPcr(Encounter pcr) {
        this.pcr = pcr;
    }

    public Encounter getCovidCase() {
        return covidCase;
    }

    public void setCovidCase(Encounter covidCase) {
        this.covidCase = covidCase;
    }

    public List<Encounter> getTests() {
        return tests;
    }

    public void setTests(List<Encounter> tests) {
        this.tests = tests;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheDate();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = commonController.endOfTheDay();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Encounter getTest() {
        return test;
    }

    public void setTest(Encounter test) {
        this.test = test;
    }

    public Item getOrderingCategory() {
        return orderingCategory;
    }

    public void setOrderingCategory(Item orderingCategory) {
        this.orderingCategory = orderingCategory;
    }

    public Item getResult() {
        return result;
    }

    public void setResult(Item result) {
        this.result = result;
    }

    public Item getTestType() {
        return testType;
    }

    public void setTestType(Item testType) {
        this.testType = testType;
    }

    public Institution getLab() {
        return lab;
    }

    public void setLab(Institution lab) {
        this.lab = lab;
    }

    public Encounter getDeleting() {
        return deleting;
    }

    public void setDeleting(Encounter deleting) {
        this.deleting = deleting;
    }

    public List<Institution> getRegionalMohsAndHospitals() {
        if (regionalMohsAndHospitals == null) {
            fillRegionalMohsAndHospitals();
        }
        return regionalMohsAndHospitals;
    }

    public void setRegionalMohsAndHospitals(List<Institution> regionalMohsAndHospitals) {
        this.regionalMohsAndHospitals = regionalMohsAndHospitals;
    }

    public Institution getMohOrHospital() {
        return mohOrHospital;
    }

    public void setMohOrHospital(Institution mohOrHospital) {
        this.mohOrHospital = mohOrHospital;
    }

    public List<InstitutionCount> getInstitutionCounts() {
        return institutionCounts;
    }

    public Boolean getNicExistsForPcr() {
        return nicExistsForPcr;
    }

    public void setNicExistsForPcr(Boolean nicExistsForPcr) {
        this.nicExistsForPcr = nicExistsForPcr;
    }

    public Boolean getNicExistsForRat() {
        return nicExistsForRat;
    }

    public void setNicExistsForRat(Boolean nicExistsForRat) {
        this.nicExistsForRat = nicExistsForRat;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Area getRdhs() {
        return this.rdhs;
    }

    public void setRdhs(Area rhds) {
        this.rdhs = rhds;
    }

    public WebUser getAssignee() {
        return assignee;
    }

    public void setAssignee(WebUser assignee) {
        this.assignee = assignee;
    }

    public List<Encounter> getSelectedToAssign() {
        return selectedToAssign;
    }

    public void setSelectedToAssign(List<Encounter> selectedToAssign) {
        this.selectedToAssign = selectedToAssign;
    }

    public List<Area> completeMohsPerDistrict(String qry) {
        return areaController.getMohAreasOfADistrict(areaController.getAreaByName(webUserController.getLoggedUser().getArea().toString(), AreaType.District, false, null));
    }

    public List<ClientEncounterComponentItem> getCecItems() {
        return cecItems;
    }

    public void setCecItems(List<ClientEncounterComponentItem> cecItems) {
        this.cecItems = cecItems;
    }

    public List<ClientEncounterComponentItem> getSelectedCecis() {
        return selectedCecis;
    }

    public void setSelectedCecis(List<ClientEncounterComponentItem> selectedCecis) {
        this.selectedCecis = selectedCecis;
    }

    public Area getMohArea() {
        return mohArea;
    }

    public void setMohArea(Area mohArea) {
        this.mohArea = mohArea;
    }

    public Item getManagementType() {
        return managementType;
    }

    public void setManagementType(Item managementType) {
        this.managementType = managementType;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Institution getReferingInstitution() {
        return referingInstitution;
    }

    public void setReferingInstitution(Institution referingInstitution) {
        this.referingInstitution = referingInstitution;
    }

    public Institution getDispatchingLab() {
        return dispatchingLab;
    }

    public void setDispatchingLab(Institution dispatchingLab) {
        this.dispatchingLab = dispatchingLab;
    }

    public Institution getDivertingLab() {
        return divertingLab;
    }

    public void setDivertingLab(Institution divertingLab) {
        this.divertingLab = divertingLab;
    }

    public List<Encounter> getListedToDispatch() {
        return listedToDispatch;
    }

    public void setListedToDispatch(List<Encounter> listedToDispatch) {
        this.listedToDispatch = listedToDispatch;
    }

    public List<Encounter> getListedToDivert() {
        return listedToDivert;
    }

    public void setListedToDivert(List<Encounter> listedToDivert) {
        this.listedToDivert = listedToDivert;
    }

    public List<Encounter> getSelectedToDivert() {
        return selectedToDivert;
    }

    public void setSelectedToDivert(List<Encounter> selectedToDivert) {
        this.selectedToDivert = selectedToDivert;
    }

    public List<InstitutionCount> getLabSummariesToReceive() {
        return labSummariesToReceive;
    }

    public void setLabSummariesToReceive(List<InstitutionCount> labSummariesToReceive) {
        this.labSummariesToReceive = labSummariesToReceive;
    }

    public List<InstitutionCount> getLabSummariesReceived() {
        return labSummariesReceived;
    }

    public void setLabSummariesReceived(List<InstitutionCount> labSummariesReceived) {
        this.labSummariesReceived = labSummariesReceived;
    }

    public List<InstitutionCount> getLabSummariesEntered() {
        return labSummariesEntered;
    }

    public void setLabSummariesEntered(List<InstitutionCount> labSummariesEntered) {
        this.labSummariesEntered = labSummariesEntered;
    }

    public List<InstitutionCount> getLabSummariesReviewed() {
        return labSummariesReviewed;
    }

    public void setLabSummariesReviewed(List<InstitutionCount> labSummariesReviewed) {
        this.labSummariesReviewed = labSummariesReviewed;
    }

    public List<InstitutionCount> getLabSummariesConfirmed() {
        return labSummariesConfirmed;
    }

    public void setLabSummariesConfirmed(List<InstitutionCount> labSummariesConfirmed) {
        this.labSummariesConfirmed = labSummariesConfirmed;
    }

    public List<InstitutionCount> getLabSummariesPositive() {
        return labSummariesPositive;
    }

    public void setLabSummariesPositive(List<InstitutionCount> labSummariesPositive) {
        this.labSummariesPositive = labSummariesPositive;
    }

    public List<Encounter> getSelectedToDispatch() {
        return selectedToDispatch;
    }

    public void setSelectedToDispatch(List<Encounter> selectedToDispatch) {
        this.selectedToDispatch = selectedToDispatch;
    }

    public List<InstitutionCount> getResultsAvailable() {
        return resultsAvailable;
    }

    public void setResultsAvailable(List<InstitutionCount> resultsAvailable) {
        this.resultsAvailable = resultsAvailable;
    }

    public List<InstitutionCount> getAwaitingDispatch() {
        return awaitingDispatch;
    }

    public List<Item> getInvestigationFilters() {
        return itemApplicationController.getInvestigationFilters();
    }

    public void setAwaitingDispatch(List<InstitutionCount> awaitingDispatch) {
        this.awaitingDispatch = awaitingDispatch;
    }

    public List<InstitutionCount> getAwaitingReceipt() {
        return awaitingReceipt;
    }

    public void setAwaitingReceipt(List<InstitutionCount> awaitingReceipt) {
        this.awaitingReceipt = awaitingReceipt;
    }

    public List<InstitutionCount> getAwaitingResults() {
        return awaitingResults;
    }

    public void setAwaitingResults(List<InstitutionCount> awaitingResults) {
        this.awaitingResults = awaitingResults;
    }

    public String getFilter() {
        return this.filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Item getVaccinationStatus() {
        return this.vaccinationStatus;
    }

    public void setVaccinationStatus(Item vStatus) {
        this.vaccinationStatus = vStatus;
    }

    public Institution getAssigningInstitution() {
        return assigningInstitution;
    }

    public void setAssigningInstitution(Institution assigningInstitution) {
        this.assigningInstitution = assigningInstitution;
    }

    public List<Encounter> getSelectedToChangeInstitution() {
        return selectedToChangeInstitution;
    }

    public void setSelectedToChangeInstitution(List<Encounter> selectedToChangeInstitution) {
        this.selectedToChangeInstitution = selectedToChangeInstitution;
    }

    
    
}
