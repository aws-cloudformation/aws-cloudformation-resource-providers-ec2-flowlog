package software.amazon.ec2.flowlog;

import software.amazon.awssdk.services.ec2.model.DestinationOptionsRequest;
import software.amazon.awssdk.services.ec2.model.DestinationOptionsResponse;
import software.amazon.awssdk.services.ec2.model.FlowLog;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class Translator {
    private static String DEFAULT_LOG_FORMAT = "${version} ${account-id} ${interface-id} ${srcaddr} ${dstaddr} ${srcport} ${dstport} ${protocol} ${packets} ${bytes} ${start} ${end} ${action} ${log-status}";
    private static Integer DEFAULT_MAX_AGGREGATION_INTERVAL = 600;

    private Translator() {
    }

    static ResourceModel createModelFromFlowLog(FlowLog flowLog) {
        return ResourceModel.builder()
            .id(flowLog.flowLogId())
            .deliverLogsPermissionArn(flowLog.deliverLogsPermissionArn())
            .logDestination(flowLog.logDestination())
            .logDestinationType(flowLog.logDestinationTypeAsString())
            .logFormat(flowLog.logFormat())
            .logGroupName(flowLog.logGroupName())
            .maxAggregationInterval(flowLog.maxAggregationInterval())
            .resourceId(flowLog.resourceId())
            .resourceType(getResourceTypeFromResourceId(flowLog.resourceId()))
            .tags(createCfnTagsFromSdkTags(flowLog.tags()))
            .trafficType(flowLog.trafficTypeAsString())
            .destinationOptions(createCfnFromSdkDestinationOptions(flowLog.destinationOptions()))
            .build();
    }

    private static String getResourceTypeFromResourceId(String resourceId) {
        String resourceType = resourceId.split("-")[0];
        switch (resourceType) {
            case "vpc":
                return "VPC";
            case "subnet":
                return "Subnet";
            case "eni":
                return "NetworkInterface";
            default:
                return null;
        }
    }

    static HandlerErrorCode getHandlerErrorForEc2Error(final String errorCode) {
        switch (errorCode) {
            case "UnauthorizedOperation":
                return HandlerErrorCode.AccessDenied;
            case "FlowLogAlreadyExists":
                return HandlerErrorCode.AlreadyExists;
            case "FlowLogsLimitExceeded":
                return HandlerErrorCode.ServiceLimitExceeded;
            case "InvalidParameter":
                return HandlerErrorCode.InvalidRequest;
            case "InvalidParameterValue":
                return HandlerErrorCode.InvalidRequest;
            case "InvalidFlowLogId.NotFound":
                return HandlerErrorCode.NotFound;
            case "ServiceUnavailable":
                return HandlerErrorCode.ServiceInternalError;
            case "InternalError":
                return HandlerErrorCode.ServiceInternalError;
            case "RequestLimitExceeded":
                return HandlerErrorCode.Throttling;
            default:
                return HandlerErrorCode.GeneralServiceException;
        }
    }

    private static software.amazon.ec2.flowlog.DestinationOptions createCfnFromSdkDestinationOptions(software.amazon.awssdk.services.ec2.model.DestinationOptionsResponse sdkDestinationOptions) {
        software.amazon.ec2.flowlog.DestinationOptions cfnDestinationOptions;
        if (sdkDestinationOptions == null) {
            return null;
        }

        cfnDestinationOptions = software.amazon.ec2.flowlog.DestinationOptions.builder()
            .fileFormat(sdkDestinationOptions.fileFormat().toString())
            .hiveCompatiblePartitions(sdkDestinationOptions.hiveCompatiblePartitions())
            .perHourPartition(sdkDestinationOptions.perHourPartition())
            .build();

        return cfnDestinationOptions;
    }

    static software.amazon.awssdk.services.ec2.model.DestinationOptionsRequest createSdkFromCfnDestinationOptions(software.amazon.ec2.flowlog.DestinationOptions cfnDestinationOptions) {
        software.amazon.awssdk.services.ec2.model.DestinationOptionsRequest sdkDestinationOptions;
        if (cfnDestinationOptions == null) {
            return null;
        }

        sdkDestinationOptions = DestinationOptionsRequest.builder()
                .fileFormat(cfnDestinationOptions.getFileFormat())
                .hiveCompatiblePartitions(cfnDestinationOptions.getHiveCompatiblePartitions())
                .perHourPartition(cfnDestinationOptions.getPerHourPartition())
                .build();

        return sdkDestinationOptions;
    }

    static List<software.amazon.ec2.flowlog.Tag> createCfnTagsFromSdkTags(final List<software.amazon.awssdk.services.ec2.model.Tag> sdkTags) {
        List<software.amazon.ec2.flowlog.Tag> cfnTags = new ArrayList<>();
        if (sdkTags == null) {
            return null;
        }

        sdkTags.stream()
            .filter(Objects::nonNull)
            .forEach(t -> cfnTags.add(
                software.amazon.ec2.flowlog.Tag.builder()
                    .key(t.key())
                    .value(t.value())
                    .build()));
        return cfnTags;
    }

    static List<software.amazon.awssdk.services.ec2.model.Tag> createSdkTagsFromCfnTags(List<software.amazon.ec2.flowlog.Tag> cfnTags) {
        List<software.amazon.awssdk.services.ec2.model.Tag> sdkTags = new ArrayList<>();
        if (cfnTags == null) {
            return null;
        }

        cfnTags.stream()
            .filter(Objects::nonNull)
            .forEach(t -> sdkTags.add(
            software.amazon.awssdk.services.ec2.model.Tag.builder()
                .key(t.getKey())
                .value(t.getValue())
                .build()));
        return sdkTags;
    }

    static List<software.amazon.awssdk.services.ec2.model.Tag> createSdkTagsFromMap(Map<String, String> tags) {
        if (tags == null) return null;
        return tags.keySet().stream().map(key -> software.amazon.awssdk.services.ec2.model.Tag.builder()
                .key(key)
                .value(tags.get(key))
                .build()
        ).collect(Collectors.toList());
    }

    static boolean isUpdateable(final ResourceModel previousResourceState, final ResourceModel desiredResourceState) {
        if (previousResourceState == null) {
            return true;
        }
        return Objects.equals(previousResourceState.getTrafficType(), desiredResourceState.getTrafficType()) &&
            Objects.equals(previousResourceState.getResourceId(), desiredResourceState.getResourceId()) &&
            Objects.equals(previousResourceState.getResourceType(), desiredResourceState.getResourceType()) &&
            Objects.equals(previousResourceState.getLogDestinationType(), desiredResourceState.getLogDestinationType()) &&
            Objects.equals(previousResourceState.getDeliverLogsPermissionArn(), desiredResourceState.getDeliverLogsPermissionArn()) &&
            Objects.equals(previousResourceState.getLogDestination(), desiredResourceState.getLogDestination()) &&
            equalsWithDefault(previousResourceState.getLogFormat(), desiredResourceState.getLogFormat(), DEFAULT_LOG_FORMAT) &&
            Objects.equals(previousResourceState.getLogGroupName(), desiredResourceState.getLogGroupName()) &&
            equalsWithDefault(previousResourceState.getMaxAggregationInterval(), desiredResourceState.getMaxAggregationInterval(), DEFAULT_MAX_AGGREGATION_INTERVAL);
    }

    private static boolean equalsWithDefault(Object previous, Object desired, Object defaultVal) {
        if (previous == null && desired == null) {
            return true;
        } else if (previous == null) {
            return desired.equals(defaultVal);
        } else if (desired == null) {
            return previous.equals(defaultVal);
        } else {
            return previous.equals(desired);
        }
    }
}
