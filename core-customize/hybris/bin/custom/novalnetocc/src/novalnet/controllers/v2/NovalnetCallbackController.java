package novalnet.controllers.v2;

import com.novalnet.facades.NovalnetCallbackFacade;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import de.novalnet.beans.NnCallbackRequestData;
import de.novalnet.beans.NnCallbackResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import novalnet.dto.payment.NnCallbackRequestWsDTO;
import novalnet.dto.payment.NnCallbackResponseWsDTO;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper.DEFAULT_LEVEL;


/**
 * Controller for handling Novalnet webhook callback requests.
 */
@Controller
@RequestMapping("/{baseSiteId}/novalnet")
@ApiVersion("v2")
@Tag(name = "Novalnet Callback")
public class NovalnetCallbackController {
    private static final Logger LOG = Logger.getLogger(NovalnetCallbackController.class);

    @Resource(name = "novalnetCallbackFacade")
    private NovalnetCallbackFacade novalnetCallbackFacade;

    @Resource
    private DataMapper dataMapper;

    @PostMapping("/callback")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(operationId = "novalnetCallback", summary = "Handle Novalnet webhook callback")
    @ApiBaseSiteIdAndUserIdParam
    public NnCallbackResponseWsDTO handleCallback(@Parameter(required = true)
                                                  @RequestBody
                                                  NnCallbackRequestWsDTO wsRequest, @ApiFieldsParam
                                                  @RequestParam(defaultValue = DEFAULT_LEVEL)
                                                  String fields, HttpServletRequest httpRequest) {
        NnCallbackResponseData response = new NnCallbackResponseData();

        try {
            NnCallbackRequestData callbackRequest = dataMapper.map(wsRequest, NnCallbackRequestData.class, fields);

            String result = novalnetCallbackFacade.processCallback(callbackRequest, httpRequest);

            response.setMessage(result);
        } catch (Exception ex) {
            LOG.error("Novalnet callback failed", ex);
            response.setMessage(ex.getMessage());
        }

        return dataMapper.map(response, NnCallbackResponseWsDTO.class, fields);
    }
}
