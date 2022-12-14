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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import lk.gov.health.phsp.ejb.CovidDataHolder;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.EncounterFacade;
import lk.gov.health.phsp.facade.NumbersFacade;
import lk.gov.health.phsp.pojcs.CovidData;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.OrderingCategoryResults;
import org.joda.time.DateTimeComparator;
import org.json.JSONObject;

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
    ItemApplicationController itemApplicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    AreaApplicationController areaApplicationController;
    @Inject
    CovidDataHolder covidDataHolder;
    @Inject
    WebUserController webuserController;

//    AreaController
    @Inject
    private AreaController areaController;

    private Date lastUpdatedAt;
    private Long todayPcr;
    private Long todayRat;
    private Long todayPositivePcr;
    private Long todayPositiveRat;
    private Long yesterdayPcr;
    private Long yesterdayRat;
    private Long yesterdayPositivePcr;
    private Long yesterdayPositiveRat;
    private Long yesterdayTests;
    private Long todaysTests;
    private Long firstContactCount;
    private Long communityRandomCount;
    private Long foreignCount;
    private Long hospitalCount;
    private Long otherCount;
    private String todayPcrPositiveRate;
    private String todayRatPositiveRate;
    private String yesterdayPcrPositiveRate;
    private String yesterdayRatPositiveRate;

//  Json data of PCR and RAT positive cases to be used to generate chart
    private JSONObject pcrPositiveCasesJSON;
    private JSONObject ratPositiveCasesJSON;

