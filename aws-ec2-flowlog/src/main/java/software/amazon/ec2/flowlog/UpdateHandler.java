package software.amazon.ec2.flowlog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DeleteTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.FlowLog;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Ec2Client> proxyClient,
        final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();
        try {
            final DescribeFlowLogsRequest describeFlowLogsRequest = DescribeFlowLogsRequest.builder()
                .flowLogIds(model.getId())
                .build();
            final DescribeFlowLogsResponse describeFlowLogsResponse = proxy.injectCredentialsAndInvokeV2(describeFlowLogsRequest,
                    proxyClient.client()::describeFlowLogs);

            if (describeFlowLogsResponse.flowLogs() == null || describeFlowLogsResponse.flowLogs().isEmpty()) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getId());
            }

            final FlowLog existingResource = describeFlowLogsResponse.flowLogs().get(0);
            if (!Translator.isUpdateable(previousModel, model)) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .message("Cannot update immutable properties")
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotUpdatable)
                    .build();
            }

            final List<software.amazon.awssdk.services.ec2.model.Tag> desiredSdkTags =
                (request.getDesiredResourceTags() == null || request.getDesiredResourceTags().isEmpty()) ?
                            new ArrayList<>() : Translator.createSdkTagsFromMap(request.getDesiredResourceTags());
            handleTags(proxy, proxyClient, existingResource, desiredSdkTags);

            logger.log(String.format("%s [%s] updated successfully",
                    ResourceModel.TYPE_NAME, model.getId()));
            ResourceModel updatedModel = Translator.createModelFromFlowLog(existingResource);
            updatedModel.setTags(Translator.createCfnTagsFromSdkTags(desiredSdkTags));

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(updatedModel)
                .status(OperationStatus.SUCCESS)
                .build();
        } catch (Ec2Exception e) {
            return ProgressEvent.defaultFailureHandler(e,
                Translator.getHandlerErrorForEc2Error(e.awsErrorDetails().errorCode()));
        }
    }

    private void handleTags(final AmazonWebServicesClientProxy proxy,
        final ProxyClient<Ec2Client> proxyClient,
        final FlowLog currentResourceState,
        final List<software.amazon.awssdk.services.ec2.model.Tag> desiredSdkTags) {

        final List<software.amazon.awssdk.services.ec2.model.Tag> currentTags =
            currentResourceState.tags() == null ?
                new ArrayList<>() : currentResourceState.tags();

        final Set<software.amazon.awssdk.services.ec2.model.Tag> tagsToCreate = desiredSdkTags
            .stream()
            .filter(tag -> !currentTags.contains(tag))
            .collect(Collectors.toSet());
        final Set<software.amazon.awssdk.services.ec2.model.Tag> tagsToDelete = currentTags
            .stream()
            .filter(tag -> !desiredSdkTags.contains(tag))
            .collect(Collectors.toSet());

        if (!tagsToDelete.isEmpty()) {
            final DeleteTagsRequest deleteTagsRequest = DeleteTagsRequest.builder()
              .tags(tagsToDelete)
              .resources(currentResourceState.flowLogId())
              .build();
            proxy.injectCredentialsAndInvokeV2(deleteTagsRequest, proxyClient.client()::deleteTags);
        }
        if (!tagsToCreate.isEmpty()) {
            final CreateTagsRequest createTagsRequest = CreateTagsRequest.builder()
                .tags(tagsToCreate)
                .resources(currentResourceState.flowLogId())
                .build();
            proxy.injectCredentialsAndInvokeV2(createTagsRequest, proxyClient.client()::createTags);
        }
    }
}
