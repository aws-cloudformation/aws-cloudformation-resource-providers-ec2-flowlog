package software.amazon.ec2.flowlog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.ec2.model.LogDestinationType;
import software.amazon.awssdk.services.ec2.model.TrafficType;
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
    public void testCreateModelFromFlowLog_WithOptionalFields() {
        ResourceModel model = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_S3);

        assertThat(model.getId()).isEqualTo(TEST_FLOW_LOG_TO_S3.flowLogId());
        assertThat(model.getLogDestination()).isEqualTo(TEST_FLOW_LOG_TO_S3.logDestination());
        assertThat(model.getLogDestinationType()).isEqualTo(TEST_FLOW_LOG_TO_S3.logDestinationTypeAsString());
        assertThat(model.getLogFormat()).isEqualTo(TEST_FLOW_LOG_TO_S3.logFormat());
        assertThat(model.getResourceId()).isEqualTo(TEST_FLOW_LOG_TO_S3.resourceId());
        assertThat(model.getResourceType()).isEqualTo("VPC");
        assertThat(model.getTrafficType()).isEqualTo(TEST_FLOW_LOG_TO_S3.trafficTypeAsString());
        assertThat(model.getMaxAggregationInterval()).isEqualTo(TEST_FLOW_LOG_TO_S3.maxAggregationInterval());
        assertThat(model.getTags()).isEqualTo(
            Translator.createCfnTagsFromSdkTags(TEST_FLOW_LOG_TO_S3.tags()));
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

    @Test
    public void testCreateCfnTagsFromSdkTags() {
        software.amazon.ec2.flowlog.Tag cfnTag = software.amazon.ec2.flowlog.Tag.builder()
            .key("k")
            .value("v")
            .build();
        software.amazon.awssdk.services.ec2.model.Tag sdkTag = software.amazon.awssdk.services.ec2.model.Tag.builder()
            .key("k")
            .value("v")
            .build();
        assertThat(Translator.createCfnTagsFromSdkTags(Arrays.asList(sdkTag))).isEqualTo(Arrays.asList(cfnTag));
    }

    @Test
    public void testCreateCfnTagsFromSdkTags_InputNull() {
        assertThat(Translator.createCfnTagsFromSdkTags(null)).isNull();
    }

    @Test
    public void testCreateSdkTagsFromCfnTags() {
        software.amazon.ec2.flowlog.Tag cfnTag = software.amazon.ec2.flowlog.Tag.builder()
            .key("k")
            .value("v")
            .build();
        software.amazon.awssdk.services.ec2.model.Tag sdkTag = software.amazon.awssdk.services.ec2.model.Tag.builder()
            .key("k")
            .value("v")
            .build();
        assertThat(Translator.createSdkTagsFromCfnTags(Arrays.asList(cfnTag))).isEqualTo(Arrays.asList(sdkTag));
    }

    @Test
    public void testCreateSdkTagsFromMap() {
        Map<String, String> desiredResourceTags = new HashMap<String, String>() {{
            put("stack-name", "test-stack");
        }};
        software.amazon.awssdk.services.ec2.model.Tag sdkTag = software.amazon.awssdk.services.ec2.model.Tag.builder()
            .key("stack-name")
            .value("test-stack")
            .build();
        assertThat(Translator.createSdkTagsFromMap(desiredResourceTags)).isEqualTo(Arrays.asList(sdkTag));
    }

    @Test
    public void testCreateSdkTagsFromCfnTags_InputNull() {
        assertThat(Translator.createSdkTagsFromCfnTags(null)).isNull();
    }

    @Test
    public void testIsUpdateable() {
        assertThat(Translator.isUpdateable(null,  null)).isTrue();
        ResourceModel baseModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        ResourceModel updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isTrue();

        updatedModel.setTags(Arrays.asList(
            software.amazon.ec2.flowlog.Tag.builder()
                .key("k")
                .value("v")
                .build()));
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isTrue();

        updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        updatedModel.setResourceId(null);
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isFalse();

        updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        updatedModel.setTrafficType(TrafficType.REJECT.toString());
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isFalse();

        updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        updatedModel.setResourceType(null);
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isFalse();

        updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        updatedModel.setLogDestinationType(LogDestinationType.S3.toString());
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isFalse();

        updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        updatedModel.setDeliverLogsPermissionArn(null);
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isFalse();

        updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        updatedModel.setLogDestination(null);
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isFalse();

        updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        updatedModel.setLogGroupName("newLogGroupName");
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isFalse();

        updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        updatedModel.setLogFormat("{version}");
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isFalse();

        updatedModel = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL);
        updatedModel.setMaxAggregationInterval(60);
        assertThat(Translator.isUpdateable(baseModel, updatedModel)).isFalse();

        assertThat(Translator.isUpdateable(baseModel, Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_S3))).isFalse();
    }

}
