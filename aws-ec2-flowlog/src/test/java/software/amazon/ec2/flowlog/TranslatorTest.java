package software.amazon.ec2.flowlog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import software.amazon.cloudformation.proxy.HandlerErrorCode;

public class TranslatorTest extends TestBase {

    @Test
    public void testCreateModelFromFlowLog() {
        ResourceModel model = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);

        assertThat(model.getId()).isEqualTo(TEST_FLOW_LOG_TO_CWL.flowLogId());
        assertThat(model.getDeliverLogsPermissionArn()).isEqualTo(TEST_FLOW_LOG_TO_CWL.deliverLogsPermissionArn());
        assertThat(model.getLogDestination()).isEqualTo(TEST_FLOW_LOG_TO_CWL.logDestination());
        assertThat(model.getLogDestinationType()).isEqualTo(TEST_FLOW_LOG_TO_CWL.logDestinationTypeAsString());
        assertThat(model.getLogGroupName()).isEqualTo(TEST_FLOW_LOG_TO_CWL.logGroupName());
        assertThat(model.getResourceId()).isEqualTo(TEST_FLOW_LOG_TO_CWL.resourceId());
        assertThat(model.getResourceType()).isEqualTo("NetworkInterface");
        assertThat(model.getTrafficType()).isEqualTo(TEST_FLOW_LOG_TO_CWL.trafficTypeAsString());
    }

    @Test
    public void testGetHandlerErrorForEc2Error() {
        assertThat(Translator.getHandlerErrorForEc2Error("UnauthorizedOperation")).isEqualTo(HandlerErrorCode.AccessDenied);
        assertThat(Translator.getHandlerErrorForEc2Error("FlowLogAlreadyExists")).isEqualTo(HandlerErrorCode.AlreadyExists);
        assertThat(Translator.getHandlerErrorForEc2Error("FlowLogsLimitExceeded")).isEqualTo(HandlerErrorCode.ServiceLimitExceeded);
        assertThat(Translator.getHandlerErrorForEc2Error("InvalidParameter")).isEqualTo(HandlerErrorCode.InvalidRequest);
        assertThat(Translator.getHandlerErrorForEc2Error("InvalidParameterValue")).isEqualTo(HandlerErrorCode.InvalidRequest);
        assertThat(Translator.getHandlerErrorForEc2Error("InvalidFlowLogId.NotFound")).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(Translator.getHandlerErrorForEc2Error("ServiceUnavailable")).isEqualTo(HandlerErrorCode.ServiceInternalError);
        assertThat(Translator.getHandlerErrorForEc2Error("InternalError")).isEqualTo(HandlerErrorCode.ServiceInternalError);
        assertThat(Translator.getHandlerErrorForEc2Error("RequestLimitExceeded")).isEqualTo(HandlerErrorCode.Throttling);
        assertThat(Translator.getHandlerErrorForEc2Error("AnythingElse")).isEqualTo(HandlerErrorCode.GeneralServiceException);
    }

}
