package software.amazon.ec2.flowlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends TestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<Ec2Client> proxyClient;

    @Mock
    private Ec2Client ec2Client;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        doReturn(ec2Client).when(proxyClient).client();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final UpdateHandler handler = new UpdateHandler();

        final DescribeFlowLogsResponse describeFlowLogsResponse = DescribeFlowLogsResponse.builder()
            .flowLogs(TEST_FLOW_LOG_TO_CWL)
            .build();

        doReturn(describeFlowLogsResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL))
            .previousResourceState(Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL))
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_UpdateTags() {
        final UpdateHandler handler = new UpdateHandler();

        final DescribeFlowLogsResponse describeFlowLogsResponse = DescribeFlowLogsResponse.builder()
            .flowLogs(TEST_FLOW_LOG_TO_S3)
            .build();

        doReturn(describeFlowLogsResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        ResourceModel model = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_S3);
        List<software.amazon.ec2.flowlog.Tag> modelTags = model.getTags();
        modelTags.remove(0);
        modelTags.add(software.amazon.ec2.flowlog.Tag.builder()
            .key("k3")
            .value("v3")
            .build());
        model.setTags(modelTags);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .previousResourceState(Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_S3))
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_OnlyDeleteTags() {
        final UpdateHandler handler = new UpdateHandler();

        final DescribeFlowLogsResponse describeFlowLogsResponse = DescribeFlowLogsResponse.builder()
            .flowLogs(TEST_FLOW_LOG_TO_S3)
            .build();

        doReturn(describeFlowLogsResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        ResourceModel model = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_S3);
        List<software.amazon.ec2.flowlog.Tag> modelTags = model.getTags();
        model.setTags(null);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .previousResourceState(Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_S3))
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_OnlyCreateTags() {
        final UpdateHandler handler = new UpdateHandler();

        final DescribeFlowLogsResponse describeFlowLogsResponse = DescribeFlowLogsResponse.builder()
            .flowLogs(TEST_FLOW_LOG_TO_S3)
            .build();

        doReturn(describeFlowLogsResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        ResourceModel model = Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_S3);
        List<software.amazon.ec2.flowlog.Tag> modelTags = model.getTags();
        modelTags.add(software.amazon.ec2.flowlog.Tag.builder()
            .key("k3")
            .value("v3")
            .build());
        model.setTags(modelTags);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .previousResourceState(Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_S3))
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isNull();
    }

    @Test
    public void handleRequest_ResourceNotFound() {
        final UpdateHandler handler = new UpdateHandler();

        final DescribeFlowLogsResponse describeFlowLogsResponse = DescribeFlowLogsResponse.builder()
            .build();

        doReturn(describeFlowLogsResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(ResourceModel.builder().build())
            .build();

        assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, proxyClient, logger);
        });
    }

    @Test
    public void handleRequest_SimpleError() {
        final UpdateHandler handler = new UpdateHandler();

        doThrow(SERVICE_UNAVAILABLE_ERROR)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(ResourceModel.builder().build())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNotNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
        assertThat(response.getNextToken()).isNull();
    }

    @Test
    public void handleRequest_NotUpdateable() {
        final UpdateHandler handler = new UpdateHandler();

        final DescribeFlowLogsResponse describeFlowLogsResponse = DescribeFlowLogsResponse.builder()
                .flowLogs(TEST_FLOW_LOG_TO_CWL)
                .build();

        doReturn(describeFlowLogsResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_S3))
            .previousResourceState(Translator.createModelFromFlowLog(TEST_FLOW_LOG_TO_CWL))
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNotNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotUpdatable);
        assertThat(response.getNextToken()).isNull();
    }
}
