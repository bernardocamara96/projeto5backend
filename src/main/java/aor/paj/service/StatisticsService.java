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
import org.apache.logging.log4j.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

    private static final Logger logger=LogManager.getLogger(StatisticsService.class);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatistics(@HeaderParam("token")String token) throws UnknownHostException {
        if (userBean.tokenValidator(token)) {
            if(appConfigurationsBean.validateTimeout(token)) {
                if (permissionBean.getPermission(token, Function.GET_STATISTICS)) {
                    StatisticsDto statisticsDto = new StatisticsDto();
                    if (statisticsBean.setStatistics(statisticsDto)) {
                        logger.info(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" requested the app statistics");
                        return Response.status(200).entity(statisticsDto).build();
                    } else {
                        logger.error(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" had an error requesting the statistics");
                        return Response.status(400).entity("Error getting the statistics").build();
                    }
                } else{
                    logger.warn(InetAddress.getLocalHost().getHostAddress()+"  "+userBean.findUsernameByToken(token)+" had access denied requesting the app statistics");
                    return Response.status(403).entity("Access denied").build();
                }
            }else {
                logger.warn(InetAddress.getLocalHost().getHostAddress()+" had session expired when requesting the app statistics");
                return Response.status(401).entity("Session has expired").build();
            }

        } else {
            logger.warn(InetAddress.getLocalHost().getHostAddress()+" had access denied when requesting the app statistics");
            return Response.status(403).entity("Access denied").build();
        }
    }
}
