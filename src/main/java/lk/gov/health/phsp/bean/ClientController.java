package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.ClientFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.entity.Sms;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.SmsFacade;
import lk.gov.health.phsp.pojcs.ClientBasicData;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.SlNic;
import lk.gov.health.phsp.pojcs.YearMonthDay;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.UploadedFile;
// </editor-fold>

@Named("clientController")
@SessionScoped
public class ClientController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private lk.gov.health.phsp.facade.ClientFacade ejbFacade;
    @EJB
    private EncounterFacade encounterFacade;
    @EJB
    private SmsFacade smsFacade;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    ApplicationController applicationController;
    @Inject
    private AreaApplicationController areaApplicationController;
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
    private ClientEncounterComponentFormSetController clientEncounterComponentFormSetController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    private DesignComponentFormSetController designComponentFormSetController;
    @Inject
    private ClientEncounterComponentItemController clientEncounterComponentItemController;
    @Inject
    private PreferenceController preferenceController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Variables">
    private List<Client> items = null;
    private List<ClientBasicData> clients = null;
    private List<Client> selectedClients = null;
    private List<ClientBasicData> selectedClientsWithBasicData = null;
    private List<Client> importedClients = null;

    private Item lastTestOrderingCategory;
    private Item lastTestPcrOrRat;

    private Encounter lastTest;

    private String serialNoColumn = "A";
    private String resultColumn = "H";
    private String ctValueColumn = "G";
    private String commentColumn = "I";

    private Integer startRow = 1;

    Boolean institutionSelectable;
    Boolean nationalLevel;

    private Item selectedTest;

    private List<ClientBasicData> selectedClientsBasic = null;

    private List<Encounter> institutionCaseEnrollments;
    private Map<Long, Encounter> institutionCaseEnrollmentMap;

    private List<Encounter> institutionTestEnrollments;
    private Map<Long, Encounter> institutionTestEnrollmentMap;

    private List<Encounter> listedToReceive;
    private List<Encounter> listedToEnterResults;
    private List<Encounter> listedToReviewResults;
    private List<Encounter> listedToConfirm;
    private List<Encounter> listedToDispatch;
    private List<Encounter> listedToDivert;
    private List<Encounter> listedToPrint;
    private List<Encounter> testList;
    private List<Encounter> caseList;

    private List<Encounter> listReceived;
    private List<Encounter> listReviewed;
    private List<Encounter> listConfirmed;
    private List<Encounter> listPositives;

    private List<Encounter> selectedToReceive;
    private List<Encounter> selectedToReview;
    private List<Encounter> selectedToConfirm;
    private List<Encounter> selectedToPrint;
    private List<Encounter> selectedToDispatch;
    private List<Encounter> selectedToDivert;

    private Client selected;
    private Long selectedId;
    private Long idFrom;
    private Long idTo;
    private Institution institution;
    private Institution referingInstitution;
    private Institution dispatchingLab;
    private Institution divertingLab;
    private List<Encounter> selectedClientsClinics;
    private List<Encounter> selectedClientEncounters;
    private String searchingId;
    private String searchingPhn;
    private String searchingPassportNo;
    private String searchingDrivingLicenceNo;
    private String searchingNicNo;
    private String searchingName;
    private String searchingPhoneNumber;
    private String searchingLocalReferanceNo;
    private String searchingSsNumber;
    private String uploadDetails;
    private String errorCode;
    private YearMonthDay yearMonthDay;
    private Institution selectedClinic;
    private int profileTabActiveIndex;
    private Integer numberOfPhnToReserve;
    private boolean goingToCaptureWebCamPhoto;
    private UploadedFile file;
    private Date clinicDate;
    private Date fromDate;
    private Date toDate;

    private Encounter selectedEncounterToMarkTest;
    private Encounter selectedEncounter;

    private Boolean nicExists;
    private Boolean phnExists;
    private Boolean emailExists;
    private Boolean phone1Exists;
    private Boolean passportExists;
    private Boolean dlExists;
    private Boolean localReferanceExists;
    private Boolean ssNumberExists;
    private String dateTimeFormat;
    private String dateFormat;
    private List<String> reservePhnList;
    private int intNo;

    private List<InstitutionCount> labSummariesToReceive;
    private List<InstitutionCount> labSummariesReceived;
    private List<InstitutionCount> labSummariesEntered;
    private List<InstitutionCount> labSummariesReviewed;
    private List<InstitutionCount> labSummariesConfirmed;
    private List<InstitutionCount> labSummariesPositive;

    private Institution continuedLab;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public ClientController() {
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Navigation">
    public String toSearchClientById() {
        userTransactionController.recordTransaction("To Search Client By Id");
        return "/client/search_by_id";
    }

    public void toMarkAllNegative() {
        if (listedToEnterResults == null) {
            JsfUtil.addErrorMessage("Nothing to mark");
            return;
        }
        for (Encounter e : listedToEnterResults) {
            e.setPcrResult(itemApplicationController.getPcrNegative());
            e.setPcrResultStr(preferenceController.getPcrNegativeTerm());
            e.setResultEntered(true);
            e.setResultEnteredAt(new Date());
            e.setResultEnteredBy(webUserController.getLoggedUser());
            getEncounterFacade().edit(e);
        }
    }

    @Deprecated
    public String toEnterTestResults() {
        fillTestEnrollmentToMark();
        return "/client/mark_test_enrollments";
    }

    public void markNilReturnForCases() {
        Encounter nilCases = encounterController.getInstitutionTypeEncounter(webUserController.getLoggedUser().getInstitution(),
                EncounterType.No_Covid, new Date());
        JsfUtil.addSuccessMessage("Marked");
    }

    public void markNilReturnForTests() {
        Encounter nilCases = encounterController.getInstitutionTypeEncounter(webUserController.getLoggedUser().getInstitution(),
                EncounterType.No_test, new Date());
        JsfUtil.addSuccessMessage("Marked");
    }

    public void reverseNilReturnForTests() {
        Encounter nilCases = encounterController.getInstitutionTypeEncounter(webUserController.getLoggedUser().getInstitution(),
                EncounterType.No_test, new Date());
        nilCases.setRetired(true);
        nilCases.setRetiredBy(webUserController.getLoggedUser());
        nilCases.setRetiredAt(new Date());
        encounterController.save(nilCases);
        JsfUtil.addSuccessMessage("Reversed");
    }

    public void reverseNilReturnForCases() {
        Encounter nilCases = encounterController.getInstitutionTypeEncounter(webUserController.getLoggedUser().getInstitution(),
                EncounterType.No_Covid, new Date());
        nilCases.setRetired(true);
        nilCases.setRetiredBy(webUserController.getLoggedUser());
        nilCases.setRetiredAt(new Date());
        encounterController.save(nilCases);
        JsfUtil.addSuccessMessage("Reversed");
    }

    public String toSearchClientByDetails() {
        return "/client/search_by_details";

    }

    public String toSelectClient() {
        return "/client/select";
    }

    public String toSelectClientBasic() {
        return "/client/select_basic";
    }

    public String toClient() {
        return "/client/client";
    }

    public void markTestAsNotReceived() {
        if (selectedEncounterToMarkTest == null) {
            JsfUtil.addErrorMessage("Nothing to Mark");
            return;
        }
        selectedEncounterToMarkTest.setResultPositive(null);
        selectedEncounterToMarkTest.setResultDate(new Date());
        selectedEncounterToMarkTest.setResultDateTime(new Date());
        encounterFacade.edit(selectedEncounterToMarkTest);
        JsfUtil.addSuccessMessage("Marked as Not Received");
    }

    public void markTestAsPositive() {
        if (selectedEncounterToMarkTest == null) {
            JsfUtil.addErrorMessage("Nothing to Mark");
            return;
        }
        selectedEncounterToMarkTest.setResultPositive(true);
        selectedEncounterToMarkTest.setResultDate(new Date());
        selectedEncounterToMarkTest.setResultDateTime(new Date());
        encounterFacade.edit(selectedEncounterToMarkTest);
//        sendPositiveSms(selectedEncounterToMarkTest);
        JsfUtil.addSuccessMessage("Marked as Positive");
    }

    public void sendPositiveSms(Encounter e) {
        String number = "";
        if (e == null) {
            System.err.println("No encounter");
            return;
        }
        if (e.getClient() == null) {
            System.err.println("No Client");
            return;
        }
        if (e.getClient().getPerson().getPhone1() != null && !e.getClient().getPerson().getPhone1().trim().equals("")) {
            number = e.getClient().getPerson().getPhone1().trim();
        } else if (e.getClient().getPerson().getPhone2() != null && !e.getClient().getPerson().getPhone2().trim().equals("")) {
            number = e.getClient().getPerson().getPhone2().trim();
        } else {
            // // System.out.println("No Phone number");
            return;
        }
        String smsType = "COVID-19 Positive SMS";
        Item item = itemController.findItemByCode("test_type");
        ClientEncounterComponentItem ceci = e.getClientEncounterComponentItem(item);
        String smsTemplate;
        if (ceci == null || ceci.getItemValue() == null) {
            smsTemplate = preferenceController.getPositiveSmsTemplate();
        } else if (ceci.getItemValue().equals(itemController.findItemByCode("covid19_pcr_test"))) {
            smsType = "Positive PCR SMS";
            smsTemplate = preferenceController.getPositivePcrSmsTemplate();
        } else if (ceci.getItemValue().equals(itemController.findItemByCode("covid19_rat"))) {
            smsTemplate = preferenceController.getPositiveRatSmsTemplate();
            smsType = "Positive RAT SMS";
        } else {
            smsTemplate = preferenceController.getPositiveSmsTemplate();
        }
        smsTemplate = "The PCR Test of #{name} done on #{reported_date} was Positive for COVID-19. Relevant Officers will contact you. #{institution}";
        Sms s = new Sms();
        s.setEncounter(e);
        s.setCreatedAt(new Date());
        s.setCreater(webUserController.getLoggedUser());
        s.setInstitution(webUserController.getLoggedUser().getInstitution());
        s.setReceipientNumber(number);
        smsTemplate = smsTemplate.replace("#{name}", s.getEncounter().getClient().getPerson().getName());
        smsTemplate = smsTemplate.replace("#{institution}", s.getEncounter().getInstitution().getName());
        smsTemplate = smsTemplate.replace("#{sampled_date}", CommonController.dateTimeToString(s.getEncounter().getEncounterDate()));
        smsTemplate = smsTemplate.replace("#{reported_date}", CommonController.dateTimeToString(s.getEncounter().getResultDate()));
        String messageBody = smsTemplate;
        s.setSendingMessage(messageBody);
        s.setSentSuccessfully(false);
        s.setAwaitingSending(true);
        s.setSendingFailed(false);
        s.setSmsType(smsType);
        getSmsFacade().create(s);
    }

    public void sendNegativeSms(Encounter e) {
        String number = "";
        if (e == null) {
            System.err.println("No encounter");
            return;
        }
        if (e.getClient() == null) {
            System.err.println("No Client");
            return;
        }
        if (e.getClient().getPerson().getPhone1() != null && !e.getClient().getPerson().getPhone1().trim().equals("")) {
            number = e.getClient().getPerson().getPhone1().trim();
        } else if (e.getClient().getPerson().getPhone2() != null && !e.getClient().getPerson().getPhone2().trim().equals("")) {
            number = e.getClient().getPerson().getPhone2().trim();
        } else {
            // // System.out.println("No Phone number");
            return;
        }
        String smsType = "COVID-19 Negative SMS";
        Item item = itemController.findItemByCode("test_type");
        ClientEncounterComponentItem ceci = e.getClientEncounterComponentItem(item);
        String smsTemplate;
        if (ceci == null || ceci.getItemValue() == null) {
            smsTemplate = preferenceController.getNegativeSmsTemplate();
        } else if (ceci.getItemValue().equals(itemController.findItemByCode("covid19_pcr_test"))) {
            smsType = "Negative PCR SMS";
            smsTemplate = preferenceController.getNegativePcrSmsTemplate();
        } else if (ceci.getItemValue().equals(itemController.findItemByCode("covid19_rat"))) {
            smsTemplate = preferenceController.getNegativeRatSmsTemplate();
            smsType = "Negative RAT SMS";
        } else {
            smsTemplate = preferenceController.getNegativeSmsTemplate();
        }

        Sms s = new Sms();
        s.setEncounter(e);
        s.setCreatedAt(new Date());
        s.setCreater(webUserController.getLoggedUser());
        s.setInstitution(webUserController.getLoggedUser().getInstitution());
        s.setReceipientNumber(number);

        // // System.out.println("smsTemplate = " + smsTemplate);
        // // System.out.println("s = " + s);
        // // System.out.println("s.getEncounter() = " + s.getEncounter());
        // // System.out.println("s.getEncounter().getClient() = " + s.getEncounter().getClient());
        smsTemplate = smsTemplate.replace("#{name}", s.getEncounter().getClient().getPerson().getName());
        smsTemplate = smsTemplate.replace("#{institution}", s.getEncounter().getInstitution().getName());
        smsTemplate = smsTemplate.replace("#{sampled_date}", CommonController.dateTimeToString(s.getEncounter().getEncounterDate()));
        smsTemplate = smsTemplate.replace("#{reported_date}", CommonController.dateTimeToString(s.getEncounter().getResultDate()));
        String messageBody = smsTemplate;
        s.setSendingMessage(messageBody);
        s.setSentSuccessfully(false);
        s.setAwaitingSending(true);
        s.setSendingFailed(false);
        s.setSmsType(smsType);
        getSmsFacade().create(s);
    }

    public void markTestAsNegative() {
        if (selectedEncounterToMarkTest == null) {
            JsfUtil.addErrorMessage("Nothing to Mark");
            return;
        }
        selectedEncounterToMarkTest.setResultPositive(false);
        selectedEncounterToMarkTest.setResultDate(new Date());
        selectedEncounterToMarkTest.setResultDateTime(new Date());
        encounterFacade.edit(selectedEncounterToMarkTest);
//        sendNegativeSms(selectedEncounterToMarkTest);
        JsfUtil.addSuccessMessage("Marked as Negative");
    }

    public String toClientProfile() {
        selectedClientEncounters = null;
        userTransactionController.recordTransaction("To Client Profile");
        selectedClientEncounters = encounterController.getItems(selected);
        updateYearDateMonth();
        return "/client/profile";
    }

    public String toClientProfileForCaseEncounter() {
        userTransactionController.recordTransaction("To Client Profile");
        DesignComponentFormSet dfs = designComponentFormSetController.getFirstCaseEnrollmentFormSet();
        if (dfs == null) {
            JsfUtil.addErrorMessage("No Default Form Set");
            return "";
        }
        ClientEncounterComponentFormSet cefs;
        cefs = clientEncounterComponentFormSetController.findLastFormsetToDataEntry(dfs, selected);
        if (cefs == null) {
            cefs = clientEncounterComponentFormSetController.createNewCaseEnrollmentFormsetToDataEntry(dfs);
        }
        if (cefs == null) {
            JsfUtil.addErrorMessage("No Patient Form Set");
            return "";
        }
        clientEncounterComponentFormSetController.loadOldFormset(cefs);
        updateYearDateMonth();
        return "/client/profile_case_enrollment";
    }

    public String toClientProfileForTestEncounter() {
        selectedClientEncounters = null;
        userTransactionController.recordTransaction("To Client Profile");

        DesignComponentFormSet dfs = designComponentFormSetController.getFirstTestEnrollmentFormSet();
        if (dfs == null) {
            JsfUtil.addErrorMessage("No Default Form Set");
            return "";
        }
        ClientEncounterComponentFormSet cefs;
        cefs = clientEncounterComponentFormSetController.findLastFormsetToDataEntry(dfs, selected);
        if (cefs == null) {
            cefs = clientEncounterComponentFormSetController.createNewTestEnrollmentFormsetToDataEntry(dfs);
        }
        if (cefs == null) {
            JsfUtil.addErrorMessage("No Patient Form Set");
            return "";
        }
        clientEncounterComponentFormSetController.loadOldFormset(cefs);
        updateYearDateMonth();
        return "/client/profile_test_enrollment";
    }

    public String toClientProfileById() {
        selected = getFacade().find(selectedId);
        if (selected == null) {
            JsfUtil.addErrorMessage("No such client");
            return "";
        }
        selectedClientsClinics = null;
        selectedClientEncounters = null;
        userTransactionController.recordTransaction("To Client Profile");
        return "/client/profile";
    }

    public String toReserverPhn() {
        numberOfPhnToReserve = 0;
        return "/client/reserve_phn";
    }

    public String toLabReceiveAll() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.referalInstitution=:rins "
                + " and c.sentToLab is not null "
                + " and c.receivedAtLab is null "
                + " group by c.institution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("rins", referingInstitution);
        // // System.out.println("j = " + j);
        // // System.out.println("m = " + m);
        // // System.out.println("getFromDate() = " + getFromDate());
        // // System.out.println("getToDate() = " + getToDate());
        // // System.out.println("referingInstitution = " + referingInstitution);
        labSummariesToReceive = new ArrayList<>();
        List<Object> obs = getFacade().findObjectByJpql(j, m, TemporalType.DATE);
        // // System.out.println("obs = " + obs.size());
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesToReceive.add((InstitutionCount) o);
            }
        }
        return "/lab/receive_all";
    }

    public String toLabSummarySamplesReceived() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.receivedAtLabAt between :fd and :td "
                + " and c.referalInstitution=:rins "
                + " group by c.institution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", CommonController.startOfTheDate(getFromDate()));
        m.put("td", CommonController.endOfTheDate(getToDate()));
        m.put("rins", referingInstitution);
        labSummariesReceived = new ArrayList<>();
        List<Object> obs = getFacade().findObjectByJpql(j, m, TemporalType.DATE);
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesReceived.add((InstitutionCount) o);
            }
        }
        return "/lab/summary_samples_received";
    }

    public String toLabSummaryResultsEntered() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.resultEnteredAt between :fd and :td "
                + " and c.referalInstitution=:rins "
                + " group by c.institution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", CommonController.startOfTheDate(getFromDate()));
        m.put("td", CommonController.endOfTheDate(getToDate()));
        m.put("rins", referingInstitution);
        labSummariesEntered = new ArrayList<>();
        List<Object> obs = getFacade().findObjectByJpql(j, m, TemporalType.DATE);
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesEntered.add((InstitutionCount) o);
            }
        }
        return "/lab/summary_samples_entered";
    }

    public String toLabSummarySamplesReviewed() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.resultReviewedAt between :fd and :td "
                + " and c.referalInstitution=:rins "
                + " group by c.institution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", CommonController.startOfTheDate(getFromDate()));
        m.put("td", CommonController.endOfTheDate(getToDate()));
        m.put("rins", referingInstitution);
        labSummariesReviewed = new ArrayList<>();
        List<Object> obs = getFacade().findObjectByJpql(j, m, TemporalType.DATE);
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesReviewed.add((InstitutionCount) o);
            }
        }
        return "/lab/summary_samples_reviewed";
    }

    public String toLabSummarySamplesConfirmed() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.resultConfirmedAt between :fd and :td "
                + " and c.referalInstitution=:rins "
                + " group by c.institution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", CommonController.startOfTheDate(getFromDate()));
        m.put("td", CommonController.endOfTheDate(getToDate()));
        m.put("rins", referingInstitution);
        labSummariesConfirmed = new ArrayList<>();
        List<Object> obs = getFacade().findObjectByJpql(j, m, TemporalType.DATE);
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesConfirmed.add((InstitutionCount) o);
            }
        }
        return "/lab/summary_samples_confirmed";
    }

    public String toSummaryByOrderedInstitutionVsLabToReceive() {
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, c.referalInstitution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.receivedAtLab is null "
                + " group by c.institution, c.referalInstitution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        labSummariesToReceive = new ArrayList<>();
        List<Object> obs = getFacade().findObjectByJpql(j, m, TemporalType.DATE);
        // // System.out.println("obs = " + obs.size());
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesToReceive.add((InstitutionCount) o);
            }
        }
        return "/moh/summary_lab_vs_ordered_to_receive";
    }

    public String toSummaryByOrderedInstitution() {
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " group by c.institution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        labSummariesToReceive = new ArrayList<>();
        List<Object> obs = getFacade().findObjectByJpql(j, m, TemporalType.DATE);
        // // System.out.println("obs = " + obs.size());
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesToReceive.add((InstitutionCount) o);
            }
        }
        return "/moh/summary_lab_ordered";
    }

    public String toSummaryByOrderedInstitutionVsLabReceived() {
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, c.referalInstitution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.receivedAtLab is not null "
                + " group by c.institution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        labSummariesToReceive = new ArrayList<>();
        List<Object> obs = getFacade().findObjectByJpql(j, m, TemporalType.DATE);
        // // System.out.println("obs = " + obs.size());
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesToReceive.add((InstitutionCount) o);
            }
        }
        return "/moh/summary_lab_vs_ordered_received";
    }

    public String toSummaryByOrderedInstitutionVsLabConfirmed() {
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, c.referalInstitution, count(c)) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.resultConfirmed is not null "
                + " group by c.institution";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        labSummariesToReceive = new ArrayList<>();
        List<Object> obs = getFacade().findObjectByJpql(j, m, TemporalType.DATE);
        // // System.out.println("obs = " + obs.size());
        for (Object o : obs) {
            if (o instanceof InstitutionCount) {
                labSummariesToReceive.add((InstitutionCount) o);
            }
        }
        return "/moh/summary_lab_vs_ordered_results_available";
    }

    private Long insLabDateCount(Institution ins, Institution lab, Date date) {
        Long c = 0l;
        String j = "select count(c) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.receivedAtLabAt between :fd and :td "
                + " and c.referalInstitution=:rins "
                + " and c.institution=:ins ";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        Date fd = CommonController.startOfTheDate(date);
        Date td = CommonController.endOfTheDate(date);
        m.put("fd", fd);
        m.put("td", td);
        m.put("ins", ins);
        m.put("rins", lab);
        c = getEncounterFacade().findLongByJpql(j, m, TemporalType.TIMESTAMP);
        c++;
        return c;
    }

    private Long labDateCount(Institution lab, Date date) {
        Long c = 0l;
        String j = "select count(c) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.receivedAtLabAt=:edate "
                + " and c.referalInstitution=:rins ";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("edate", date);
        m.put("rins", lab);
        c = getEncounterFacade().findLongByJpql(j, m, TemporalType.DATE);
        c++;
        return c;
    }

    private Long labCount(Institution lab) {
        Long c = 0l;
        String j = "select count(c) "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.referalInstitution=:rins "
                + " and c.receivedAtLab is not null";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("rins", lab);
        c = getEncounterFacade().findLongByJpql(j, m, TemporalType.DATE);
        c++;
        return c;
    }

    public String labReceiveAll() {
        String labPrefix;
        Long startCount;
        String dateString = CommonController.formatDate("ddMMyy");
        switch (getPreferenceController().getLabNumberGeneration()) {
            case "InsLabDateCount":
                startCount = insLabDateCount(institution, webUserController.getLoggedUser().getInstitution(), new Date());
                labPrefix = institution.getCode()
                        + "/"
                        + webUserController.getLoggedUser().getInstitution().getCode()
                        + "/"
                        + dateString
                        + "/";
                break;
            case "InsDateCount":
                startCount = insLabDateCount(institution, webUserController.getLoggedUser().getInstitution(), new Date());
                labPrefix = institution.getCode()
                        + "/"
                        + dateString
                        + "/";
                break;
            case "LabDateCount":
                startCount = insLabDateCount(institution, webUserController.getLoggedUser().getInstitution(), new Date());
                labPrefix = institution.getCode()
                        + "/"
                        + dateString
                        + "/";
                break;
            case "DateCount":
                startCount = labDateCount(webUserController.getLoggedUser().getInstitution(), new Date());
                labPrefix
                        = dateString
                        + "/";
                break;
            case "Count":
                startCount = labCount(webUserController.getLoggedUser().getInstitution());
                Long add = 0l;
                try {
                    add = Long.parseLong(preferenceController.getStartingSerialCount());
                } catch (Exception e) {
                    add = 0l;
                }
                startCount += add;
                labPrefix
                        = webUserController.getLoggedUser().getInstitution().getCode();
                break;
            case "YearCount":
            case "MonthCount":
            default:
                startCount = 1l;
                labPrefix = "NOTSET/";
        }

        String j = "select c "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.referalInstitution=:rins "
                + " and c.institution=:ins "
                + " and c.sentToLab is not null "
                + " and c.receivedAtLab=true";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("ins", institution);
        m.put("rins", webUserController.getLoggedUser().getInstitution());
        labSummariesToReceive = new ArrayList<>();
        List<Encounter> receivingSamplesTmp = encounterFacade.findByJpql(j, m);
        for (Encounter e : receivingSamplesTmp) {
            e.setReceivedAtLab(true);
            e.setReceivedAtLabAt(new Date());
            e.setReceivedAtLabBy(webUserController.getLoggedUser());
            e.setLabNumber(labPrefix + startCount);
            startCount++;
            encounterFacade.edit(e);
        }
        return toLabReceiveAll();
    }

    public String toLabReceiveSelected() {
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=false "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.referalInstitution=:rins "
                + " and c.institution=:ins "
                + " and c.sentToLab is not null "
                + " and c.receivedAtLab is null";
        Map m = new HashMap();
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("ins", institution);
        m.put("rins", webUserController.getLoggedUser().getInstitution());
        listedToReceive = encounterFacade.findByJpql(j, m);
        return "/lab/receive_orders";
    }

    public String toLabReports() {
        return "/lab/reports_index";
    }

    public String toLabOrderByReferringInstitution() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired<>:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        if (institution != null) {
            j += " and c.institution=:ins ";
            m.put("ins", institution);
        }
        j += " and c.referalInstitution=:rins"
                + " order by c.id";
        m.put("ret", true);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("rins", referingInstitution);
        testList = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/lab/order_list";
    }

    public String toLabListReceivedByInstitution() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        Map m = new HashMap();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired<>:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.referalInstitution=:rins"
                + " order by c.id";
        m.put("ret", true);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("rins", referingInstitution);
        listReceived = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/lab/list_received";
    }

    public String markAllAsReceived() {
        String j = "select c "
                + " from Encounter c "
                + " where c.retired<>:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins"
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        List<Encounter> receiving = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        for (Encounter e : receiving) {
            e.setReceivedAtLab(true);
            e.setReceivedAtLabAt(new Date());
            e.setReceivedAtLabBy(webUserController.getLoggedUser());
            encounterFacade.edit(e);
        }
        return "/lab/order_list";
    }

    public String toLabEnterResults() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired<>:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins"
                + " and c.receivedAtLab=:rec "
                + " and c.resultEntered is null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("rec", true);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        listedToEnterResults = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/lab/enter_results";
    }

    public String toMohEnterResults() {
        institution = webUserController.getLoggedUser().getInstitution();
        if (selectedTest == null) {
            String j = "select c "
                    + " from Encounter c "
                    + " where c.retired<>:ret "
                    + " and c.encounterType=:type "
                    + " and c.encounterDate between :fd and :td "
                    + " and c.institution=:ins "
                    + " order by c.id";
            Map m = new HashMap();
            m.put("ret", true);
            m.put("type", EncounterType.Test_Enrollment);
            m.put("fd", fromDate);
            m.put("td", toDate);
            m.put("ins", institution);
            listedToEnterResults = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        } else {
            String j = "select c "
                    + " from ClientEncounterComponentItem i join i.itemEncounter c "
                    + " where c.retired<>:ret "
                    + " and c.encounterType=:type "
                    + " and c.encounterDate between :fd and :td "
                    + " and c.institution=:ins "
                    + " and i.itemValue=:itemValue "
                    + " order by c.id";
            Map m = new HashMap();
            m.put("ret", true);
            m.put("type", EncounterType.Test_Enrollment);
            m.put("fd", fromDate);
            m.put("td", toDate);
            m.put("ins", institution);
            m.put("itemValue", selectedTest);
            listedToEnterResults = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        }
        return "/moh/enter_results";
    }

    public String toLabReviewResults() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired<>:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins"
                + " and c.resultEntered=:rec "
                + " and c.resultReviewed is null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("rec", true);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        listedToReviewResults = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/lab/review_results";
    }

    public String toDispatchSamples() {
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.sentToLab is null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        listedToDispatch = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/moh/dispatch_samples";
    }

    public String toDispatchSamplesWithReferringLab() {
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins "
                + " and c.sentToLab is null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        listedToDispatch = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/moh/dispatch_samples";
    }

    public String toDivertSamples() {
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
        listedToDivert = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/moh/divert_samples";
    }

    public String toConfirmResults() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins"
                + " and c.resultReviewed=:rec "
                + " and c.resultConfirmed is null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("rec", true);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        listedToConfirm = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/lab/confirm_results";
    }

    public String toLabToSelectForPrinting() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins"
                + " and c.resultConfirmed is not null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        listedToPrint = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/lab/print_results";
    }

    public String toMohToSelectForPrinting() {
        institution = webUserController.getLoggedUser().getInstitution();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired=:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.resultConfirmed is not null "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", institution);
        listedToPrint = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/moh/print_results";
    }

    public void saveEncounterResults(Encounter se) {
        if (se == null) {
            return;
        }
        String labApprovalSteps = preferenceController.findPreferanceValue("labApprovalSteps", webUserController.getLoggedUser().getInstitution());

        switch (labApprovalSteps) {
            case "Entry":
                se.setResultConfirmed(true);
                se.setResultConfirmedAt(new Date());
                se.setResultConfirmedBy(webUserController.getLoggedUser());
            case "EntryConfirm":
                se.setResultReviewed(true);
                se.setResultReviewedAt(new Date());
                se.setResultReviewedBy(webUserController.getLoggedUser());
            case "EntryReviewConfirm":
                se.setResultEntered(true);
                se.setResultEnteredAt(new Date());
                se.setResultEnteredBy(webUserController.getLoggedUser());
                break;
            default:
                se.setResultConfirmed(true);
                se.setResultConfirmedAt(new Date());
                se.setResultConfirmedBy(webUserController.getLoggedUser());
        }

        if (se.getPcrResult() != null) {
            if (se.getPcrResult().equals(itemApplicationController.getPcrPositive())) {
                se.setPcrResultStr(preferenceController.getPcrPositiveTerm());
            } else if (se.getPcrResult().equals(itemApplicationController.getPcrInconclusive())) {
                se.setPcrResultStr(preferenceController.getPcrInconclusiveTerm());
            } else if (se.getPcrResult().equals(itemApplicationController.getPcrInvalid())) {
                se.setPcrResultStr(preferenceController.getPcrInvalidTerm());
            } else if (se.getPcrResult().equals(itemApplicationController.getPcrNegative())) {
                se.setPcrResultStr(preferenceController.getPcrNegativeTerm());
            } else {

            }
        } else {
            se.setPcrResultStr("");
        }
        encounterFacade.edit(se);
    }

    public void saveLabNo(Encounter se) {
        if (se == null) {
            return;
        }
        encounterFacade.edit(se);
    }

    public void saveEncounterResultsAtMoh(Encounter se) {
        if (se == null) {
            return;
        }
        se.setResultConfirmed(true);
        se.setResultConfirmedAt(new Date());
        se.setResultConfirmedBy(webUserController.getLoggedUser());
        se.setResultReviewed(true);
        se.setResultReviewedAt(new Date());
        se.setResultReviewedBy(webUserController.getLoggedUser());
        se.setResultEntered(true);
        se.setResultEnteredAt(new Date());
        se.setResultEnteredBy(webUserController.getLoggedUser());
        se.setResultNoted(true);
        se.setResultNotedAt(new Date());
        se.setResultNotedBy(webUserController.getLoggedUser());
        encounterFacade.edit(se);
    }

    public String confirmSelectedResults() {
        for (Encounter e : selectedToConfirm) {
            e.setResultConfirmed(true);
            e.setResultConfirmedAt(new Date());
            e.setResultConfirmedBy(webUserController.getLoggedUser());
            //TODO : Remove try catch
            try {
                String labReport = generateLabReport(e);
                e.setResultPrintHtml(labReport);
            } catch (Exception ex) {
                // System.out.println("ex = " + ex);
            }
            encounterFacade.edit(e);
        }
        selectedToConfirm = null;
        return toConfirmResults();
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
        return toDivertSamples();
    }

    public String markSelectedAsReceivedResults() {
        String labPrefix;
        Long startCount;
        String dateString = CommonController.formatDate("ddMMyy");

        String labNoGen = getPreferenceController().getLabNumberGeneration();
        // System.out.println("labNoGen = " + labNoGen);
        switch (labNoGen) {
            case "InsLabDateCount":
                startCount = insLabDateCount(institution, webUserController.getLoggedUser().getInstitution(), new Date());
                labPrefix = institution.getCode()
                        + "/"
                        + webUserController.getLoggedUser().getInstitution().getCode()
                        + "/"
                        + dateString
                        + "/";
                break;
            case "InsDateCount":
                startCount = insLabDateCount(institution, webUserController.getLoggedUser().getInstitution(), new Date());
                labPrefix = institution.getCode()
                        + "/"
                        + dateString
                        + "/";
                break;
            case "LabDateCount":
                startCount = insLabDateCount(institution, webUserController.getLoggedUser().getInstitution(), new Date());
                labPrefix = institution.getCode()
                        + "/"
                        + dateString
                        + "/";
                break;
            case "DateCount":
                startCount = labDateCount(webUserController.getLoggedUser().getInstitution(), new Date());
                labPrefix
                        = dateString
                        + "/";
                break;
            case "Count":
                startCount = labCount(webUserController.getLoggedUser().getInstitution());
                // System.out.println("startCount = " + startCount);
                Long add = 0l;
                try {
                    add = Long.parseLong(preferenceController.getStartingSerialCount());
                } catch (Exception e) {
                    add = 0l;
                }
                // System.out.println("add = " + add);
                startCount += add;
                // System.out.println("startCount = " + startCount);
                labPrefix
                        = webUserController.getLoggedUser().getInstitution().getCode();
                break;
            case "YearCount":
            case "MonthCount":

            default:
                startCount = 1l;
                labPrefix = "NOTSET/";
        }

        for (Encounter e : selectedToReceive) {
            e.setReceivedAtLab(true);
            e.setReceivedAtLabAt(new Date());
            e.setReceivedAtLabBy(webUserController.getLoggedUser());
            e.setLabNumber(labPrefix + startCount);
            startCount++;
            encounterFacade.edit(e);
        }
        selectedToReceive = null;
        return toLabReceiveAll();
    }

    public String toLabPrintSelected() {
        for (Encounter e : selectedToPrint) {
            e.setResultPrinted(true);
            e.setResultPrintedAt(new Date());
            e.setResultPrintedBy(webUserController.getLoggedUser());
            encounterFacade.edit(e);
        }
        return "/lab/printing_results";
    }

    public String toMohPrintSelected() {
        for (Encounter e : selectedToPrint) {
            e.setResultPrinted(true);
            e.setResultPrintedAt(new Date());
            e.setResultPrintedBy(webUserController.getLoggedUser());
            encounterFacade.edit(e);
        }
        return "/moh/printing_results";
    }

    public String generateLabReport(Encounter e) {
        // // System.out.println("generateLabReport");
        if (e == null) {
            return "No Encounter";
        }
        String html = getPreferenceController().findPreferanceValue("labReportHeader", webUserController.getLoggedUser().getInstitution());
        if (html == null || html.trim().equals("")) {
            return "No Report Format";
        }
        //Patient Properties
        html = html.replace("{name}", e.getClient().getPerson().getName());
        e.getClient().getPerson().calAgeFromDob();
        html = html.replace("{age}", e.getClient().getPerson().getAge());
        html = html.replace("{sex}", e.getClient().getPerson().getSex().getName());
        html = html.replace("{address}", e.getClient().getPerson().getAddress());
        html = html.replace("{phone1}", e.getClient().getPerson().getAddress());
        html = html.replace("{phone2}", e.getClient().getPerson().getAddress());
        if (e.getLabNumber() != null) {
            html = html.replace("{lab_no}", e.getLabNumber());
        }
        if (e.getEncounterNumber() != null) {
            html = html.replace("{ref_no}", e.getEncounterNumber());
        }
        if (e.getClient().getPerson().getGnArea() != null) {
            html = html.replace("{gn}", e.getClient().getPerson().getGnArea().getName());
        }
        if (e.getClient().getPerson().getPhiArea() != null) {
            html = html.replace("{phi}", e.getClient().getPerson().getPhiArea().getName());
        }

        //Institute Properties
        html = html.replace("{institute}", e.getInstitution().getName());

        html = html.replace("{ref_institute_name}", e.getInstitution().getName());
        html = html.replace("{ref_institute_address}", e.getInstitution().getAddress());
        html = html.replace("{ref_institute_phone}", e.getInstitution().getPhone());
        html = html.replace("{ref_institute_fax}", e.getInstitution().getFax());
        html = html.replace("{ref_institute_email}", e.getInstitution().getEmail());

        html = html.replace("{ref_institute_email}", e.getInstitution().getEmail());

        if (e.getReceivedAtLabAt() != null) {
            html = html.replace("{received_date}", CommonController.dateTimeToString(e.getReceivedAtLabAt()));
        }

        if (e.getResultEnteredAt() != null) {
            html = html.replace("{entered_date}", CommonController.dateTimeToString(e.getResultEnteredAt()));
        }

        if (e.getResultConfirmedAt() != null) {
            html = html.replace("{confirmed_date}", CommonController.dateTimeToString(e.getResultConfirmedAt()));
        }

        if (e.getResultEnteredBy() != null) {
            html = html.replace("{entered_by}", e.getResultEnteredBy().getPerson().getName());
        }

        if (e.getResultConfirmedBy() != null) {
            html = html.replace("{approved_by}", e.getResultConfirmedBy().getPerson().getName());
        }

//        Item test = itemController.findItemByCode("test_type");
//        ClientEncounterComponentItem testValueCi = e.getClientEncounterComponentItem(test);
//        if (testValueCi != null) {
//            Item testVal = testValueCi.getItemValue();
//            if (testVal != null) {
//                html = html.replace("{test}", testVal.getName());
//            }
//        }
        if (e.getPcrResult() != null) {
            html = html.replace("{pcr_result}", e.getPcrResult().getName());
        }
        if (e.getPcrResultStr() != null) {
            html = html.replace("{pcr_result}", e.getPcrResultStr());
        }
        if (e.getCtValue() != null) {
            html = html.replace("{pcr_ct}", e.getCtValue().toString());
        }
        if (e.getResultComments() != null) {
            html = html.replace("{pcr_comments}", e.getResultComments());
        }
        return html;
    }

    public void reviewOkForSelectedResults() {
        for (Encounter e : selectedToReview) {
            e.setResultReviewed(true);
            e.setResultReviewedAt(new Date());
            e.setResultReviewedBy(webUserController.getLoggedUser());
            encounterFacade.edit(e);
        }
        selectedToReview = null;
        toLabReviewResults();
    }

    public String toLabOrderByReferringInstitutionToPrintResults() {
        referingInstitution = webUserController.getLoggedUser().getInstitution();
        String j = "select c "
                + " from Encounter c "
                + " where c.retired<>:ret "
                + " and c.encounterType=:type "
                + " and c.encounterDate between :fd and :td "
                + " and c.institution=:ins "
                + " and c.referalInstitution=:rins "
                + " and c.resultConfirmed=:con "
                + " order by c.id";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("type", EncounterType.Test_Enrollment);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("con", true);
        m.put("ins", institution);
        m.put("rins", referingInstitution);
        testList = getEncounterFacade().findByJpql(j, m, TemporalType.DATE);
        return "/lab/result_list";
    }

    public String toAddNewClientForCaseEnrollment() {
        setSelected(new Client());
        selected.setRetired(true);
        selected.getPerson().setDistrict(webUserController.getLoggedUser().getInstitution().getDistrict());
        selected.getPerson().setMohArea(webUserController.getLoggedUser().getInstitution().getMohArea());
        saveClient(selected);
        clearRegisterNewExistsValues();
        selectedClientsClinics = null;
        selectedClientEncounters = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        userTransactionController.recordTransaction("to add a new client for case");
        DesignComponentFormSet dfs = designComponentFormSetController.getFirstCaseEnrollmentFormSet();
        if (dfs == null) {
            JsfUtil.addErrorMessage("No Default Form Set");
            return "";
        }
        ClientEncounterComponentFormSet cefs = clientEncounterComponentFormSetController.createNewCaseEnrollmentFormsetToDataEntry(dfs);
        if (cefs == null) {
            JsfUtil.addErrorMessage("No Patient Form Set");
            return "";
        }
        clientEncounterComponentFormSetController.loadOldFormset(cefs);
        clientEncounterComponentFormSetController.getSelected().getEncounter().setReferalInstitution(continuedLab);
        return "/client/client_case_enrollment";
    }

    public String toSelectedForCaseEnrollment() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return "";
        }
        selected.getPerson().calAgeFromDob();
        clearRegisterNewExistsValues();
        selectedClientsClinics = null;
        selectedClientEncounters = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        userTransactionController.recordTransaction("to add a new client for case");
        DesignComponentFormSet dfs = designComponentFormSetController.getFirstCaseEnrollmentFormSet();
        if (dfs == null) {
            JsfUtil.addErrorMessage("No Default Form Set");
            return "";
        }
        ClientEncounterComponentFormSet cefs = clientEncounterComponentFormSetController.createNewCaseEnrollmentFormsetToDataEntry(dfs);
        if (cefs == null) {
            JsfUtil.addErrorMessage("No Patient Form Set");
            return "";
        }
        clientEncounterComponentFormSetController.loadOldFormset(cefs);

        return "/client/client_case_enrollment";
    }

    public String toNewCaseEnrollmentFromEncounter() {
        if (selectedEncounter == null) {
            JsfUtil.addErrorMessage("No encounter");
            return "";
        }
        if (selectedEncounter.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        setSelected(selectedEncounter.getClient());
        saveClient(selected);
        clearRegisterNewExistsValues();
        selectedClientsClinics = null;
        selectedClientEncounters = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        userTransactionController.recordTransaction("to add a new client for case");
        DesignComponentFormSet dfs = designComponentFormSetController.getFirstCaseEnrollmentFormSet();
        if (dfs == null) {
            JsfUtil.addErrorMessage("No Default Form Set");
            return "";
        }
        ClientEncounterComponentFormSet cefs = clientEncounterComponentFormSetController.createNewCaseEnrollmentFormsetToDataEntry(dfs);
        if (cefs == null) {
            JsfUtil.addErrorMessage("No Patient Form Set");
            return "";
        }
        clientEncounterComponentFormSetController.loadOldFormset(cefs);
        updateYearDateMonth();
        return "/client/client_case_enrollment";
    }

    public String toNewTestEnrollmentFromEncounter() {
        if (selectedEncounter == null) {
            JsfUtil.addErrorMessage("No encounter");
            return "";
        }
        if (selectedEncounter.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        setSelected(selectedEncounter.getClient());
        saveClient(selected);
        clearRegisterNewExistsValues();
        selectedClientsClinics = null;
        selectedClientEncounters = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        userTransactionController.recordTransaction("to add a new client for case");
        DesignComponentFormSet dfs = designComponentFormSetController.getFirstTestEnrollmentFormSet();
        if (dfs == null) {
            JsfUtil.addErrorMessage("No Default Form Set");
            return "";
        }
        ClientEncounterComponentFormSet cefs = clientEncounterComponentFormSetController.createNewTestEnrollmentFormsetToDataEntry(dfs);
        if (cefs == null) {
            JsfUtil.addErrorMessage("No Patient Form Set");
            return "";
        }
        clientEncounterComponentFormSetController.loadOldFormset(cefs);
        updateYearDateMonth();
        return "/client/client_test_enrollment";
    }

    public String toDeleteTestEncounter() {
        if (selectedEncounter == null) {
            JsfUtil.addErrorMessage("No encounter");
            return "";
        }
        if (selectedEncounter.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        selectedEncounter.setRetired(true);
        selectedEncounter.setRetiredAt(new Date());
        selectedEncounter.setRetiredBy(webUserController.getLoggedUser());
        encounterController.save(selectedEncounter);
        fillTestList();
        selectedEncounter = null;
        return "";
    }

    public String toDeleteCaseEncounter() {
        if (selectedEncounter == null) {
            JsfUtil.addErrorMessage("No encounter");
            return "";
        }
        if (selectedEncounter.getClient() == null) {
            JsfUtil.addErrorMessage("No Client");
            return "";
        }
        selectedEncounter.setRetired(true);
        selectedEncounter.setRetiredAt(new Date());
        selectedEncounter.setRetiredBy(webUserController.getLoggedUser());
        encounterController.save(selectedEncounter);
        fillCaseList();
        selectedEncounter = null;
        return "";
    }

    public void addLastAddress() {
        if (lastTest == null) {
            return;
        }
        if (selected == null) {
            return;
        }
        selected.getPerson().setAddress(lastTest.getClient().getPerson().getAddress());
    }

    public void addLastPhones() {
        if (lastTest == null) {
            return;
        }
        if (selected == null) {
            return;
        }
        selected.getPerson().setPhone1(lastTest.getClient().getPerson().getPhone1());
        selected.getPerson().setPhone2(lastTest.getClient().getPerson().getPhone2());
    }

    public void addLastGn() {
        if (lastTest == null) {
            return;
        }
        if (selected == null) {
            return;
        }
        selected.getPerson().setGnArea(lastTest.getClient().getPerson().getGnArea());
    }

    public void addLastPhi() {
        if (lastTest == null) {
            return;
        }
        if (selected == null) {
            return;
        }
        selected.getPerson().setPhiArea(lastTest.getClient().getPerson().getPhiArea());
    }

    public String toAddNewClientForTestEnrollment() {
        setSelected(new Client());
        selected.getPerson().setDistrict(webUserController.getLoggedUser().getInstitution().getDistrict());
        selected.getPerson().setMohArea(webUserController.getLoggedUser().getInstitution().getMohArea());

        selected.setRetired(true);
        saveClient(selected);
        clearRegisterNewExistsValues();
        selectedClientsClinics = null;
        selectedClientEncounters = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        userTransactionController.recordTransaction("to add a new client for test ordering");
        DesignComponentFormSet dfs = designComponentFormSetController.getFirstTestEnrollmentFormSet();
        if (dfs == null) {
            JsfUtil.addErrorMessage("No Default Form Set");
            return "";
        }
        ClientEncounterComponentFormSet cefs = clientEncounterComponentFormSetController.createNewTestEnrollmentFormsetToDataEntry(dfs);
        if (cefs == null) {
            JsfUtil.addErrorMessage("No Patient Form Set");
            return "";
        }
        clientEncounterComponentFormSetController.loadOldFormset(cefs);
        clientEncounterComponentFormSetController.getSelected().getEncounter().setReferalInstitution(continuedLab);
        clientEncounterComponentFormSetController.getSelected().getEncounter().setPcrOrderingCategory(lastTestOrderingCategory);
        clientEncounterComponentFormSetController.getSelected().getEncounter().setPcrTestType(lastTestPcrOrRat);
        return "/client/client_test_enrollment";
    }

    public String toSelectedForNewTestEnrollment() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No Patient");
            return "";
        }
        clearRegisterNewExistsValues();
        selectedClientsClinics = null;
        selectedClientEncounters = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        userTransactionController.recordTransaction("to new test for selected client");
        DesignComponentFormSet dfs = designComponentFormSetController.getFirstTestEnrollmentFormSet();
        if (dfs == null) {
            JsfUtil.addErrorMessage("No Default Form Set");
            return "";
        }
        ClientEncounterComponentFormSet cefs = clientEncounterComponentFormSetController.createNewTestEnrollmentFormsetToDataEntry(dfs);
        if (cefs == null) {
            JsfUtil.addErrorMessage("No Patient Form Set");
            return "";
        }
        clientEncounterComponentFormSetController.loadOldFormset(cefs);
        updateYearDateMonth();
        return "/client/client_test_enrollment";
    }

    public String toFromFromEncounter() {
        if (selectedEncounter == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }

        setSelected(selectedEncounter.getClient());
        selected.getPerson().calAgeFromDob();

        clearRegisterNewExistsValues();
        selectedClientsClinics = null;
        selectedClientEncounters = null;
        selectedClinic = null;
        yearMonthDay = new YearMonthDay();
        userTransactionController.recordTransaction("to From from a Selected Encounter");

        ClientEncounterComponentFormSet cefs = clientEncounterComponentFormSetController.findFormsetFromEncounter(selectedEncounter);

        if (cefs == null) {
            JsfUtil.addErrorMessage("No such formset");
            return "";
        }

        DesignComponentFormSet dfs = cefs.getReferanceDesignComponentFormSet();
        if (dfs == null) {
            JsfUtil.addErrorMessage("No DFS");
            return "";
        }
        clientEncounterComponentFormSetController.loadOldFormset(cefs);
        if (dfs.isCaseEnrollmentForm()) {
            return "/client/client_test_enrollment";
        } else if (dfs.isTestEnrollmentForm()) {
            return "/client/client_test_enrollment";
        } else {
            JsfUtil.addErrorMessage("No Form");
            return "";
        }
    }

    public String toViewCorrectedDuplicates() {
        String j;
        j = "select c"
                + " from Client c "
                + " where c.reservedClient <> true and c.comments is not null";
        items = getFacade().findByJpql(j);
        userTransactionController.recordTransaction("To View Corrected Duplicates");
        return "/systemAdmin/clients_with_corrected_duplicate_phn";
    }

    public String toDetectPhnDuplicates() {
        String j;
        Map m = new HashMap();
        j = "SELECT c.phn "
                + " FROM Client c "
                + " "
                + " GROUP BY c.phn"
                + " HAVING COUNT(c.phn) > 1 ";
        List<String> duplicatedPhnNumbers = getFacade().findString(j);
        items = new ArrayList<>();
        for (String dupPhn : duplicatedPhnNumbers) {
            j = "select c"
                    + " from Client c "
                    + " where c.phn=:phn";
            m = new HashMap();
            m.put("phn", dupPhn);
            List<Client> temClients = getFacade().findByJpql(j, m);
            items.addAll(temClients);
        }
        userTransactionController.recordTransaction("To Detect PHN Duplicates");
        return "/systemAdmin/clients_with_phn_duplication";
    }

    public String correctPhnDuplicates() {
        String j;
        Map m = new HashMap();
        j = "SELECT c.phn "
                + " FROM Client c "
                + " "
                + " GROUP BY c.phn"
                + " HAVING COUNT(c.phn) > 1 ";
        List<String> duplicatedPhnNumbers = getFacade().findString(j, intNo);
        items = new ArrayList<>();
        for (String dupPhn : duplicatedPhnNumbers) {
            // // System.out.println("dupPhn = " + dupPhn);
            j = "select c"
                    + " from Client c "
                    + " where c.phn=:phn";
            m = new HashMap();
            m.put("phn", dupPhn);
            List<Client> temClients = getFacade().findByJpql(j, m);
            int n = 0;
            for (Client c : temClients) {
                if (n == 0) {

                } else {
                    if (c.getPerson().getLocalReferanceNo() == null || c.getPerson().getLocalReferanceNo().trim().equals("")) {
                        c.setComments("Duplicate PHN. Old PHN Stored as Local Ref");
                        // // System.out.println("Duplicate PHN. Old PHN Stored as Local Ref");
                        // // System.out.println("c.getPhn()");
                        c.getPerson().setLocalReferanceNo(c.getPhn());
                        // // System.out.println("c.getPerson().getLocalReferanceNo() = " + c.getPerson().getLocalReferanceNo());
                        c.setPhn(generateNewPhn(c.getCreateInstitution()));
                        // // System.out.println("c.getPhn()");
                    } else if (c.getPerson().getSsNumber() == null || c.getPerson().getSsNumber().trim().equals("")) {
                        c.setComments("Duplicate PHN. Old PHN Stored as SC No");
                        // // System.out.println("Duplicate PHN. Old PHN Stored as SC No");
                        // // System.out.println("c.getPhn()");
                        c.getPerson().setSsNumber(c.getPhn());
                        // // System.out.println("c.getPerson().getSsNumber() = " + c.getPerson().getSsNumber());
                        c.setPhn(generateNewPhn(c.getCreateInstitution()));
                        // // System.out.println("c.getPhn()");
                    } else {
                        // // System.out.println("No Space to Store Old PHN");
                    }
                    getFacade().edit(c);
                }
                n++;
            }
            items.addAll(temClients);
        }
        userTransactionController.recordTransaction("Correct PHN Duplicates");
        return "/systemAdmin/clients_with_corrected_duplicate_phn";
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public String importResultsFromExcel() {
        // System.out.println("importResultsFromExcel = ");

        // System.out.println("try 1");
        String strId;
        String strResult;
        String strCtValue;
        String strComment;
        Long id;

        int serialNoColInt;
        int ctValueColInt;
        int resultColInt;
        int commentColInt;

        serialNoColInt = CommonController.excelColFromHeader(serialNoColumn);
        ctValueColInt = CommonController.excelColFromHeader(ctValueColumn);
        resultColInt = CommonController.excelColFromHeader(resultColumn);
        commentColInt = CommonController.excelColFromHeader(commentColumn);

        File inputWorkbook;
        Workbook w;
        Cell cell;
        InputStream in;

        JsfUtil.addSuccessMessage(file.getFileName());

        try {
            JsfUtil.addSuccessMessage(file.getFileName());
            in = file.getInputstream();
            File f;
            f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
            FileOutputStream out = new FileOutputStream(f);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            in.close();
            out.flush();
            out.close();

            inputWorkbook = new File(f.getAbsolutePath());

            JsfUtil.addSuccessMessage("Excel File Opened");
            w = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = w.getSheet(0);

            errorCode = "";

            for (int i = startRow; i < sheet.getRows(); i++) {

                cell = sheet.getCell(serialNoColInt, i);
                strId = cell.getContents();

                id = CommonController.getLongValue(strId);

                Encounter resultEntering = null;

                for (Encounter te : listedToEnterResults) {
                    if (te.getId().equals(id)) {
                        resultEntering = te;
                    }
                }

                if (resultEntering == null) {
                    JsfUtil.addErrorMessage("Could NOT find serial. Please check column numbers");
                    continue;
                }

                cell = sheet.getCell(resultColInt, i);
                strResult = cell.getContents();
                strResult = strResult.trim().toLowerCase();

                resultEntering.setPcrResultStr(strResult);

                cell = sheet.getCell(ctValueColInt, i);
                strCtValue = cell.getContents();
                resultEntering.setCtValueStr(strCtValue);

                cell = sheet.getCell(commentColInt, i);
                strComment = cell.getContents();
                resultEntering.setResultComments(strComment);
                String localPositive = preferenceController.getPcrPositiveTerm().trim().toLowerCase();
                String localNegative = preferenceController.getPcrNegativeTerm().trim().toLowerCase();
                String localInvalid = preferenceController.getPcrInvalidTerm().trim().toLowerCase();
                String localInconclusive = preferenceController.getPcrInconclusiveTerm().trim().toLowerCase();

                Item pcrPositive = itemApplicationController.getPcrPositive();
                Item pcrNegative = itemApplicationController.getPcrNegative();
                Item pcrInconclusive = itemApplicationController.getPcrInconclusive();
                Item pcrInvalid = itemApplicationController.getPcrInvalid();

                if (strResult.contains(localNegative)) {
                    resultEntering.setPcrResult(pcrNegative);
                } else if (strResult.contains(localInvalid)) {
                    resultEntering.setPcrResult(pcrInvalid);
                } else if (strResult.contains(localInconclusive)) {
                    resultEntering.setPcrResult(pcrInconclusive);
                } else if (strResult.contains(localPositive)) {
                    resultEntering.setPcrResult(pcrPositive);
                } else {
                    continue;
                }

                resultEntering.setResultEntered(true);
                resultEntering.setResultEnteredAt(new Date());
                resultEntering.setResultEnteredBy(webUserController.getLoggedUser());

                getEncounterFacade().edit(resultEntering);

            }

            JsfUtil.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
            return "";
        } catch (IOException ex) {
            JsfUtil.addErrorMessage(ex.getMessage());
            System.err.println("e = " + ex.getMessage());
            return "";
        } catch (BiffException e) {
            JsfUtil.addErrorMessage(e.getMessage());
            System.err.println("e = " + e.getMessage());
            return "";
        }

    }

    public List<Area> completeClientsGnArea(String qry) {
        List<Area> areas = new ArrayList<>();
        if (selected == null) {
            return areas;
        }
        if (selected.getPerson().getDistrict() == null) {
            return areaApplicationController.completeGnAreas(qry);
        } else {
            return areaApplicationController.completeGnAreasOfDistrict(qry, selected.getPerson().getDistrict());
        }
    }

    public List<Area> completeClientsMohArea(String qry) {
        List<Area> areas = new ArrayList<>();
        if (selected == null) {
            return areas;
        }
        if (selected.getPerson().getDistrict() == null) {
            return areaApplicationController.completeMohAreas(qry);
        } else {
            return areaApplicationController.completeGnAreasOfDistrict(qry, selected.getPerson().getDistrict());
        }
    }

    public List<Area> completeClientsPhiArea(String qry) {
        List<Area> areas = new ArrayList<>();
        if (selected == null) {
            return areas;
        }
        if (selected.getPerson().getMohArea() == null) {
            return areaApplicationController.completePhiAreas(qry);
        } else {
            return areaApplicationController.completePhiAreasOfMoh(qry, selected.getPerson().getMohArea());
        }
    }

    public void clearRegisterNewExistsValues() {
        phnExists = false;
        nicExists = false;
        emailExists = false;
        phone1Exists = false;
        ssNumberExists = false;
    }

    public void clearExistsValues() {
        phnExists = false;
        nicExists = false;
        passportExists = false;
        dlExists = false;
    }

    public void checkPhnExists() {
        phnExists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPhn() == null) {
            return;
        }
        if (selected.getPhn().trim().equals("")) {
            return;
        }
        phnExists = checkPhnExists(selected.getPhn(), selected);
    }

    public Boolean checkPhnExists(String phn, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.phn=:phn ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("phn", phn);
        if (c != null && c.getId() != null) {
            jpql += " and c <> :client";
            m.put("client", c);
        }
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }
    }

    public void checkNicExists() {
        nicExists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPerson() == null) {
            return;
        }
        if (selected.getPerson().getNic() == null) {
            return;
        }
        if (selected.getPerson().getNic().trim().equals("")) {
            return;
        }
        nicExists = checkNicExists(selected.getPerson().getNic(), selected);
    }

    public void ageAndSexFromNic() {
        // // System.out.println("ageAndSexFromNic");
        if (getSelected().getPerson().getNic() != null) {
            SlNic n = new SlNic();
            n.setNic(getSelected().getPerson().getNic());
            if (n.getDateOfBirth() != null) {
                getSelected().getPerson().setDateOfBirth(n.getDateOfBirth());
            }
            if (n.getSex() != null) {
                if (n.getSex().equalsIgnoreCase("male")) {
                    getSelected().getPerson().setSex(itemApplicationController.getMale());
                } else {
                    getSelected().getPerson().setSex(itemApplicationController.getFemale());
                }
            }
        }
        updateYearDateMonth();
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
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }

    }

    public void addCreatedDateFromCreatedAt() {
        String j = "select c from Client c where c.createdOn is null";
        List<Client> cs = getFacade().findByJpql(j, 1000);
        // // System.out.println("cs.getSize() = " + cs.size());
        for (Client c : cs) {
            if (c.getCreatedOn() == null) {
                c.setCreatedOn(c.getCreatedAt());
                getFacade().edit(c);
            }
        }
    }

    public void checkEmailExists() {
        emailExists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPerson() == null) {
            return;
        }
        if (selected.getPerson().getEmail() == null) {
            return;
        }
        if (selected.getPerson().getEmail().trim().equals("")) {
            return;
        }
        emailExists = checkEmailExists(selected.getPerson().getEmail(), selected);
    }

    public Boolean checkEmailExists(String email, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.email=:email ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("email", email);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }

    }

    public void checkPhone1Exists() {
        phone1Exists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPerson() == null) {
            return;
        }
        if (selected.getPerson().getPhone1() == null) {
            return;
        }
        if (selected.getPerson().getPhone1().trim().equals("")) {
            return;
        }
        phone1Exists = checkPhone1Exists(selected.getPerson().getPhone1(), selected);
    }

    public Boolean checkPhone1Exists(String phone1, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.phone1=:phone1 ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("phone1", phone1);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }

    }

    public void checkSsNumberExists() {
        ssNumberExists = null;
        if (selected == null) {
            return;
        }
        if (selected.getPerson() == null) {
            return;
        }
        if (selected.getPerson().getSsNumber() == null) {
            return;
        }
        if (selected.getPerson().getSsNumber().trim().equals("")) {
            return;
        }
        ssNumberExists = checkSsNumberExists(selected.getPerson().getSsNumber(), selected);
    }

    public Boolean checkSsNumberExists(String ssNumber, Client c) {
        String jpql = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.person.ssNumber=:ssNumber ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("ssNumber", ssNumber);
        if (c != null && c.getPerson() != null && c.getPerson().getId() != null) {
            jpql += " and c.person <> :person";
            m.put("person", c.getPerson());
        }
        Long count = getFacade().countByJpql(jpql, m);
        if (count == null || count == 0l) {
            return false;
        } else {
            return true;
        }

    }

    public void fixClientPersonCreatedAt() {
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";

        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        List<Client> cs = getFacade().findByJpql(j, m);
        for (Client c : cs) {

            if (c.getCreatedAt() == null && c.getPerson().getCreatedAt() != null) {
                c.setCreatedAt(c.getPerson().getCreatedAt());
                getFacade().edit(c);
            } else if (c.getCreatedAt() != null && c.getPerson().getCreatedAt() == null) {
                c.getPerson().setCreatedAt(c.getCreatedAt());
                getFacade().edit(c);
            } else if (c.getCreatedAt() == null && c.getPerson().getCreatedAt() == null) {
                c.getPerson().setCreatedAt(new Date());
                c.setCreatedAt(new Date());
                getFacade().edit(c);
            }

        }
        userTransactionController.recordTransaction("Fix Client Person Created At");
    }

    public void updateClientCreatedInstitution() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.id > :idf "
                + " and c.id < :idt ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("idf", idFrom);
        m.put("idt", idTo);
        List<Client> cs = getFacade().findByJpql(j, m);
        for (Client c : cs) {
            c.setCreateInstitution(institution);
            getFacade().edit(c);
        }
        userTransactionController.recordTransaction("Update Client Created Institution");
    }

    public void updateClientDateOfBirth() {
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.id > :idf "
                + " and c.id < :idt ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        m.put("idf", idFrom);
        m.put("idt", idTo);
        List<Client> cs = getFacade().findByJpql(j, m);
        for (Client c : cs) {
            Calendar cd = Calendar.getInstance();

            if (c.getPerson().getDateOfBirth() != null) {

                cd.setTime(c.getPerson().getDateOfBirth());

                int dobYear = cd.get(Calendar.YEAR);

                if (dobYear < 1800) {
                    cd.add(Calendar.YEAR, 2000);
                    c.getPerson().setDateOfBirth(cd.getTime());
                    getFacade().edit(c);
                }

            }
        }
        userTransactionController.recordTransaction("Update Client Date Of Birth");
    }

    public Long countOfRegistedClients(Institution ins, Area gn) {
        String j = "select count(c) from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        if (ins != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", ins);
        }
        if (gn != null) {
            j += " and c.person.gnArea=:gn ";
            m.put("gn", gn);
        }
        return getFacade().countByJpql(j, m);
    }

    public String toRegisterdClientsDemo() {
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        if (webUserController.getLoggedUser().getInstitution() != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", webUserController.getLoggedUser().getInstitution());
        } else {
            items = new ArrayList<>();
        }

        items = getFacade().findByJpql(j, m);
        return "/insAdmin/registered_clients";
    }

    public String toRegisterdClientsWithDates() {
        String j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        if (webUserController.getLoggedUser().getInstitution() != null) {
            j += " and c.createInstitution=:ins ";
            m.put("ins", webUserController.getLoggedUser().getInstitution());
        }
        j = j + " and c.createdAt between :fd and :td ";
        j = j + " order by c.id desc";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        items = getFacade().findByJpql(j, m, TemporalType.TIMESTAMP);
        return "/insAdmin/registered_clients";
    }

    public String toRegisterdClientsWithDatesForSystemAdmin() {
        userTransactionController.recordTransaction("To Registerd Clients With Dates For SystemAdmin");
        return "/systemAdmin/all_clients";
    }

    public void fillClients() {
        String j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.gnArea.name, "
                + "c.createInstitution.name, "
                + "c.person.dateOfBirth, "
                + "c.createdAt, "
                + "c.person.sex.name, "
                + "c.person.nic,"
                + "c.person.name "
                + ") "
                + "from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";
        j = j + " order by c.id desc";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        selectedClientsBasic = null;
        clients = getFacade().findByJpql(j, m, TemporalType.TIMESTAMP);
        userTransactionController.recordTransaction("Fill Clients - SysAdmin");
    }

    public void fillRegisterdClientsWithDatesForInstitution() {
        String j = "select c from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";

        if (institution != null) {
            j = j + " and c.createInstitution =:ins ";
            m.put("ins", institution);
        } else {
            j = j + " and c.createInstitution in :ins ";
            m.put("ins", webUserController.getLoggableInstitutions());
        }

        j = j + " order by c.id desc";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        selectedClients = null;
        items = getFacade().findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public void saveSelectedImports() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        for (Client c : selectedClients) {
            c.setCreateInstitution(institution);
            if (!checkPhnExists(c.getPhn(), null)) {
                c.setId(null);
                saveClient(c);
            }
        }
    }

    public void fillClientsWithWrongPhnLength() {
        String j = "select c from Client c where length(c.phn) <>11 and reservedClient<>true order by c.id";
        items = getFacade().findByJpql(j);
        userTransactionController.recordTransaction("Fill Clients With Wrong PHN Length");
    }

    public void fillRetiredClients() {
        String j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.gnArea.name, "
                + "c.createInstitution.name, "
                + "c.person.dateOfBirth, "
                + "c.createdAt, "
                + "c.person.sex.name, "
                + "c.person.nic,"
                + "c.person.name "
                + ") "
                + "from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";
        j = j + " order by c.id desc";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        selectedClientsBasic = null;
        clients = getFacade().findByJpql(j, m, TemporalType.TIMESTAMP);
        userTransactionController.recordTransaction("Fill Retired Clients - SysAdmin");
    }

    public String retireSelectedClients() {
        for (ClientBasicData cb : selectedClientsBasic) {

            Client c = getFacade().find(cb.getId());

            if (c == null) {
                continue;
            };

            c.setRetired(true);
            c.setRetireComments("Bulk Delete");
            c.setRetiredAt(new Date());
            c.setRetiredBy(webUserController.getLoggedUser());

            c.getPerson().setRetired(true);
            c.getPerson().setRetireComments("Bulk Delete");
            c.getPerson().setRetiredAt(new Date());
            c.getPerson().setRetiredBy(webUserController.getLoggedUser());

            getFacade().edit(c);
        }
        selectedClients = null;
        userTransactionController.recordTransaction("Retire Selected Clients - SysAdmin");
        return toRegisterdClientsWithDatesForSystemAdmin();
    }

    public String unretireSelectedClients() {
        for (ClientBasicData cb : selectedClientsBasic) {

            Client c = getFacade().find(cb.getId());

            if (c == null) {
                continue;
            };
            c.setRetired(false);
            c.setRetireComments("Bulk Un Delete");
            c.setLastEditBy(webUserController.getLoggedUser());
            c.setLastEditeAt(new Date());

            c.getPerson().setRetired(false);
            c.getPerson().setRetireComments("Bulk Un Delete");
            c.getPerson().setEditedAt(new Date());
            c.getPerson().setEditer(webUserController.getLoggedUser());

            getFacade().edit(c);
        }
        selectedClients = null;
        userTransactionController.recordTransaction("Unretire Selected Clients - SysAdmin");
        return toRegisterdClientsWithDatesForSystemAdmin();
    }

    public void retireSelectedClient() {
        Client c = selected;
        if (c != null) {
            c.setRetired(true);
            c.setRetiredBy(webUserController.getLoggedUser());
            c.setRetiredAt(new Date());

            c.getPerson().setRetired(true);
            c.getPerson().setRetiredBy(webUserController.getLoggedUser());
            c.getPerson().setRetiredAt(new Date());

            getFacade().edit(c);
        }
    }

    public void saveAllImports() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        for (Client c : importedClients) {
            c.setCreateInstitution(institution);
            if (!checkPhnExists(c.getPhn(), null)) {
                c.setId(null);
                saveClient(c);
            }
        }
    }

