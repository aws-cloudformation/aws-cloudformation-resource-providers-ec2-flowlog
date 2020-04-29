package software.amazon.ec2.flowlog;

import java.util.List;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        
        try { 
            final DescribeFlowLogsResponse describeFlowLogsResponse =
                proxy.injectCredentialsAndInvokeV2(createListRequest(request.getNextToken()),
                    ClientBuilder.getClient()::describeFlowLogs);
            
            logger.log(String.format("%n %s listed successfully",
                    describeFlowLogsResponse.flowLogs().size(), ResourceModel.TYPE_NAME));
            
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.SUCCESS)
                .resourceModels(createListResourceModels(describeFlowLogsResponse))
                .nextToken(describeFlowLogsResponse.nextToken())
                .build();
        } catch (Ec2Exception e) {
            return ProgressEvent.defaultFailureHandler(e, 
                    Translator.getHandlerErrorForEc2Error(e.awsErrorDetails().errorCode()));
        }
    }
    
    private DescribeFlowLogsRequest createListRequest(final String nextToken) {
        return DescribeFlowLogsRequest.builder()
            .maxResults(500)
            .nextToken(nextToken)
            .build();
    }
    
    private List<ResourceModel> createListResourceModels(final DescribeFlowLogsResponse response) {
        return response.flowLogs()
            .stream()
            .map(flowLog -> Translator.createModelFromFlowLog(flowLog))
            .collect(Collectors.toList());
    }
}
