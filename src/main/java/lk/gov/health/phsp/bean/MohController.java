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
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.facade.ClientFacade;
import java.util.ArrayList;
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

import com.itextpdf.text.html.WebColors;

import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.AreaFacade;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
// </editor-fold>

/**
 *
 * @author buddhika
 */
@Named(value = "mohController")
@SessionScoped
public class MohController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">

    @EJB
    private ClientFacade clientFacade;
    @EJB
    private EncounterFacade encounterFacade;
    @EJB
    private ClientEncounterComponentItemFacade ceciFacade;
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
    private PreferenceController preferenceController;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Variables">
    private Institution dispatchingLab;
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
    private List<Encounter> listedToDispatch;
    private List<Encounter> selectedToDispatch;
    private Date fromDate;
    private Date toDate;

    Area phiArea;
    List<Area> phiAreas;

    private Item orderingCategory;
    private Item result;
    private Item testType;
    private Item managementType;
    private Institution lab;
    private Institution mohOrHospital;

    private List<Institution> regionalMohsAndHospitals;
    private List<InstitutionCount> institutionCounts;

    private Area district;
    private Area mohArea;

    private String filter;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Constructors">
    public MohController() {
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Functions">
    public String toAddPhiArea() {
        phiArea = new Area();
        phiArea.setType(AreaType.PHI);
        phiArea.setParentArea(webUserController.getLoggedInstitution().getMohArea());
        phiArea.setMoh(webUserController.getLoggedInstitution().getMohArea());
        phiArea.setDistrict(webUserController.getLoggedInstitution().getDistrict());
        phiArea.setRdhsArea(webUserController.getLoggedInstitution().getRdhsArea());
        phiArea.setProvince(webUserController.getLoggedInstitution().getProvince());
        phiArea.setPdhsArea(webUserController.getLoggedInstitution().getPdhsArea());
        return "/moh/admin/phi_area";
    }

    public String toViewPhiArea() {
        phiAreas = areaApplicationController.getPhiAreasOfMoh(webUserController.getLoggedInstitution().getMohArea());
        return "/moh/admin/phi_areas";
    }

    public String toViewPhiAreaOfMoh() {
        if (phiArea == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return "";
        }
        return "/moh/admin/phi_area";
    }

    public void savePhiArea() {
        if (phiArea == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        areaApplicationController.saveArea(phiArea, webUserController.getLoggedUser());
        phiAreas=null;
        JsfUtil.addSuccessMessage("Saved");
        toViewPhiAreaOfMoh();
    }

    public String toCountOfTestsByGn() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.gnArea, c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        if (false) {
            Encounter c = new Encounter();
            c.getClient().getPerson().getMohArea();
//           c.client.person.gnArea
        }

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and (c.client.person.mohArea=:moh) ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

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
        j += " group by c.client.person.gnArea, c.institution"
                + " order by c.client.person.gnArea.name ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/moh/count_of_tests_by_gn";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/moh/count_of_tests_by_gn";
    }

    public String toCountOfResultsByGnArea() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.gnArea, c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and (c.client.person.mohArea=:moh) ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

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
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        j += " group by c.client.person.gnArea, c.institution"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);

        if (objCounts == null || objCounts.isEmpty()) {
            return "/moh/count_of_results_by_gn";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/moh/count_of_results_by_gn";
    }

    public String toCountOfResultsByPhiArea() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.phiArea, c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and (c.client.person.mohArea=:moh) ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

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
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        j += " group by c.client.person.gnArea, c.institution"
                + " order by count(c) desc ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/moh/count_of_results_by_phi";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/moh/count_of_results_by_phi";
    }

    public String toCountOfTestsByPhi() {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.phiArea, c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        if (false) {
            Encounter c = new Encounter();
            c.getClient().getPerson().getPhiArea();
//           c.client.person.gnArea
        }

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and (c.client.person.mohArea=:moh) ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

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
        j += " group by c.client.person.phiArea, c.institution"
                + " order by c.client.person.phiArea.name ";

        institutionCounts = new ArrayList<>();

        List<Object> objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        if (objCounts == null || objCounts.isEmpty()) {
            return "/moh/count_of_tests_by_phi";
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
            }
        }

        return "/moh/count_of_tests_by_phi";
    }

