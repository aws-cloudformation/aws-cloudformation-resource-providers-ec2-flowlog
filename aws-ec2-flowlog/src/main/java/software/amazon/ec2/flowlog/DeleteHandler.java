package software.amazon.ec2.flowlog;

import software.amazon.awssdk.services.ec2.model.DeleteFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.DeleteFlowLogsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        try {
            final DeleteFlowLogsRequest deleteFlowLogsRequest = DeleteFlowLogsRequest.builder()
                .flowLogIds(model.getId())
                .build();
            final DeleteFlowLogsResponse deleteFlowLogsResponse = proxy.injectCredentialsAndInvokeV2(deleteFlowLogsRequest, 
                ClientBuilder.getClient()::deleteFlowLogs);
            
            if (deleteFlowLogsResponse.hasUnsuccessful() && !deleteFlowLogsResponse.unsuccessful().isEmpty()) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.GeneralServiceException)
                    .message(deleteFlowLogsResponse.unsuccessful().get(0).error().message())
                    .build();
            }
            
            logger.log(String.format("%s [%s] deleted successfully",
                ResourceModel.TYPE_NAME, model.getId()));
        } catch (Ec2Exception e) {
            return ProgressEvent.defaultFailureHandler(e, 
                Translator.getHandlerErrorForEc2Error(e.awsErrorDetails().errorCode()));
        }

        return ProgressEvent.defaultSuccessHandler(null);
    }
}
