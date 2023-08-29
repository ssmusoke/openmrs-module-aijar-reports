package org.openmrs.module.ugandaemrreports.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrreports.model.DashboardReportObject;
import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/dashboardReport", supportedClass = DashboardReportObject.class, supportedOpenmrsVersions = {
        "1.8 - 9.0.*"})
public class DashboardReportObjectResource extends DelegatingCrudResource<DashboardReportObject> {

    @Override
    public DashboardReportObject newDelegate() {
        return new DashboardReportObject();
    }

    @Override
    public DashboardReportObject save(DashboardReportObject dashboardReportObject) {
        return Context.getService(UgandaEMRReportsService.class).saveDashboardReportObject(dashboardReportObject);
    }

    @Override
    public DashboardReportObject getByUniqueId(String uniqueId) {
        DashboardReportObject dashboardReportObject = null;
        Integer id = null;

        dashboardReportObject = Context.getService(UgandaEMRReportsService.class).getDashboardReportObjectByUUID(uniqueId);
        if (dashboardReportObject == null && uniqueId != null) {
            try {
                id = Integer.parseInt(uniqueId);
            } catch (Exception e) {
            }

            if (id != null) {
                dashboardReportObject = Context.getService(UgandaEMRReportsService.class).getDashboardReportObjectById(id);
            }
        }

        return dashboardReportObject;
    }

    @Override
    public NeedsPaging<DashboardReportObject> doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<DashboardReportObject>(new ArrayList<DashboardReportObject>(Context.getService(UgandaEMRReportsService.class)
                .getAllDashboardReportObjects()), context);
    }

    @Override
    public List<Representation> getAvailableRepresentations() {
        return Arrays.asList(Representation.DEFAULT, Representation.FULL);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if (rep instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("name");
            description.addProperty("description");
            description.addProperty("type");
            description.addProperty("columns");
            description.addProperty("rows");
            description.addProperty("aggregator");
            description.addProperty("report_request_object");
            description.addSelfLink();
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("name");
            description.addProperty("description");
            description.addProperty("type");
            description.addProperty("columns");
            description.addProperty("rows");
            description.addProperty("aggregator");
            description.addProperty("report_request_object");
            description.addProperty("creator", Representation.REF);
            description.addProperty("dateCreated");
            description.addProperty("changedBy", Representation.REF);
            description.addProperty("dateChanged");
            description.addProperty("voidedBy", Representation.REF);
            description.addProperty("dateVoided");
            description.addProperty("voidReason");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("name");
            description.addProperty("description");
            description.addProperty("type");
            description.addProperty("columns");
            description.addProperty("rows");
            description.addProperty("aggregator");
            description.addProperty("report_request_object");
            description.addSelfLink();
            return description;
        }
        return null;
    }



    @Override
    protected void delete(DashboardReportObject dashboardReportObject, String s, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public void purge(DashboardReportObject dashboardReportObject, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("name");
        description.addProperty("description");
        description.addProperty("type");
        description.addProperty("columns");
        description.addProperty("rows");
        description.addProperty("aggregator");
        description.addProperty("report_request_object");

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {

        return new NeedsPaging<DashboardReportObject>(new ArrayList<>(), context);
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty()).property("name", new StringProperty()).property("rows", new StringProperty())
                    .property("description", new StringProperty()).property("type", new StringProperty()).property("columns", new StringProperty())
                    .property("rows", new StringProperty()).property("aggregator", new StringProperty()).property("report_request_object", new StringProperty());
        }
        if (rep instanceof DefaultRepresentation) {
//            model.property("generatorProfile", new RefProperty("#/definitions/SyncFhirProfileGetRef"));

        } else if (rep instanceof FullRepresentation) {
//            model.property("generatorProfile", new RefProperty("#/definitions/SyncFhirProfileGetRef"));
        }
        return model;
    }

    @Override
    public Model getCREATEModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty()).property("name", new StringProperty()).property("rows", new StringProperty())
                    .property("description", new StringProperty()).property("type", new StringProperty()).property("columns", new StringProperty())
                    .property("rows", new StringProperty()).property("aggregator", new StringProperty()).property("report_request_object", new StringProperty());
        }
        if (rep instanceof DefaultRepresentation) {
            model.property("creator", new RefProperty("#/definitions/UserGetRef"))
                    .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));;

        } else if (rep instanceof FullRepresentation) {
            model.property("creator", new RefProperty("#/definitions/UserGetRef"))
                    .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
        }
        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        return new ModelImpl().property("uuid", new StringProperty()).property("name", new StringProperty())
        .property("description", new StringProperty())
        .property("type", new StringProperty())
        .property("columns", new StringProperty())
        .property("rows", new StringProperty())
        .property("aggregator", new StringProperty())
        .property("report_request_object", new StringProperty())
                .property("creator", new RefProperty("#/definitions/UserGetRef"))
                .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
    }
}
