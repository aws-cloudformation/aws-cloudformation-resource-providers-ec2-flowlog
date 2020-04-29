package software.amazon.ec2.flowlog;

import java.util.Collection;

import software.amazon.awssdk.services.ec2.model.FlowLog;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

class Translator {
    private Translator() {
    }

    static ResourceModel createModelFromFlowLog(FlowLog flowLog) {
        return ResourceModel.builder()
            .id(flowLog.flowLogId())
            .deliverLogsPermissionArn(flowLog.deliverLogsPermissionArn())
            .logDestination(flowLog.logDestination())
            .logDestinationType(flowLog.logDestinationTypeAsString())
            .logGroupName(flowLog.logGroupName())
            .resourceId(flowLog.resourceId())
            .resourceType(getResourceTypeFromResourceId(flowLog.resourceId()))
            .trafficType(flowLog.trafficTypeAsString())
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
}