//    public boolean phnExists(String phn) {
//        String j = "select c fromDate Client c where c.retired=:ret "
//                + " and c.phn=:phn";
//        Map m = new HashMap();
//        m.put("ret", false);
//        m.put("phn", phn);
//        Client c = getFacade().findFirstByJpql(j, m);
//        if (c == null) {
//            return false;
//        }
//        return true;
//    }
    public String importClientsFromExcel() {

        importedClients = new ArrayList<>();

        if (institution == null) {
            JsfUtil.addErrorMessage("Add Institution");
            return "";
        }

        if (uploadDetails == null || uploadDetails.trim().equals("")) {
            JsfUtil.addErrorMessage("Add Column Names");
            return "";
        }

        String[] cols = uploadDetails.split("\\r?\\n");
        if (cols == null || cols.length < 5) {
            JsfUtil.addErrorMessage("No SUfficient Columns");
            return "";
        }

        try {
            File inputWorkbook;
            Workbook w;
            Cell cell;
            InputStream in;
            try {
                in = file.getInputstream();
                File f;
                f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
                FileOutputStream out = new FileOutputStream(f);
                int read = 0;
                byte[] bytes = new byte[1024];
                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                in.close();
                out.flush();
                out.close();

                inputWorkbook = new File(f.getAbsolutePath());

                JsfUtil.addSuccessMessage("Excel File Opened");
                w = Workbook.getWorkbook(inputWorkbook);
                Sheet sheet = w.getSheet(0);

                errorCode = "";

                int startRow = 1;

                Long temId = 0L;

                for (int i = startRow; i < sheet.getRows(); i++) {

                    Map m = new HashMap();

                    Client c = new Client();
                    Person p = new Person();
                    c.setPerson(p);

                    int colNo = 0;

                    String gnAreaName = null;
                    String gnAreaCode = null;
                    for (String colName : cols) {
                        cell = sheet.getCell(colNo, i);
                        String cellString = cell.getContents();
                        switch (colName) {
                            case "client_gn_area_name":
                                gnAreaName = cellString;
                                break;
                            case "client_gn_area_code":
                                gnAreaCode = cellString;
                                break;
                        }
                        colNo++;
                    }
                    Area gnArea = null;
//                    //// // System.out.println("gnAreaName = " + gnAreaName);
//                    //// // System.out.println("gnAreaCode = " + gnAreaCode);
                    if (gnAreaName != null && gnAreaCode != null) {
                        gnArea = areaController.getGnAreaByNameAndCode(gnAreaName, gnAreaCode);
                    } else if (gnAreaName != null) {
                        gnArea = areaController.getGnAreaByName(gnAreaName);
                    } else if (gnAreaCode != null) {
                        gnArea = areaController.getGnAreaByCode(gnAreaCode);
                    }
                    if (gnArea != null) {
//                        //// // System.out.println("gnArea = " + gnArea.getName());
                    }

                    colNo = 0;

                    for (String colName : cols) {
                        cell = sheet.getCell(colNo, i);
                        String cellString = cell.getContents();
                        switch (colName) {
                            case "client_name":
                                c.getPerson().setName(cellString);
                                break;
                            case "client_phn_number":
                                c.setPhn(cellString);
                                break;
                            case "client_sex":
                                Item sex;
                                if (cellString.toLowerCase().contains("f")) {
                                    sex = itemController.findItemByCode("sex_female");
                                } else {
                                    sex = itemController.findItemByCode("sex_male");
                                }
                                c.getPerson().setSex(sex);
                                break;
                            case "client_citizenship":
                                Item cs;
                                if (cellString == null) {
                                    cs = null;
                                } else if (cellString.toLowerCase().contains("sri")) {
                                    cs = itemController.findItemByCode("citizenship_local");
                                } else {
                                    cs = itemController.findItemByCode("citizenship_foreign");
                                }
                                c.getPerson().setCitizenship(cs);
                                break;

                            case "client_ethnic_group":
                                Item eg = null;
                                if (cellString == null || cellString.trim().equals("")) {
                                    eg = null;
                                } else if (cellString.equalsIgnoreCase("Sinhala")) {
                                    eg = itemController.findItemByCode("sinhalese");
                                } else if (cellString.equalsIgnoreCase("moors")) {
                                    eg = itemController.findItemByCode("citizenship_local");
                                } else if (cellString.equalsIgnoreCase("SriLankanTamil")) {
                                    eg = itemController.findItemByCode("tamil");
                                } else {
                                    eg = itemController.findItemByCode("ethnic_group_other");;
                                }
                                c.getPerson().setEthinicGroup(eg);
                                break;
                            case "client_religion":
                                Item re = null;
                                if (cellString == null || cellString.trim().equals("")) {
                                    re = null;
                                } else if (cellString.equalsIgnoreCase("Buddhist")) {
                                    re = itemController.findItemByCode("buddhist");
                                } else if (cellString.equalsIgnoreCase("Christian")) {
                                    re = itemController.findItemByCode("christian");
                                } else if (cellString.equalsIgnoreCase("Hindu")) {
                                    re = itemController.findItemByCode("hindu");
                                } else {
                                    re = itemController.findItemByCode("religion_other");;
                                }
                                c.getPerson().setReligion(re);
                                break;
                            case "client_marital_status":
                                Item ms = null;
                                if (cellString == null || cellString.trim().equals("")) {
                                    ms = null;
                                } else if (cellString.equalsIgnoreCase("Married")) {
                                    ms = itemController.findItemByCode("married");
                                } else if (cellString.equalsIgnoreCase("Separated")) {
                                    ms = itemController.findItemByCode("seperated");
                                } else if (cellString.equalsIgnoreCase("Single")) {
                                    ms = itemController.findItemByCode("unmarried");
                                } else {
                                    ms = itemController.findItemByCode("marital_status_other");;
                                }
                                c.getPerson().setMariatalStatus(ms);
                                break;
                            case "client_title":
                                Item title = null;
                                String ts = cellString;
                                switch (ts) {
                                    case "Baby":
                                        title = itemController.findItemByCode("baby");
                                        break;
                                    case "Babyof":
                                        title = itemController.findItemByCode("baby_of");
                                        break;
                                    case "Mr":
                                        title = itemController.findItemByCode("mr");
                                        break;
                                    case "Mrs":
                                        title = itemController.findItemByCode("mrs");
                                        break;
                                    case "Ms":
                                        title = itemController.findItemByCode("ms");
                                        break;
                                    case "Prof":
                                        title = itemController.findItemByCode("prof");
                                        break;
                                    case "Rev":
                                    case "Thero":
                                        title = itemController.findItemByCode("rev");
                                        break;
                                }
                                c.getPerson().setTitle(title);
                                break;
                            case "client_nic_number":
                                c.getPerson().setNic(cellString);
                                break;
                            case "client_data_of_birth":
                                Date tdob = null;
                                Date today = new Date();
                                int ageInYears = 0;
                                int birthYear;
                                int thisYear;

                                try {
                                    tdob = commonController.dateFromString(cellString, dateFormat);
                                    Calendar bc = Calendar.getInstance();
                                    bc.setTime(tdob);
                                    birthYear = bc.get(Calendar.YEAR);
                                    Calendar tc = Calendar.getInstance();
                                    thisYear = tc.get(Calendar.YEAR);
                                    ageInYears = thisYear - birthYear;
//                                    //// // System.out.println("ageInYears = " + ageInYears);
                                } catch (Exception e) {
//                                    //// // System.out.println("e = " + e);
                                }
                                if (ageInYears < 0) {
                                    tdob = today;
                                } else if (ageInYears > 200) {
                                    tdob = today;
                                }

                                c.getPerson().setDateOfBirth(tdob);
                                break;
                            case "client_permanent_address":
                                c.getPerson().setAddress(cellString);
                                break;
                            case "client_current_address":
                                c.getPerson().setAddress(cellString);
                                break;
                            case "client_mobile_number":
                                c.getPerson().setPhone1(cellString);
                                break;
                            case "client_home_number":
                                c.getPerson().setPhone2(cellString);
                                break;
                            case "client_registered_at":
                                Date reg = commonController.dateFromString(cellString, dateTimeFormat);
                                c.getPerson().setCreatedAt(reg);
                                c.setCreatedAt(reg);
                                break;
                            case "client_gn_area":
                                //// // System.out.println("GN");
                                //// // System.out.println("cellString = " + cellString);

                                Area tgn;
                                if (gnArea == null) {
                                    gnArea = areaController.getAreaByCodeIfNotName(cellString, AreaType.GN);
                                }

                                break;
                        }

                        colNo++;
                    }

                    //// // System.out.println("tgn = " + gnArea);
                    if (gnArea != null) {
                        c.getPerson().setGnArea(gnArea);
                        c.getPerson().setDsArea(gnArea.getDsd());
                        c.getPerson().setMohArea(gnArea.getMoh());
                        c.getPerson().setPhmArea(gnArea.getPhm());
                        c.getPerson().setDistrict(gnArea.getDistrict());
                        c.getPerson().setProvince(gnArea.getProvince());
                    }
                    c.setCreateInstitution(institution);

                    c.setId(temId);
                    temId++;

                    importedClients.add(c);

                }

                lk.gov.health.phsp.facade.util.JsfUtil.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
                errorCode = "";
                return "save_imported_clients";
            } catch (IOException ex) {
                errorCode = ex.getMessage();
                lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage(ex.getMessage());
                return "";
            } catch (BiffException ex) {
                lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage(ex.getMessage());
                errorCode = ex.getMessage();
                return "";
            }
        } catch (IndexOutOfBoundsException e) {
            errorCode = e.getMessage();
            return "";
        }
    }

    public void prepareToCapturePhotoWithWebCam() {
        goingToCaptureWebCamPhoto = true;
    }

    public void finishCapturingPhotoWithWebCam() {
        goingToCaptureWebCamPhoto = false;
    }

    public void onTabChange(TabChangeEvent event) {

        // ////// // System.out.println("profileTabActiveIndex = " + profileTabActiveIndex);
        TabView tabView = (TabView) event.getComponent();

        profileTabActiveIndex = tabView.getChildren().indexOf(event.getTab());

    }

    public List<Encounter> fillEncounters(Client client,
            EncounterType encType, Integer maxRecordCount) {
        // ////// // System.out.println("fillEncounters");
        String j = "select e from Encounter e where e.retired=false ";
        Map m = new HashMap();
        if (client != null) {
            j += " and e.client=:c ";
            m.put("c", client);
        }
        if (maxRecordCount == null) {
            return encounterFacade.findByJpql(j, m);
        } else {
            return encounterFacade.findByJpql(j, m, maxRecordCount);
        }

    }

    public List<Encounter> fillEncounters(Client client, InstitutionType insType, EncounterType encType, boolean excludeCompleted, Integer maxRecordCount) {
        // ////// // System.out.println("fillEncounters");
        String j = "select e from Encounter e where e.retired=false ";
        Map m = new HashMap();
        if (client != null) {
            j += " and e.client=:c ";
            m.put("c", client);
        }
        if (insType != null) {
            j += " and e.institution.institutionType=:it ";
            m.put("it", insType);
        }
        if (insType != null) {
            j += " and e.encounterType=:et ";
            m.put("et", encType);
        }
        if (excludeCompleted) {
            j += " and e.completed=:com ";
            m.put("com", false);
        }
        if (maxRecordCount == null) {
            return encounterFacade.findByJpql(j, m);
        } else {
            return encounterFacade.findByJpql(j, m, maxRecordCount);
        }

    }

    public List<Encounter> fillEncounters(Client client, InstitutionType insType, EncounterType encType, boolean excludeCompleted) {
        return fillEncounters(client, insType, encType, true, null);
    }

    public void enrollInClinic() {
        if (selectedClinic == null) {
            JsfUtil.addErrorMessage("Please select an clinic to enroll.");
            return;
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a client to enroll.");
            return;
        }
        if (selectedClinic.getId() == null) {
            JsfUtil.addErrorMessage("Please select a valid clinic to enroll.");
            return;
        }
        if (selected.getId() == null) {
            JsfUtil.addErrorMessage("Please save the client first before enrolling.");
            return;
        }
        if (encounterController.clinicEnrolmentExists(selectedClinic, selected)) {
            JsfUtil.addErrorMessage("This client is already enrolled.");
            return;
        }
        Encounter encounter = new Encounter();
        encounter.setClient(selected);
        encounter.setEncounterType(EncounterType.Death);
        encounter.setCreatedAt(new Date());
        encounter.setCreatedBy(webUserController.getLoggedUser());
        encounter.setInstitution(selectedClinic);
        if (clinicDate != null) {
            encounter.setEncounterDate(clinicDate);
        } else {
            encounter.setEncounterDate(new Date());
        }
        encounter.setEncounterNumber(encounterController.createClinicEnrollNumber(selectedClinic));
        encounter.setCompleted(false);
        encounterFacade.create(encounter);
        JsfUtil.addSuccessMessage(selected.getPerson().getNameWithTitle() + " was Successfully Enrolled in " + selectedClinic.getName() + "\nThe Clinic number is " + encounter.getEncounterNumber());
        selectedClientsClinics = null;
    }

    public void generateAndAssignNewPhn() {
        if (selected == null) {
            return;
        }
        Institution poiIns;
        if (webUserController.getLoggedUser().getInstitution() == null) {
            JsfUtil.addErrorMessage("You do not have an Institution. Please contact support.");
            return;
        }
        //// // System.out.println("webUserController.getLoggedUser().getInstitution() = " + webUserController.getLoggedUser().getInstitution().getLastHin());
        if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
            poiIns = webUserController.getLoggedUser().getInstitution().getPoiInstitution();
        } else {
            poiIns = webUserController.getLoggedUser().getInstitution();
        }
        if (poiIns.getPoiNumber() == null || poiIns.getPoiNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("A Point of Issue is NOT assigned to your Institution. Please discuss with the System Administrator.");
            return;
        }
        selected.setPhn(applicationController.createNewPersonalHealthNumberformat(poiIns));
    }

    public String generateNewPhn(Institution ins) {
        Institution poiIns;
        if (ins == null) {
            // // System.out.println("Ins is null");
            return null;
        }
        if (ins.getPoiInstitution() != null) {
            poiIns = ins.getPoiInstitution();
        } else {
            poiIns = ins;
        }
        if (poiIns.getPoiNumber() == null || poiIns.getPoiNumber().trim().equals("")) {
            // // System.out.println("A Point of Issue is NOT assigned to the Institution. Please discuss with the System Administrator.");
            return null;
        }
        return applicationController.createNewPersonalHealthNumberformat(poiIns);
    }

    public void gnAreaChanged() {
        if (selected == null) {
            return;
        }
        if (selected.getPerson().getGnArea() != null) {
            selected.getPerson().setDsArea(selected.getPerson().getGnArea().getDsd());
            selected.getPerson().setMohArea(selected.getPerson().getGnArea().getMoh());
            selected.getPerson().setPhmArea(selected.getPerson().getGnArea().getPhm());
            selected.getPerson().setDistrict(selected.getPerson().getGnArea().getDistrict());
            selected.getPerson().setProvince(selected.getPerson().getGnArea().getProvince());
        }
    }

    public void selectedClientChanged() {
        clientEncounterComponentFormSetController.setLastFiveClinicVisits(null);
    }

    public void updateYearDateMonth() {
        getYearMonthDay();
        if (selected != null) {
            yearMonthDay.setYear(selected.getPerson().getAgeYears() + "");
            yearMonthDay.setMonth(selected.getPerson().getAgeMonths() + "");
            yearMonthDay.setDay(selected.getPerson().getAgeDays() + "");
            selected.getPerson().setDobIsAnApproximation(false);
        } else {
            yearMonthDay = new YearMonthDay();
        }
    }

    public void yearMonthDateChanged() {
        if (selected == null) {
            return;
        }
        selected.getPerson().setDobIsAnApproximation(true);
        selected.getPerson().setDateOfBirth(guessDob(yearMonthDay));
    }

    public Date guessDob(YearMonthDay yearMonthDay) {
        // ////// ////// // System.out.println("year string is " + docStr);
        int years = 0;
        int month = 0;
        int day = 0;
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("IST"));
        try {
            if (yearMonthDay.getYear() != null && !yearMonthDay.getYear().isEmpty()) {
                years = Integer.valueOf(yearMonthDay.getYear());
                now.add(Calendar.YEAR, -years);
            }

            if (yearMonthDay.getMonth() != null && !yearMonthDay.getMonth().isEmpty()) {
                month = Integer.valueOf(yearMonthDay.getMonth());
                now.add(Calendar.MONTH, -month);
            }

            if (yearMonthDay.getDay() != null && !yearMonthDay.getDay().isEmpty()) {
                day = Integer.valueOf(yearMonthDay.getDay());
                now.add(Calendar.DATE, -day);
            }

            return now.getTime();
        } catch (Exception e) {
            ////// ////// // System.out.println("Error is " + e.getMessage());
            return new Date();

        }
    }

    public void addNewPhnNumberToSelectedClient() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No Client is Selected");
            return;
        }
        if (webUserController.getLoggedUser().getInstitution().getPoiNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("No POI is configured for your institution. Please contact support.");
            return;
        }
        selected.setPhn(applicationController.createNewPersonalHealthNumber(webUserController.getLoggedUser().getInstitution()));
    }

    public String searchByPhn() {
        selectedClients = listPatientsByPhn(searchingPhn);
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            if (selected.isReservedClient()) {
                return "/client/client";
            }
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchByNic() {
        selectedClients = listPatientsByNic(searchingNicNo);
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchByPhoneNumber() {
        selectedClients = listPatientsByPhone(searchingPhoneNumber);
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchByPassportNo() {
        selectedClients = listPatientsByPassportNo(searchingPassportNo);
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchByDrivingLicenseNo() {
        selectedClients = listPatientsByDrivingLicenseNo(searchingDrivingLicenceNo);
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchByLocalReferanceNo() {
        if (webUserController.getLoggedUser().isSystemAdministrator()) {
            selectedClients = listPatientsByLocalReferanceNoForSystemAdmin(searchingLocalReferanceNo);
        } else {
            selectedClients = listPatientsByLocalReferanceNo(searchingLocalReferanceNo);
        }
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchBySsNo() {
        selectedClients = listPatientsBySsNo(searchingSsNumber);
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchByAllId() {
        selectedClients = new ArrayList<>();
        if (searchingPhn != null && !searchingPhn.trim().equals("")) {
            selectedClients.addAll(listPatientsByPhn(searchingPhn));
        }
        if (searchingNicNo != null && !searchingNicNo.trim().equals("")) {
            selectedClients.addAll(listPatientsByNic(searchingNicNo));
        }
        if (searchingPhoneNumber != null && !searchingPhoneNumber.trim().equals("")) {
            selectedClients.addAll(listPatientsByPhone(searchingPhoneNumber));
        }
        if (searchingPassportNo != null && !searchingPassportNo.trim().equals("")) {
            selectedClients.addAll(listPatientsByPassportNo(searchingPassportNo));
        }
        if (searchingDrivingLicenceNo != null && !searchingDrivingLicenceNo.trim().equals("")) {
            selectedClients.addAll(listPatientsByDrivingLicenseNo(searchingDrivingLicenceNo));
        }
        if (searchingLocalReferanceNo != null && !searchingLocalReferanceNo.trim().equals("")) {
            selectedClients.addAll(listPatientsByLocalReferanceNo(searchingLocalReferanceNo));
        }
        if (searchingSsNumber != null && !searchingSsNumber.trim().equals("")) {
            selectedClients.addAll(listPatientsBySsNo(searchingSsNumber));
        }

        if (selectedClients == null || selectedClients.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            return "";
        }
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchById() {
        clearExistsValues();
        if (searchingPhn != null && !searchingPhn.trim().equals("")) {
            selectedClients = listPatientsByPhn(searchingPhn);
        } else if (searchingNicNo != null && !searchingNicNo.trim().equals("")) {
            selectedClients = listPatientsByNic(searchingNicNo);
        } else if (searchingPhoneNumber != null && !searchingPhoneNumber.trim().equals("")) {
            selectedClients = listPatientsByPhone(searchingPhoneNumber);
        } else if (searchingPassportNo != null && !searchingPassportNo.trim().equals("")) {
            selectedClients = listPatientsByPassportNo(searchingPassportNo);
        } else if (searchingDrivingLicenceNo != null && !searchingDrivingLicenceNo.trim().equals("")) {
            selectedClients = listPatientsByDrivingLicenseNo(searchingDrivingLicenceNo);
        } else if (searchingLocalReferanceNo != null && !searchingLocalReferanceNo.trim().equals("")) {
            selectedClients = listPatientsByLocalReferanceNo(searchingLocalReferanceNo);
        } else if (searchingSsNumber != null && !searchingSsNumber.trim().equals("")) {
            selectedClients = listPatientsBySsNo(searchingSsNumber);
        }
        if (selectedClients == null || selectedClients.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            return "";
        }
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            clearSearchById();
            return toClientProfile();
        } else {
            selected = null;
            clearSearchById();
            return toSelectClient();
        }
    }

    public String searchByAnyIdWithBasicData() {
        userTransactionController.recordTransaction("Search By Any Id");
        clearExistsValues();
        if (searchingId == null) {
            searchingId = "";
        }

        selectedClientsWithBasicData = listPatientsByIDsStepviceWithBasicData(searchingId.trim().toUpperCase());

        if (selectedClientsWithBasicData == null || selectedClientsWithBasicData.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            userTransactionController.recordTransaction("Search By Any Id Failed as no match");
            return "/client/search_by_id";
        }
        if (selectedClientsWithBasicData.size() == 1) {
            selected = getFacade().find(selectedClientsWithBasicData.get(0).getId());
            selectedClients = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id returend single match");
            return toClientProfile();
        } else {
            selected = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id returned multiple matches");
            return toSelectClientBasic();
        }
    }

    public String searchByPhnWithBasicData() {
        // // System.out.println("searchByPhnWithBasicData");
        userTransactionController.recordTransaction("Search By PHN");
        clearExistsValues();
        if (searchingId == null) {
            searchingId = "";
        }

        selectedClientsWithBasicData = listPatientsByPhnWithBasicData(searchingId.trim().toUpperCase());

        if (selectedClientsWithBasicData == null || selectedClientsWithBasicData.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            userTransactionController.recordTransaction("Search By Any Id Failed as no match");
            return "/client/search_by_id";
        }
        if (selectedClientsWithBasicData.size() == 1) {
            selected = getFacade().find(selectedClientsWithBasicData.get(0).getId());
            selectedClients = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id returend single match");
            return toClientProfile();
        } else {
            selected = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id returned multiple matches");
            return toSelectClientBasic();
        }
    }

    public String searchByAnyId() {
        clearExistsValues();
        if (searchingId == null) {
            searchingId = "";
        }

        selectedClients = listPatientsByIDsStepvice(searchingId.trim().toUpperCase());

        if (selectedClients == null || selectedClients.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            userTransactionController.recordTransaction("Search By Any Id");
            return "/client/search_by_id";
        }
        if (selectedClients.size() == 1) {
            setSelected(selectedClients.get(0));
            selectedClients = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id");
            return toClientProfile();
        } else {
            selected = null;
            searchingId = "";
            userTransactionController.recordTransaction("Search By Any Id");
            return toSelectClient();
        }
    }

    public void clearSearchById() {
        searchingId = "";
        searchingPhn = "";
        searchingPassportNo = "";
        searchingDrivingLicenceNo = "";
        searchingNicNo = "";
        searchingName = "";
        searchingPhoneNumber = "";
        searchingLocalReferanceNo = "";
        searchingSsNumber = "";
    }

    public List<Client> listPatientsByPhn(String phn) {
        String j = "select c from Client c where c.retired=false and upper(c.phn)=:q order by c.phn";
        Map m = new HashMap();
        m.put("q", phn.trim().toUpperCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByNic(String phn) {
        String j = "select c from Client c where c.retired=false and c.reservedClient<>:res and upper(c.person.nic)=:q order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", phn.trim().toUpperCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByPhone(String phn) {
        String j = "select c from Client c where c.retired=false and c.reservedClient<>:res and (upper(c.person.phone1)=:q or upper(c.person.phone2)=:q) order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", phn.trim().toUpperCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByLocalReferanceNo(String refNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.localReferanceNo)=:q "
                + " and c.createInstitution=:ins "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", refNo.trim().toLowerCase());
        m.put("ins", webUserController.getLoggedUser().getInstitution());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByLocalReferanceNoForSystemAdmin(String refNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.localReferanceNo)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", refNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsBySsNo(String ssNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.ssNumber)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", ssNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByDrivingLicenseNo(String dlNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.drivingLicenseNumber)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", dlNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByPassportNo(String passportNo) {
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and lower(c.person.passportNumber)=:q "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", passportNo.trim().toLowerCase());
        return getFacade().findByJpql(j, m);
    }

    public List<Client> listPatientsByIDsStepvice(String ids) {
        //// // System.out.println("ids = " + ids);
        if (ids == null || ids.trim().equals("")) {
            return null;
        }
        List<Client> cs;
        if (ids == null || ids.trim().equals("")) {
            cs = new ArrayList<>();
            return cs;
        }
        String j;
        Map m;
        m = new HashMap();
        j = "select c from Client c "
                + " where c.retired=false "
                + " and upper(c.phn)=:q "
                + " order by c.phn";
        m.put("q", ids.trim().toUpperCase());
        //// // System.out.println("m = " + m);
        //// // System.out.println("j = " + j);
        cs = getFacade().findByJpql(j, m);

        if (cs != null && !cs.isEmpty()) {
            //// // System.out.println("cs.size() = " + cs.size());
            return cs;
        }

        j = "select c from Client c "
                + " where c.retired=false "
                + " and ("
                + " upper(c.person.phone1)=:q "
                + " or "
                + " upper(c.person.phone2)=:q "
                + " or "
                + " upper(c.person.nic)=:q "
                + " ) "
                + " order by c.phn";
        cs = getFacade().findByJpql(j, m);
        //// // System.out.println("m = " + m);
        //// // System.out.println("j = " + j);
        if (cs != null && !cs.isEmpty()) {
            //// // System.out.println("cs.size() = " + cs.size());
            return cs;
        }

        j = "select c from Client c "
                + " where c.retired=false "
                + " and ("
                + " c.person.localReferanceNo=:q "
                + " or "
                + " c.person.ssNumber=:q "
                + " ) "
                + " order by c.phn";

        return getFacade().findByJpql(j, m);
    }

    public List<ClientBasicData> listPatientsByIDsStepviceWithBasicData(String ids) {
        if (ids == null || ids.trim().equals("")) {
            return null;
        }
        List<ClientBasicData> cs;
        List<Object> objs;
        if (ids.trim().equals("")) {
            cs = new ArrayList<>();
            return cs;
        }
        String j;
        Map m;
        m = new HashMap();
        j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.name, "
                + "c.person.nic, "
                + "c.person.phone1, "
                + "c.person.address "
                + ") ";
        j += " from Client c "
                + " where c.retired=false "
                + " and c.phn=:q "
                + " order by c.phn";
        m.put("q", ids.trim().toUpperCase());
        //// // System.out.println("m = " + m);
        //// // System.out.println("j = " + j);
        objs = getFacade().findByJpql(j, m);

        if (objs != null && !objs.isEmpty()) {
            cs = objectsToClientBasicDataObjects(objs);
            return cs;
        }

        j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.name, "
                + "c.person.nic, "
                + "c.person.phone1, "
                + "c.person.address "
                + ") "
                + " from Client c "
                + " where c.retired=false "
                + " and ("
                + " c.person.phone1=:q "
                + " or "
                + " c.person.phone2=:q "
                + " or "
                + " c.person.nic=:q "
                + " ) "
                + " order by c.phn";
        objs = getFacade().findByJpql(j, m);
        //// // System.out.println("m = " + m);
        //// // System.out.println("j = " + j);
        if (objs != null && !objs.isEmpty()) {
            cs = objectsToClientBasicDataObjects(objs);
            return cs;
        }

        j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.name, "
                + "c.person.nic, "
                + "c.person.phone1, "
                + "c.person.address "
                + ") "
                + " from Client c "
                + " where c.retired=false "
                + " and ("
                + " c.person.localReferanceNo=:q "
                + " or "
                + " c.person.ssNumber=:q "
                + " ) "
                + " order by c.phn";

        objs = getFacade().findByJpql(j, m);
        if (objs != null && !objs.isEmpty()) {
            cs = objectsToClientBasicDataObjects(objs);
            return cs;
        }

        cs = new ArrayList<>();
        return cs;
    }

    public List<ClientBasicData> listPatientsByPhnWithBasicData(String ids) {
        if (ids == null || ids.trim().equals("")) {
            return null;
        }
        List<ClientBasicData> cs;
        List<Object> objs;
        if (ids.trim().equals("")) {
            cs = new ArrayList<>();
            return cs;
        }
        String j;
        Map m;
        m = new HashMap();
        j = "select new lk.gov.health.phsp.pojcs.ClientBasicData("
                + "c.id, "
                + "c.phn, "
                + "c.person.name, "
                + "c.person.sex.name,"
                + "c.person.nic, "
                + "c.person.phone1, "
                + "c.person.address "
                + ") ";
        j += " from Client c "
                + " where c.retired=false "
                + " and upper(c.phn)=:q "
                + " order by c.phn";
        m.put("q", ids.trim().toUpperCase());
        objs = getFacade().findByJpql(j, m);

        if (objs != null && !objs.isEmpty()) {
            cs = objectsToClientBasicDataObjects(objs);
            return cs;
        }
        cs = new ArrayList<>();
        return cs;
    }

    public List<ClientBasicData> objectsToClientBasicDataObjects(List<Object> objs) {
        List<ClientBasicData> cbds = new ArrayList<>();
        if (objs == null || objs.isEmpty()) {
            return cbds;
        }
        for (Object o : objs) {
            if (o instanceof ClientBasicData) {
                ClientBasicData c = (ClientBasicData) o;
                cbds.add(c);
            }
        }
        return cbds;
    }

    public List<Client> listPatientsByIDs(String ids) {
        if (ids == null || ids.trim().equals("")) {
            return null;
        }
        String j = "select c from Client c "
                + " where c.retired=false "
                + " and c.reservedClient<>:res "
                + " and ("
                + " upper(c.person.phone1)=:q "
                + " or "
                + " upper(c.person.phone2)=:q "
                + " or "
                + " upper(c.person.nic)=:q "
                + " or "
                + " upper(c.phn)=:q "
                + " or "
                + " c.person.localReferanceNo=:q "
                + " or "
                + " c.person.ssNumber=:q "
                + " ) "
                + " order by c.phn";
        Map m = new HashMap();
        m.put("res", true);
        m.put("q", ids.trim().toUpperCase());
        return getFacade().findByJpql(j, m);
    }

    public Client prepareCreate() {
        selected = new Client();
        return selected;
    }

    public String saveClientAndCaseEnrollment() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return "";
        }

        continuedLab = clientEncounterComponentFormSetController.getSelected().getEncounter().getReferalInstitution();
        Institution createdIns = null;
        selected.setRetired(false);
        if (selected.getCreateInstitution() == null) {
            if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
                createdIns = webUserController.getLoggedUser().getInstitution().getPoiInstitution();
            } else {
                createdIns = webUserController.getLoggedUser().getInstitution();
            }
            selected.setCreateInstitution(createdIns);
        } else {
            createdIns = selected.getCreateInstitution();
        }

        if (createdIns == null || createdIns.getPoiNumber() == null || createdIns.getPoiNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("The institution you logged has no POI. Can not generate a PHN.");
            return "";
        }

        if (selected.getPhn() == null || selected.getPhn().trim().equals("")) {
            String newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);

            int count = 0;
            while (checkPhnExists(newPhn, null)) {
                newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);
                count++;
                if (count > 100) {
                    JsfUtil.addErrorMessage("Generating New PHN Failed. Client NOT saved. Please contact System Administrator.");
                    return "";
                }
            }
            selected.setPhn(newPhn);
        }

        if (selected.getId() == null) {
            if (checkPhnExists(selected.getPhn(), null)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (selected.getPerson().getNic() != null && !selected.getPerson().getNic().trim().equals("")) {

                if (checkNicExists(selected.getPerson().getNic(), null)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        } else {
            if (checkPhnExists(selected.getPhn(), selected)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (selected.getPerson().getNic() != null && !selected.getPerson().getNic().trim().equals("")) {
                if (checkNicExists(selected.getPerson().getNic(), selected)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        }
        selected.setReservedClient(false);

        saveClient(selected);

        if (clientEncounterComponentFormSetController.getSelected().getEncounter() != null) {

            clientEncounterComponentFormSetController.getSelected().getEncounter().setEncounterNumber(encounterController.createCaseNumber(webUserController.getLoggedUser().getInstitution()));

            clientEncounterComponentFormSetController.getSelected().getEncounter().setRetired(false);
            encounterFacade.edit(clientEncounterComponentFormSetController.getSelected().getEncounter());
        }
        getInstitutionCaseEnrollmentMap().put(selected.getId(), clientEncounterComponentFormSetController.getSelected().getEncounter());

        JsfUtil.addSuccessMessage("Saved.");
        return "/client/profile_case_enrollment";
    }

    public String saveClientAndTestEnrollment() {
        // // System.out.println("saveClientAndTestEnrollment");
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return "";
        }
        continuedLab = clientEncounterComponentFormSetController.getSelected().getEncounter().getReferalInstitution();
        Institution createdIns = null;
        selected.setRetired(false);
        if (selected.getCreateInstitution() == null) {
            if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
                createdIns = webUserController.getLoggedUser().getInstitution().getPoiInstitution();
            } else {
                createdIns = webUserController.getLoggedUser().getInstitution();
            }
            selected.setCreateInstitution(createdIns);
        } else {
            createdIns = selected.getCreateInstitution();
        }

        if (createdIns == null || createdIns.getPoiNumber() == null || createdIns.getPoiNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("The institution you logged has no POI. Can not generate a PHN.");
            return "";
        }

        if (selected.getPhn() == null || selected.getPhn().trim().equals("")) {
            String newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);

            int count = 0;
            while (checkPhnExists(newPhn, null)) {
                newPhn = applicationController.createNewPersonalHealthNumberformat(createdIns);
                count++;
                if (count > 100) {
                    JsfUtil.addErrorMessage("Generating New PHN Failed. Client NOT saved. Please contact System Administrator.");
                    return "";
                }
            }
            selected.setPhn(newPhn);
        }

        if (selected.getId() == null) {
            if (checkPhnExists(selected.getPhn(), null)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (selected.getPerson().getNic() != null && !selected.getPerson().getNic().trim().equals("")) {

                if (checkNicExists(selected.getPerson().getNic(), null)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        } else {
            if (checkPhnExists(selected.getPhn(), selected)) {
                JsfUtil.addErrorMessage("PHN already exists.");
                return null;
            }
            if (selected.getPerson().getNic() != null && !selected.getPerson().getNic().trim().equals("")) {
                if (checkNicExists(selected.getPerson().getNic(), selected)) {
                    JsfUtil.addErrorMessage("NIC already exists.");
                    return null;
                }
            }
        }

        saveClient(selected);

        if (clientEncounterComponentFormSetController.getSelected().getEncounter() != null) {

            if (clientEncounterComponentFormSetController.getSelected().getEncounter().getEncounterNumber() == null
                    || clientEncounterComponentFormSetController.getSelected().getEncounter().getEncounterNumber().trim().equals("")) {
                clientEncounterComponentFormSetController.getSelected().getEncounter().setEncounterNumber(encounterController.createTestNumber(webUserController.getLoggedUser().getInstitution()));
            }
            Encounter te
                    = clientEncounterComponentFormSetController.getSelected().getEncounter();
            te.setRetired(false);
            te.setSampled(true);
            te.setSampledAt(new Date());
            te.setSampledBy(webUserController.getLoggedUser());
//            te.setSentToLab(true);
//            te.setSentToLabAt(new Date());
//            te.setSentToLabBy(webUserController.getLoggedUser());
            encounterFacade.edit(te);
            lastTestOrderingCategory = te.getPcrOrderingCategory();
            lastTestPcrOrRat = te.getPcrTestType();
            lastTest = te;
        }

        // clientEncounterComponentFormSetController.completeFormsetForTestEnrollment();
        getInstitutionTestEnrollmentMap().put(selected.getId(), clientEncounterComponentFormSetController.getSelected().getEncounter());

        JsfUtil.addSuccessMessage("Saved.");
        return "/client/profile_test_enrollment";
    }

    public void reserverPhn() {
        Institution createdIns;
        int i = 0;

        if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
            createdIns = webUserController.getLoggedUser().getInstitution().getPoiInstitution();
        } else {
            createdIns = webUserController.getLoggedUser().getInstitution();
        }

        if (createdIns == null) {
            JsfUtil.addErrorMessage("No POI");
            return;
        }

        if (numberOfPhnToReserve == null) {
            JsfUtil.addErrorMessage("No Numner of PHN to add");
            return;
        }

        if (numberOfPhnToReserve > 100) {
            JsfUtil.addErrorMessage("Only upto 100 PHNs can reserve at a time.");
            return;
        }
        reservePhnList = new ArrayList<>();

        while (i < numberOfPhnToReserve) {
            String newPhn = generateNewPhn(createdIns);

            if (!checkPhnExists(newPhn, null)) {
                reservePhnList.add(newPhn);

                Client rc = new Client();

                rc.setPhn(newPhn);
                rc.setCreatedBy(webUserController.getLoggedUser());
                rc.setCreatedAt(new Date());
                rc.setCreatedOn(new Date());
                rc.setCreateInstitution(createdIns);
                if (rc.getPerson().getCreatedAt() == null) {
                    rc.getPerson().setCreatedAt(new Date());
                }
                if (rc.getPerson().getCreatedBy() == null) {
                    rc.getPerson().setCreatedBy(webUserController.getLoggedUser());
                }
                rc.setReservedClient(true);

                getFacade().create(rc);
                i = i + 1;
            }
        }
    }

    public void saveClient(Client c) {
        if (c == null) {
            JsfUtil.addErrorMessage("No Client Selected to save.");
            return;
        }
        if (c.getId() == null) {
            c.setCreatedBy(webUserController.getLoggedUser());
            if (c.getCreatedAt() == null) {
                c.setCreatedAt(new Date());
            }
            if (c.getCreatedOn() == null) {
                c.setCreatedOn(new Date());
            }
            if (c.getCreateInstitution() == null) {
                if (webUserController.getLoggedUser().getInstitution().getPoiInstitution() != null) {
                    c.setCreateInstitution(webUserController.getLoggedUser().getInstitution().getPoiInstitution());
                } else if (webUserController.getLoggedUser().getInstitution() != null) {
                    c.setCreateInstitution(webUserController.getLoggedUser().getInstitution());
                }
            }
            if (c.getPerson().getCreatedAt() == null) {
                c.getPerson().setCreatedAt(new Date());
            }
            if (c.getPerson().getCreatedBy() == null) {
                c.getPerson().setCreatedBy(webUserController.getLoggedUser());
            }
            getFacade().create(c);
        } else {
            c.setLastEditBy(webUserController.getLoggedUser());
            c.setLastEditeAt(new Date());
            getFacade().edit(c);
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items toDate trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ClientDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items toDate trigger re-query.
        }
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
            }
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions - Temporary">
    public void convertFormsetDataInToEncounterDate() {
        String j = "select e "
                + " from Encounter e "
                + " where "
                + " e.pcrTestType is null "
                + " and e.retired=false "
                + " and e.institution is not null "
                + " and e.encounterDate is not null"
                + " and e.encounterType=:t";
        Map m = new HashMap();
        m.put("t", EncounterType.Test_Enrollment);
        if (idFrom == null) {
            idFrom = 1000l;
        }
        List<Encounter> cs = encounterFacade.findByJpql(j, m, idFrom.intValue());
        errorCode = "";
        for (Encounter e : cs) {

            System.out.println("e = " + e.getId());

            if (e.getInstitution() == null) {
                errorCode += "No Institution";
                continue;
            }
            if (e.getEncounterDate() == null) {
                continue;
            }
            if (e.getClient() == null || e.getClient().getPerson() == null) {
                continue;
            }

            errorCode += "\n Institution = " + e.getInstitution().getName() + "\n Date : " + e.getEncounterDate()
                    + "\n Patient = " + e.getClient().getPerson().getName();
            ClientEncounterComponentItem eTestType = e.getClientEncounterComponentItemByCode("test_type");

            if (eTestType == null || eTestType.getItemValue() == null) {
                e.setPcrTestType(itemApplicationController.getPcr());
                errorCode += "PCR Type Not Found";
            } else {
                e.setPcrTestType(eTestType.getItemValue());
            }

            eTestType = e.getClientEncounterComponentItemByCode("covid_19_test_ordering_context_category");

            if (eTestType == null || eTestType.getItemValue() == null) {
                e.setPcrOrderingCategory(itemApplicationController.getPcr());
                errorCode += "Ordering Category Not Found";
            } else {
                e.setPcrOrderingCategory(eTestType.getItemValue());
            }

            encounterFacade.edit(e);

        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public String getSearchingId() {
        return searchingId;
    }

    public void setSearchingId(String searchingId) {
        this.searchingId = searchingId;
    }

    public String getSearchingPhn() {
        return searchingPhn;
    }

    public void setSearchingPhn(String searchingPhn) {
        this.searchingPhn = searchingPhn;
    }

    public String getSearchingPassportNo() {
        return searchingPassportNo;
    }

    public void setSearchingPassportNo(String searchingPassportNo) {
        this.searchingPassportNo = searchingPassportNo;
    }

    public String getSearchingDrivingLicenceNo() {
        return searchingDrivingLicenceNo;
    }

    public void setSearchingDrivingLicenceNo(String searchingDrivingLicenceNo) {
        this.searchingDrivingLicenceNo = searchingDrivingLicenceNo;
    }

    public String getSearchingNicNo() {
        return searchingNicNo;
    }

    public void setSearchingNicNo(String searchingNicNo) {
        this.searchingNicNo = searchingNicNo;
    }

    public String getSearchingName() {
        return searchingName;
    }

    public void setSearchingName(String searchingName) {
        this.searchingName = searchingName;
    }

    public ClientFacade getEjbFacade() {
        return ejbFacade;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public Client getSelected() {
        return selected;
    }

    public void setSelected(Client selected) {
        this.selected = selected;
        updateYearDateMonth();
        selectedClientChanged();
        selectedClientsClinics = null;
        selectedClientEncounters = null;
    }

    private ClientFacade getFacade() {
        return ejbFacade;
    }

    public List<Client> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
        return items;
    }

    public List<Client> getItems(String jpql, Map m) {
        return getFacade().findByJpql(jpql, m);
    }

    public Client getClient(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Client> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Client> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public String getSearchingPhoneNumber() {
        return searchingPhoneNumber;
    }

    public void setSearchingPhoneNumber(String searchingPhoneNumber) {
        this.searchingPhoneNumber = searchingPhoneNumber;
    }

    public List<Client> getSelectedClients() {
        return selectedClients;
    }

    public void setSelectedClients(List<Client> selectedClients) {
        this.selectedClients = selectedClients;
    }

    public YearMonthDay getYearMonthDay() {
        if (yearMonthDay == null) {
            yearMonthDay = new YearMonthDay();
        }
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public Institution getSelectedClinic() {
        return selectedClinic;
    }

    public void setSelectedClinic(Institution selectedClinic) {
        this.selectedClinic = selectedClinic;
    }

    public List<Encounter> getSelectedClientsClinics() {
        if (selectedClientsClinics == null) {
            selectedClientsClinics = fillEncounters(selected, InstitutionType.Clinic, EncounterType.Death, true);
        }
        return selectedClientsClinics;
    }

    public void setSelectedClientsClinics(List<Encounter> selectedClientsClinics) {
        this.selectedClientsClinics = selectedClientsClinics;
    }

    public int getProfileTabActiveIndex() {
        return profileTabActiveIndex;
    }

    public void setProfileTabActiveIndex(int profileTabActiveIndex) {
        this.profileTabActiveIndex = profileTabActiveIndex;
    }

    public EncounterFacade getEncounterFacade() {
        return encounterFacade;
    }

    public EncounterController getEncounterController() {
        return encounterController;
    }

    public boolean isGoingToCaptureWebCamPhoto() {
        return goingToCaptureWebCamPhoto;
    }

    public void setGoingToCaptureWebCamPhoto(boolean goingToCaptureWebCamPhoto) {
        this.goingToCaptureWebCamPhoto = goingToCaptureWebCamPhoto;
    }

    public String getUploadDetails() {
        if (uploadDetails == null || uploadDetails.trim().equals("")) {
            uploadDetails
                    = "client_phn_number" + "\n"
                    + "client_nic_number" + "\n"
                    + "client_title" + "\n"
                    + "client_name" + "\n"
                    + "client_sex" + "\n"
                    + "client_data_of_birth" + "\n"
                    + "client_citizenship" + "\n"
                    + "client_ethnic_group" + "\n"
                    + "client_religion" + "\n"
                    + "client_marital_status" + "\n"
                    + "client_permanent_address" + "\n"
                    + "client_gn_area_name" + "\n"
                    + "client_gn_area_code" + "\n"
                    + "client_mobile_number" + "\n"
                    + "client_home_number" + "\n"
                    + "client_email" + "\n"
                    + "client_registered_at" + "\n";
        }

        return uploadDetails;
    }

    public void setUploadDetails(String uploadDetails) {
        this.uploadDetails = uploadDetails;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public List<Client> getImportedClients() {
        return importedClients;
    }

    public void setImportedClients(List<Client> importedClients) {
        this.importedClients = importedClients;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public AreaController getAreaController() {
        return areaController;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Long getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(Long idFrom) {
        this.idFrom = idFrom;
    }

    public Long getIdTo() {
        return idTo;
    }

    public void setIdTo(Long idTo) {
        this.idTo = idTo;
    }

    public Date getClinicDate() {
        return clinicDate;
    }

    public void setClinicDate(Date clinicDate) {
        this.clinicDate = clinicDate;
    }

    public Boolean getNicExists() {
        return nicExists;
    }

    public void setNicExists(Boolean nicExists) {
        this.nicExists = nicExists;
    }

    public Boolean getPhnExists() {
        return phnExists;
    }

    public void setPhnExists(Boolean phnExists) {
        this.phnExists = phnExists;
    }

    public Boolean getPassportExists() {
        return passportExists;
    }

    public void setPassportExists(Boolean passportExists) {
        this.passportExists = passportExists;
    }

    public Boolean getDlExists() {
        return dlExists;
    }

    public void setDlExists(Boolean dlExists) {
        this.dlExists = dlExists;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public void setInstitutionController(InstitutionController institutionController) {
        this.institutionController = institutionController;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = commonController.startOfTheDay();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        // // System.out.println("getTo");
        // // System.out.println("to = " + toDate);
        if (toDate == null) {
            toDate = commonController.endOfTheDay();
        }
        // // System.out.println("to = " + toDate);
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;

    }

    public String getDateTimeFormat() {
        if (dateTimeFormat == null) {
            dateTimeFormat = "yyyy-MM-dd hh:mm:ss";
        }
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public String getDateFormat() {
        if (dateFormat == null) {
            dateFormat = "yyyy/MM/dd";
        }
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getSearchingLocalReferanceNo() {
        return searchingLocalReferanceNo;
    }

    public void setSearchingLocalReferanceNo(String searchingLocalReferanceNo) {
        this.searchingLocalReferanceNo = searchingLocalReferanceNo;
    }

    public String getSearchingSsNumber() {
        return searchingSsNumber;
    }

    public void setSearchingSsNumber(String searchingSsNumber) {
        this.searchingSsNumber = searchingSsNumber;
    }

    public Boolean getLocalReferanceExists() {
        return localReferanceExists;
    }

    public void setLocalReferanceExists(Boolean localReferanceExists) {
        this.localReferanceExists = localReferanceExists;
    }

    public Boolean getSsNumberExists() {
        return ssNumberExists;
    }

    public void setSsNumberExists(Boolean ssNumberExists) {
        this.ssNumberExists = ssNumberExists;
    }

    public ClientEncounterComponentFormSetController getClientEncounterComponentFormSetController() {
        return clientEncounterComponentFormSetController;
    }

    public void setClientEncounterComponentFormSetController(ClientEncounterComponentFormSetController clientEncounterComponentFormSetController) {
        this.clientEncounterComponentFormSetController = clientEncounterComponentFormSetController;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        selected = getFacade().find(selectedId);
        this.selectedId = selectedId;
    }

    public List<ClientBasicData> getClients() {
        return clients;
    }

    public void setClients(List<ClientBasicData> clients) {
        this.clients = clients;
    }

    public List<ClientBasicData> getSelectedClientsBasic() {
        return selectedClientsBasic;
    }

    public void setSelectedClientsBasic(List<ClientBasicData> selectedClientsBasic) {
        this.selectedClientsBasic = selectedClientsBasic;
    }

    public int getIntNo() {
        return intNo;
    }

    public void setIntNo(int intNo) {
        this.intNo = intNo;
    }

    public List<Encounter> getSelectedClientEncounters() {
        if (selectedClientEncounters == null) {
            selectedClientEncounters = fillEncounters(selected, EncounterType.Test_Enrollment, 5);

        }
        return selectedClientEncounters;
    }

    public void setSelectedClientEncounters(List<Encounter> selectedClientEncounters) {
        this.selectedClientEncounters = selectedClientEncounters;
    }

    public Boolean getEmailExists() {
        return emailExists;
    }

    public void setEmailExists(Boolean emailExists) {
        this.emailExists = emailExists;
    }

    public Boolean getPhone1Exists() {
        return phone1Exists;
    }

    public void setPhone1Exists(Boolean phone1Exists) {
        this.phone1Exists = phone1Exists;
    }

    public Integer getNumberOfPhnToReserve() {
        return numberOfPhnToReserve;
    }

    public void setNumberOfPhnToReserve(Integer numberOfPhnToReserve) {
        this.numberOfPhnToReserve = numberOfPhnToReserve;
    }

    public List<ClientBasicData> getSelectedClientsWithBasicData() {
        return selectedClientsWithBasicData;
    }

    public void setSelectedClientsWithBasicData(List<ClientBasicData> selectedClientsWithBasicData) {
        this.selectedClientsWithBasicData = selectedClientsWithBasicData;
    }

    public List<String> getReservePhnList() {
        return reservePhnList;
    }

    public void setReservePhnList(List<String> reservePhnList) {
        this.reservePhnList = reservePhnList;
    }

    public List<Encounter> getInstitutionCaseEnrollments() {
        institutionCaseEnrollments = new ArrayList<>(getInstitutionCaseEnrollmentMap().values());
        return institutionCaseEnrollments;
    }

    public void setInstitutionCaseEnrollments(List<Encounter> institutionCaseEnrollments) {
        this.institutionCaseEnrollments = institutionCaseEnrollments;
    }

    public List<Encounter> getInstitutionTestEnrollments() {
        institutionTestEnrollments = new ArrayList<>(getInstitutionTestEnrollmentMap().values());
        return institutionTestEnrollments;
    }

    public void setInstitutionTestEnrollments(List<Encounter> institutionTestEnrollments) {
        this.institutionTestEnrollments = institutionTestEnrollments;
    }

    private Map<Long, Encounter> findTodaysInstitutionCaseEnrollmentEncounters() {
        String j = "select c from Encounter c "
                + " where c.retired=false"
                + " and c.institution=:ins "
                + " and c.encounterDate=:d "
                + " and c.encounterType=:t "
                + " order by c.id desc";
        Map m = new HashMap();
        m.put("ins", webUserController.getLoggedUser().getInstitution());
        m.put("d", new Date());
        m.put("t", EncounterType.Case_Enrollment);
        List<Encounter> cs = getEncounterFacade().findByJpql(j, m);
        Map<Long, Encounter> tm = new HashMap<>();
        if (cs != null) {
            for (Encounter c : cs) {
                tm.put(c.getId(), c);
            }
        }
        return tm;
    }

    private Map<Long, Encounter> findTodaysInstitutionTestEnrollmentEncounters() {
        String j = "select c from Encounter c "
                + " where c.retired=false "
                + " and c.institution=:ins "
                + " and c.encounterDate=:d "
                + " and c.encounterType=:t "
                + " order by c.id desc";
        Map m = new HashMap();
        m.put("ins", webUserController.getLoggedUser().getInstitution());
        m.put("d", new Date());
        m.put("t", EncounterType.Test_Enrollment);
        List<Encounter> cs = getEncounterFacade().findByJpql(j, m);
        Map<Long, Encounter> tm = new HashMap<>();
        if (cs != null) {
            for (Encounter c : cs) {
                tm.put(c.getId(), c);
            }
        }
        return tm;
    }

    private void prepareSelectionPrivileges() {
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Moh:
            case Nurse:
            case Phi:
            case Phm:
            case Client:
            case Hospital_Admin:
            case Hospital_User:
                institution = webUserController.getLoggedUser().getInstitution();
                institutionSelectable = false;
                nationalLevel = false;
                break;
            case ChiefEpidemiologist:
            case Epidemiologist:
            case Super_User:
            case System_Administrator:
            case User:
                institution = null;
                institutionSelectable = true;
                nationalLevel = true;
                break;
            case Pdhs:
            case Rdhs:
            case Re:
                institution = null;
                institutionSelectable = true;
                nationalLevel = false;
                break;
            case Lab_Consultant:
            case Lab_Mo:
            case Lab_Mlt:
            case Lab_User:
                institution = null;
                institutionSelectable = true;
                nationalLevel = true;
                break;

        }
    }

    public String toListCases() {
        return "/client/case_list";
    }

    public String toListTests() {
        return "/client/test_list";
    }

    public void fillTestEnrollmentToMark() {
        String j = "select c from Encounter c "
                + " where c.retired=false"
                + " and c.institution=:ins "
                + " and c.encounterDate between :fd and :td "
                + " and c.encounterType=:t "
                + " order by c.id";
        Map m = new HashMap();
        Institution ins = webUserController.getLoggedUser().getInstitution();
        m.put("ins", ins);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("t", EncounterType.Test_Enrollment);
        listedToReceive = getEncounterFacade().findByJpql(j, m);
    }

    public void fillTestList() {
        Map m = new HashMap();
        String j = "select c from Encounter c "
                + " where c.retired=false";
        j += " and c.institution in :inss ";
        m.put("inss", webUserController.getLoggableInstitutions());
        j += " and c.encounterDate between :fd and :td "
                + " and c.encounterType=:t "
                + " order by c.id";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("t", EncounterType.Test_Enrollment);
        testList = getEncounterFacade().findByJpql(j, m);
    }

    public void fillCaseList() {
        Map m = new HashMap();
        String j = "select c from Encounter c "
                + " where c.retired=false";
        if (getInstitutionSelectable()) {

            if (getNationalLevel()) {
                if (getInstitution() == null) {
                } else {
                    j += " and c.institution = :ins ";
                    m.put("ins", getInstitution());
                }
            } else {
                if (getInstitution() == null) {
                    j += " and c.institution in :inss ";
                    m.put("inss", webUserController.getLoggableInstitutions());
                } else {
                    j += " and c.institution = :ins ";
                    m.put("ins", getInstitution());
                }
            }
        } else {
            j += " and c.institution=:ins ";
            m.put("ins", webUserController.getLoggedUser().getInstitution());
        }

        j += " and c.encounterDate between :fd and :td "
                + " and c.encounterType=:t "
                + " order by c.id";

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("t", EncounterType.Case_Enrollment);
        caseList = getEncounterFacade().findByJpql(j, m);
    }

    public Map<Long, Encounter> getInstitutionCaseEnrollmentMap() {
        if (institutionCaseEnrollmentMap == null) {
            institutionCaseEnrollmentMap = findTodaysInstitutionCaseEnrollmentEncounters();
        }
        return institutionCaseEnrollmentMap;
    }

    public void setInstitutionCaseEnrollmentMap(Map<Long, Encounter> institutionCaseEnrollmentMap) {
        this.institutionCaseEnrollmentMap = institutionCaseEnrollmentMap;
    }

    public Map<Long, Encounter> getInstitutionTestEnrollmentMap() {
        if (institutionTestEnrollmentMap == null) {
            institutionTestEnrollmentMap = findTodaysInstitutionTestEnrollmentEncounters();
        }
        return institutionTestEnrollmentMap;
    }

    public void setInstitutionTestEnrollmentMap(Map<Long, Encounter> institutionTestEnrollmentMap) {
        this.institutionTestEnrollmentMap = institutionTestEnrollmentMap;
    }

    public Encounter getSelectedEncounterToMarkTest() {
        return selectedEncounterToMarkTest;
    }

    public void setSelectedEncounterToMarkTest(Encounter selectedEncounterToMarkTest) {
        this.selectedEncounterToMarkTest = selectedEncounterToMarkTest;
    }

    public List<Encounter> getListedToReceive() {
        return listedToReceive;
    }

    public void setListedToReceive(List<Encounter> listedToReceive) {
        this.listedToReceive = listedToReceive;
    }

    public Boolean getInstitutionSelectable() {
        if (institutionSelectable == null) {
            prepareSelectionPrivileges();
        }
        return institutionSelectable;
    }

    public void setInstitutionSelectable(Boolean institutionSelectable) {
        this.institutionSelectable = institutionSelectable;
    }

    public Boolean getNationalLevel() {
        if (nationalLevel == null) {
            prepareSelectionPrivileges();
        }
        return nationalLevel;
    }

    public void setNationalLevel(Boolean nationalLevel) {
        this.nationalLevel = nationalLevel;
    }

    public List<Encounter> getTestList() {
        return testList;
    }

    public void setTestList(List<Encounter> testList) {
        this.testList = testList;
    }

    public List<Encounter> getCaseList() {
        return caseList;
    }

    public void setCaseList(List<Encounter> caseList) {
        this.caseList = caseList;
    }

    public Encounter getSelectedEncounter() {
        return selectedEncounter;
    }

    public void setSelectedEncounter(Encounter selectedEncounter) {
        this.selectedEncounter = selectedEncounter;
    }

    public SmsFacade getSmsFacade() {
        return smsFacade;
    }

    public void setSmsFacade(SmsFacade smsFacade) {
        this.smsFacade = smsFacade;
    }

    public List<InstitutionCount> getLabSummariesToReceive() {
        return labSummariesToReceive;
    }

    public void setLabSummariesToReceive(List<InstitutionCount> labSummariesToReceive) {
        this.labSummariesToReceive = labSummariesToReceive;
    }

    public Institution getReferingInstitution() {
        return referingInstitution;
    }

    public void setReferingInstitution(Institution referingInstitution) {
        this.referingInstitution = referingInstitution;
    }

    public AreaApplicationController getAreaApplicationController() {
        return areaApplicationController;
    }

    public void setAreaApplicationController(AreaApplicationController areaApplicationController) {
        this.areaApplicationController = areaApplicationController;
    }

    public InstitutionApplicationController getInstitutionApplicationController() {
        return institutionApplicationController;
    }

    public void setInstitutionApplicationController(InstitutionApplicationController institutionApplicationController) {
        this.institutionApplicationController = institutionApplicationController;
    }

    public ItemApplicationController getItemApplicationController() {
        return itemApplicationController;
    }

    public void setItemApplicationController(ItemApplicationController itemApplicationController) {
        this.itemApplicationController = itemApplicationController;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public void setUserTransactionController(UserTransactionController userTransactionController) {
        this.userTransactionController = userTransactionController;
    }

    public DesignComponentFormSetController getDesignComponentFormSetController() {
        return designComponentFormSetController;
    }

    public void setDesignComponentFormSetController(DesignComponentFormSetController designComponentFormSetController) {
        this.designComponentFormSetController = designComponentFormSetController;
    }

    public ClientEncounterComponentItemController getClientEncounterComponentItemController() {
        return clientEncounterComponentItemController;
    }

    public void setClientEncounterComponentItemController(ClientEncounterComponentItemController clientEncounterComponentItemController) {
        this.clientEncounterComponentItemController = clientEncounterComponentItemController;
    }

    public PreferenceController getPreferenceController() {
        return preferenceController;
    }

    public void setPreferenceController(PreferenceController preferenceController) {
        this.preferenceController = preferenceController;
    }

    public Institution getContinuedLab() {
        return continuedLab;
    }

    public void setContinuedLab(Institution continuedLab) {
        this.continuedLab = continuedLab;
    }

    public List<Encounter> getSelectedToReceive() {
        return selectedToReceive;
    }

    public void setSelectedToReceive(List<Encounter> selectedToReceive) {
        this.selectedToReceive = selectedToReceive;
    }

    public List<Encounter> getSelectedToConfirm() {
        return selectedToConfirm;
    }

    public void setSelectedToConfirm(List<Encounter> selectedToConfirm) {
        this.selectedToConfirm = selectedToConfirm;
    }

    public List<Encounter> getListedToConfirm() {
        return listedToConfirm;
    }

    public void setListedToConfirm(List<Encounter> listedToConfirm) {
        this.listedToConfirm = listedToConfirm;
    }

    public List<Encounter> getListedToEnterResults() {
        return listedToEnterResults;
    }

    public void setListedToEnterResults(List<Encounter> listedToEnterResults) {
        this.listedToEnterResults = listedToEnterResults;
    }

    public List<Encounter> getListedToReviewResults() {
        return listedToReviewResults;
    }

    public void setListedToReviewResults(List<Encounter> listedToReviewResults) {
        this.listedToReviewResults = listedToReviewResults;
    }

    public List<Encounter> getSelectedToReview() {
        return selectedToReview;
    }

    public void setSelectedToReview(List<Encounter> selectedToReview) {
        this.selectedToReview = selectedToReview;
    }

    public List<Encounter> getListedToPrint() {
        return listedToPrint;
    }

    public void setListedToPrint(List<Encounter> listedToPrint) {
        this.listedToPrint = listedToPrint;
    }

    public List<Encounter> getSelectedToPrint() {
        return selectedToPrint;
    }

    public void setSelectedToPrint(List<Encounter> selectedToPrint) {
        this.selectedToPrint = selectedToPrint;
    }

    public Encounter getLastTest() {
        return lastTest;
    }

    public void setLastTest(Encounter lastTest) {
        this.lastTest = lastTest;
    }

    public Item getSelectedTest() {
        return selectedTest;
    }

    public void setSelectedTest(Item selectedTest) {
        this.selectedTest = selectedTest;
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

    public List<Encounter> getSelectedToDispatch() {
        return selectedToDispatch;
    }

    public void setSelectedToDispatch(List<Encounter> selectedToDispatch) {
        this.selectedToDispatch = selectedToDispatch;
    }

    public List<Encounter> getSelectedToDivert() {
        return selectedToDivert;
    }

    public void setSelectedToDivert(List<Encounter> selectedToDivert) {
        this.selectedToDivert = selectedToDivert;
    }

    public Institution getDivertingLab() {
        return divertingLab;
    }

    public void setDivertingLab(Institution divertingLab) {
        this.divertingLab = divertingLab;
    }

    public Institution getDispatchingLab() {
        return dispatchingLab;
    }

    public void setDispatchingLab(Institution dispatchingLab) {
        this.dispatchingLab = dispatchingLab;
    }

    public String getSerialNoColumn() {
        return serialNoColumn;
    }

    public void setSerialNoColumn(String serialNoColumn) {
        this.serialNoColumn = serialNoColumn;
    }

    public String getResultColumn() {
        return resultColumn;
    }

    public void setResultColumn(String resultColumn) {
        this.resultColumn = resultColumn;
    }

    public String getCtValueColumn() {
        return ctValueColumn;
    }

    public void setCtValueColumn(String ctValueColumn) {
        this.ctValueColumn = ctValueColumn;
    }

    public String getCommentColumn() {
        return commentColumn;
    }

    public void setCommentColumn(String commentColumn) {
        this.commentColumn = commentColumn;
    }

    public Integer getStartRow() {
        return startRow;
    }

    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    public Item getLastTestOrderingCategory() {
        return lastTestOrderingCategory;
    }

    public void setLastTestOrderingCategory(Item lastTestOrderingCategory) {
        this.lastTestOrderingCategory = lastTestOrderingCategory;
    }

    public Item getLastTestPcrOrRat() {
        return lastTestPcrOrRat;
    }

    public void setLastTestPcrOrRat(Item lastTestPcrOrRat) {
        this.lastTestPcrOrRat = lastTestPcrOrRat;
    }

    public List<InstitutionCount> getLabSummariesReceived() {
        return labSummariesReceived;
    }

    public void setLabSummariesReceived(List<InstitutionCount> labSummariesReceived) {
        this.labSummariesReceived = labSummariesReceived;
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

    public List<Encounter> getListReceived() {
        return listReceived;
    }

    public void setListReceived(List<Encounter> listReceived) {
        this.listReceived = listReceived;
    }

    public List<Encounter> getListReviewed() {
        return listReviewed;
    }

    public void setListReviewed(List<Encounter> listReviewed) {
        this.listReviewed = listReviewed;
    }

    public List<Encounter> getListConfirmed() {
        return listConfirmed;
    }

    public void setListConfirmed(List<Encounter> listConfirmed) {
        this.listConfirmed = listConfirmed;
    }

    public List<Encounter> getListPositives() {
        return listPositives;
    }

    public void setListPositives(List<Encounter> listPositives) {
        this.listPositives = listPositives;
    }

    public List<InstitutionCount> getLabSummariesEntered() {
        return labSummariesEntered;
    }

    public void setLabSummariesEntered(List<InstitutionCount> labSummariesEntered) {
        this.labSummariesEntered = labSummariesEntered;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Inner Classes">
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = Client.class)
    public static class ClientControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ClientController controller = (ClientController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "clientController");
            return controller.getClient(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Client) {
                Client o = (Client) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Client.class.getName()});
                return null;
            }
        }

    }

// </editor-fold>
}
