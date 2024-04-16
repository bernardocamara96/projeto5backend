package aor.paj.service;

import aor.paj.bean.AppConfigurationsBean;
import aor.paj.bean.PermissionBean;
import aor.paj.bean.StatisticsBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.StatisticsDto;
import aor.paj.service.status.Function;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/statistics")
public class StatisticsService {
    @EJB
    StatisticsBean statisticsBean;
    @EJB
    UserBean userBean;
    @EJB
    PermissionBean permissionBean;
    @EJB
    AppConfigurationsBean appConfigurationsBean;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatistics(@HeaderParam("token")String token) {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.GET_STATISTICS)) {
                    StatisticsDto statisticsDto = new StatisticsDto();
                    if (statisticsBean.setStatistics(statisticsDto)) {
                        return Response.status(200).entity(statisticsDto).build();
                    } else return Response.status(400).entity("Error getting the statistics").build();
                } else return Response.status(403).entity("Access denied").build();
            }else return Response.status(401).entity("Session has expired").build();

        } else return Response.status(403).entity("Access denied").build();
    }
}
