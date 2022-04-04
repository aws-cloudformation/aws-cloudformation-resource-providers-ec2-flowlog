package software.amazon.ec2.flowlog;

import java.util.List;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.CreateFlowLogsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.ResourceType;
import software.amazon.awssdk.services.ec2.model.TagSpecification;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Ec2Client> proxyClient,
        final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        CreateFlowLogsRequest.Builder createFlowLogsRequestBuilder =
            CreateFlowLogsRequest.builder()
                .clientToken(request.getClientRequestToken())
                .deliverLogsPermissionArn(model.getDeliverLogsPermissionArn())
                .logDestination(model.getLogDestination())
                .logDestinationType(model.getLogDestinationType())
                .logFormat(model.getLogFormat())
                .logGroupName(model.getLogGroupName())
                .maxAggregationInterval(model.getMaxAggregationInterval())
                .resourceIds(model.getResourceId())
                .resourceType(model.getResourceType())
                .trafficType(model.getTrafficType())
                .destinationOptions(Translator.createSdkFromCfnDestinationOptions(model.getDestinationOptions()));

        List<software.amazon.ec2.flowlog.Tag> cfnTags = model.getTags();
        if (cfnTags != null && !cfnTags.isEmpty()) {
            createFlowLogsRequestBuilder
                .tagSpecifications(TagSpecification.builder()
                    .resourceType(ResourceType.VPC_FLOW_LOG)
                    .tags(Translator.createSdkTagsFromCfnTags(model.getTags()))
                    .build()
                );
        }

        final CreateFlowLogsRequest createFlowLogsRequest = createFlowLogsRequestBuilder.build();
        try {
            CreateFlowLogsResponse createFlowLogsResponse = proxy.injectCredentialsAndInvokeV2(createFlowLogsRequest,
                    proxyClient.client()::createFlowLogs);

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

            final DescribeFlowLogsRequest describeFlowLogsRequest = DescribeFlowLogsRequest.builder()
                    .flowLogIds(createFlowLogsResponse.flowLogIds().get(0))
                    .build();
            final DescribeFlowLogsResponse describeFlowLogsResponse = proxy.injectCredentialsAndInvokeV2(describeFlowLogsRequest,
                    proxyClient.client()::describeFlowLogs);

            logger.log(String.format("%s [%s] created successfully",
                    ResourceModel.TYPE_NAME, createFlowLogsResponse.flowLogIds().get(0)));

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
