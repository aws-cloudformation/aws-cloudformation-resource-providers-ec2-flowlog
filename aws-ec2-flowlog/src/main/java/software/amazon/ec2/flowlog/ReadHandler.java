package software.amazon.ec2.flowlog;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Ec2Client> proxyClient,
        final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        try {
            final DescribeFlowLogsRequest describeFlowLogsRequest = DescribeFlowLogsRequest.builder()
                .flowLogIds(model.getId())
                .build();
            final DescribeFlowLogsResponse describeFlowLogsResponse = proxy.injectCredentialsAndInvokeV2(describeFlowLogsRequest,
                    proxyClient.client()::describeFlowLogs);

            if (describeFlowLogsResponse.flowLogs() == null || describeFlowLogsResponse.flowLogs().isEmpty()) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getId());
            }
            logger.log(String.format("%s [%s] read successfully",
                ResourceModel.TYPE_NAME, model.getId()));

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(Translator.createModelFromFlowLog(
                     describeFlowLogsResponse.flowLogs().get(0)))
                .status(OperationStatus.SUCCESS)
                .build();
        } catch (Ec2Exception e) {
            return ProgressEvent.defaultFailureHandler(e,
                Translator.getHandlerErrorForEc2Error(e.awsErrorDetails().errorCode()));
        }
    }
}
