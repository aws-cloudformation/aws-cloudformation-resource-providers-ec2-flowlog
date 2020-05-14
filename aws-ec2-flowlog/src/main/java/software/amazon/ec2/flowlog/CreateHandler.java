package software.amazon.ec2.flowlog;

import software.amazon.awssdk.services.ec2.model.CreateFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.CreateFlowLogsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        final CreateFlowLogsRequest createFlowLogsRequest =
            CreateFlowLogsRequest.builder()
                .clientToken(request.getClientRequestToken())
                .deliverLogsPermissionArn(model.getDeliverLogsPermissionArn())
                .logDestination(model.getLogDestination())
                .logDestinationType(model.getLogDestinationType())
                .logGroupName(model.getLogGroupName())
                .resourceIds(model.getResourceId())
                .resourceType(model.getResourceType())
                .trafficType(model.getTrafficType())
                .build();
        try {
            CreateFlowLogsResponse createFlowLogsResponse = proxy.injectCredentialsAndInvokeV2(createFlowLogsRequest,
                ClientBuilder.getClient()::createFlowLogs);

            if (createFlowLogsResponse.hasUnsuccessful() && !createFlowLogsResponse.unsuccessful().isEmpty()) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.GeneralServiceException)
                    .message(createFlowLogsResponse.unsuccessful().get(0).error().message())
                    .build();
            }

            if (createFlowLogsResponse.flowLogIds().size() != 1) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.InternalFailure)
                    .message(String.format("%n %s created successfully",
                        createFlowLogsResponse.flowLogIds().size(), ResourceModel.TYPE_NAME))
                    .build();
            }

            logger.log(String.format("%s [%s] created successfully",
                ResourceModel.TYPE_NAME, createFlowLogsResponse.flowLogIds().get(0)));

            model.setId(createFlowLogsResponse.flowLogIds().get(0));
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
        } catch (Ec2Exception e) {
            return ProgressEvent.defaultFailureHandler(e,
                Translator.getHandlerErrorForEc2Error(e.awsErrorDetails().errorCode()));
        }
    }
}