//    public String toDispatchSamplesByMohOrHospital() {
//        String j = "select c "
//                + " from Encounter c "
//                + " where (c.retired is null or c.retired=:ret ) "
//                + " and c.encounterType=:type "
//                + " and c.encounterDate between :fd and :td "
//                + " and c.institution=:ins "
//                + " and (c.sentToLab is null or c.sentToLab=:stl) "
//                + " order by c.id";
//        Map m = new HashMap();
//        m.put("ret", false);
//        m.put("type", EncounterType.Test_Enrollment);
//        m.put("fd", getFromDate());
//        m.put("td", getToDate());
//        m.put("stl", false);
//        m.put("ins", webUserController.getLoggedInstitution());
//        listedToDispatch = encounterFacade.findByJpql(j, m, TemporalType.DATE);
//        return "/moh/dispatch_samples";
//    }
    public String toDispatchSamples() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.sentToLab is null ";
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        j += " order by c.id";
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", CommonController.startOfTheDate(getFromDate()));
        m.put("td", CommonController.endOfTheDate(getToDate()));
        m.put("ins", webUserController.getLoggedInstitution());
        listedToDispatch = encounterFacade.findByJpql(j, m, TemporalType.DATE);
        return "/moh/dispatch_samples";
    }

    public String toAssignInvestigation() {
//        testType = itemApplicationController.getPcr();
        result = itemApplicationController.getPcrPositive();

        System.out.println("toTestList");
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) "
                + " and (c.completed is null or c.completed=:com) ";
        m.put("ret", false);
        m.put("com", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.client.person.mohArea=:moh ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

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

        return "/moh/assign_investigation";
    }

    public String toStartInvestigation() {
        return "/moh/start_investigation";
    }

    public String toViewInvestigatedCases() {
        return "/moh/view_investigated_cases";
    }

    public void assignToInvestigate() {
        if (assignee == null) {
            JsfUtil.addErrorMessage("Please select someone to assign the investigation");
            return;
        }
        if (selectedToAssign == null || selectedToAssign.isEmpty()) {
            JsfUtil.addErrorMessage("Please select cases to assign the investigation");
            return;
        }
        for (Encounter e : selectedToAssign) {
            e.setCompletedBy(assignee);
            encounterFacade.edit(e);
        }
        selectedToAssign = null;
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

    public Boolean checkNicExists(String nic, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.nic=:nic ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("nic", nic);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = clientFacade.countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }
    }

    public void checkNicExistsForRat() {
        nicExistsForRat = null;
        if (rat == null) {
            return;
        }
        if (rat.getClient() == null) {
            return;
        }
        if (rat.getClient().getPerson() == null) {
            return;
        }
        if (rat.getClient().getPerson().getNic() == null) {
            return;
        }
        if (rat.getClient().getPerson().getNic().trim().equals("")) {
            return;
        }
        nicExistsForRat = checkNicExists(rat.getClient().getPerson().getNic(), rat.getClient());
    }

    public void checkNicExistsForPcr() {
        nicExistsForPcr = null;
        if (pcr == null) {
            return;
        }
        if (pcr.getClient() == null) {
            return;
        }
        if (pcr.getClient().getPerson() == null) {
            return;
        }
        if (pcr.getClient().getPerson().getNic() == null) {
            return;
        }
        if (pcr.getClient().getPerson().getNic().trim().equals("")) {
            return;
        }
        nicExistsForPcr = checkNicExists(pcr.getClient().getPerson().getNic(), pcr.getClient());
    }

    public Client lastClientWithNic(String nic, Client c) {
        if (nic == null || nic.trim().equals("")) {
            return null;
        }
        String jpql = "select c "
                + " from Client c "
                + " where c.retired=:ret "
                + " and c.person.nic=:nic ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("nic", nic);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        jpql += " order by c.id desc";
        return clientFacade.findFirstByJpql(jpql, m);
    }

    public String toPcrPositiveReportsIndexNational() {
        fromDate = CommonController.startOfTheDate();
        toDate = CommonController.endOfTheDate();
        return "/national/pcr_positive_links";
    }

    public String toPcrPositiveCasesList() {
        result = itemApplicationController.getPcrPositive();
        testType = itemApplicationController.getPcr();
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
        return "/national/result_list_pcr_positive";
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

//    public String toListOfTests() {
//        return "/moh/list_of_tests";
//    }
//
//    public String toListOfTestsWithoutMohForRegionalLevel() {
//        Map m = new HashMap();
//        String j = "select c "
//                + " from Encounter c "
//                + " where (c.retired is null or c.retired=:ret) ";
//        m.put("ret", false);
//
//        j += " and c.encounterType=:etype ";
//        m.put("etype", EncounterType.Test_Enrollment);
//
//        j += " and c.client.person.mohArea is null ";
//
//        if (mohOrHospital != null) {
//            j += " and c.institution=:ins ";
//            m.put("ins", mohOrHospital);
//        } else {
//            j += " and (c.institution.rdhsArea=:rdhs or c.client.person.district=:district) ";
//            m.put("rdhs", webUserController.getLoggedInstitution().getRdhsArea());
//            m.put("district", webUserController.getLoggedInstitution().getDistrict());
//        }
//
//        j += " and c.createdAt between :fd and :td ";
//        m.put("fd", getFromDate());
//        System.out.println("getFromDate() = " + getFromDate());
//        m.put("td", getToDate());
//        System.out.println(" getToDate() = " + getToDate());
//        if (testType != null) {
//            j += " and c.pcrTestType=:tt ";
//            m.put("tt", testType);
//        }
//        if (orderingCategory != null) {
//            j += " and c.pcrOrderingCategory=:oc ";
//            m.put("oc", orderingCategory);
//        }
//        if (result != null) {
//            j += " and c.pcrResult=:result ";
//            m.put("result", result);
//        }
//        if (lab != null) {
//            j += " and c.referalInstitution=:ri ";
//            m.put("ri", lab);
//        }
//        System.out.println("j = " + j);
//        System.out.println("m = " + m);
//
//        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
//        System.out.println("tests = " + tests.size());
//
//        return "/regional/list_of_tests_without_moh";
//    }
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
        return "/moh/list_of_first_contacts_to_test";
    }

    public String toListOfFirstContactsToTest() {
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

        j += " and ci.areaValue=:moh ";
        m.put("moh", webUserController.getLoggedUser().getArea());

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
        return toListOfResultsForOrdersFromMyMoh();
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
            return toRatEdit();
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

    public String toViewRequest() {
        if (test == null) {
            JsfUtil.addErrorMessage("No Test");
            return "";
        }
        return "/moh/request_view";
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

    // Render the view PCR request with result
    public String toAddNewPCRWithResult() {
        pcr = new Encounter();
        nicExistsForPcr = null;
        Date date = new Date();
        Client client = new Client();

        client.getPerson().setDistrict(webUserController.getLoggedUser().getInstitution().getDistrict());
        client.getPerson().setMohArea(webUserController.getLoggedUser().getInstitution().getMohArea());
        client.getPerson().setPhiArea(webUserController.getLoggedUser().getInstitution().getPhiArea());
        pcr.setPcrTestType(itemApplicationController.getPcr());
        pcr.setPcrOrderingCategory(sessionController.getLastPcrOrdringCategory());
        pcr.setClient(client);
        pcr.setInstitution(webUserController.getLoggedInstitution());
        pcr.setCreatedInstitution(webUserController.getLoggedInstitution());
        pcr.setEncounterType(EncounterType.Test_Enrollment);
        pcr.setEncounterDate(date);
        pcr.setEncounterFrom(date);
        pcr.setEncounterMonth(CommonController.getMonth(date));
        pcr.setEncounterQuarter(CommonController.getQuarter(date));
        pcr.setEncounterYear(CommonController.getYear(date));

        if (sessionController.getLastLab() == null) {
            pcr.setReferalInstitution(sessionController.getLastLab());
        } else {
            pcr.setReferalInstitution(webUserController.getLoggedInstitution());
        }

        pcr.setCreatedAt(date);
        pcr.setCreatedBy(webUserController.getLoggedUser());

        pcr.setSampled(true);
        pcr.setSampledAt(date);
        pcr.setSampledBy(webUserController.getLoggedUser());

        pcr.setResultConfirmedAt(date);

        if (sessionController.getLastWorkplace() != null) {
            pcr.getClient().getPerson().setWorkPlace(sessionController.getLastWorkplace());
        }

        if (sessionController.getLastContactOfWorkplace() != null) {
            pcr.getClient().getPerson().setWorkplaceContact(sessionController.getLastContactOfWorkplace());
        }

        if (sessionController.getLastContactOfWorkplaceDetails() != null) {
            pcr.getClient().getPerson().setWorkplaceContactDetails(sessionController.getLastContactOfWorkplaceDetails());
        }

        pcr.setCreatedAt(date);

        return "/moh/pcr_with_result";
    }

    public String toSaveAndNewPcrWithResult() {
        if (savePcr() != null) {
            return toAddNewPcrResultWithNewClient();
        } else {
            return "";
        }
    }

    public String toAddNewRatWithNewClient() {
        rat = new Encounter();
        nicExistsForRat = null;
        Date d = new Date();
        Client c = new Client();
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);
        rat.setInstitution(webUserController.getLoggedInstitution());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));

        if (sessionController.getLastLab() != null) {
            rat.setReferalInstitution(sessionController.getLastLab());
        } else {
            rat.setReferalInstitution(webUserController.getLoggedInstitution());
        }

        rat.setCreatedAt(new Date());
        rat.setCreatedBy(webUserController.getLoggedUser());

        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());

        rat.setResultConfirmedAt(new Date());

        if (sessionController.getLastWorkplace() != null) {
            rat.getClient().getPerson().setWorkPlace(sessionController.getLastWorkplace());
        }

        if (sessionController.getLastContactOfWorkplace() != null) {
            rat.getClient().getPerson().setWorkplaceContact(sessionController.getLastContactOfWorkplace());
        }

        if (sessionController.getLastContactOfWorkplaceDetails() != null) {
            rat.getClient().getPerson().setWorkplaceContactDetails(sessionController.getLastContactOfWorkplaceDetails());
        }

        rat.setCreatedAt(new Date());
        return "/moh/rat";
    }

    public String toAddNewRatOrderWithNewClient() {
        rat = new Encounter();
        nicExistsForRat = null;
        Date d = new Date();
        Client c = new Client();
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);
        rat.setInstitution(webUserController.getLoggedInstitution());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setReferalInstitution(lab);
        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));

        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());

        rat.setCreatedAt(new Date());

        rat.setCreatedAt(new Date());
        rat.setCreatedBy(webUserController.getLoggedUser());

        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());

        rat.setSentToLab(true);
        rat.setSentToLabAt(new Date());
        rat.setSentToLabBy(webUserController.getLoggedUser());

        rat.setReceivedAtLab(true);
        rat.setReceivedAtLabAt(d);
        rat.setReceivedAtLabBy(webUserController.getLoggedUser());

        if (sessionController.getLastWorkplace() != null) {
            rat.getClient().getPerson().setWorkPlace(sessionController.getLastWorkplace());
        }

        if (sessionController.getLastContactOfWorkplace() != null) {
            rat.getClient().getPerson().setWorkplaceContact(sessionController.getLastContactOfWorkplace());
        }

        if (sessionController.getLastContactOfWorkplaceDetails() != null) {
            rat.getClient().getPerson().setWorkplaceContactDetails(sessionController.getLastContactOfWorkplaceDetails());
        }

        return "/moh/rat_order";
    }

    public String toAddNewPcrResultWithNewClient() {
        pcr = new Encounter();
        nicExistsForPcr = null;
        Date d = new Date();
        Client c = new Client();
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        pcr.setPcrTestType(itemApplicationController.getPcr());
        pcr.setPcrOrderingCategory(sessionController.getLastPcrOrdringCategory());

        pcr.setClient(c);
        pcr.setInstitution(webUserController.getLoggedInstitution());
        pcr.setCreatedInstitution(webUserController.getLoggedInstitution());
        pcr.setReferalInstitution(lab);
        pcr.setEncounterType(EncounterType.Test_Enrollment);
        pcr.setEncounterDate(d);
        pcr.setEncounterFrom(d);
        pcr.setEncounterMonth(CommonController.getMonth(d));
        pcr.setEncounterQuarter(CommonController.getQuarter(d));
        pcr.setEncounterYear(CommonController.getYear(d));
        pcr.setSampled(true);
        pcr.setSampledAt(new Date());
        pcr.setSampledBy(webUserController.getLoggedUser());
        pcr.setCreatedAt(new Date());

        if (sessionController.getLastWorkplace() != null) {
            pcr.getClient().getPerson().setWorkPlace(sessionController.getLastWorkplace());
        }

        if (sessionController.getLastContactOfWorkplace() != null) {
            pcr.getClient().getPerson().setWorkplaceContact(sessionController.getLastContactOfWorkplace());
        }

        if (sessionController.getLastContactOfWorkplaceDetails() != null) {
            pcr.getClient().getPerson().setWorkplaceContactDetails(sessionController.getLastContactOfWorkplaceDetails());
        }

        return "/moh/pcr_with_result";
    }

    public String toAddNewPcrWithNewClient() {
        pcr = new Encounter();
        nicExistsForPcr = null;
        Date d = new Date();
        Client c = new Client();
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        pcr.setPcrTestType(itemApplicationController.getPcr());
        pcr.setPcrOrderingCategory(sessionController.getLastPcrOrdringCategory());

        pcr.setClient(c);
        pcr.setInstitution(webUserController.getLoggedInstitution());
        pcr.setCreatedInstitution(webUserController.getLoggedInstitution());
        pcr.setReferalInstitution(lab);
        pcr.setEncounterType(EncounterType.Test_Enrollment);
        pcr.setEncounterDate(d);
        pcr.setEncounterFrom(d);
        pcr.setEncounterMonth(CommonController.getMonth(d));
        pcr.setEncounterQuarter(CommonController.getQuarter(d));
        pcr.setEncounterYear(CommonController.getYear(d));
        pcr.setSampled(true);
        pcr.setSampledAt(new Date());
        pcr.setSampledBy(webUserController.getLoggedUser());
        pcr.setCreatedAt(new Date());

        if (sessionController.getLastWorkplace() != null) {
            pcr.getClient().getPerson().setWorkPlace(sessionController.getLastWorkplace());
        }

        if (sessionController.getLastContactOfWorkplace() != null) {
            pcr.getClient().getPerson().setWorkplaceContact(sessionController.getLastContactOfWorkplace());
        }

        if (sessionController.getLastContactOfWorkplaceDetails() != null) {
            pcr.getClient().getPerson().setWorkplaceContactDetails(sessionController.getLastContactOfWorkplaceDetails());
        }

        return "/moh/pcr";
    }

    public String toAddNewPcrWithExistingNic(int path) {
        if (pcr == null) {
            return "";
        }
        if (pcr.getClient() == null) {
            return "";
        }
        if (pcr.getClient().getPerson() == null) {
            return "";
        }
        if (pcr.getClient().getPerson().getNic() == null || pcr.getClient().getPerson().getNic().trim().equals("")) {
            return "";
        }
        Client nicClient = lastClientWithNic(pcr.getClient().getPerson().getNic(), pcr.getClient());
        if (nicClient == null) {
            return "";
        }
        nicExistsForPcr = null;
        Encounter tmpEnc = pcr;
        pcr = new Encounter();
        pcr.setEncounterNumber(tmpEnc.getEncounterNumber());
        Date d = new Date();
        Client c = nicClient;
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        pcr.setPcrTestType(itemApplicationController.getPcr());
        pcr.setPcrOrderingCategory(sessionController.getLastPcrOrdringCategory());
        pcr.setClient(c);
        pcr.setInstitution(webUserController.getLoggedInstitution());
        pcr.setCreatedInstitution(webUserController.getLoggedInstitution());
        pcr.setReferalInstitution(lab);
        pcr.setEncounterType(EncounterType.Test_Enrollment);
        pcr.setEncounterDate(d);
        pcr.setEncounterFrom(d);
        pcr.setEncounterMonth(CommonController.getMonth(d));
        pcr.setEncounterQuarter(CommonController.getQuarter(d));
        pcr.setEncounterYear(CommonController.getYear(d));
        pcr.setSampled(true);
        pcr.setSampledAt(new Date());
        pcr.setSampledBy(webUserController.getLoggedUser());
        pcr.setCreatedAt(new Date());

        if (path == 1) {
            return "/moh/pcr";
        } else if (path == 2) {
            return "/moh/pcr_with_result";
        } else {
            return "/moh/pcr";
        }
    }

    public String toAddNewPcrWithExistingClient() {
        if (clientController.getSelected() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }

        Client nicClient = clientController.getSelected();
        if (nicClient == null) {
            return "";
        }
        nicExistsForPcr = null;
        Encounter tmpEnc = pcr;
        pcr = new Encounter();
        pcr.setEncounterNumber(tmpEnc.getEncounterNumber());
        Date d = new Date();
        Client c = nicClient;
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        pcr.setPcrTestType(itemApplicationController.getPcr());
        pcr.setPcrOrderingCategory(sessionController.getLastPcrOrdringCategory());
        pcr.setClient(c);
        pcr.setInstitution(webUserController.getLoggedInstitution());
        pcr.setCreatedInstitution(webUserController.getLoggedInstitution());
        pcr.setReferalInstitution(lab);
        pcr.setEncounterType(EncounterType.Test_Enrollment);
        pcr.setEncounterDate(d);
        pcr.setEncounterFrom(d);
        pcr.setEncounterMonth(CommonController.getMonth(d));
        pcr.setEncounterQuarter(CommonController.getQuarter(d));
        pcr.setEncounterYear(CommonController.getYear(d));
        pcr.setSampled(true);
        pcr.setSampledAt(new Date());
        pcr.setSampledBy(webUserController.getLoggedUser());
        pcr.setCreatedAt(new Date());
        return "/moh/pcr";
    }

    public String toAddNewRatOrderWithExistingNic() {
        if (rat == null) {
            return "";
        }
        if (rat.getClient() == null) {
            return "";
        }
        if (rat.getClient().getPerson() == null) {
            return "";
        }
        if (rat.getClient().getPerson().getNic() == null || rat.getClient().getPerson().getNic().trim().equals("")) {
            return "";
        }
        Client nicClient = lastClientWithNic(rat.getClient().getPerson().getNic(), rat.getClient());
        if (nicClient == null) {
            return "";
        }
        nicExistsForRat = null;
        Encounter tmpEnc = rat;
        rat = new Encounter();
        rat.setEncounterNumber(tmpEnc.getEncounterNumber());
        Date d = new Date();

        Client c = nicClient;
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);
        rat.setInstitution(webUserController.getLoggedInstitution());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setReferalInstitution(lab);
        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));
        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());
        rat.setCreatedAt(new Date());
        return "/moh/rat_order";
    }

    public String toAddNewRatOrderWithExistingClient() {
        if (clientController.getSelected() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        Client nicClient = clientController.getSelected();
        if (nicClient == null) {
            return "";
        }
        nicExistsForRat = null;
        Encounter tmpEnc = rat;
        rat = new Encounter();
        rat.setEncounterNumber(tmpEnc.getEncounterNumber());
        Date d = new Date();

        Client c = nicClient;
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);
        rat.setInstitution(webUserController.getLoggedInstitution());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setReferalInstitution(lab);
        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));
        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());
        rat.setCreatedAt(new Date());
        return "/moh/rat_order";
    }

    // The event is triggered when user clicks NIC exists on a RAT ordering page
    // If NIC exists and called from New Rat Request page the user is redirected to RAT Request page with user details
    // If user called the function from RAT request with results page the user will be redirected to RAT request with results page with user details
    public String toAddNewRatWithExistingNic(int path) {
        if (rat == null) {
            return "";
        }
        if (rat.getClient() == null) {
            return "";
        }
        if (rat.getClient().getPerson() == null) {
            return "";
        }
        if (rat.getClient().getPerson().getNic() == null || rat.getClient().getPerson().getNic().trim().equals("")) {
            return "";
        }
        Client nicClient = lastClientWithNic(rat.getClient().getPerson().getNic(), rat.getClient());
        if (nicClient == null) {
            return "";
        }
        nicExistsForRat = null;
        Encounter tmpEnc = rat;
        rat = new Encounter();
        rat.setEncounterNumber(tmpEnc.getEncounterNumber());
        Date d = new Date();
        Client c = nicClient;
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);
        rat.setInstitution(webUserController.getLoggedInstitution());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));

        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());

        rat.setReferalInstitution(webUserController.getLoggedInstitution());

        rat.setResultConfirmed(Boolean.TRUE);
        rat.setResultConfirmedAt(d);
        rat.setResultConfirmedBy(webUserController.getLoggedUser());

        rat.setCreatedAt(new Date());
        if (path == 1) {
            return "/moh/rat_order";
        } else if (path == 2) {
            return "/moh/rat";
        } else {
            return "/moh/rat_order";
        }
    }

    public String toAddNewRatWithExistingClient() {
        if (clientController.getSelected() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        Client exClient = clientController.getSelected();
        if (exClient == null) {
            return "";
        }
        nicExistsForRat = null;
        Encounter tmpEnc = rat;
        rat = new Encounter();
        rat.setEncounterNumber(tmpEnc.getEncounterNumber());
        Date d = new Date();
        Client c = exClient;
        c.getPerson().setDistrict(webUserController.getLoggedInstitution().getDistrict());
        c.getPerson().setMohArea(webUserController.getLoggedInstitution().getMohArea());
        rat.setPcrTestType(itemApplicationController.getRat());
        rat.setPcrOrderingCategory(sessionController.getLastRatOrderingCategory());
        rat.setClient(c);
        rat.setInstitution(webUserController.getLoggedInstitution());
        rat.setCreatedInstitution(webUserController.getLoggedInstitution());
        rat.setEncounterType(EncounterType.Test_Enrollment);
        rat.setEncounterDate(d);
        rat.setEncounterFrom(d);
        rat.setEncounterMonth(CommonController.getMonth(d));
        rat.setEncounterQuarter(CommonController.getQuarter(d));
        rat.setEncounterYear(CommonController.getYear(d));

        rat.setSampled(true);
        rat.setSampledAt(new Date());
        rat.setSampledBy(webUserController.getLoggedUser());

        rat.setReferalInstitution(webUserController.getLoggedInstitution());

        rat.setResultConfirmed(Boolean.TRUE);
        rat.setResultConfirmedAt(d);
        rat.setResultConfirmedBy(webUserController.getLoggedUser());

        rat.setCreatedAt(new Date());
        return "/moh/rat";
    }

    public String saveRatAndToNewRat() {
        if (saveRat() == null) {
            return "";
        }
        JsfUtil.addSuccessMessage("Ready to enter a new RAT");
        return toAddNewRatWithNewClient();
    }

    public String saveRatAndToNewRatOrder() {
        if (saveRat() == null) {
            return "";
        }
        JsfUtil.addSuccessMessage("Ready to enter a new RAT Order");
        return toAddNewRatOrderWithNewClient();
    }

    public String saveRatAndToRatView() {
        if (saveRat() != null) {
            return toRatView();
        } else {
            return "";
        }
    }

    public String saveRatAndToTestList() {
        boolean newOne = false;
        if (pcr != null) {
            if (pcr.getId() == null) {
                newOne = true;
            }
        }
        if (saveRat() != null) {
            if (newOne) {
                return toTestList();
            }
            return toTestListNoProcess();
        } else {
            return "";
        }
    }

    public String saveRatAndToRatOrderView() {
        saveRat();
        return toRatView();
    }

    public String savePcrAndToNewPcr() {
        if (savePcr() != null) {
            return toAddNewPcrWithNewClient();
        } else {
            return "";
        }
    }

    public String savePcrAndToPcrView() {
        if (savePcr() != null) {
            return toPcrView();
        } else {
            return "";
        }
    }

    public String savePcrAndToTestList() {
        boolean newOne = false;
        if (pcr != null) {
            if (pcr.getId() == null) {
                newOne = true;
            }
        }
        if (savePcr() != null) {
            if (newOne) {
                return toTestList();
            }
            return toTestListNoProcess();
        } else {
            return "";
        }
    }

    public String saveRat() {
        if (rat == null) {
            JsfUtil.addErrorMessage("No RAT to save");
            return "";
        }
        if (rat.getClient() == null) {
            JsfUtil.addErrorMessage("No Client to save");
            return "";
        }
        Institution createdIns = null;
        if (rat.getClient().getCreateInstitution() == null) {
            if (webUserController.getLoggedInstitution().getPoiInstitution() != null) {
                createdIns = webUserController.getLoggedInstitution().getPoiInstitution();
            } else {
                createdIns = webUserController.getLoggedInstitution();
            }
            rat.getClient().setCreateInstitution(createdIns);
        } else {
            createdIns = rat.getClient().getCreateInstitution();
        }

//        if (createdIns == null || createdIns.getPoiNumber() == null || createdIns.getPoiNumber().trim().equals("")) {
//            JsfUtil.addErrorMessage("The institution you logged has no POI. Can not generate a PHN.");
//            return "";
//        }
        if (rat.getClient().getPhn() == null || rat.getClient().getPhn().trim().equals("")) {
            String newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);

            int count = 0;
            while (clientApplicationController.checkPhnExists(newPhn, null)) {
                newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);
                count++;
                if (count > 100) {
                    JsfUtil.addErrorMessage("Generating New PHN Failed. Client NOT saved. Please contact System Administrator.");
                    return "";
                }
            }
            rat.getClient().setPhn(newPhn);
        }
        if (rat.getClient().getId() == null) {
            if (clientApplicationController.checkPhnExists(rat.getClient().getPhn(), null)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (rat.getClient().getPerson().getNic() != null && !rat.getClient().getPerson().getNic().trim().equals("")) {
                if (clientApplicationController.checkNicExists(rat.getClient().getPerson().getNic(), null)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        } else {
            if (clientApplicationController.checkPhnExists(rat.getClient().getPhn(), rat.getClient())) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (rat.getClient().getPerson().getNic() != null && !rat.getClient().getPerson().getNic().trim().equals("")) {
                if (clientApplicationController.checkNicExists(rat.getClient().getPerson().getNic(), rat.getClient())) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        }
        clientController.saveClient(rat.getClient());
        if (rat.getEncounterNumber() == null
                || rat.getEncounterNumber().trim().equals("")) {
            rat.setEncounterNumber(encounterController.createTestNumber(webUserController.getLoggedInstitution()));
        }

        encounterController.save(rat);

        if (rat.getPcrResult() != null) {
            rat.setSentToLab(true);
            rat.setSentToLabAt(rat.getResultConfirmedAt());
            rat.setSentToLabBy(webUserController.getLoggedUser());

            rat.setReceivedAtLab(true);
            rat.setReceivedAtLabAt(rat.getResultConfirmedAt());
            rat.setReceivedAtLabBy(webUserController.getLoggedUser());

            rat.setResultEntered(true);
            rat.setResultEnteredAt(rat.getResultConfirmedAt());
            rat.setResultEnteredBy(webUserController.getLoggedUser());

            rat.setResultReviewed(true);
            rat.setResultReviewedAt(rat.getResultConfirmedAt());
            rat.setResultReviewedBy(webUserController.getLoggedUser());

            rat.setResultConfirmed(Boolean.TRUE);
            rat.setResultConfirmedBy(webUserController.getLoggedUser());
        } else {
            rat.setSentToLab(true);
            rat.setSentToLabAt(new Date());
            rat.setSentToLabBy(webUserController.getLoggedUser());

            rat.setReceivedAtLab(false);
            rat.setResultEntered(false);
            rat.setResultReviewed(false);
            rat.setResultConfirmed(false);
        }

        sessionController.setLastRatOrderingCategory(rat.getPcrOrderingCategory());

        if (rat.getClient().getPerson().getWorkPlace() != null) {
            sessionController.setLastWorkplace(rat.getClient().getPerson().getWorkPlace());
        }

        if (rat.getClient().getPerson().getWorkplaceContact() != null) {
            sessionController.setLastContactOfWorkplace(rat.getClient().getPerson().getWorkplaceContact());
        }

        if (rat.getClient().getPerson().getWorkplaceContactDetails() != null) {
            sessionController.setLastContactOfWorkplaceDetails(rat.getClient().getPerson().getWorkplaceContactDetails());
        }

        if (rat.getReferalInstitution() != null) {
            sessionController.setLastLab(rat.getReferalInstitution());
        }

        sessionController.setLastRat(rat);

        sessionController.getRats().put(rat.getId(), rat);

        JsfUtil.addSuccessMessage("Saved.");
        return "/moh/rat_view";
    }

    public String savePcr() {
        if (pcr == null) {
            JsfUtil.addErrorMessage("No pcr to save");
            return "";
        }
        if (pcr.getClient() == null) {
            JsfUtil.addErrorMessage("No Client to save");
            return "";
        }
        Institution createdIns = null;
        if (pcr.getClient().getCreateInstitution() == null) {
            if (webUserController.getLoggedInstitution().getPoiInstitution() != null) {
                createdIns = webUserController.getLoggedInstitution().getPoiInstitution();
            } else {
                createdIns = webUserController.getLoggedInstitution();
            }
            pcr.getClient().setCreateInstitution(createdIns);
        } else {
            createdIns = pcr.getClient().getCreateInstitution();
        }

        if (createdIns == null || createdIns.getPoiNumber() == null || createdIns.getPoiNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("The institution you logged has no POI. Can not generate a PHN.");
            return "";
        }

        if (pcr.getClient().getPhn() == null || pcr.getClient().getPhn().trim().equals("")) {
            String newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);

            int count = 0;
            while (clientApplicationController.checkPhnExists(newPhn, null)) {
                newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);
                count++;
                if (count > 100) {
                    JsfUtil.addErrorMessage("Generating New PHN Failed. Client NOT saved. Please contact System Administrator.");
                    return "";
                }
            }
            pcr.getClient().setPhn(newPhn);
        }
        if (pcr.getClient().getId() == null) {
            if (clientApplicationController.checkPhnExists(pcr.getClient().getPhn(), null)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (pcr.getClient().getPerson().getNic() != null && !pcr.getClient().getPerson().getNic().trim().equals("")) {
                if (clientApplicationController.checkNicExists(pcr.getClient().getPerson().getNic(), null)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        } else {
            if (clientApplicationController.checkPhnExists(pcr.getClient().getPhn(), pcr.getClient())) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (pcr.getClient().getPerson().getNic() != null && !pcr.getClient().getPerson().getNic().trim().equals("")) {
                if (clientApplicationController.checkNicExists(pcr.getClient().getPerson().getNic(), pcr.getClient())) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        }
        clientController.saveClient(pcr.getClient());
        if (pcr.getEncounterNumber() == null
                || pcr.getEncounterNumber().trim().equals("")) {
            pcr.setEncounterNumber(encounterController.createTestNumber(webUserController.getLoggedInstitution()));
        }

        encounterController.save(pcr);

        sessionController.setLastPcrOrdringCategory(pcr.getPcrOrderingCategory());
        sessionController.setLastPcr(pcr);

        if (pcr.getClient().getPerson().getWorkPlace() != null) {
            sessionController.setLastWorkplace(pcr.getClient().getPerson().getWorkPlace());
        }

        if (pcr.getClient().getPerson().getWorkplaceContact() != null) {
            sessionController.setLastContactOfWorkplace(pcr.getClient().getPerson().getWorkplaceContact());
        }

        if (pcr.getClient().getPerson().getWorkplaceContactDetails() != null) {
            sessionController.setLastContactOfWorkplaceDetails(pcr.getClient().getPerson().getWorkplaceContactDetails());
        }

        lab = pcr.getReferalInstitution();
        sessionController.getPcrs().put(pcr.getId(), pcr);

        JsfUtil.addSuccessMessage("PCR Saved.");
        return "/moh/pcr_view";
    }

    public void retrieveLastAddressForRat() {
        if (rat == null || rat.getClient() == null || sessionController.getLastRat() == null || sessionController.getLastRat().getClient() == null) {
            return;
        }
        rat.getClient().getPerson().setAddress(sessionController.getLastRat().getClient().getPerson().getAddress());
    }

    public void retrieveLastAddressForPcr() {
        if (pcr == null || pcr.getClient() == null
                || sessionController.getLastPcr() == null
                || sessionController.getLastPcr().getClient() == null) {
            return;
        }
        pcr.getClient().getPerson().setAddress(sessionController.getLastPcr().getClient().getPerson().getAddress());
    }

    public void retrieveLastPhoneForRat() {
        if (rat == null || rat.getClient() == null || sessionController.getLastRat() == null || sessionController.getLastRat().getClient() == null) {
            return;
        }
        rat.getClient().getPerson().setPhone1(sessionController.getLastRat().getClient().getPerson().getPhone1());
    }

    public void retrieveLastPhoneForPcr() {
        if (pcr == null
                || pcr.getClient() == null
                || sessionController.getLastPcr() == null
                || sessionController.getLastPcr().getClient() == null) {
            return;
        }
        pcr.getClient().getPerson().setPhone1(sessionController.getLastPcr().getClient().getPerson().getPhone1());
    }

    public void retrieveLastGnRat() {
        if (rat == null || rat.getClient() == null || sessionController.getLastRat() == null || sessionController.getLastRat().getClient() == null) {
            return;
        }
        rat.getClient().getPerson().setGnArea(sessionController.getLastRat().getClient().getPerson().getGnArea());
    }

    public void retrieveLastGnPcr() {
        if (pcr == null
                || pcr.getClient() == null
                || sessionController.getLastPcr() == null
                || sessionController.getLastPcr().getClient() == null) {
            return;
        }
        pcr.getClient().getPerson().setGnArea(sessionController.getLastPcr().getClient().getPerson().getGnArea());
    }

    public List<Area> completePhiAreasForRat(String qry) {
        List<Area> areas = new ArrayList<>();
        if (rat == null) {
            return areas;
        }
        if (rat.getClient() == null) {
            return areas;
        }
        if (rat.getClient().getPerson().getMohArea() == null) {
            return areaApplicationController.completePhiAreas(qry);
        } else {
            return areaApplicationController.completePhiAreasOfMoh(qry, rat.getClient().getPerson().getMohArea());
        }
    }

    public List<Area> completePhiAreasForPcr(String qry) {
        List<Area> areas = new ArrayList<>();
        if (pcr == null) {
            return areas;
        }
        if (pcr.getClient() == null) {
            return areas;
        }
        if (pcr.getClient().getPerson().getMohArea() == null) {
            return areaApplicationController.completePhiAreas(qry);
        } else {
            return areaApplicationController.completePhiAreasOfMoh(qry, pcr.getClient().getPerson().getMohArea());
        }
    }

    public List<Area> completeGnAreasForRat(String qry) {
        return completeGnAreas(qry, rat);
    }

    public List<Area> completeGnAreasForPcr(String qry) {
        return completeGnAreas(qry, pcr);
    }

    private List<Area> completeGnAreas(String qry, Encounter e) {
        List<Area> areas = new ArrayList<>();
        if (e == null) {
            return areas;
        }
        if (e.getClient() == null) {
            return areas;
        }
        if (e.getClient().getPerson().getDistrict() == null) {
            return areaApplicationController.completeGnAreas(qry);
        } else {
            return areaApplicationController.completeGnAreasOfDistrict(qry, e.getClient().getPerson().getDistrict());
        }
    }

    public String toListOfResultsOrderedFromOtherInstitutes() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.client.person.mohArea=:moh ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

        j += " and c.institution <> :ins ";
        m.put("ins", webUserController.getLoggedInstitution());

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
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        return "/moh/list_of_results_from_other_institutions";
    }

    public String toListOfFirstContacts() {
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

        j += " and ci.areaValue=:moh ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        cecItems = ceciFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_first_contacts";
    }

    public String toListCasesByManagement() {
        Map m = new HashMap();
        String j = "select distinct(c) "
                + " from ClientEncounterComponentItem ci join ci.encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Case_Enrollment);

        j += " and (c.client.person.mohArea=:moh) ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

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

        return "/moh/list_of_cases_by_management_plan";
    }

    public String toListOfResultsForOrdersFromMyMoh() {
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

//        webUserController.getLoggedInstitution();
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
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_results_for_orders_from_my_moh";
    }

    public String toPrintResults() {
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.institution=:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        Map m2 = new HashMap();

        String j2 = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m2.put("ret", false);

        j2 += " and c.encounterType=:etype ";
        m2.put("etype", EncounterType.Test_Enrollment);

        j2 += " and c.institution<>:ins ";
        m2.put("ins", webUserController.getLoggedInstitution());

        j2 += " and (c.client.person.mohArea=:moh) ";
        m2.put("moh", webUserController.getLoggedInstitution().getMohArea());

        j2 += " and c.resultConfirmedAt between :fd and :td ";
        m2.put("fd", getFromDate());
        m2.put("td", getToDate());

        if (testType != null) {
            j2 += " and c.pcrTestType=:tt ";
            m2.put("tt", testType);
        }
        if (orderingCategory != null) {
            j2 += " and c.pcrOrderingCategory=:oc ";
            m2.put("oc", orderingCategory);
        }
        if (result != null) {
            j2 += " and c.pcrResult=:result ";
            m2.put("result", result);
        }
        if (lab != null) {
            j2 += " and c.referalInstitution=:ri ";
            m2.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        List<Encounter> tests2 = encounterFacade.findByJpql(j2, m2, TemporalType.TIMESTAMP);
        tests.addAll(tests2);
        return "moh/print_results";
    }

    public String toViewResults() {
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.institution=:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
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
        return "/moh/view_results";
    }

    public String toListOfResultsForPersonsInMyArea() {
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and (c.client.person.mohArea=:moh) ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

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
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_results_for_persons_in_my_area";
    }

    public String toListOfResultsForOrdersFromOtherInstitutes() {
        Map m = new HashMap();

        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);

        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and c.institution<>:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

        j += " and (c.client.person.mohArea=:moh) ";
        m.put("moh", webUserController.getLoggedInstitution().getMohArea());

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
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }

        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_results_for_orders_from_other_moh";
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

//    public String toTestRequestDistrictCounts() {
//        Map m = new HashMap();
//        String j = "select c "
//                + " from Encounter c "
//                + " where (c.retired is null or c.retired=:ret) ";
//        m.put("ret", false);
//        j += " and c.encounterType=:etype ";
//        m.put("etype", EncounterType.Test_Enrollment);
//        j += " and c.createdAt between :fd and :td ";
//        m.put("fd", getFromDate());
//        m.put("td", getToDate());
//        if (testType != null) {
//            j += " and c.pcrTestType=:tt ";
//            m.put("tt", testType);
//        }
//        if (orderingCategory != null) {
//            j += " and c.pcrOrderingCategory=:oc ";
//            m.put("oc", orderingCategory);
//        }
//        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
//        return "/moh/list_of_tests";
//    }
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
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_tests_without_results";
    }

    public String toTestListNoProcess() {
        return "/moh/list_of_tests";
    }

    public String toTestList() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.institution=:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

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
        m.put("fd", CommonController.startOfTheDate(getFromDate()));
        m.put("td", CommonController.endOfTheDate(getToDate()));
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
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_tests";
    }

    public String toListOfOrderesByMyMoh() {
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.institution=:ins ";
        m.put("ins", webUserController.getLoggedInstitution());

        if (this.filter == null) {
            this.filter = "createdat";
        }

        switch (this.filter.toUpperCase()) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
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
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        tests = encounterFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/moh/list_of_orderes_by_my_moh";
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

        if (this.filter == null) {
            this.filter = "createdat";
        }

        switch (this.filter) {
            case "CREATEDAT":
                j += " and c.createdAt between :fd and :td ";
                break;
            case "SAMPLEDAT":
                j += " and c.sampledAt between :fd and :td ";
                break;
            case "RESULTSAT":
                j += " and c.resultConfirmedAt between :fd and :td ";
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

    public String toListCasesByManagementForRegionalLevel() {
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

    public String toEnterResults() {
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
        m.put("fd", CommonController.startOfTheDate(getFromDate()));
        m.put("td", CommonController.endOfTheDate(getToDate()));
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
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

    public Area getPhiArea() {
        return phiArea;
    }

    public void setPhiArea(Area phiArea) {
        this.phiArea = phiArea;
    }

    public List<Area> getPhiAreas() {
        return phiAreas;
    }

    public void setPhiAreas(List<Area> phiAreas) {
        this.phiAreas = phiAreas;
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

    public List<Encounter> getListedToDispatch() {
        return listedToDispatch;
    }

    public void setListedToDispatch(List<Encounter> listedToDispatch) {
        this.listedToDispatch = listedToDispatch;
    }

    public Institution getDispatchingLab() {
        return dispatchingLab;
    }

    public void setDispatchingLab(Institution dispatchingLab) {
        this.dispatchingLab = dispatchingLab;
    }

    public List<Encounter> getSelectedToDispatch() {
        return selectedToDispatch;
    }

    public void setSelectedToDispatch(List<Encounter> selectedToDispatch) {
        this.selectedToDispatch = selectedToDispatch;
    }

    public String getFilter() {
        return this.filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

}