//  Round double values to two decimal format
    private static DecimalFormat df = new DecimalFormat("0.0");

    private List<InstitutionCount> orderingCounts;
    List<CovidData> covidDatas;

    Item testType;
    Item orderingCat;
    Item pcr;
    Item rat;

    Boolean dashboardPrepared;

    /**
     * Creates a new instance of DashboardController
     */
    public DashboardApplicationController() {
    }

    @PostConstruct
    public void updateDashboard() {
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        lastUpdatedAt = new Date();
        
        c.add(Calendar.DATE, -1);

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

        todayPcr = getOrderCount(null, todayStart, now,
                itemApplicationController.getPcr(), null, null, null);
        todayRat = getOrderCount(null, todayStart, now,
                itemApplicationController.getRat(), null, null, null);
        yesterdayPcr = getOrderCount(null, yesterdayStart, yesterdayEnd,
                itemApplicationController.getPcr(), null, null, null);
        yesterdayRat = getOrderCount(null, yesterdayStart, yesterdayEnd,
                itemApplicationController.getRat(), null, null, null);

        todayPositivePcr = getConfirmedCount(null,
                todayStart,
                now,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        todayPositiveRat = getConfirmedCount(null,
                todayStart,
                now,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

        yesterdayPositivePcr = getConfirmedCount(null,
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        yesterdayPositiveRat = getConfirmedCount(null,
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        orderingCounts = listOrderingCategoryCounts(null, yesterdayStart, now, null, null, null, null);

//        Get the PCR Positive cases within the last 14 days
        Map<String, String> pcrPositiveSeries = this.getSeriesOfCases(
                now,
                30,
                this.itemApplicationController.getPcr(),
                this.itemApplicationController.getPcrPositive(),
                null

        );

//        Get the RAT positive cases within the last 14 days
        Map<String, String> ratPositiveSeries = this.getSeriesOfCases(
                now,
                30,
                this.itemApplicationController.getRat(),
                this.itemApplicationController.getPcrPositive(),
                null
        );


        this.pcrPositiveCasesJSON = new JSONObject(pcrPositiveSeries);
        this.ratPositiveCasesJSON = new JSONObject(ratPositiveSeries);

//      This will return today's pcr positive rate as a percentage
        if (this.todayPcr != 0) {
        	double tempRate = ((double) this.todayPositivePcr/this.todayPcr) * 100;
        	this.todayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayPcrPositiveRate = "0.0%";
        }

//		This will return today's rat positive rate as a percentage
        if (this.todayRat != 0) {
        	double tempRate = ((double) this.todayPositiveRat/this.todayRat) * 100;
        	this.todayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayRatPositiveRate = "0.0%";
        }

//      This will return yesterday's pcr positive rate as a percentage
        if (this.yesterdayPcr != 0) {
        	double tempRate = ((double) this.yesterdayPositivePcr/this.yesterdayPcr) * 100;
        	this.yesterdayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayPcrPositiveRate = "0.0%";
        }

//		This will return yesterday's RAT positive rate as a percentage
        if (this.yesterdayRat != 0) {
        	double tempRate = ((double) this.yesterdayPositiveRat/this.yesterdayRat) * 100;
        	this.yesterdayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayRatPositiveRate = "0.0%";

        }
        categorizeOrderingCounts(orderingCounts);
    }

    private void categorizeOrderingCounts(List<InstitutionCount> ocs) {
        firstContactCount = 0l;
        communityRandomCount = 0l;
        foreignCount = 0l;
        hospitalCount = 0l;
        otherCount = 0l;
        for (InstitutionCount oc : ocs) {
            String code = "";
            if (oc.getItemValue() != null && oc.getItemValue().getCode() != null) {
                code = oc.getItemValue().getCode();
            }
            if (oc.getCount() == null) {
                continue;
            }
            switch (code) {
                case "exit_for_first_contacts":
                case "first_contact_non_exit":
                    firstContactCount += oc.getCount();
                    break;
                case "community_screening_random":
                case "workplace_random":
                    communityRandomCount += oc.getCount();
                    break;
                case "overseas_returnees_and_foreign_travelers_initial_or_arrival":
                case "overseas_returnees_and_foreign_travelers_exit":
                    foreignCount += oc.getCount();
                    break;

                case "routine_for_procedures":
                case "opd_symptomatic":
                case "opd_inward_symptomatic":
                    hospitalCount += oc.getCount();
                    break;

                case "postmortem_screening":

                case "workplace_routine":
                case "covid_19_test_ordering_category_other":
                case "":
                    otherCount += oc.getCount();
                    break;
            }
        }
    }

    public List<InstitutionCount> listOrderingCategoryCounts(
            Institution ins,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.pcrOrderingCategory, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (ins != null) {
            j += " and c.institution=:ins ";
            m.put("ins", ins);
        }

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
        j += " group by c.pcrOrderingCategory";
        List<Object> objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        List<InstitutionCount> tics = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                tics.add(ic);
            }
        }
        return tics;
    }

    // This will generate the list of investigations done by insitutions in a given RDHS area

    public Map<String, List<String>> generateRdhsInvestigationHashmap(
            List<Institution> myInstitutions
    ) {

        Map<String, List<String>> hashMap = new HashMap<>();

        Date todayStart = CommonController.startOfTheDate(CommonController.getYesterday());
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        for (Institution ins:myInstitutions) {
            System.out.println("mohArea = " + ins.getName());
            List<String> tempList = new ArrayList<>();
            Long tempTodayPcr = this.getOrderCount(
                    ins,
                    todayStart,
                    now,
                    itemApplicationController.getPcr(),
                    null,
                    null,
                    null
            );
            System.out.println("tempTodayPcr = " + tempTodayPcr);
            Long tempTodayRat = this.getOrderCount(
                    ins,
                    todayStart,
                    now,
                    itemApplicationController.getRat(),
                    null,
                    null,
                    null
            );
            System.out.println("tempTodayRat = " + tempTodayRat);
            tempList.add(tempTodayPcr.toString());
            tempList.add(tempTodayRat.toString());
            hashMap.put(ins.getName(), tempList);
        }

        return hashMap;
    }

    public Long getOrderCount(Institution ins,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (ins != null) {
            j += " and c.institution=:ins ";
            m.put("ins", ins);
        }

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
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

//  This will return the count of investigations pending dispatch
public Long samplesAwaitingDispatch(
        Area area,
        Date fromDate,
        Date toDate,
        Institution institution,
        Item testType
) {
    Map hashMap = new HashMap();
    String jpql = "select count(c) "
            + " from Encounter c "
            + " where c.retired=:ret "
            + " and c.encounterType=:type "
            + " and c.encounterDate between :fd and :td "
            + " and c.pcrTestType=:testType ";

    if (institution != null){
        jpql += " and c.institution=:ins ";
         hashMap.put("ins", institution);
    }

    if (testType != null) {
        jpql += " and c.pcrTestType=:tt";
        hashMap.put("tt", testType);
    }

    if( area != null) {
        if (null != area.getType()) switch (area.getType()) {
            case District:
                jpql += "and c.institution.district=:district ";
                hashMap.put("district", area.getDistrict());
                break;
            case RdhsAra:
                jpql += " and (c.institution.rdhsArea=:rdArea or c.institution.district=:district) ";
                hashMap.put("rdArea", area);
                hashMap.put("district", area.getDistrict());
                break;
            case PdhsArea:
                jpql += " and (c.institution.pdhsArea=:pdArea or c.institution.province=:province) ";
                hashMap.put("phArea", area);
                hashMap.put("province", area.getProvince());
                break;
            case Province:
                jpql += " and c.institution.province=:province ";
                hashMap.put("province", area.getProvince());
                break;
            case MOH:
                jpql += " and (c.institution.mohArea=:mohArea) ";
                hashMap.put("mohArea", area);
                break;
            default:
                break;
        }
    }



    jpql += " and (c.sentToLab is null or c.sentToLab = :sl) ";

    hashMap.put("ret", false);
    hashMap.put("type", EncounterType.Test_Enrollment);
    hashMap.put("fd", fromDate);
    hashMap.put("sl", false);
    hashMap.put("td", toDate);
    hashMap.put("testType", testType);


    return encounterFacade.findLongByJpql(jpql, hashMap, TemporalType.DATE);

}

//This function will return a series of cases depending on a provided time period
public Map<String, String> getSeriesOfCases(
        Date fromDate,
        int duration,
        Item testType,
        Item result,
        Institution institution
) {
    int MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

    Map<String, String> hashMap = new HashMap<String, String>();
    Date startOfTheDate = CommonController.startOfTheDate(fromDate);

    if (duration < 1) {
        hashMap.put("", "");
    } else {
        for (int i = 0; i <= duration; i++) {
            int dayCount = i +1;
            Date currentDate = new Date(startOfTheDate.getTime() - (long) MILLIS_IN_A_DAY * dayCount);
            Date endDate = CommonController.endOfTheDate(currentDate);
            Long positive_cases = this.getConfirmedCount(
                    null,
                    currentDate,
                    endDate,
                    testType,
                    null,
                    result,
                    institution);
            hashMap.put(currentDate.toString(), Long.toString(positive_cases));
        }
    }
    return hashMap;
}

//  This will return count of Investigations where MOH area is not given
    public Long getOrderCountWithoutMoh(Area area,
                              Date fromDate,
                              Date toDate,
                              Item testType,
                              Item orderingCategory,
                              Item result,
                              Institution lab) {

        Map hashMap = new HashMap();

        String jpql = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";

        hashMap.put("ret", false);

        jpql += " and c.encounterType=:etype ";

        hashMap.put("etype", EncounterType.Test_Enrollment);

        if (area != null) {
            if (area.getType() == AreaType.RdhsAra) {
                jpql += " and (c.institution.rdhsArea=:area or c.institution.district=:dis) ";
                hashMap.put("area", area);
                hashMap.put("dis", area.getDistrict());
            } else if (area.getType() == AreaType.Province) {
                jpql += " and (c.institution.pdhsArea=:area or c.institution.province=:pro) ";
                hashMap.put("area", area);
                hashMap.put("pro", area.getProvince());
            }
            jpql += " and (c.client.person.mohArea=null) ";
        }

        jpql += " and c.createdAt between :fd and :td ";
        hashMap.put("fd", fromDate);
        hashMap.put("td", toDate);

        if (testType != null) {
            jpql += " and c.pcrTestType=:tt ";
            hashMap.put("tt", testType);
        }
        if (orderingCategory != null) {
            jpql += " and c.pcrOrderingCategory=:oc ";
            hashMap.put("oc", orderingCategory);
        }
        if (result != null) {
            jpql += " and c.pcrResult=:result ";
            hashMap.put("result", result);
        }
        if (lab != null) {
            jpql += " and c.referalInstitution=:ri ";
            hashMap.put("ri", lab);
        }
        return encounterFacade.findLongByJpql(jpql, hashMap, TemporalType.TIMESTAMP);
    }

    public Long getOrderCountArea(Area area,
            Date fromDate,
            Date toDate,
            Item pcrOrRat,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (area != null) {
            if (area.getType() == AreaType.RdhsAra) {
                j += " and (c.institution.rdhsArea=:area or c.institution.district=:dis) ";
                m.put("area", area);
                m.put("dis", area.getDistrict());
            } else if (area.getType() == AreaType.Province) {
                j += " and (c.institution.pdhsArea=:area or c.institution.province=:pro) ";
                m.put("area", area);
                m.put("pro", area.getProvince());
            } else if (area.getType() == AreaType.MOH) {
                j += " and (c.institution.mohArea=:area) ";
                m.put("area", area);
            }
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (pcrOrRat != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", pcrOrRat);
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
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getProvincialOrderCountArea(Area pdArea,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (pdArea != null) {
            if (pdArea.getType() == AreaType.RdhsAra) {
                j += " and c.institution.rdhsArea=:area ";
                m.put("area", pdArea);
            } else if (pdArea.getType() == AreaType.Province) {
                j += " and c.institution.pdhsArea=:area ";
                m.put("area", pdArea);
            }
        }

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
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getConfirmedCountByInstitution(Institution ins,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (ins != null) {
            j += " and c.institution=:ins ";
            m.put("ins", ins);
        }

        j += " and c.resultConfirmedAt between :fd and :td ";
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
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getConfirmedCount(Area area,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (area != null && area.getType() != null) {
            if (null != area.getType()) {
                switch (area.getType()) {
                    case District:
                        j += " and c.client.person.district=:dis ";
                        m.put("dis", area);
                        break;
                    case Province:
                        j += " and c.client.person.district.province=:pro ";
                        m.put("pro", area);
                        break;
                    case MOH:
                        j += " and c.client.person.mohArea=:moh ";
                        m.put("moh", area);
                        break;
                    default:
                        break;
                }
            }
        }

        j += " and c.resultConfirmedAt between :fd and :td ";
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
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getConfirmedCountArea(Area area,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        if (area != null) {
            if (area.getType() == AreaType.RdhsAra) {
                j += " and c.institution.rdhsArea=:area ";
                m.put("area", area);
            } else if (area.getType() == AreaType.Province) {
                j += " and c.institution.pdhsArea=:area ";
                m.put("area", area);
            }
        }

        j += " and c.resultConfirmedAt between :fd and :td ";
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
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getPositivePcr(Date fd, Date td) {
        String j = "select count(e "
                + " from Encounter e "
                + " where (e.retired is null or e.retired=false) "
                + " and e.pcrTestType=:pcr "
                + " and e.resultConfirmedAt between :fd and :td "
                + " and e.pcrResult=:pos ";
        Map m;
        m = new HashMap();
        m.put("fd", fd);
        m.put("td", td);
        m.put("pos", itemApplicationController.getPcrPositive());
        m.put("pcr", itemApplicationController.getPcr());
        return encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Boolean getDashboardPrepared() {
        if (dashboardPrepared == null) {
            updateDashboard();
            dashboardPrepared = true;
        }
        return dashboardPrepared;
    }

    public Long getTodayPcr() {
        if (todayPcr == null) {
            updateDashboard();
        }
        return todayPcr;
    }

    public Long getTodayRat() {
        if (todayRat == null) {
            updateDashboard();
        }
        return todayRat;
    }

    public Long getTodayPositivePcr() {
        if (todayPositivePcr == null) {
            updateDashboard();
        }
        return todayPositivePcr;
    }

    public Long getTodayPositiveRat() {
        if (todayPositiveRat == null) {
            updateDashboard();
        }
        return todayPositiveRat;
    }

//    Getter for cases JSON data
    public JSONObject getPcrPositiveCasesJSON() {
        return pcrPositiveCasesJSON;
    }

    public JSONObject getRatPositiveCasesJSON() {
        return ratPositiveCasesJSON;
    }

    public Long getYesterdayPcr() {
        if (yesterdayPcr == null) {
            updateDashboard();
        }
        return yesterdayPcr;
    }




    /**
	 * @return the todayPcrPositiveRate
	 */
	public String getTodayPcrPositiveRate() {
		if (this.todayPcr == null) {
			this.updateDashboard();
		}
		return todayPcrPositiveRate;
	}

	/**
	 * @param todayPcrPositiveRate the todayPcrPositiveRate to set
	 */
	public void setTodayPcrPositiveRate(String todayPcrPositiveRate) {
		this.todayPcrPositiveRate = todayPcrPositiveRate;
	}

	/**
	 * @return the todayRatPositiveRate
	 */
	public String getTodayRatPositiveRate() {
		if (this.todayRat == null) {
			this.updateDashboard();
		}
		return todayRatPositiveRate;
	}

	/**
	 * @param todayRatPositiveRate the todayRatPositiveRate to set
	 */
	public void setTodayRatPositiveRate(String todayRatPositiveRate) {
		this.todayRatPositiveRate = todayRatPositiveRate;
	}

	/**
	 * @return the yesterdayPcrPositiveRate
	 */
	public String getYesterdayPcrPositiveRate() {
		if (this.yesterdayPcr == null) {
			this.updateDashboard();
		}
		return yesterdayPcrPositiveRate;
	}

	/**
	 * @param yesterdayPcrPositiveRate the yesterdayPcrPositiveRate to set
	 */
	public void setYesterdayPcrPositiveRate(String yesterdayPcrPositiveRate) {
		this.yesterdayPcrPositiveRate = yesterdayPcrPositiveRate;
	}

	/**
	 * @return the yesterdayRatPositiveRate
	 */
	public String getYesterdayRatPositiveRate() {
		if (this.yesterdayRat == null) {
			this.updateDashboard();
		}
		return yesterdayRatPositiveRate;
	}

	/**
	 * @param yesterdayRatPositiveRate the yesterdayRatPositiveRate to set
	 */
	public void setYesterdayRatPositiveRate(String yesterdayRatPositiveRate) {
		this.yesterdayRatPositiveRate = yesterdayRatPositiveRate;
	}

	public void setYesterdayPcr(Long yesterdayPcr) {
        this.yesterdayPcr = yesterdayPcr;
    }

    public Long getYesterdayRat() {
        if (yesterdayRat == null) {
            updateDashboard();
        }
        return yesterdayRat;
    }

    public void setYesterdayRat(Long yesterdayRat) {
        this.yesterdayRat = yesterdayRat;
    }

    public Long getYesterdayPositivePcr() {
        if (yesterdayPositivePcr == null) {
            updateDashboard();
        }
        return yesterdayPositivePcr;
    }

    public void setYesterdayPositivePcr(Long yesterdayPositivePcr) {
        this.yesterdayPositivePcr = yesterdayPositivePcr;
    }

    public Long getYesterdayPositiveRat() {
        if (yesterdayPositiveRat == null) {
            updateDashboard();
        }
        return yesterdayPositiveRat;
    }

    public void setYesterdayPositiveRat(Long yesterdayPositiveRat) {
        this.yesterdayPositiveRat = yesterdayPositiveRat;
    }

    public Long getYesterdayTests() {
        if (getYesterdayPcr() != null && getYesterdayRat() != null) {
            yesterdayTests = getYesterdayPcr() + getYesterdayRat();
        } else if (getYesterdayPcr() != null) {
            yesterdayTests = getYesterdayPcr();
        } else if (getYesterdayRat() != null) {
            yesterdayTests = getYesterdayRat();
        } else {
            yesterdayTests = 0l;
        }
        return yesterdayTests;
    }

    public Long getTodaysTests() {
        if (getTodayPcr() != null && getTodayRat() != null) {
            todaysTests = getTodayPcr() + getTodayRat();
        } else if (getTodayPcr() != null) {
            todaysTests = getTodayPcr();
        } else if (getTodayRat() != null) {
            todaysTests = getTodayRat();
        } else {
            todaysTests = 0l;
        }
        return todaysTests;
    }

    public void setTodaysTests(Long todaysTests) {
        this.todaysTests = todaysTests;
    }

    public List<InstitutionCount> getOrderingCounts() {
        if (orderingCounts == null) {
            updateDashboard();
        }
        return orderingCounts;
    }

    public void setOrderingCounts(List<InstitutionCount> orderingCounts) {
        this.orderingCounts = orderingCounts;
    }

    public Long getFirstContactCount() {
        return firstContactCount;
    }

    public Long getCommunityRandomCount() {
        return communityRandomCount;
    }

    public Long getForeignCount() {
        return foreignCount;
    }

    public Long getHospitalCount() {
        return hospitalCount;
    }

    public Long getOtherCount() {
        return otherCount;
    }

    CovidData findMyCovidData(WebUser user) {
        CovidData mcd = null;
        if (user == null) {
            return mcd;
        }
        if (user.getWebUserRoleLevel() == null) {
            return mcd;
        }
        Date dbDate = CommonController.getYesterday();
        for (CovidData cd : getCovidDatas()) {
            int dateCorrect = DateTimeComparator.getDateOnlyInstance().compare(dbDate, cd.getDate());
            if (dateCorrect == 0) {
                switch (user.getWebUserRoleLevel()) {
                    case Hospital:
                        if (cd.getType() == WebUserRoleLevel.Hospital) {
                            if (cd.getInstitution().equals(user.getInstitution())) {
                                mcd = cd;
                            }
                        }
                        break;
                    case Lab:
                        if (cd.getType() == WebUserRoleLevel.Lab) {
                            if (cd.getInstitution().equals(user.getInstitution())) {
                                mcd = cd;
                            }
                        }
                        break;
                    case Moh:
                        if (cd.getType() == WebUserRoleLevel.Moh) {
                            if(cd.getArea()==null ||  user.getInstitution()==null|| user.getInstitution().getMohArea()==null){
                                continue;
                            }
                            if (cd.getArea().equals(user.getInstitution().getMohArea())) {
                                mcd = cd;
                            }
                        }
                        break;
                    case National:
                        if (cd.getType() == WebUserRoleLevel.National) {
                            mcd = cd;
                        }
                        break;
                    case National_Lab:
                        if (cd.getType() == WebUserRoleLevel.National_Lab) {
                            mcd = cd;
                        }
                        break;
                    case Provincial:
                        if (cd.getType() == WebUserRoleLevel.Provincial) {
                            if (cd.getArea().equals(user.getInstitution().getPdhsArea())) {
                                mcd = cd;
                            }
                        }
                        break;
                    case Regional:
                        if (cd.getType() == WebUserRoleLevel.Regional) {
                            if (cd.getArea().equals(user.getInstitution().getRdhsArea())) {
                                mcd = cd;
                            }
                        }
                        break;
                }
            }
        }
        if (mcd == null) {
            mcd = generateCovidData(dbDate, user);
            getCovidDatas().add(mcd);
        }
        return mcd;
    }

    public CovidData generateCovidData(Date date, WebUser wu) {
        CovidData cd = null;
        WebUserRoleLevel rl = wu.getWebUserRoleLevel();
        switch (rl) {
            case Hospital:

            case Lab:

            case Moh:
                cd = generateMohCovidData(wu.getInstitution().getMohArea(), date);
                break;
            case National:

            case National_Lab:
                cd = generateNationalCovidData(date);


            case Provincial:

            case Regional:
        }
        if (cd == null) {
            cd = new CovidData();
        }
        return cd;
    }

    public CovidData generateMohCovidData(Area a, Date date) {
        CovidData cd = new CovidData();
        cd.setDate(date);
        cd.setArea(a);
        cd.setType(WebUserRoleLevel.Moh);
        cd.setFrom(CommonController.startOfTheDate(date));
        cd.setTo(CommonController.endOfTheDate(date));
        cd.setRatPositives(getConfirmedCount(a,
                cd.getFrom(),
                cd.getTo(),
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null));
        cd.setPcrPositives(getConfirmedCount(a,
                cd.getFrom(),
                cd.getTo(),
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null));
        cd.setDailyPositives(cd.getPcrPositives() + cd.getRatPositives());
        List<OrderingCategoryResults> ocrs = new ArrayList<>();
        for (Item oc : itemApplicationController.getCovidTestOrderingCategories()) {
            OrderingCategoryResults r = new OrderingCategoryResults();
            r.setOrderingCategory(oc);
            r.setOrdered(getConfirmedCount(
                    a,
                    cd.getFrom(),
                    cd.getTo(),
                    null,
                    oc,
                    null,
                    null));
            r.setPositives(getConfirmedCount(
                    a,
                    cd.getFrom(),
                    cd.getTo(),
                    null,
                    oc,
                    itemApplicationController.getPcrPositive(),
                    null));
            if (r.getOrdered() != null && r.getPositives() != null && r.getOrdered() > 0) {
                r.setPositivityRate((double)r.getPositives()/r.getOrdered());
                ocrs.add(r);
            }
        }
        cd.setOrderingCategoryResults(ocrs);
        cd.setSubAreaCounts(countOfResultsByGnArea(a, cd.getFrom(), cd.getTo(), null, null, 5));
        return cd;
    }

    public CovidData generateNationalCovidData(Date date) {
        CovidData cd = new CovidData();
        cd.setDate(date);
        cd.setType(WebUserRoleLevel.National);
        cd.setFrom(CommonController.startOfTheDate(date));
        cd.setTo(CommonController.endOfTheDate(date));
        cd.setRatPositives(getConfirmedCount(null,
                cd.getFrom(),
                cd.getTo(),
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null));
        cd.setPcrPositives(getConfirmedCount(null,
                cd.getFrom(),
                cd.getTo(),
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null));
        cd.setDailyPositives(cd.getPcrPositives() + cd.getRatPositives());
        List<OrderingCategoryResults> ocrs = new ArrayList<>();
        for (Item oc : itemApplicationController.getCovidTestOrderingCategories()) {
            OrderingCategoryResults r = new OrderingCategoryResults();
            r.setOrderingCategory(oc);
            r.setOrdered(getConfirmedCount(
                    null,
                    cd.getFrom(),
                    cd.getTo(),
                    null,
                    oc,
                    null,
                    null));
            r.setPositives(getConfirmedCount(
                    null,
                    cd.getFrom(),
                    cd.getTo(),
                    null,
                    oc,
                    itemApplicationController.getPcrPositive(),
                    null));
            if (r.getOrdered() != null && r.getPositives() != null && r.getOrdered() > 0) {
                r.setPositivityRate((double)r.getPositives()/r.getOrdered());
                ocrs.add(r);
            }
        }
        cd.setOrderingCategoryResults(ocrs);
        cd.setSubAreaCounts(countOfResultsByProvince(cd.getFrom(), cd.getTo(), null, null, 5));
        return cd;
    }


    public List<CovidData> getCovidDatas() {
        if (covidDatas == null) {
            covidDatas = new ArrayList<>();
        }
        return covidDatas;
    }

    public void setCovidDatas(List<CovidData> covidDatas) {
        this.covidDatas = covidDatas;
    }


    public List<InstitutionCount> countOfResultsByGnArea(Area moh,
            Date from,
            Date to,
            Item orderingCategory,
            Item result,
            Integer numberOfResults
            ) {
        List<InstitutionCount> ics ;
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.gnArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);

        j += " and (c.client.person.mohArea=:moh) ";
        m.put("moh",moh);

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", from);
        m.put("td", to);
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
        j += " group by c.client.person.gnArea"
                + " order by count(c) desc ";

        ics = new ArrayList<>();

        List<Object> objCounts;
        if(numberOfResults!=null){
            objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP,numberOfResults);
        }else{
            objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        }

        if (objCounts == null || objCounts.isEmpty()) {
            return ics;
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                ics.add(ic);
            }
        }
        return ics;
    }

    
    
    public List<InstitutionCount> countOfResultsByProvince(
            Date from,
            Date to,
            Item orderingCategory,
            Item result,
            Integer numberOfResults
            ) {
        List<InstitutionCount> ics ;
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.district.province, c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", EncounterType.Test_Enrollment);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", from);
        m.put("td", to);
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
        j += " group by c.client.person.district.province "
                + " order by count(c) desc ";

        ics = new ArrayList<>();

        List<Object> objCounts;
        if(numberOfResults!=null){
            objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP,numberOfResults);
        }else{
            objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        }

        if (objCounts == null || objCounts.isEmpty()) {
            return ics;
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                ics.add(ic);
            }
        }
        return ics;
    }

    public Date getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Date lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

}
